package to.etc.webapp.mailer;

import javax.annotation.*;

import to.etc.webapp.query.*;

/**
 * Looks like a stringbuffer which can be used to collect text containing generic links. Links have a specific format, and
 * can be used to generically specify some target.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 5, 2010
 */
public class LinkedText /* implements Appendable */{
	private StringBuilder m_sb = new StringBuilder();

	public LinkedText() {}

	public LinkedText(String start) {
		m_sb.append(start);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Generate text containing links.						*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see java.lang.Appendable#append(char)
	 */
	@Nonnull
	public LinkedText append(char c) {
		if(c == '$')
			m_sb.append("\\$");
		else
			m_sb.append(c);
		return this;
	}

	@Nonnull
	public LinkedText append(@Nonnull CharSequence csq) {
		m_sb.append(csq);
		return this;
	}

	@Nonnull
	public LinkedText append(@Nonnull CharSequence csq, int start, int end) {
		m_sb.append(csq, start, end);
		return this;
	}

	@Nonnull
	public LinkedText link(@Nonnull String key, @Nonnull String id, @Nonnull String txt) {
		m_sb.append("$[");
		appendQ$(key);
		m_sb.append('$');
		appendQ$(id);
		m_sb.append('$');
		appendQ$(txt);
		m_sb.append(']');
		return this;
	}

	private void appendQ$(String key) {
		for(int i = 0, len = key.length(); i < len; i++) {
			char c = key.charAt(i);
			if(c == '$')
				m_sb.append('\\');
			m_sb.append(c);
		}
	}

	/**
	 * Render a link to a data class instance. The class type must have been registered with
	 * {@link TextLinkInfo#register(Class, String)} or equivalent. The class's primary key
	 * must be renderable with a "toString". The "text" part of the link will be the toString
	 * of the passed instance.
	 *
	 * @param p
	 * @return
	 */
	@Nonnull
	public LinkedText link(@Nonnull IIdentifyable< ? > instance) {
		return link(instance, String.valueOf(instance));
	}

	/**
	 * Render a link to any database item with a specified link text.
	 * @param p
	 * @return
	 */
	@Nonnull
	public LinkedText link(@Nonnull IIdentifyable< ? > instance, @Nonnull String linktext) {
		if(null == instance) {
			append("[null link to ").append(linktext).append("]");
			return this;
		}
		TextLinkInfo li = TextLinkInfo.getInfo(instance);
		if(null == li) {
			link(instance.getClass().getName(), String.valueOf(instance.getId()), linktext);
		} else {
			link(li.getLinkname(), String.valueOf(instance.getId()), linktext);
		}
		return this;
	}

	@Override
	public String toString() {
		return m_sb.toString();
	}

	public void clear() {
		m_sb.setLength(0);
	}

	/**
	 * Scan the raw text and traverse into segments.
	 * @param in
	 * @return
	 */
	static public void decode(@Nonnull ITextLinkRenderer r, @Nonnull String in) {
		if(in == null)
			in = "";
		StringBuilder sb = new StringBuilder();
		int	ix	= 0;
		int	len = in.length();
		while(ix < len) {
			char c = in.charAt(ix++);
			if(c == '$' && ix < len && in.charAt(ix) == '[') {
				//-- Link-open command. Decode TYPE/KEY/TEXT
				ix++;
				if(sb.length() > 0) {
					r.appendText(sb.toString());
					sb.setLength(0);
				}
				ix = decodeKey(r, sb, ix, in, len);
			} else {
				sb.append(c);
			}
		}
		if(sb.length() > 0)
			r.appendText(sb.toString());
	}

	/**
	 * Decode the link sequence $[type$key$text], then call the handler.
	 * @param r
	 * @param sb
	 * @param in
	 * @param len
	 * @return
	 */
	private static int decodeKey(@Nonnull ITextLinkRenderer r, @Nonnull StringBuilder sb, int ix, @Nonnull String in, int len) {
		int dol = 0;
		String type = null;
		String key = null;
		String text = null;
		sb.setLength(0);
		while(ix < len) {
			char c = in.charAt(ix++);
			if(c == '\\' && ix < len) {
				sb.append(in.charAt(ix++)); 				// Escaped char added verbatim.
			} else if(c == ']') {
				//-- End-of-key. Add collected part.
				text = sb.toString();
				sb.setLength(0);
				if(null == key)
					key = "?";
				if(null == text)
					text = "?";

				//-- Find the info
				TextLinkInfo tli = TextLinkInfo.getInfo(type);
				if(null == tli) {
					r.appendText(text + " (key:" + key + ", type:" + type);
				} else {
					r.appendLink(tli.getFullUrl(key), text);
				}
				return ix;
			} else if(c == '$') { 							// Item separator?
				if(dol == 0)
					type = sb.toString();
				else if(dol == 1)
					key = sb.toString();
				else
					throw new IllegalStateException("Too many dollar separators");
				sb.setLength(0);
				dol++;
			} else
				sb.append(c);
		}
		throw new IllegalStateException("Missing link end indicator ']'");
	}


}

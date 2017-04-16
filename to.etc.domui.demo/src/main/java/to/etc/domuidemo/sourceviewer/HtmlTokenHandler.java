package to.etc.domuidemo.sourceviewer;

import to.etc.domui.dom.html.*;
import to.etc.syntaxer.*;
import to.etc.syntaxer.TokenMarker.*;
import to.etc.util.*;

import javax.swing.text.*;
import java.util.*;

/**
 * jEdit token handler which converts handler calls into HTML to be used in an XmlTextNode. Used
 * for showing syntax-highlighted source.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 18, 2010
 */
public class HtmlTokenHandler implements TokenHandler {
	private String m_lastEnding;

	private byte m_lastType = -1;

	private int m_tabsize = 4;

	private int m_x;

	private String m_baseclass;

	private NodeContainer	m_target;

	private StringBuilder	m_sb = new StringBuilder(128);

	private List<String> m_importList;

	/** All looked-up crossreferences. */
	private Map<String, String> m_xrefMap = new HashMap<String, String>();

	public HtmlTokenHandler() {
	}
	public void setTabsize(int tabsize) {
		m_tabsize = tabsize;
	}
	public void setTarget(NodeContainer nc) {
		m_target = nc;
		m_sb.setLength(0);
		m_lastType = -1;
		m_x = 0;
	}
	public void	setBaseClass(String s) {
		m_baseclass = s;
	}

	public void setImportList(List<String> l) {
		m_importList = l;
	}

	@Override
	public void handleToken(Segment seg, byte id, int offset, int length, LineContext context) {
		try {
			if(id == 127 || (length == 1 && seg.array[seg.offset + offset] == '\n')) {
				if(m_lastType != -1)
					m_sb.append(m_lastEnding);
				XmlTextNode l = new XmlTextNode();
				l.setText(m_sb.toString());
				//				LiteralXhtml l = new LiteralXhtml();
				//				l.setXml(m_sb.toString());
				m_target.add(l);
				l.setCssClass("dsdp-cl");
				m_sb.setLength(0);
				m_x = 0;
				return;
			}

			//-- Check for known ids
			boolean done = false;
			String name = new String(seg.array, seg.offset + offset, length);
			//			System.out.println("LOOKUP=" + name + " id=" + id);

			if(id == Token.FUNCTION || (id == Token.NULL && StringTool.isValidJavaIdentifier(name))) {
				//					System.out.println("LOOKUP=" + name);
				String ref = getXref(name);
				if(ref != null) {
					done = true;
					//					System.out.println("LOOKUP: " + name + " = " + ref);

					if(m_lastType != -1)
						m_sb.append(m_lastEnding);
					m_lastType = -2;
					String css = Token.tokenToString(id).toLowerCase();
					m_lastEnding = "</a>";
					m_sb.append("<a class=\"s-");
					m_sb.append(css);
					if(m_baseclass != null && m_baseclass.length() > 0) {
						m_sb.append(' ');
						m_sb.append(m_baseclass);
					}
					m_sb.append("\" href=\"");
					m_sb.append(SourcePage.class.getName());
					m_sb.append(".ui?name=");
					m_sb.append(StringTool.encodeURLEncoded(ref));
					m_sb.append("\">");
					done = true;
				}
			}

			if(!done && m_lastType != id) {
				String css = Token.tokenToString(id).toLowerCase();
				if(m_lastType != -1)
					m_sb.append(m_lastEnding);

				m_lastEnding = "</span>";
				m_sb.append("<span class=\"s-");
				m_sb.append(css);
				if(m_baseclass != null && m_baseclass.length() > 0) {
					m_sb.append(' ');
					m_sb.append(m_baseclass);
				}
				m_sb.append("\">");
				//				m_sb.append("[" + id + "]");
				m_lastType = id;
			}

			int nsp = 0;
			for(int i = seg.offset + offset; --length >= 0; i++) {
				char c = seg.array[i];
				if(c != ' ' && c != '\t')
					nsp = 0;
				if(c == '<')
					m_sb.append("&lt;");
				else if(c == '>')
					m_sb.append("&gt;");
				else if(c == '&')
					m_sb.append("&amp;");
				else if(c == ' ') {
					if(++nsp == 1)
						m_sb.append(' ');
					else
						m_sb.append('\u00a0'); // NBSP
				} else if(c == '\t') {
					int m = m_tabsize - (m_x % m_tabsize);
					while(m-- >= 0) {
						if(++nsp == 1)
							m_sb.append(' ');
						else
							m_sb.append('\u00a0'); // NBSP
						m_x++;
					}
					continue;
				}
				else
					m_sb.append(c);
				m_x++;
			}
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	private String getXref(String name) {
		String ref = m_xrefMap.get(name);
		if(ref != null) {
			if(ref.length() == 0) // Empty string is not found
				return null;
			return ref;
		}

		//-- Need to lookup.
		ref = "";
		for(String path : m_importList) {
			String lookup = null;
			if(path.endsWith(".*")) {
				lookup = path.substring(0, path.length() - 2) + "." + name;
			} else if(path.endsWith("." + name)) {
				lookup = path;
			}
			if(lookup != null) {
				lookup = lookup.replace('.', '/') + ".java";
				SourceFile sf = SourceLocator.getInstance().findSource(lookup);
				//				System.out.println("    look " + lookup);
				if(sf != null) {
					ref = lookup;
					break;
				}
			}
		}
		m_xrefMap.put(name, ref);
		if(ref.length() == 0)
			return null;
		return ref;
	}

	@Override
	public void setLineContext(LineContext lineContext) {
	}
}

package to.etc.smtp;

import java.io.*;
import java.util.*;

import to.etc.util.*;

/**
 * Utility class to generate email messages containing embedded HTML and plaintext, and allowing
 * for attachment images. This generates an HTML and text-only message at the same time.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 22, 2010
 */
public class MailBuilder {
	/** The stringbuffer for the text-only part */
	private StringBuilder m_text_sb = new StringBuilder();

	/** The part for the HTML variant. */
	private StringBuilder m_html_sb = new StringBuilder();

	private String m_subject;

	static private class Attachment implements IMailAttachment {
		public String mime;

		public String ident;

		public File source;

		public Attachment(String mime, String ident, File source) {
			this.mime = mime;
			this.ident = ident;
			this.source = source;
		}

		public String getIdent() {
			return ident;
		}

		public InputStream getInputStream() throws Exception {
			return new FileInputStream(source);
		}

		public String getMime() {
			return mime;
		}
	}

	private List<Attachment> m_attachmentList = new ArrayList<Attachment>();

	private int m_attindex = 1;

	public MailBuilder() {}

	public void initialize(String subject) {
		m_subject = subject;
		m_text_sb.setLength(0);
		m_html_sb.setLength(0);
		m_attachmentList.clear();
		m_attindex = 0;

		m_html_sb.append("<html><head>");
		m_html_sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
		m_html_sb.append("</head>");
		m_html_sb.append("<body>");
		//		m_html_sb.append("");
		//		m_html_sb.append("");
		//		m_html_sb.append("");
		//		m_html_sb.append("");
		//		m_html_sb.append("");
		//		m_html_sb.append("");


	}

	/**
	 * Just add verbatim text, without anything else. Quotes all html content.
	 * @param s
	 * @return
	 */
	public MailBuilder append(String s) {
		m_text_sb.append(s);
		StringTool.htmlStringize(m_html_sb, s);
		return this;
	}

	public MailBuilder ttl(String s) {
		m_text_sb.append(s);
		m_text_sb.append("\n");
		for(int i = s.length(); --i >= 0;)
			m_text_sb.append('=');
		m_text_sb.append("\n");

		//-- HTML fragment
		m_html_sb.append("<h2>");
		StringTool.htmlStringize(m_html_sb, s);
		m_html_sb.append("</h2>\n");
		return this;
	}

	public MailBuilder i(String s) {
		m_text_sb.append(s);
		m_html_sb.append("<i>");
		StringTool.htmlStringize(m_html_sb, s);
		m_html_sb.append("</i>");
		return this;
	}

	public MailBuilder b(String s) {
		m_text_sb.append(s);
		m_html_sb.append("<b>");
		StringTool.htmlStringize(m_html_sb, s);
		m_html_sb.append("</b>");
		return this;
	}

	public MailBuilder nl() {
		m_text_sb.append("\n");
		m_html_sb.append("<br>");
		return this;
	}

	/**
	 * Render a link in HTML, embedding "text" in the link. The text message will show
	 * <pre>
	 * text (link)
	 * </pre>
	 * @param rurl
	 * @param text
	 * @return
	 */
	public MailBuilder link(String url, String text) {
		m_text_sb.append(text);
		m_text_sb.append(" (");
		m_text_sb.append(url);
		m_text_sb.append(")");

		m_html_sb.append("<a href=\"");
		m_html_sb.append(StringTool.encodeURLEncoded(url));
		m_html_sb.append("\">");
		StringTool.htmlStringize(m_html_sb, text);
		m_html_sb.append("</a>");
		return this;
	}

	public MailBuilder linkNoText(String url, String text) {
		m_text_sb.append(url);
		m_html_sb.append("<a href=\"");
		m_html_sb.append(url);
		m_html_sb.append("\">");
		StringTool.htmlStringize(m_html_sb, text);
		m_html_sb.append("</a>");
		return this;
	}

	/**
	 * Append an image as an attachment, and embed the image in the HTML stream. The text
	 * stream just contains a reference like (see image xxx). The image must be a supported
	 * mime type.
	 *
	 * @param name
	 * @param source
	 * @return
	 * @throws Exception
	 */
	public MailBuilder image(String name, File source, String mime) throws Exception {
		String imgkey = name + "-" + (m_attindex++);

		m_text_sb.append("(see attached image ");
		m_text_sb.append(imgkey);
		m_text_sb.append(") ");

		m_html_sb.append("<img src=\"cid:");
		m_html_sb.append(imgkey);
		m_html_sb.append("\">");

		//-- Create the attachment image.
		m_attachmentList.add(new Attachment(mime, imgkey, source));
		return this;
	}


	/**
	 * Send it.
	 * @param dest
	 * @throws Exception
	 */
	public void send(Message m) throws Exception {
		//-- Finish html
		m_html_sb.append("</body></html>\n");
		m.setSubject(m_subject);
		m.setBody(m_text_sb.toString());
		m.setHtmlBody(m_html_sb.toString());
		for(Attachment a: m_attachmentList)
			m.addAttachment(a);
		m.send();
	}

	public void send(SmtpTransport t, Address from, List<Address> dest) throws Exception {
		Message m = t.createMessage();
		m.setTo(dest);
		m.setFrom(from);
		send(m);
	}

	public void send(SmtpTransport t, Address from, Address to) throws Exception {
		Message m = t.createMessage();
		List<Address> tol = new ArrayList<Address>();
		tol.add(to);
		m.setTo(tol);
		m.setFrom(from);
		send(m);
	}
}

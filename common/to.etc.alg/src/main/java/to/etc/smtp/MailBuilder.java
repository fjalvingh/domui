/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.smtp;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.util.StringTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to generate email messages containing embedded HTML and plaintext, and allowing
 * for attachment images. This generates an HTML and text-only message at the same time.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 22, 2010
 */
public class MailBuilder {
	/** The stringbuffer for the text-only part */
	private StringBuilder m_textSb = new StringBuilder();

	/** The part for the HTML variant. */
	private StringBuilder m_htmlSb = new StringBuilder();

	private final static String STYLE_PLACEHOLDER = "{STYLE}";

	/** The STYLE part for the HTML variant. Has to go into the header, can't go to body */
	private StringBuilder m_styleSb = new StringBuilder();

	private String m_subject;

	/** If T, adds HTML header and body automatically, otherwise allow user to define whole HTML. */
	private final boolean m_decorateHtml;

	static private class Attachment implements IMailAttachment {
		public String mime;

		public String ident;

		public InputStream source;

		public ContentDisposition m_contentDisposition = ContentDisposition.attachment;

		public Attachment(String mime, String ident, InputStream source) {
			this.mime = mime;
			this.ident = ident;
			this.source = source;
		}

		public Attachment(String mime, String ident, InputStream source, ContentDisposition disposition) {
			this.mime = mime;
			this.ident = ident;
			this.source = source;
			m_contentDisposition = disposition;
		}

		public String getIdent() {
			return ident;
		}

		public void setIdent(String ident) {
			this.ident = ident;
		}

		public InputStream getInputStream() throws Exception {
			return source;
		}

		public String getMime() {
			return mime;
		}

		public void setMime(String mime) {
			this.mime = mime;
		}

		@Override
		public ContentDisposition getContentDisposition() {
			return m_contentDisposition;
		}

		public void setContentDisposition(ContentDisposition contentDisposition) {
			m_contentDisposition = contentDisposition;
		}
	}

	private List<Attachment> m_attachmentList = new ArrayList<Attachment>();

	private int m_attindex = 1;

	/**
	 * Creates mail builder that automatically creates html header and body. To have full control over HTML content use {@link MailBuilder#createNondecoratedMailBuilder()}
	 */
	public MailBuilder() {
		this(true);
	}

	private MailBuilder(boolean decorateHTML) {
		m_decorateHtml = decorateHTML;
	}

	/**
	 * Creates mail builder that allows full control over HTML content.
	 */
	@NonNull
	public static MailBuilder createNondecoratedMailBuilder() {
		return new MailBuilder(false);
	}

	public void initialize(String subject) {
		m_subject = subject;
		m_textSb.setLength(0);
		m_htmlSb.setLength(0);
		m_styleSb.setLength(0);
		m_attachmentList.clear();
		m_attindex = 0;

		if(m_decorateHtml) {
			m_htmlSb.append("<html><head>");
			m_htmlSb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
			m_htmlSb.append(STYLE_PLACEHOLDER);
			m_htmlSb.append("</head>");
			m_htmlSb.append("<body>");
		}
	}

	/**
	 * Just add verbatim text, without anything else. Quotes all html content.
	 */
	public MailBuilder append(String s) {
		m_textSb.append(s);
		StringTool.htmlStringizewithLF(m_htmlSb, s);
		return this;
	}

	public MailBuilder appendText(String s) {
		m_textSb.append(s);
		return this;
	}

	public StringBuilder getHtmlBuffer() {
		return m_htmlSb;
	}

	public StringBuilder getStyleBuffer() {
		return m_styleSb;
	}

	public StringBuilder getTextBuffer() {
		return m_textSb;
	}

	public MailBuilder appendHTML(String s) {
		m_htmlSb.append(s);
		return this;
	}

	public MailBuilder appendStyle(String s) {
		m_styleSb.append(s);
		return this;
	}

	public MailBuilder ttl(String s) {
		m_textSb.append(s);
		m_textSb.append("\n");
		for(int i = s.length(); --i >= 0; )
			m_textSb.append('=');
		m_textSb.append("\n");

		//-- HTML fragment
		m_htmlSb.append("<h2>");
		StringTool.htmlStringize(m_htmlSb, s);
		m_htmlSb.append("</h2>\n");
		return this;
	}

	public MailBuilder i(String s) {
		m_textSb.append(s);
		m_htmlSb.append("<i>");
		StringTool.htmlStringize(m_htmlSb, s);
		m_htmlSb.append("</i>");
		return this;
	}

	public MailBuilder b(String s) {
		m_textSb.append(s);
		m_htmlSb.append("<b>");
		StringTool.htmlStringize(m_htmlSb, s);
		m_htmlSb.append("</b>");
		return this;
	}

	public MailBuilder nl() {
		m_textSb.append("\n");
		m_htmlSb.append("<br>");
		return this;
	}

	/**
	 * Render a link in HTML, embedding "text" in the link. The text message will show
	 * <pre>
	 * text (link)
	 * </pre>
	 */
	public MailBuilder link(String url, String text) {
		m_textSb.append(text);
		m_textSb.append(" (");
		m_textSb.append(url);
		m_textSb.append(")");

		m_htmlSb.append("<a href=\"");
		m_htmlSb.append(url);
		m_htmlSb.append("\">");
		StringTool.htmlStringize(m_htmlSb, text);
		m_htmlSb.append("</a>");
		return this;
	}

	public MailBuilder linkNoText(String url, String text) {
		m_textSb.append(url);
		m_htmlSb.append("<a href=\"");
		m_htmlSb.append(url);
		m_htmlSb.append("\">");
		StringTool.htmlStringize(m_htmlSb, text);
		m_htmlSb.append("</a>");
		return this;
	}

	/**
	 * Add attachment to the email
	 *
	 * @param name    Name of an attachment
	 * @param source of attachment
	 */
	@NonNull
	public MailBuilder addAttachment(@NonNull String name, @NonNull InputStream source, String mimeType) throws Exception {
		m_attachmentList.add(new Attachment(mimeType, name, source));
		return this;
	}

	@NonNull
	public static String getMimeByFile(@NonNull File file) throws IOException {
		Path path = FileSystems.getDefault().getPath(file.getPath());
		return Files.probeContentType(path);
	}

	/**
	 * Create an inline image attachment that can later be referred using the "src" that is returned
	 * by this method (which can be simplified by calling {@link #imageRef(String)}.
	 */
	public String addImageAttachment(String name, Class<?> resourceBase, String resourceName, String mime) {
		String imgkey = name + "-" + (m_attindex++);
		Attachment a = new Attachment(mime, imgkey, null, ContentDisposition.inline) {
			@Override
			public InputStream getInputStream() throws Exception {
				InputStream is = resourceBase.getResourceAsStream(resourceName);
				if(is == null)
					throw new IllegalArgumentException("Missing class resource " + resourceName + " using base class " + resourceBase);
				return is;
			}
		};

		//-- Create the attachment image.
		m_attachmentList.add(a);
		return "cid:" + imgkey;
	}

	/**
	 * Create an inline image attachment that can later be referred using the "src" that is returned
	 * by this method (which can be simplified by calling {@link #imageRef(String)}.
	 */
	public String addImageAttachment(String name, InputStream source, String mime) {
		//-- Create the attachment image.
		String imgkey = name + "-" + (m_attindex++);
		m_attachmentList.add(new Attachment(mime, imgkey, source, ContentDisposition.inline));
		return "cid:" + imgkey;
	}

	/**
	 * Append an image as an attachment, and embed the image in the HTML stream. The text
	 * stream just contains a reference like (see image xxx). The image must be a supported
	 * mime type.
	 * Returns image ref key.
	 */
	public String image(String name, InputStream source, String mime) throws Exception {
		return addImageAttachment(name, source, mime);
	}

	/**
	 * Add an image by adding an attachment and an inlike "img" tag referring to that image.
	 * <b>Do not use if you want to add the same image multiple times</b>, in that case use
	 * addImageAttachment() followed by addImageRef().
	 */
	public MailBuilder image(String name, String mime, Attachment a) throws Exception {
		String imgkey = name + "-" + (m_attindex++);
		a.setContentDisposition(ContentDisposition.inline);			// Must be inline

		m_textSb.append("(see attached image ");
		m_textSb.append(imgkey);
		m_textSb.append(") ");

		m_htmlSb.append("<img src=\"cid:");
		m_htmlSb.append(imgkey);
		m_htmlSb.append("\">");
		a.setMime(mime);
		a.setIdent(imgkey);

		//-- Create the attachment image.
		m_attachmentList.add(a);
		return this;
	}

	public MailBuilder image(String name, final Class<?> rbase, final String rname, String mime) throws Exception {
		String imgkey = addImageAttachment(name, rbase, rname, mime);
		imageRef(imgkey);
		return this;
	}

	public MailBuilder imageRef(String imageKey) {
		m_textSb.append("(see attached image ");
		m_textSb.append(imageKey);
		m_textSb.append(") ");

		m_htmlSb.append("<img src=\"");
		m_htmlSb.append(imageKey);
		m_htmlSb.append("\">");
		return this;
	}

	/**
	 * Send it.
	 */
	public void send(Message m) throws Exception {
		if(m_decorateHtml) {
			//-- Finish html
			m_htmlSb.append("</body></html>\n");
		}
		m.setSubject(m_subject);
		m.setBody(m_textSb.toString());
		String html = m_decorateHtml
			? m_htmlSb.toString().replace(STYLE_PLACEHOLDER, m_styleSb.toString())
			: m_htmlSb.toString();
		m.setHtmlBody(html);
		for(Attachment a : m_attachmentList)
			m.addAttachment(a);
		m.send();
	}

	@NonNull
	public Message createMessage() {
		Message m = new Message();
		m.setSubject(m_subject);
		m.setBody(m_textSb.toString());
		String html = m_decorateHtml
			? m_htmlSb.toString().replace(STYLE_PLACEHOLDER, m_styleSb.toString())
			: m_htmlSb.toString();
		m.setHtmlBody(html);
		for(Attachment a : m_attachmentList)
			m.addAttachment(a);
		return m;
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

	public static void main(String[] args) throws Exception {
		MailBuilder mb = new MailBuilder();
		mb.initialize("Hello, world");
		mb.append("Test message with").b("html").append("markup").nl();

		Address from = new Address("puzzler@etc.to");
		Address to = new Address("jal@etc.to");

		SmtpTransport tp = new SmtpTransport("127.0.0.1", 2500);

		mb.send(tp, from, to);

	}
}

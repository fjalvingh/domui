package to.etc.webapp.mailer;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.naming.*;

import to.etc.smtp.*;
import to.etc.util.*;
import to.etc.webapp.core.*;
import to.etc.webapp.query.*;

/**
 * This is a simple mail builder class to help with writing nice emails
 * in both HTML and text format. It has methods to handle simple HTML markup
 * and has the ability to add embedded images as attachments.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 7, 2012
 */
public class MailHelper {
	/** The stringbuffer for the text-only part */
	final private StringBuilder m_text_sb = new StringBuilder();

	/** The part for the HTML variant. */
	final private StringBuilder m_html_sb = new StringBuilder();

	private String m_subject;

	private Address m_from;

	/** If the mail maker supports embedded text links this can be set to a text renderer that will render those properly. If unset it will try to render links by itself. */
	@Nullable
	private ITextLinkRenderer m_linkRenderer;

	/** If the mail maker supports embedded text links, this can be set to the application's root URL; in that case the mail message will construct the link URL's by itself. */
	@Nullable
	private String m_applicationURL;

	/**
	 * A stored attachment to the mail message. Can be an embedded image or something else.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on May 7, 2012
	 */
	static private class Attachment implements IMailAttachment {
		@Nonnull
		final public String m_mime;

		@Nonnull
		final public String m_ident;

		@Nullable
		final public File m_source;

		@Nullable
		final private byte[][] m_buffers;

		public Attachment(@Nonnull String mime, @Nonnull String ident, @Nonnull File source) {
			m_mime = mime;
			m_ident = ident;
			m_source = source;
			m_buffers = null;
		}

		public Attachment(@Nonnull String mime, @Nonnull String ident, @Nonnull byte[][] data) {
			m_mime = mime;
			m_ident = ident;
			m_buffers = data;
			m_source = null;
		}

		@Nonnull
		@Override
		public String getIdent() {
			return m_ident;
		}

		@Nonnull
		@Override
		public InputStream getInputStream() throws Exception {
			if(m_source != null)
				return new FileInputStream(m_source);
			return new ByteBufferInputStream(m_buffers);
		}

		@Nonnull
		@Override
		public String getMime() {
			return m_mime;
		}
	}

	/** The attachments. */
	@Nonnull
	final private List<Attachment> m_attachmentList = new ArrayList<Attachment>();

	@Nonnull
	final private List<Address> m_to = new ArrayList<Address>();

	private int m_attindex = 1;

	@Nullable
	private String m_lastImgKey;

	private boolean m_init;

	public MailHelper() {
}

	public void setRoot(File root) {
		m_root = root;
	}

	private void init() {
		if(m_init)
			return;
		m_text_sb.setLength(0);
		m_html_sb.setLength(0);
		m_attachmentList.clear();
		m_attindex = 0;

		m_html_sb.append("<html><head>");
		m_html_sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
		m_html_sb.append("</head>");
		m_html_sb.append("<body>");
		m_init = true;
		addGreeting();
	}

	public void start(Address to, String subject) {
		m_htmlLen = 0;
		setSubject(subject);
		addTo(to);
	}

	@Nonnull
	private ITextLinkRenderer getLinkRenderer() {
		ITextLinkRenderer linkRenderer = m_linkRenderer;
		if(null == linkRenderer) {
			//-- Create a default link renderer.
			linkRenderer = m_linkRenderer = new ITextLinkRenderer() {
				@Override
				public void appendText(@Nonnull String text) {
					appendVerbatim(text);
				}

				@Override
				public void appendLink(@Nonnull String rurl, @Nonnull String text) {
					String appurl = getApplicationURL();
					if(null == appurl)
						throw new IllegalStateException("To render LinkedText-like links you must set applicationURL or linkRenderer.");
					link(appurl + rurl, text);
				}
			};
		}
		return linkRenderer;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	HTML Primitives.									*/
	/*--------------------------------------------------------------*/
	private int m_htmlLen = 0;

	private static final int MAXLINE = 78;

	private void htmlWrap(@Nonnull String seg) {
		int len = seg.length();
		if(m_htmlLen + len >= MAXLINE) {
			internalNL();
		}
		m_html_sb.append(seg);
		m_htmlLen += len;
	}

	/**
	 * Render a html tag with optional attr=value pairs.
	 * @param tag
	 * @param attrs
	 */
	public void htmlTag(String tag, String... attrs) {
		htmlWrap("<" + tag);
		for(int i = 0; i < attrs.length; i += 2) {
			htmlAttr(attrs[i], attrs[i + 1]);
		}
		htmlWrap(">");
	}

	private void htmlAttr(String name, String value) {
		htmlWrap(" " + name + "=");
		htmlWrap("\"" + value + "\"");
	}

	public void htmlEndTag(String tag) {
		htmlWrap("</" + tag + ">");
	}

	/**
	 * Split text into whitespace-separated things, and make sure lines are smaller than MAXLINE.
	 * @param text
	 */
	public void htmlText(String text) {
		int ix = 0;
		int eix = text.length();
		int six = 0;
		while(ix < eix) {
			char c = text.charAt(ix);
			if(!Character.isWhitespace(c)) {
				ix++;
				continue;
			}

			//-- We have a run of characters from six to ix, and we have a whitespace char now...
			boolean wrapped = false;
			int sz = ix - six;
			if(sz > 0) {
				if(m_htmlLen + sz >= MAXLINE) {
					//-- We need to wrap. Do so.
					m_htmlLen = 0;
					m_html_sb.append("\r\n");
					wrapped = true;
				}
				m_htmlLen += sz;
				m_html_sb.append(text, six, ix);
			}

			//-- Is the current whitespace \r\n? Eat it,
			ix++;									// Past whitespace char
			if(c == '\r') {
				if(ix < eix && text.charAt(ix) == '\n')
					ix++;							// Eat \n
				if(!wrapped)
					internalNL();

			} else if(c == '\n') {
				if(!wrapped)
					internalNL();
			} else {
				m_html_sb.append(c);
				m_htmlLen++;
			}
			six = ix;
		}

		int sz = ix - six;
		if(sz > 0) {
			if(m_htmlLen + sz >= MAXLINE) {
				internalNL();
			}
			m_htmlLen += sz;
			m_html_sb.append(text, six, ix);
		}
	}

	private void internalNL() {
		m_html_sb.append("\r\n");
		m_htmlLen = 0;
	}

	/**
	 * Just add verbatim text, without anything else. Quotes all html content. This will decode text generated with {@link LinkedText} into something
	 * that might be edible by email.
	 * @param s
	 * @return
	 */
	@Nonnull
	public MailHelper append(@Nonnull String s) {
		init();
		ITextLinkRenderer r = getLinkRenderer();
		LinkedText.decode(r, s);
		return this;
	}

	/**
	 * Append the text without scanning for any kind of embedded links.
	 * @param s
	 */
	public void appendVerbatim(@Nonnull String s) {
		init();
		m_text_sb.append(s);
		String html = StringTool.htmlStringize(s);
		htmlText(html);
	}

	@Nonnull
	public MailHelper ttl(@Nonnull String s) {
		init();
		htmlTag("h2");
		append(s);

		m_text_sb.append("\n");
		for(int i = s.length(); --i >= 0;)
			m_text_sb.append('=');
		m_text_sb.append("\n");

		//-- HTML fragment
		htmlEndTag("h2");
		htmlText("\r\n");
		return this;
	}

	@Nonnull
	public MailHelper i(String s) {
		init();
		htmlTag("i");
		append(s);
		htmlEndTag("i");
		return this;
	}

	@Nonnull
	public MailHelper b(String s) {
		init();
		htmlTag("b");
		append(s);
		htmlEndTag("b");
		return this;
	}

	@Nonnull
	public MailHelper nl() {
		init();
		m_text_sb.append("\r\n");
		htmlTag("br/");
		htmlText("\r\n");
		return this;
	}

	@Nonnull
	public MailHelper pre(String content) {
		init();
		htmlTag("pre");
		append(content);
		htmlEndTag("pre");
		htmlText("\r\n");
		return this;
	}

	/**
	 * Render a link in HTML, embedding "text" in the link. The text message will show
	 * <pre>
	 * text (link)
	 * </pre>
	 * @param url			The full URL to link to.
	 * @param text			The link's text.
	 * @return
	 */
	@Nonnull
	public MailHelper link(@Nonnull String url, @Nonnull String text) {
		init();
		m_text_sb.append(text);
		m_text_sb.append(" (");
		m_text_sb.append(url);
		m_text_sb.append(")");

		htmlTag("a", "href", url);
		htmlText(StringTool.htmlStringize(text));
		htmlEndTag("a");
		return this;
	}

	/**
	 * Use the LinkedText mechanism to render a link to some entity. There must be some {@link TextLinkInfo} registered for the class.
	 * @param text
	 * @param inst
	 * @return
	 */
	@Nonnull
	public MailHelper link(@Nonnull String text, @Nonnull IIdentifyable< ? > inst) {
		init();
		TextLinkInfo info = TextLinkInfo.getInfo(inst);
		if(info == null) {
			append(text).append(" (").append(String.valueOf(inst)).append(")");
		} else {
			getLinkRenderer().appendLink(info.getFullUrl(String.valueOf(inst.getId())), text);
		}
		return this;
	}

	/**
	 * Create a link in html only.
	 * @param url
	 * @param text
	 * @return
	 */
	@Nonnull
	public MailHelper linkNoText(String url, String text) {
		init();
		m_text_sb.append(url);

		htmlTag("a", "href", url);
		htmlText(StringTool.htmlStringize(text));
		htmlEndTag("a");
		return this;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Appending embedded images.							*/
	/*--------------------------------------------------------------*/
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
	@Nonnull
	public MailHelper image(String name, String mime, File source) throws Exception {
		String imgkey = name + "-" + (m_attindex++);
		image(new Attachment(mime, imgkey, source), name);
		return this;
	}

	/**
	 * Add a class resource as an image.
	 * @param name
	 * @param resourceClass
	 * @param resourceName
	 * @return
	 */
	@Nonnull
	public MailHelper image(String name, Class< ? > resourceClass, String resourceName) throws Exception {
		String ext = FileTool.getFileExtension(resourceName);
		String mime = ServerTools.getExtMimeType(ext);
		InputStream is = resourceClass.getResourceAsStream(resourceName);
		if(null == is)
			throw new IllegalArgumentException("Resource " + resourceClass + ":" + resourceName + " not found on the classpath");
		try {
			byte[][] bufs = FileTool.loadByteBuffers(is);
			String imgkey = name + "-" + (m_attindex++);
			image(new Attachment(mime, imgkey, bufs), name);
			return this;
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	private File m_root;


	/**
	 * Add a web resource as an image. For this to work you must override {@link #getApplicationResource(String)}.
	 * @param name
	 * @param mime
	 * @param rurl
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public MailHelper image(@Nonnull String name, @Nonnull String mime, @Nonnull String rurl) throws Exception {
		InputStream is;
		if(m_root != null) {
			is = new FileInputStream(new File(m_root, rurl));
		} else {
			is = getApplicationResource(rurl);
		}

		byte[][] buf;
		try {
			buf = FileTool.loadByteBuffers(is);
		} finally {
			FileTool.closeAll(is);
		}
		String s = MimeWriter.generateContentID();
//		String s = m_lastImgKey = name + "-" + (m_attindex++);
		image(new Attachment(mime, s, buf), name);
		return this;
	}

	/**
	 * Add a web resource as an image. For this to work you must override {@link #getApplicationResource(String)}.
	 *
	 * @param name
	 * @param rurl
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public MailHelper image(@Nonnull String name, @Nonnull String rurl) throws Exception {
		String ext = FileTool.getFileExtension(rurl);
		String mime = ServerTools.getExtMimeType(ext);
		return image(name, mime, rurl);
	}

	/**
	 * Add a web resource as an image, and return the image's internal ID. For this to work you must override {@link #getApplicationResource(String)}.
	 * @param name
	 * @param rurl
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public String addImage(@Nonnull String name, @Nonnull String rurl) throws Exception {
		image(name, rurl);
		String s = m_lastImgKey;
		if(s == null)
			throw new IllegalStateException("Last image not set");
		return s;
	}

	/**
	 * Add an image link for a specified attachment.
	 * @param a
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public MailHelper image(@Nonnull Attachment a, @Nonnull String name) throws Exception {
		init();
		m_text_sb.append("(see attached image ");
		m_text_sb.append(a.m_ident);
		m_text_sb.append(") ");

		htmlTag("img", "src", "cid:" + a.m_ident);

		//-- Create the attachment image.
		m_attachmentList.add(a);
		return this;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handle sending.										*/
	/*--------------------------------------------------------------*/

	/**
	 * Send the message using the {@link BulkMailer}'s default instance, using a newly allocated connection.
	 * @throws Exception
	 */
	public void send() throws Exception {
		sendInternal(null);
	}

	/**
	 * Send the message immediately through the specified transport - only use this if the BulkMailer should
	 * not be used. If the sending fails it will not be repeated (an exception will be thrown).
	 *
	 * @param transport
	 * @throws Exception
	 */
	public void send(@Nonnull SmtpTransport transport) throws Exception {
		sendInternal(transport);
	}

	/**
	 * Send the message using the {@link BulkMailer} using the specified connection. The message will be sent only
	 * when that connection is commited, storing the data into the database for the BulkMailer to find.
	 * @param dc
	 * @throws Exception
	 */
	public void send(@Nonnull QDataContext dc) throws Exception {
		sendInternal(dc);
	}

	/**
	 * Queue the message for transmission.
	 * @throws Exception
	 */
	public void sendInternal(@Nullable Object through) throws Exception {
		addTrailer();
		htmlEndTag("body");
		htmlEndTag("html");
		if(DeveloperOptions.getBool("email.send", true)) {
			String alt = DeveloperOptions.getString("email.debug");
			if(alt != null) {
				List<Address> replaced = new ArrayList<Address>();
				for(Address adr : getTo()) {
					//-- In debug use alternate address but ORIGINAL name
					String name = adr.getName();
					if(name == null)
						name = adr.getEmail().replace('@', '-');
					replaced.add(new Address(alt, name));
				}
				getTo().clear();
				for(Address adr : replaced) {
					addTo(adr);
				}
			}

			//-- Create the message
			if(m_from == null)
				throw new IllegalStateException("No 'from' specified.");
			Message m = new Message();
			m.setFrom(m_from);
			for(Address to : m_to)
				m.addTo(to);
			m.setSubject(getSubject());
			m.setBody(getTextBuffer().toString());
			m.setHtmlBody(getHtmlBuffer().toString());
			for(IMailAttachment aa : getAttachmentList())
				m.addAttachment(aa);

			//-- Now queue/send
			if(through instanceof SmtpTransport)
				((SmtpTransport) through).send(m);
			else if(through instanceof QDataContext)
				BulkMailer.getInstance().store((QDataContext) through, m);
			else if(null == through)
				BulkMailer.getInstance().store(m);
			else
				throw new IllegalStateException("Unknown store method");
		} else {
			System.out.println("MAIL: on " + new Date() + " should send to: ");
			for(Address a : getTo()) {
				System.out.println("\t" + a.getName() + " [" + a.getEmail() + "]");
			}
			System.out.println("\tSUBJECT: " + getSubject());
			System.out.println(getTextBuffer().toString());
			System.out.println("--- html ---");
			System.out.println(getHtmlBuffer().toString());
			System.out.println("--- end ---");
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Simple getters and setters.							*/
	/*--------------------------------------------------------------*/


	@Nonnull
	public StringBuilder getHtmlBuffer() {
		init();
		return m_html_sb;
	}

	@Nonnull
	public StringBuilder getTextBuffer() {
		init();
		return m_text_sb;
	}

	@Nonnull
	public List<Attachment> getAttachmentList() {
		return Collections.unmodifiableList(m_attachmentList);
	}

	@Nonnull
	public List<Address> getTo() {
		return m_to;
	}

	public MailHelper addTo(@Nonnull Address a) {
		m_to.add(a);
		return this;
	}

	@Nonnull
	public MailHelper addTo(@Nonnull String email) {
		m_to.add(new Address(email));
		return this;
	}

	@Nonnull
	public MailHelper setFrom(@Nonnull String from) {
		m_from = new Address(from, from);
		return this;
	}

	@Nonnull
	public MailHelper setFrom(@Nonnull Address from) {
		m_from = from;
		return this;
	}

	@Nullable
	public String getSubject() {
		return m_subject;
	}

	@Nonnull
	public MailHelper setSubject(@Nonnull String subject) {
		m_subject = subject;
		return this;
	}

	public void setLinkRenderer(@Nonnull ITextLinkRenderer linkRenderer) {
		m_linkRenderer = linkRenderer;
	}

	/**
	 * Set the root application URL. This URL should always end with "/".
	 * @param applicationURL
	 */
	public void setApplicationURL(@Nonnull String applicationURL) {
		if(!applicationURL.endsWith("/"))
			applicationURL += "/";
		m_applicationURL = applicationURL;
	}

	@Nullable
	public String getApplicationURL() {
		return m_applicationURL;
	}

	/**
	 * Can be overridden to add a custom greeting.
	 */
	protected void addGreeting() {}

	/**
	 * Can be overridden to add a default trailer.
	 */
	protected void addTrailer() throws Exception {}

	@Nonnull
	protected InputStream getApplicationResource(@Nonnull String name) throws Exception {
		throw new OperationNotSupportedException("Override getApplicationResource(String)");

//		//-- Get the appfile represented by that RURL.
//		IResourceRef rr = DomApplication.get().getResource(rurl, ResourceDependencyList.NULL);
//		if(!rr.exists())
//			throw new IllegalStateException("The application resource '" + rurl + "' does not exist.");


	}
}

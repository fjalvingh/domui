package to.etc.smtp;

import java.util.*;

/**
 * An email message. To allow this to be sent it needs
 * at least one TO address, a valid FROM address and
 * a non-empty subject.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 16, 2006
 */
final public class Message {
	private SmtpTransport	m_transport;

	private Address			m_from;

	private Address			m_replyTo;

	private List<Address>	m_to	= new ArrayList<Address>();

	private List<Address>	m_bcc;

	private List<Address>	m_cc;

	private String			m_subject;

	/** Text variant of body */
	private String			m_body;

	private String			m_htmlBody;

	private List<IMailAttachment>	m_attachment	= new ArrayList<IMailAttachment>();

	public Message() {
	}

	public Message(SmtpTransport t) {
		m_transport = t;
	}

	public void send() throws Exception {
		if(m_transport == null)
			throw new MailException("The transport is not set");
		m_transport.send(this);
	}

	public List<Address> getBcc() {
		return m_bcc;
	}

	public void setBcc(List<Address> bcc) {
		m_bcc = bcc;
	}

	public void addBcc(Address a) {
		if(m_bcc == null)
			m_bcc = new ArrayList<Address>(2);
		m_bcc.add(a);
	}

	public String getBody() {
		return m_body;
	}

	public void setBody(String body) {
		m_body = body;
	}

	public List<Address> getCc() {
		return m_cc;
	}

	public void setCc(List<Address> cc) {
		m_cc = cc;
	}

	public void addCc(Address a) {
		if(m_cc == null)
			m_cc = new ArrayList<Address>();
		m_cc.add(a);
	}

	public Address getFrom() {
		return m_from;
	}

	public void setFrom(Address from) {
		m_from = from;
	}

	public Address getReplyTo() {
		return m_replyTo;
	}

	public void setReplyTo(Address replyTo) {
		m_replyTo = replyTo;
	}

	public String getSubject() {
		return m_subject;
	}

	public void setSubject(String subject) {
		m_subject = subject;
	}

	public List<Address> getTo() {
		return m_to;
	}

	public void setTo(List<Address> to) {
		m_to = to;
	}

	public void addTo(Address a) {
		m_to.add(a);
	}

	public void setHtmlBody(String htmlBody) {
		m_htmlBody = htmlBody;
	}

	public void addAttachment(IMailAttachment a) {
		m_attachment.add(a);
	}

	public String getHtmlBody() {
		return m_htmlBody;
	}

	public List<IMailAttachment> getAttachmentList() {
		return m_attachment;
	}
}

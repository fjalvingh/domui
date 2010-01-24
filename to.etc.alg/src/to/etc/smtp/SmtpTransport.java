package to.etc.smtp;

import java.io.*;
import java.net.*;
import java.util.*;

import to.etc.util.*;

/**
 * A thingy which uses the SMTP protocol to send messages.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 16, 2006
 */
public class SmtpTransport {
	static private final String		BOUNDARY	= "boun-da-ry-0xababaeaGfHdNarcolethe-mumble-to-content-eNCoDer-gxixmar-rennes-le-chateau";

	static private final boolean	DEBUG	= false;

	private String					m_myhostname;

	/** The address of the SMTP server. */
	private InetAddress				m_server;

	/** The smtp port number, defaults to 25 */
	private int						m_port	= 25;

	private Address					m_from;

	public SmtpTransport(InetAddress a, int port) {
		m_server = a;
		m_port = port;
	}

	public SmtpTransport(InetAddress a) {
		m_server = a;
	}

	public SmtpTransport(String host) throws UnknownHostException {
		m_server = InetAddress.getByName(host);
	}

	public SmtpTransport(String host, String myhostname) throws UnknownHostException {
		m_server = InetAddress.getByName(host);
		m_myhostname = myhostname;
	}

	public SmtpTransport(String host, int port) throws UnknownHostException {
		m_server = InetAddress.getByName(host);
		m_port = port;
	}

	public synchronized void setMyHostName(String hn) {
		m_myhostname = hn;
	}

	public synchronized void setFrom(Address a) {
		m_from = a;
	}

	private synchronized String getMyHost() {
		if(m_myhostname == null) {
			try {
				m_myhostname = InetAddress.getLocalHost().getCanonicalHostName();
			} catch(UnknownHostException x) {
				return "localhost";
			}
		}
		return m_myhostname;
	}

	public void send(Message msg) throws Exception {
		//-- Check the message for validity
		Address from = msg.getFrom();
		if(from == null) {
			from = m_from;
			if(from == null)
				throw new MailException("Missing 'from' address in message");
		}
		if(msg.getTo() == null || msg.getTo().size() == 0)
			throw new MailException("Missing 'to' address in message");
		if(msg.getSubject() == null || msg.getSubject().trim().length() == 0)
			throw new MailException("The 'subject' is empty in message");

		Socket s = null;
		InputStream is = null;
		OutputStream os = null;
		try {
			s = new Socket(m_server, m_port);
			s.setSoTimeout(30 * 1000); // Reads must answer within 30 secs
			is = s.getInputStream();
			os = s.getOutputStream();

			//-- Expect response
			String cr = readLine(is);
			if(!cr.startsWith("2"))
				throw new MailException("SMTP server failed to send proper response after connect: '" + cr + "'");

			write(os, "EHLO " + getMyHost() + "\r\n");

			//			boolean	mime8 = false;
			boolean accepted = false;

			//-- Read the response strings, if needed
			for(;;) {
				String res = readLine(is);
				if(res.length() < 3)
					throw new MailException("SMTP error: empty response");
				String code = res.substring(0, 3);
				boolean more = res.length() > 3 ? res.charAt(3) == '-' : false;
				String rest = res.length() > 4 ? res.substring(4) : "";
				//				System.out.println("response: code="+code+", more="+more+", string="+rest);

				//-- Handle codes.
				char c = code.charAt(0);
				if(c != '2') {
					accepted = false;
					break;
				}

				//				if(rest.equalsIgnoreCase("8BITMIME"))
				//					mime8 = true;
				accepted = true;
				if(!more)
					break;
			}

			if(!accepted) {
				//-- Send HELO command.
				write(os, "HELO " + getMyHost() + "\r\n");
				accepted = false;
				String res = readLine(is);
				if(!res.startsWith("250"))
					throw new MailException("Host " + m_server + " does not accept HELO nor EHLO command.");
				accepted = true;
			}
			if(!accepted)
				throw new MailException(m_server + " does not accept my HELO/EHLO..");

			//-- Start sending- the "from" address
			write(os, "MAIL FROM: ");
			write(os, "<" + from.getEmail() + ">\r\n");
			cr = readLine(is);
			if(!cr.startsWith("250"))
				throw new MailException(m_server + ": the server did not accept the 'from' address '" + msg.getFrom().getEmail() + "'");

			//-- Send all recipients.
			sendAddressList(is, os, "RCPT TO: ", msg.getTo());
			sendAddressList(is, os, "RCPT TO: ", msg.getBcc());
			sendAddressList(is, os, "RCPT TO: ", msg.getCc());

			//-- Start to write the data
			write(os, "DATA\r\n");
			cr = readLine(is);
			if(!cr.startsWith("354") && !cr.startsWith("250"))
				throw new MailException(m_server + ": did not accept the 'DATA' command, it mumbled " + cr);

			//-- Start writing a nice body.
			write(os, "From: ");
			writeAddress(os, msg.getFrom());
			write(os, "\r\n");
			writeAddressList(os, "To: ", msg.getTo());
			writeAddressList(os, "CC: ", msg.getCc());

			write(os, "Reply-To: ");
			writeAddress(os, msg.getReplyTo() == null ? from : msg.getReplyTo());
			write(os, "\r\n");

			write(os, "Subject: " + msg.getSubject() + "\r\n");

			//-- Does this need to be a MIME message?
			boolean ismime = msg.getAttachmentList().size() > 0 || msg.getHtmlBody() != null;
			if(ismime) {
				writeMime(os, msg);
			} else {
				writeText(os, msg);
			}

			cr = readLine(is);
			if(!cr.startsWith("250"))
				throw new MailException(m_server + ": did not accept the DATA message, it answered: " + cr);
			write(os, "QUIT\r\n");
			cr = readLine(is);
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
			if(s != null) {
				try {
					s.close();
				} catch(Exception x) {}
			}
		}
	}

	private void writeMime(OutputStream os, Message msg) throws Exception {
		//-- 1.. Make sure all string data is crlf terminated, and all lines starting with '.' are escaped.
		EmailOutputStream eos = new EmailOutputStream(os);
		MimeWriter w = MimeWriter.createMimeWriter(eos, "multipart/alternative", "type=\"text/plain\"");

		//-- Write the text/plain part
		w.partStart(false, "text/plain", "; charset=\"UTF-8\"");
		Writer pw = w.partWriter("UTF-8"); // The writer for this part's contents; also indicates end of header writing.
		pw.append(msg.getBody()); // Just write raw string stream here.
		pw.close();

		//-- Start HTML section.
		MimeWriter hw = w.createSubMime("multipart/related", "text/html");
		hw.partStart(false, "text/html", "; charset=\"UTF-8\"");
		pw = hw.partWriter("UTF-8"); // The writer for this part's contents; also indicates end of header writing.
		pw.append(msg.getHtmlBody()); // Just write raw string stream here.
		pw.close();

		//-- Start writing attachments in base64 encoding.
		if(msg.getAttachmentList().size() > 0) {
			for(IMailAttachment ma: msg.getAttachmentList()) {
				hw.partStart(true, ma.getMime(), "");
				hw.partHeader("Content-Location", "CID:blarf.net");
				hw.partHeader("Content-ID", "<" + ma.getIdent() + ">");

				//-- Now- encapsulate
				InputStream is = ma.getInputStream();
				OutputStream ios = hw.partStream();
				try {
					FileTool.copyFile(ios, is);
				} finally {
					FileTool.closeAll(is, ios);
				}
			}
		}
		hw.close();
		w.close();
		eos.flush();
		write(os, ".\r\n");
	}

	private void writeStringData(MimeWriter w, String str) throws Exception {
		if(str != null && str.length() > 0) {
			int ix = 0;
			int len = str.length();
			while(ix < len) {
				int pos = str.indexOf('\n', ix);
				if(pos == -1) {
					sendLine(w, str.substring(ix));
					break;
				}
				String ss = str.substring(ix, pos);
				sendLine(w, ss);
				ix = pos + 1;
			}
		}
	}

	private void sendLine(MimeWriter w, String line) throws Exception {
		if(line.startsWith(".")) {
			w.write("." + line);
		} else {
			w.write(line);
		}
		w.writeCRLF();
	}

	private void writeMimeOLD(OutputStream os, Message msg) throws Exception {
		write(os, "Mime-Version: 1.0\r\n");
		write(os, "Content-Type: multipart/alternative; boundary=\"" + BOUNDARY + "\"; type=\"text/plain\"\r\n");

		//-- Lead-in boundary and multipart segment containing the text version.
		write(os, "\r\n--"); // Empty line + boundary lead
		write(os, BOUNDARY);
		write(os, "\r\n");

		//-- Write this-part's headers.
		write(os, "Content-Type: text/plain; charset=\"UTF-8\"\r\n");
		write(os, "Content-Transfer-Encoding: quoted-printable\r\n");

		write(os, "\r\n"); // End of headers indicator; data follows.
		writeStringData(os, msg.getBody());

		//-- Start HTML section.
		write(os, "\r\n--"); // Write boundary to next part
		write(os, BOUNDARY);
		write(os, "\r\n");
		write(os, "Content-Type: text/html; charset=\"UTF-8\"\r\n");
		write(os, "\r\n"); // End of headers indicator; data follows.
		writeStringData(os, msg.getHtmlBody());

		//-- Start writing attachments in base64 encoding.
		if(msg.getAttachmentList().size() > 0) {
			for(IMailAttachment ma : msg.getAttachmentList()) {
				write(os, "\r\n--"); // Write boundary to next part
				write(os, BOUNDARY);
				write(os, "\r\n");

				write(os, "Content-Location: CID:blarf.net\r\n"); // disregarded

				write(os, "Content-ID: <");
				write(os, ma.getIdent());
				write(os, ">\r\n");

				write(os, "Content-Type: ");
				write(os, ma.getMime());
				write(os, "\r\n");

				write(os, "Content-Transfer-Encoding: BASE64\r\n");
				write(os, "\r\n"); // End of headers

				//-- Now- encapsulate
				InputStream is = ma.getInputStream();
				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					FileTool.copyFile(bos, is);
					bos.close();
					writeStringData(os, StringTool.encodeBase64ToString(bos.toByteArray()));
				} finally {
					FileTool.closeAll(is);
				}
			}
		}

		//-- Write the last and final boundary
		write(os, "\r\n--"); // Write boundary to next part
		write(os, BOUNDARY);
		write(os, "--\r\n");
		write(os, ".\r\n");
	}

	private void writeText(OutputStream os, Message msg) throws Exception {
		String str = msg.getBody();
		if(str != null && str.length() > 0) {
			int ix = 0;
			int len = str.length();
			while(ix < len) {
				int pos = str.indexOf('\n', ix);
				if(pos == -1) {
					sendLine(os, str.substring(ix));
					break;
				}
				String ss = str.substring(ix, pos);
				sendLine(os, ss);
				ix = pos + 1;
			}
		}
		write(os, ".\r\n");
	}

	private void writeStringData(OutputStream os, String str) throws Exception {
		if(str != null && str.length() > 0) {
			int ix = 0;
			int len = str.length();
			while(ix < len) {
				int pos = str.indexOf('\n', ix);
				if(pos == -1) {
					sendLine(os, str.substring(ix));
					break;
				}
				String ss = str.substring(ix, pos);
				sendLine(os, ss);
				ix = pos + 1;
			}
		}
	}


	private void sendLine(OutputStream os, String line) throws Exception {
		if(line.startsWith("."))
			write(os, "." + line + "\r\n");
		else
			write(os, line + "\r\n");
	}

	private void writeAddressList(OutputStream os, String hdr, List<Address> al) throws Exception {
		if(al == null || al.size() == 0)
			return;
		write(os, hdr);
		boolean first = true;
		for(Address a : al) {
			if(first)
				first = false;
			else
				write(os, ", ");
			writeAddress(os, a);
		}
		write(os, "\r\n");
	}

	private void writeAddress(OutputStream os, Address a) throws Exception {
		if(a.getName() != null) {
			write(os, a.getName() + " ");
		}
		write(os, "<" + a.getEmail() + ">");
	}

	private void sendAddressList(InputStream is, OutputStream os, String hdr, List<Address> al) throws Exception {
		if(al == null)
			return;
		for(Address a : al) {
			write(os, hdr + "<" + a.getEmail() + ">\r\n");
			String res = readLine(is);
			if(!res.startsWith("250"))
				throw new MailException(m_server + ": refused the recipient '" + a.getEmail() + "': " + res);
		}
	}

	/**
	 * Does a raw write of the string in ASCII 7 encoding.
	 * @param os
	 * @param s
	 */
	private void write(OutputStream os, String s) throws Exception {
		byte[] b = s.getBytes("US-ASCII");
		os.write(b);
		if(DEBUG)
			System.out.println("[write] " + s);
	}

	private String readLine(InputStream is) throws Exception {
		byte[] buf = new byte[128];
		int ix = 0;
		int lch = 0;
		for(;;) {
			int ch = is.read();
			if(ch == '\n') {
				if(lch == '\r')
					ix--;
				String s = new String(buf, 0, ix, "US-ASCII");
				if(DEBUG)
					System.out.println("[read] " + s);
				return s;
			}
			lch = ch;
			if(ix >= buf.length) {
				byte[] a = new byte[ix + 128];
				System.arraycopy(buf, 0, a, 0, ix);
				buf = a;
			}
			buf[ix++] = (byte) ch;
		}
	}

	public Message createMessage() {
		return new Message(this);
	}

	static public void main(String[] args) {
		try {
			SmtpTransport t = new SmtpTransport("localhost");

			Message m = new Message();
			m.addTo(new Address("jal@etc.to"));
			m.setSubject("Test for SMTP sender");
			m.setBody("This is a body of work");
			m.setFrom(new Address("jal@etc.to"));

			t.send(m);
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}

package to.etc.server.servlet.cmd;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import javax.servlet.http.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import to.etc.server.servlet.*;
import to.etc.util.*;
import to.etc.xml.*;

abstract public class CommandServletContext implements RequestContext, ErrorHandler {
	static private final Logger		LOG		= Logger.getLogger(CommandServletContext.class.getName());

	static private final String		AUTH_NS	= "http://xml.nccw.nl//xml/soap-auth/";

	private HttpServletRequest		m_request;

	private HttpServletResponse		m_response;

	/** The registry with commands for this command handler. */
	private CommandServletRegistry	m_registry;

	private CommandServletBase		m_base;

	private boolean					m_isPost;

	private String					m_authenticatedId;

	private boolean					m_responseByHandler;

	public CommandServletContext(HttpServletRequest req, HttpServletResponse res, CommandServletRegistry r, CommandServletBase base, boolean ispost) {
		m_request = req;
		m_response = res;
		m_registry = r;
		m_base = base;
		m_isPost = ispost;
	}

	final public String getAuthenticatedId() {
		return m_authenticatedId;
	}

	final public CommandServletBase getServlet() {
		return m_base;
	}

	/**
	 * @see to.etc.server.servlet.ContextServletContext#close()
	 */
	public void close() {
	}

	/**
	 * @see to.etc.server.servlet.ContextServletContext#getLastModified()
	 */
	final public long getLastModified() throws Exception {
		return -1;
	}

	/**
	 * Returns the servlet request.
	 */
	public HttpServletRequest getRequest() {
		return m_request;
	}

	/**
	 * @see to.etc.server.servlet.ContextServletContext#getResponse()
	 */
	public HttpServletResponse getResponse() {
		return m_response;
	}

	public boolean isPost() {
		return m_isPost;
	}

	/**
	 * @see to.etc.server.servlet.ContextServletContext#initialize()
	 */
	public void initialize() {
	}

	/**
	 * @see to.etc.server.servlet.ContextServletContext#setResponse(javax.servlet.http.HttpServletResponse, boolean)
	 */
	public void setResponse(HttpServletResponse res, boolean ispost) {
		m_response = res;
	}

	final public CommandServletRegistry getRegistry() {
		if(m_registry == null)
			throw new IllegalStateException("Command registry not set.");
		return m_registry;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Proxies to the base servlet for logging.			*/
	/*--------------------------------------------------------------*/
	public void exception(Throwable t, String s) {
		m_base.exception(t, s);
	}

	public boolean isLogging() {
		return m_base.isLogging();
	}

	public void log(String s) {
		m_base.log(s);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Code to write an XML response (buffered)			*/
	/*--------------------------------------------------------------*/
	/** The XML writer to use */
	private XmlWriter		m_xmlw	= null;

	/** The StringWriter caching the response */
	private StringWriter	m_sw;

	protected XmlWriter getXmlWriter() throws IOException {
		if(m_xmlw == null) {
			if(m_responseByHandler)
				throw new IllegalStateException("Cannot get an XML writer if the handler generates it's own output");

			//-- Create an utf8 writer to a stringbuffer.
			m_sw = new StringWriter(8192);
			m_xmlw = new XmlWriter(m_sw);
			m_xmlw.wraw("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		}
		return m_xmlw;
	}

	/**
	 * Resets the XMLwriter and discards all of the contents
	 * that was generated. Used when an exception occurs to
	 * discard all data generated.
	 */
	protected void resetWriter() {
		if(m_sw != null) {
			m_sw.getBuffer().setLength(0);
			m_xmlw = new XmlWriter(m_sw);
			try {
				m_xmlw.wraw("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			} catch(Exception x) {}
		}
	}

	/**
	 * Writes the generated XML output to the output stream.
	 */
	protected void writeOutput() throws IOException {
		if(m_sw == null)
			throw new IllegalStateException("Request did not generate any output!?");
		String buf = m_sw.getBuffer().toString();
		if(buf.length() == 0)
			throw new IllegalStateException("Request did not generate any output!?");

		//-- Dump to logging, if applicable
		if(isLogging()) {
			log("------ Response to request:");
			log(buf);
			log("------ End of response.");
		}

		//-- Now output proper
		byte[] data = buf.getBytes("utf-8");
		OutputStream os = null;
		try {
			getResponse().setContentLength(data.length);
			getResponse().setContentType("text/xml; encoding=utf-8");
			os = getResponse().getOutputStream();
			os.write(data);
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Authentication.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Calculate the challenge nonce. This is a random thingy generated by 
	 * creating some big random number and the time. The nonce is stored 
	 * in the servlet's nonce table identified by itself. As long as the
	 * nonce stays in that table will requests be validateable against it;
	 * for that all of the client request data must match!
	 */
	private void execGetchallenge() throws Exception {
		NonceData nd = m_base.makeNonce(getRequest());
		getXmlWriter().tagfull("nonce", nd.getNonce());
	}

	private void checkAuthentication(String nonce, String ident, String auth) throws Exception {
		if(nonce == null)
			throw new IllegalStateException("Missing 'nonce' field in authentication header of SOAP message");
		if(ident == null)
			throw new IllegalStateException("Missing 'ident' field in authentication header of SOAP message");
		if(auth == null)
			throw new IllegalStateException("Missing 'auth' field in authentication header of SOAP message");
		m_authenticatedId = null;

		//-- Try to find the nonce data
		NonceData nd = m_base.findNonce(nonce);
		if(nd == null)
			throw new IllegalStateException("The authentication information was not found or is no longer valid");

		String clad = getRequest().getRemoteAddr();
		if(!clad.equalsIgnoreCase(nd.getClientAddr()))
			throw new IllegalStateException("The client address has changed in-between calls: WHAT ARE YOU DOING!? Signalling SYSADMIN for possible hack attempt!");

		String password = m_base.findIdentPassword(ident);
		if(password == null)
			throw new IllegalStateException("The identification or password is not known");

		//-- At least the nonce data is valid.. Check if the authentication is valid,
		String s = ident + ":" + password;
		String secret = StringTool.toHex(SecurityUtils.md5Hash(s.getBytes())).toUpperCase();
		s = secret + ":" + nd.getNonce();
		String myauth = StringTool.toHex(SecurityUtils.md5Hash(s.getBytes()));

		if(!myauth.equalsIgnoreCase(auth))
			throw new IllegalStateException("The identification or password is not known"); // Must be same as above or people can guess a name
		m_authenticatedId = ident;
	}

	private String getAuthValue(CommandContext cc, String name) throws Exception {
		//-- URL request always need the authentication names as parameters
		if(cc instanceof URLCommandContext)
			return cc.getStringParam(name, null);

		//-- We have a SOAP call. Prefer the thingy in the SOAP Header.
		if(m_soap_header != null) {
			Node aun = DomTools.nodeFind(m_soap_header, "authentication");
			if(aun != null)
				return DomTools.stringNode(aun, name, null);

			//-- Try using namespace
			aun = NSDomTools.nodeFind(m_soap_header, AUTH_NS, "authentication");
			if(aun != null)
				return NSDomTools.stringNode(aun, AUTH_NS, name, null);
		}

		//-- Fallback to the soap body.
		Node aun = DomTools.nodeFind(m_soap_body, "authentication");
		if(aun != null)
			return DomTools.stringNode(aun, name, null);

		//-- Try using namespace
		aun = NSDomTools.nodeFind(m_soap_body, AUTH_NS, "authentication");
		if(aun != null)
			return NSDomTools.stringNode(aun, AUTH_NS, name, null);
		return null;
	}


	/**
	 * Called to see if the request contains authentication. This version
	 * continues the request if authentication is not present and mustAuthenticate
	 * is true. If authentication is present it must be OK.
	 */
	private void checkGlobalAuthentication(CommandContext cc) throws Exception {
		//-- Get nonce, ident and auth
		String nonce = getAuthValue(cc, "nonce");
		if(nonce != null) {
			String ident = getAuthValue(cc, "ident");
			if(ident != null) {
				String auth = getAuthValue(cc, "auth");
				if(auth != null) {
					checkAuthentication(nonce, ident, auth);
					return;
				}
			}
		}

		//-- Some or all authentication thingies are missing...
		if(m_base.isAuthenticationRequired())
			throw new IllegalStateException("Authentication is required");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handling commands by registered command classes		*/
	/*--------------------------------------------------------------*/
	/** All instantiated command handlers (implementing ICommandServletHandler) for this exchange. */
	private Hashtable	m_handler_ht	= new Hashtable();

	protected void handleRegisteredCommand(String name, CommandContext cc) throws Exception {
		//-- Is this an authentication nonce request?
		if(name.equals("get-challenge") || name.equals("getChallenge")) {
			execGetchallenge();
			return;
		}

		//-- Check for global authentication.
		checkGlobalAuthentication(cc);

		//-- 1. Try to find a method to handle this command from the registered command handler classes
		Method m = getRegistry().handlerByName(name);
		if(m == null)
			throw new Exception(name + ": unknown command");

		/*
		 * Is the implementing class already instantiated? If not instantiate and init
		 * it.
		 */
		Object h = m_handler_ht.get(m.getDeclaringClass());
		if(h == null)
			h = m.getDeclaringClass().newInstance();
		if(h instanceof ICommandServletHandler) {
			m_handler_ht.put(m.getDeclaringClass(), h);
			((ICommandServletHandler) h).initialize(this);
		}

		try {
			m.invoke(h, new Object[]{cc});
		} catch(InvocationTargetException x) {
			Exception nx = x;
			if(x.getCause() instanceof Exception)
				nx = (Exception) x.getCause();
			if(nx instanceof WrappedException)
				nx = (Exception) x.getCause();
			throw nx;
		}
	}

	/**
	 * This calls the close() entrypoint for all persistent handler instances
	 * that were instantiated to execute the commands on this request. 
	 */
	private void closeAllHandlers() {
		for(Enumeration e = m_handler_ht.elements(); e.hasMoreElements();) {
			ICommandServletHandler ch = (ICommandServletHandler) e.nextElement();
			try {
				ch.close();
			} catch(Exception x) {
				exception(x, "In terminating command handler " + ch.getClass().getName() + ": " + x.toString());
			}
		}
		m_handler_ht.clear();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Decode XML input from servlet input stream.			*/
	/*--------------------------------------------------------------*/
	/** This string buffer receives error messages while the document gets parsed. */
	private StringBuffer	m_xmlerr_sb;

	/**
	 * Parses the document, and returns the DOM associated with the 
	 * thing. If errors occur they are logged into an error
	 * message string; these are thrown as an exception when complete.
	 */
	public Document getDocument(Reader r, String ident) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		m_xmlerr_sb = new StringBuffer();
		try {
			//-- Assign myself as the error handler for parsing,
			db.setErrorHandler(this);
			InputSource ins = new InputSource(r);
			if(ident != null)
				ins.setPublicId(ident);
			Document doc = db.parse(ins);

			//-- If we're already in trouble now then exit with an exception.
			if(m_xmlerr_sb.length() > 0)
				throw new Exception("Input document parse exceptions: " + m_xmlerr_sb.toString());
			return doc;
		} catch(IOException x) {
			throw new IOException("XML Parser IO error on " + ident + ": " + x.toString());
		}
	}

	/**
	 * Uses the input stream to obtain the SOAP call document.
	 */
	public Document getSOAPCallDocument() throws Exception {
		StringWriter sw = null;
		Reader r = null;
		try {
			r = getRequest().getReader();
			if(isLogging()) // Are we logging?
			{
				sw = new StringWriter(1024);
				r = new CopyingReader(r, sw); // Create a copying reader to copy all that is read into sw
			}
			return getDocument(r, "request");
		} finally {
			//-- Log copyreader shtuff.
			if(sw != null) {
				String data = sw.toString();

				log("----- XML Data read from peer is:");
				log(data);
				log("----- End of XML data from peer.");
			}
		}
	}

	private void genErr(SAXParseException exception, String type) {
		if(m_xmlerr_sb.length() > 0)
			m_xmlerr_sb.append("\n");
		StringBuffer sb = new StringBuffer(64);
		sb.append("SOAP request @");
		sb.append(Integer.toString(exception.getLineNumber()));
		sb.append(':');
		sb.append(Integer.toString(exception.getColumnNumber()));
		sb.append(" ");
		sb.append(type);
		sb.append(":");
		sb.append(exception.getMessage());
		m_xmlerr_sb.append(sb);
		if(isLogging())
			log("xml parser: " + sb.toString());
	}

	public final void warning(SAXParseException exception) throws SAXException {
		genErr(exception, "warning");
	}

	public final void error(SAXParseException exception) throws SAXException {
		genErr(exception, "error");
	}

	public final void fatalError(SAXParseException exception) throws SAXException {
		genErr(exception, "fatal");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Decoding and encoding SOAP constructs.				*/
	/*--------------------------------------------------------------*/
	private Node					m_soap_envelope;

	private Node					m_soap_body;

	private Node					m_soap_header;

	static private final String[]	NSURIS	= {"http://www.w3.org/2001/12/soap-envelope", "http://schemas.xmlsoap.org/soap/envelope/"};

	static private boolean isValidNS(String ns) {
		for(int i = NSURIS.length; --i >= 0;) {
			if(NSURIS[i].equals(ns))
				return true;
		}
		return false;
	}

	/**
	 * Called to decode and handle the command as a SOAP call.
	 */
	public void decodeSoap() throws Exception {
		//-- Reading the document should work or we throw a normal exception.
		Document doc = getSOAPCallDocument();
		Node root = DomTools.getRootElement(doc);

		//-- The root node must be an envelope.
		if(!root.getLocalName().equals("Envelope"))
			throw new IllegalStateException("Expecting an 'Envelope' node as root node but got a " + root.getNodeName());

		//-- Find the namespace of the node and see if it is defined and acceptable
		//		String	prefix	= root.getPrefix();
		String nsuri = root.getNamespaceURI();
		if(!isValidNS(nsuri))
			throw new IllegalStateException("The namespace " + nsuri + " is not accepted by this server as a SOAP namespace.");
		m_soap_envelope = root;
		//-- From here on all exceptions are reported as FAULT's.
		boolean inbody = false;
		try {
			m_soap_header = NSDomTools.nodeFind(m_soap_envelope, nsuri, "Header");
			m_soap_body = NSDomTools.nodeFind(m_soap_envelope, nsuri, "Body");
			if(m_soap_header != null) // Is there a header?
				decodeSoapHeader(m_soap_header); // If so decode it
			inbody = true;
			if(m_soap_body == null)
				throw new IllegalStateException("Missing SOAP Body");

			//-- Prepare writing a SOAP response,
			writeEnvelope();
			decodeSoapBody(m_soap_body);
			getXmlWriter().tagendnl(); // End of body
			getXmlWriter().tagendnl(); // End of envelope
		} catch(Exception x) {
			x.printStackTrace();
			sendFault(x, inbody);
		}
	}

	/**
	 * FIXME This should decode the header. It should just check
	 * for elements with mustUnderstand=true and abort if one is found.
	 *
	 * @param n
	 * @throws Exception
	 */
	protected void decodeSoapHeader(Node n) throws Exception {
	}

	/**
	 * Writes a response envelope, copying the required
	 * parts of the request envelope, and the body.
	 */
	private void writeEnvelope() throws IOException {
		if(m_soap_envelope == null)
			throw new IllegalStateException("No SOAP envelope known");
		XmlWriter x = getXmlWriter();
		x.tag("SOAP-ENV:Envelope", ""); // Start envelope
		NamedNodeMap atts = m_soap_envelope.getAttributes();
		for(int i = 0; i < atts.getLength(); i++) {
			x.write(' ');
			x.writeAttr(atts.item(i).getNodeName(), atts.item(i).getNodeValue());
		}
		x.write(">\n");
		x.tag("SOAP-ENV:Body");
	}

	/**
	 * Send a SOAP Fault frame back. This discards any pending output and
	 * sends a fault frame instead.
	 */
	protected void sendFault(Throwable x, boolean frombody) throws Exception {
		resetWriter();
		writeEnvelope();
		XmlWriter w = getXmlWriter();
		w.tag("SOAP-ENV:Fault"); // Immediate child of Body
		w.tagfull("faultcode", "SOAP-ENV:Server"); // Machine edible
		w.tagfull("faultstring", x.toString()); // Human edible

		//-- If the fault occured in the body then we MUST have a details element.
		if(frombody) {
			w.tag("detail", "xmlns:exception=\"http://xml.nccw.nl/xml/wms/soap-fault\" >\n");
			w.tag("exception"); // My frame

			w.tagfull("exception:type", x.getClass().getName());
			w.tagfull("exception:message", x.toString());
			w.tagfull("exception:when", "undefined");
			w.tag("exception:stackTrace", ">\n");
			String ss = StringTool.strStacktrace(x);
			w.cdata(ss);
			w.tagendnl(); // Exception:stacktrace

			w.tagendnl(); // end exception
			w.tagendnl(); // end details
		}

		//-- Terminate the response.
		w.tagendnl(); // End fault
		w.tagendnl(); // End body
		w.tagendnl(); // End envelope.
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Soap body decoder using the command registry		*/
	/*--------------------------------------------------------------*/
	protected void decodeSoapBody(Node bodynode) throws Exception {
		//-- Walk all of the top nodes and call their handlers.
		String name = "undefined";
		long ts = 0;
		try {
			NodeList nl = bodynode.getChildNodes();
			for(int i = 0; i < nl.getLength(); i++) {
				Node nd = nl.item(i);
				//				System.out.println("Handling "+nd.getNodeName());
				if(!nd.getNodeName().equalsIgnoreCase("#text") && nd.getNodeType() == Node.ELEMENT_NODE) {
					ts = PrecisionTimer.getTime();
					name = nd.getNodeName();
					execNode(name, nd);
					ts = PrecisionTimer.getTime() - ts;
					LOG.info("cmd: " + name + " took " + ts + " us");
				}
			}
			closeAllHandlers();
		} catch(Exception x) {
			ts = PrecisionTimer.getTime() - ts;

			//-- Unwrap the exception :-(
			Throwable t = x;
			if(x instanceof InvocationTargetException)
				t = ((InvocationTargetException) x).getCause();
			if(t instanceof WrappedException)
				t = ((WrappedException) t).getCause();

			LOG.info("cmd: " + name + " exception " + t + " after " + ts + " us");
			exception(t, "Command " + name + " caused exception.");
			if(t instanceof Exception)
				throw (Exception) t;
			else
				throw new WrappedException(t);
		}
	}

	/**
	 * Finds the handler for the name specified, and 
	 * @param name
	 * @param n
	 * @throws Exception
	 */
	private void execNode(String name, Node n) throws Exception {
		String cmd = name;
		int col = cmd.indexOf(':'); // Has namespace shit?
		if(col != -1)
			cmd = cmd.substring(col + 1); // Strip namespace

		//-- Create a SoapCommandContext wrapper for this call.
		SoapCommandContext cc = new SoapCommandContext(this, n, cmd);
		handleRegisteredCommand(cmd, cc);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Default handler.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Default Handler. This tries to use an URL command (using the action= parameter
	 * as the name) if this is a GET. It uses SOAP if this is a post.
	 */
	public void execute() throws Exception {
		if(isPost())
			handleSoapCall();
		else
			handleURLCall();
		if(!m_responseByHandler)
			writeOutput();
	}

	public void signalBinaryOutput() {
		m_responseByHandler = true;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	SOAP handling.										*/
	/*--------------------------------------------------------------*/
	private void handleSoapCall() throws Exception {
		decodeSoap();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	URL call based handling.							*/
	/*--------------------------------------------------------------*/
	private void handleURLCall() throws Exception {
		try {
			String name = getRequest().getParameter("action");
			if(name == null)
				throw new IllegalStateException("Missing 'action' parameter.");

			//-- Create a SoapCommandContext wrapper for this call.
			URLCommandContext cc = new URLCommandContext(this, name);
			handleRegisteredCommand(name, cc);
		} catch(Exception x) {
			x.printStackTrace();
			handleURLException(x);
		}
	}

	protected void handleURLException(Exception x) throws Exception {
		throw x;
	}
}

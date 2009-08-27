package to.etc.server.servlet;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import to.etc.util.*;
import to.etc.xml.*;

public class XmlContextBase extends CtxServletContextBase {
	private String			m_path;

	private Document		m_inputDocument;

	private XmlWriter		m_w;

	private boolean			m_log	= true;

	private StringWriter	m_logsw;

	private String			m_mime	= "text/xml; encoding=UTF-8";

	public XmlContextBase(CtxServlet servlet, HttpServletRequest request, HttpServletResponse response, String method) {
		super(servlet, request, response, method);
	}

	protected Writer getLogWriter() {
		if(m_logsw == null) {
			m_logsw = new StringWriter(8192);
		}
		return m_logsw;
	}

	protected boolean isLogging() {
		return m_log;
	}

	public void setMime(String mime) {
		m_mime = mime;
	}

	@Override
	public void log(String s) {
		getServlet().log(s);
	}

	@Override
	public void exception(Throwable t, String s) {
		getServlet().exception(t, s);
	}


	@Override
	protected void initialize() throws Exception {
		logCall();
	}

	private void logCall() throws Exception {
		if(!m_log)
			return;

		Writer w = getLogWriter();
		w.append("--- Entry at " + new Date() + ", by " + getRequest().getRemoteHost() + "\n");
		w.append("Path   : " + getRequest().getPathInfo() + "\n");
		w.append("Method : " + getRequest().getMethod() + "\n");
		w.append("Headers:\n");
		for(Enumeration<String> en = getRequest().getHeaderNames(); en.hasMoreElements();) {
			String hdr = en.nextElement();
			for(Enumeration<String> e2 = getRequest().getHeaders(hdr); e2.hasMoreElements();) {
				w.append("  " + hdr + ": " + e2.nextElement() + "\n");
			}
		}

		if(!hasInput())
			w.append("The request contains no data stream as input.\n");
	}

	public boolean hasInput() {
		return getRequest().getContentLength() != 0;
	}

	/**
	 * Loads the input request as an XML document. 
	 * @return
	 */
	public Document getInputDocument() throws Exception {
		if(m_inputDocument != null)
			return m_inputDocument;
		DocumentBuilderFactory documentBuilderFactory = null;
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

		InputStream is = getRequest().getInputStream();
		if(m_log) {
			is = new LogInputStream(is);
		}
		try {
			m_inputDocument = documentBuilder.parse(new InputSource(is));
		} finally {
			if(m_log) {
				byte[] data = ((LogInputStream) is).getData();
				String txt = new String(data, "UTF-8");
				getLogWriter().append("\n---- Input document: (can be incomplete) ---\n");
				getLogWriter().append(txt);
				getLogWriter().append("\n---- end of input ---\n");
			}
		}
		return m_inputDocument;
	}

	public Element getInputRoot() throws Exception {
		return getInputDocument().getDocumentElement();
	}

	public XmlWriter w() throws IOException {
		if(m_w == null) {
			m_w = new XmlWriter(getResponseWriter());
			m_w.wraw("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		}
		return m_w;
	}

	private Writer	m_responseWriter;

	public Writer getResponseWriter() throws IOException {
		if(m_responseWriter == null) {
			Writer w = getResponse().getWriter();
			if(m_log) {
				Writer sw = getLogWriter();
				sw.append("\n---- Output -----\n");
				w = new TeeWriter(w, sw);
			}
			m_responseWriter = w;
		}
		return m_responseWriter;
	}

	@Override
	protected void destroy() throws Exception {
		if(m_log) {
			m_logsw.append("Completed....\n");
			if(getServlet().isLogging()) {
				getServlet().log(m_logsw.toString());
			} else
				System.out.println(m_logsw.toString());
		}
	}
}

package to.etc.domui.autotest;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.servlet.http.*;

import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.domui.util.upload.*;

/**
 * The request/response data for an automated inline test.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 9, 2013
 */
public class TestRequestResponse implements IRequestResponse {
	@Nonnull
	final private IDomUITestInfo m_testInfo;

	@Nonnull
	final private Map<String, Object> m_parameterMap = new HashMap<String, Object>();

	@Nonnull
	private String m_requestURI;

	@Nullable
	private ByteArrayOutputStream m_os;

	@Nullable
	private Writer m_writer;

	@Nullable
	private String m_outputEncoding;

	@Nullable
	private String m_outputContentType;

	@Nonnull
	private TestResponseType m_responseType = TestResponseType.NOTHING;

	@Nullable
	private String m_redirectURL;

	private int m_errorCode;

	@Nullable
	private String m_errorMessage;

	@Nonnull
	final private TestServerSession m_session;

	@Nonnull
	final private String m_queryString;

	public TestRequestResponse(@Nonnull TestServerSession session, @Nonnull IDomUITestInfo info, @Nonnull String requestURI, @Nonnull String queryString) {
		m_testInfo = info;
		m_requestURI = requestURI;
		m_session = session;
		m_queryString = queryString;
		PageParameters pp = PageParameters.decodeParameters(queryString);
		initParameters(pp);
	}


	public TestRequestResponse(@Nonnull TestServerSession session, @Nonnull IDomUITestInfo info, @Nonnull String requestURI, @Nonnull PageParameters parameters) {
		m_testInfo = info;
		m_requestURI = requestURI;
		m_session = session;
		initParameters(parameters);
		StringBuilder sb = new StringBuilder();
		DomUtil.addUrlParameters(sb, parameters, true);
		m_queryString = sb.toString();
	}

	private void initParameters(@Nonnull PageParameters parameters) {
		for(String name : parameters.getParameterNames()) {
			String[] vals = parameters.getStringArray(name);
			m_parameterMap.put(name, vals);
		}
	}

	@Override
	@Nonnull
	public String getRequestURI() {
		return m_requestURI;
	}

	@Override
	@Nonnull
	public String getQueryString() {
		return m_queryString;
	}

	public void setRequestURI(@Nonnull String requestURI) {
		m_requestURI = requestURI;
	}

	@Override
	@Nonnull
	public String getUserAgent() {
		return m_testInfo.getUserAgent();
	}

	@Override
	@Nonnull
	public String getApplicationURL() {
		StringBuilder sb = new StringBuilder();
		sb.append(m_testInfo.getApplicationHost());
		String wc = m_testInfo.getWebappContext();
		if(wc.length() > 0) {
			sb.append(wc);
			sb.append('/');
		}
		return sb.toString();
	}

	@Override
	@Nonnull
	public String[] getParameters(@Nonnull String name) {
		String[] vals = (String[]) m_parameterMap.get(name);
		return null == vals ? new String[0] : vals;
	}

	@Override
	@Nullable
	public String getParameter(@Nonnull String name) {
		String[] vals = getParameters(name);
		if(vals.length >= 1)
			return vals[0];
		else
			return null;
	}

	@Override
	@Nonnull
	public String[] getParameterNames() {
		return m_parameterMap.keySet().toArray(new String[m_parameterMap.keySet().size()]);
	}

	@Override
	@Nonnull
	public String[] getFileParameters() {
		return new String[0];
	}

	@Override
	@Nonnull
	public UploadItem[] getFileParameter(@Nonnull String name) {
		throw new IllegalStateException("Unsupported");
	}

	@Override
	@Nonnull
	public Writer getOutputWriter(@Nonnull String contentType, @Nullable String encoding) throws Exception {
		if(m_writer != null)
			throw new IllegalStateException("Duplicate request for output writer");

		OutputStream os = getOutputStream(contentType, encoding, -1);
		Writer osw = m_writer = new OutputStreamWriter(os, encoding);
		return osw;
	}

	@Override
	@Nonnull
	public OutputStream getOutputStream(@Nonnull String contentType, @Nullable String encoding, int contentLength) throws Exception {
		if(m_os != null)
			throw new IllegalStateException("Duplicate request for output stream");
		setType(TestResponseType.DOCUMENT);
		ByteArrayOutputStream os = m_os = new ByteArrayOutputStream();
		m_outputContentType = contentType;
		m_outputEncoding = encoding;
		return os;
	}

	public void flush() throws Exception {
		if(m_writer != null)
			m_writer.flush();
	}

	private void setType(@Nonnull TestResponseType type) {
		if(m_responseType != TestResponseType.NOTHING)
			throw new IllegalStateException("Reassigning output from " + m_responseType + " to ");
		m_responseType = type;
	}

	@Override
	public void redirect(@Nonnull String newUrl) throws Exception {
		setType(TestResponseType.REDIRECT);
		m_redirectURL = newUrl;
	}

	@Override
	public void sendError(int httpErrorCode, @Nonnull String message) throws Exception {
		setType(TestResponseType.ERROR);
		m_errorCode = httpErrorCode;
		m_errorMessage = message;
	}

	@Override
	@Nonnull
	public String getWebappContext() {
		return m_testInfo.getWebappContext();
	}

	@Nonnull @Override public String getHostName() {
		return m_testInfo.getApplicationHost();
	}

	@Override
	public void addCookie(@Nonnull Cookie cookie) {
	}

	@Override
	@Nonnull
	public Cookie[] getCookies() {
		return new Cookie[0];
	}

	@Override
	public void setExpiry(int cacheTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void releaseUploads() {}


	@Override
	public void setNoCache() {}

	@Override
	public void addHeader(@Nonnull String name, @Nonnull String value) {}

	@Nonnull
	public TestResponseType getResponseType() {
		return m_responseType;
	}

	public int getErrorCode() {
		return m_errorCode;
	}

	@Nullable
	public String getErrorMessage() {
		return m_errorMessage;
	}

	@Nullable
	public String getRedirectURL() {
		return m_redirectURL;
	}

	@Nullable
	public String getOutputContentType() {
		return m_outputContentType;
	}

	@Nullable
	public String getTextDocument() throws Exception {
		ByteArrayOutputStream os = m_os;
		if(null == os)
			return null;

		String ct = m_outputContentType;
		String enc = m_outputEncoding;
		if(ct == null)
			return null;
		int pos = ct.indexOf(';');
		if(pos > 0) {
			//-- We might have contentType+charset.
			String rest = ct.substring(pos + 1).trim();
			ct = ct.substring(0, pos).trim();

			//-- Split rest into strings separated by ';'
			String[] splits = rest.split(";");
			for(String frag : splits) {
				int eq = frag.indexOf('=');
				String name = frag;
				String value = "";
				if(eq > 0) {
					name = frag.substring(0, eq).trim().toLowerCase();
					value = frag.substring(eq + 1).trim();
				}

				if("charset".equalsIgnoreCase(name))
					enc = value;
			}
		}

		//-- We should have encoding and type.
		ct = ct.toLowerCase();
		if(ct.contains("text") || ct.contains("xml") || ct.contains("javascript")) {
			String str = new String(os.toByteArray(), enc);
			return str;
		}

		System.out.println("Unknown text document type: " + ct + " encoding " + enc);
		return null;
	}

	@Nonnull @Override public String getHostURL() {
		return "http://www.test.nl/";
	}

	@Override
	@Nullable
	public String getRemoteUser() {
		return m_testInfo.getRemoteUser();
	}

	@Override
	@Nullable
	public IServerSession getServerSession(boolean create) {
		return m_session;
	}
}

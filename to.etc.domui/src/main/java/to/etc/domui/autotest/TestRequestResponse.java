package to.etc.domui.autotest;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.server.IRequestResponse;
import to.etc.domui.server.IServerSession;
import to.etc.domui.state.PageParameters;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.upload.UploadItem;

import javax.servlet.http.Cookie;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * The request/response data for an automated inline test.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 9, 2013
 */
public class TestRequestResponse implements IRequestResponse {
	@NonNull
	final private IDomUITestInfo m_testInfo;

	@NonNull
	final private Map<String, Object> m_parameterMap = new HashMap<String, Object>();

	@NonNull
	private String m_requestURI;

	@Nullable
	private ByteArrayOutputStream m_os;

	@Nullable
	private Writer m_writer;

	@Nullable
	private String m_outputEncoding;

	@Nullable
	private String m_outputContentType;

	@NonNull
	private TestResponseType m_responseType = TestResponseType.NOTHING;

	@Nullable
	private String m_redirectURL;

	private int m_errorCode;

	@Nullable
	private String m_errorMessage;

	@NonNull
	final private TestServerSession m_session;

	@NonNull
	final private String m_queryString;

	public TestRequestResponse(@NonNull TestServerSession session, @NonNull IDomUITestInfo info, @NonNull String requestURI, @NonNull String queryString) {
		m_testInfo = info;
		m_requestURI = requestURI;
		m_session = session;
		m_queryString = queryString;
		PageParameters pp = PageParameters.decodeParameters(queryString);
		initParameters(pp);
	}


	public TestRequestResponse(@NonNull TestServerSession session, @NonNull IDomUITestInfo info, @NonNull String requestURI, @NonNull PageParameters parameters) {
		m_testInfo = info;
		m_requestURI = requestURI;
		m_session = session;
		initParameters(parameters);
		StringBuilder sb = new StringBuilder();
		DomUtil.addUrlParameters(sb, parameters, true);
		m_queryString = sb.toString();
	}

	private void initParameters(@NonNull PageParameters parameters) {
		for(String name : parameters.getParameterNames()) {
			String[] vals = parameters.getStringArray(name);
			m_parameterMap.put(name, vals);
		}
	}

	@Override
	@NonNull
	public String getRequestURI() {
		return m_requestURI;
	}

	@Override
	@NonNull
	public String getQueryString() {
		return m_queryString;
	}

	public void setRequestURI(@NonNull String requestURI) {
		m_requestURI = requestURI;
	}

	@Override
	@NonNull
	public String getUserAgent() {
		return m_testInfo.getUserAgent();
	}

	@Override
	@NonNull
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
	@NonNull
	public String[] getParameters(@NonNull String name) {
		String[] vals = (String[]) m_parameterMap.get(name);
		return null == vals ? new String[0] : vals;
	}

	@Override
	@Nullable
	public String getParameter(@NonNull String name) {
		String[] vals = getParameters(name);
		if(vals.length >= 1)
			return vals[0];
		else
			return null;
	}

	@Override
	@NonNull
	public String[] getParameterNames() {
		return m_parameterMap.keySet().toArray(new String[m_parameterMap.keySet().size()]);
	}

	@Override
	@NonNull
	public String[] getFileParameters() {
		return new String[0];
	}

	@Override
	@NonNull
	public UploadItem[] getFileParameter(@NonNull String name) {
		throw new IllegalStateException("Unsupported");
	}

	@Override
	@NonNull
	public Writer getOutputWriter(@NonNull String contentType, @Nullable String encoding) throws Exception {
		if(m_writer != null)
			throw new IllegalStateException("Duplicate request for output writer");

		OutputStream os = getOutputStream(contentType, encoding, -1);
		Writer osw = m_writer = new OutputStreamWriter(os, encoding);
		return osw;
	}

	@Override
	@NonNull
	public OutputStream getOutputStream(@NonNull String contentType, @Nullable String encoding, int contentLength) throws Exception {
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

	private void setType(@NonNull TestResponseType type) {
		if(m_responseType != TestResponseType.NOTHING)
			throw new IllegalStateException("Reassigning output from " + m_responseType + " to ");
		m_responseType = type;
	}

	@Override
	public void redirect(@NonNull String newUrl) throws Exception {
		setType(TestResponseType.REDIRECT);
		m_redirectURL = newUrl;
	}

	@Override
	public void sendError(int httpErrorCode, @NonNull String message) throws Exception {
		setType(TestResponseType.ERROR);
		m_errorCode = httpErrorCode;
		m_errorMessage = message;
	}

	@Override
	@NonNull
	public String getWebappContext() {
		return m_testInfo.getWebappContext();
	}

	@NonNull @Override public String getHostName() {
		return m_testInfo.getApplicationHost();
	}

	@Override
	public void addCookie(@NonNull Cookie cookie) {
	}

	@Override
	@NonNull
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
	public void addHeader(@NonNull String name, @NonNull String value) {}

	@NonNull
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

	@NonNull @Override public String getHostURL() {
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

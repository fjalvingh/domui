package to.etc.domui.autotest;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.servlet.http.*;

import to.etc.domui.server.*;
import to.etc.domui.state.*;
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

	public TestRequestResponse(@Nonnull IDomUITestInfo info, @Nonnull String requestURI, @Nonnull PageParameters parameters) {
		m_testInfo = info;
		m_requestURI = requestURI;
		initParameters(parameters);
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
		OutputStream os = m_os = new ByteArrayOutputStream();
		m_outputContentType = contentType;
		m_outputEncoding = encoding;
		return os;
	}

	@Override
	@Nonnull
	public String getWebappContext() {
		return m_testInfo.getWebappContext();
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
	public void redirect(@Nonnull String newUrl) throws Exception {}

	@Override
	public void sendError(int httpErrorCode, @Nonnull String message) throws Exception {
	}

	@Override
	public void releaseUploads() {}


	@Override
	public void setNoCache() {}

	@Override
	public void addHeader(@Nonnull String name, @Nonnull String value) {}
}

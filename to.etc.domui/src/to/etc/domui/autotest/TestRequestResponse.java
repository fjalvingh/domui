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

	public TestRequestResponse(@Nonnull IDomUITestInfo info, @Nonnull String requestURI, @Nonnull PageParameters parameters) {
		m_testInfo = info;
		m_requestURI = requestURI;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Nullable
	public String getParameter(@Nonnull String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Nonnull
	public String[] getParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Nonnull
	public String[] getFileParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Nonnull
	public UploadItem[] getFileParameter(@Nonnull String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Nonnull
	public Writer getOutputWriter(@Nonnull String contentType, @Nullable String encoding) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Nonnull
	public OutputStream getOutputStream(@Nonnull String contentType, @Nullable String encoding, int contentLength) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Nonnull
	public String getWebappContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addCookie(@Nonnull Cookie cookie) {
		// TODO Auto-generated method stub

	}

	@Override
	@Nonnull
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return null;
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

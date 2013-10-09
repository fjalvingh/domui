package to.etc.domui.server;

import java.io.*;

import javax.annotation.*;
import javax.servlet.http.*;

import to.etc.domui.util.upload.*;

/**
 *
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 3, 2013
 */
public interface IRequestResponse {
	/**
	 * Must return the path part of a request URL. It contains both webapp context and relative path, but no host name etc. It is required to
	 * start with a '/'.
	 * @return
	 */
	@Nonnull
	public String getRequestURI();

	/**
	 * Called when uploaded files are no longer needed; this can then discard of them.
	 */
	public void releaseUploads();

	@Nonnull
	public String getUserAgent();

	/**
	 * The "remote" user ID from a server request, if present. This does <b>not</b> need to represent the
	 * real logged-in user; it will however be filled if JSDK declarative security is used. This field is
	 * null if DomUI internal login code is used.
	 * @return
	 */
	@Nullable
	public String getRemoteUser();

	/**
	 * Returns a wrapper for a {@link HttpSession}. If "create" is true the session will be
	 * created if it does not exist in which case the call will never return null.
	 * @param create
	 * @return
	 */
	@Nullable
	public IServerSession getServerSession(boolean create);

	/**
	 * Return the base URL to the web application from the current requests. This uses hostname, protocol, portname
	 * and web application context from the incoming requests and returns it. The returned URL is guaranteed to end
	 * in a slash.
	 * @return
	 */
	@Nonnull
	public String getApplicationURL();

	@Nonnull
	public String[] getParameters(@Nonnull String name);

	@Nullable
	public String getParameter(@Nonnull String name);

	@Nonnull
	public String[] getParameterNames();

	@Nonnull
	public String[] getFileParameters();

	@Nonnull
	public UploadItem[] getFileParameter(@Nonnull String name);

	public void setNoCache();

	/*--- Content output ---*/

	public void addHeader(@Nonnull String name, @Nonnull String value);

	@Nonnull
	public Writer getOutputWriter(@Nonnull String contentType, @Nullable String encoding) throws Exception;

	@Nonnull
	public OutputStream getOutputStream(@Nonnull String contentType, @Nullable String encoding, int contentLength) throws Exception;

	/**
	 * Returns the webapp context as either an empty string for the ROOT context or a string starting without a slash and always ending
	 * in one, like "viewpoint/".
	 * @return
	 */
	@Nonnull
	public String getWebappContext();

	public void addCookie(@Nonnull Cookie cookie);

	@Nonnull
	public Cookie[] getCookies();

	public void setExpiry(int cacheTime);

	/**
	 * Send a redirect response to the client.
	 * @param newUrl
	 */
	public void redirect(@Nonnull String newUrl) throws Exception;

	/**
	 * Send an error back to the client.
	 * @param httpErrorCode
	 * @param message
	 */
	public void sendError(int httpErrorCode, @Nonnull String message) throws Exception;
}

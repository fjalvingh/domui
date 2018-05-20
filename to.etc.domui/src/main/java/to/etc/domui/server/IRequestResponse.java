package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.util.upload.UploadItem;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.OutputStream;
import java.io.Writer;

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
	@NonNull String getRequestURI();

	@NonNull String getQueryString();

	/**
	 * Called when uploaded files are no longer needed; this can then discard of them.
	 */
	void releaseUploads();

	@NonNull String getUserAgent();

	/**
	 * The "remote" user ID from a server request, if present. This does <b>not</b> need to represent the
	 * real logged-in user; it will however be filled if JSDK declarative security is used. This field is
	 * null if DomUI internal login code is used.
	 * @return
	 */
	@Nullable String getRemoteUser();

	/**
	 * Returns a wrapper for a {@link HttpSession}. If "create" is true the session will be
	 * created if it does not exist in which case the call will never return null.
	 * @param create
	 * @return
	 */
	@Nullable IServerSession getServerSession(boolean create);

	/**
	 * Return the base URL to the web application from the current requests. This uses hostname, protocol, portname
	 * and web application context from the incoming requests and returns it. The returned URL is guaranteed to end
	 * in a slash.
	 * @return
	 */
	@NonNull String getApplicationURL();

	@NonNull
	String getHostURL();

	@NonNull
	String getHostName();

	@NonNull String[] getParameters(@NonNull String name);

	@Nullable String getParameter(@NonNull String name);

	@NonNull String[] getParameterNames();

	@NonNull String[] getFileParameters() throws Exception;

	@NonNull UploadItem[] getFileParameter(@NonNull String name) throws Exception;

	void setNoCache();

	/*--- Content output ---*/

	void addHeader(@NonNull String name, @NonNull String value);

	@NonNull Writer getOutputWriter(@NonNull String contentType, @Nullable String encoding) throws Exception;

	@NonNull OutputStream getOutputStream(@NonNull String contentType, @Nullable String encoding, int contentLength) throws Exception;

	/**
	 * Returns the webapp context as either an empty string for the ROOT context or a string starting without a slash and always ending
	 * in one, like "viewpoint/".
	 * @return
	 */
	@NonNull String getWebappContext();

	void addCookie(@NonNull Cookie cookie);

	@NonNull Cookie[] getCookies();

	void setExpiry(int cacheTime);

	/**
	 * Send a redirect response to the client.
	 * @param newUrl
	 */
	void redirect(@NonNull String newUrl) throws Exception;

	/**
	 * Send an error back to the client.
	 * @param httpErrorCode
	 * @param message
	 */
	void sendError(int httpErrorCode, @NonNull String message) throws Exception;
}

package to.etc.domui.autotest;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.server.DomApplication;

public interface IDomUITestInfo {
	/**
	 * Return the Application instance under test.
	 * @return
	 */
	@NonNull DomApplication getApplication();

	/**
	 * Return the user agent string to use to define browser version in test.
	 * @return
	 */
	@NonNull String getUserAgent();

	/**
	 * Return the fake application URL.
	 * @return
	 */
	@NonNull String getApplicationHost();

	/**
	 * Return the web app context. This is either the empty string (for a ROOT context) or
	 * a string without any slashes.
	 * @return
	 */
	@NonNull String getWebappContext();

	@Nullable String getRemoteUser();

}

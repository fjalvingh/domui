package to.etc.domui.autotest;

import javax.annotation.*;

import to.etc.domui.server.*;

public interface IDomUITestInfo {
	/**
	 * Return the Application instance under test.
	 * @return
	 */
	@Nonnull DomApplication getApplication();

	/**
	 * Return the user agent string to use to define browser version in test.
	 * @return
	 */
	@Nonnull String getUserAgent();

	/**
	 * Return the fake application URL.
	 * @return
	 */
	@Nonnull String getApplicationHost();

	/**
	 * Return the web app context. This is either the empty string (for a ROOT context) or
	 * a string without any slashes.
	 * @return
	 */
	@Nonnull String getWebappContext();

	@Nullable String getRemoteUser();

}

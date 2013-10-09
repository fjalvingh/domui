package to.etc.domui.autotest;

import javax.annotation.*;

import to.etc.domui.server.*;

public interface IDomUITestInfo {
	/**
	 * Return the Application instance under test.
	 * @return
	 */
	@Nonnull
	public DomApplication getApplication();

	/**
	 * Return the user agent string to use to define browser version in test.
	 * @return
	 */
	@Nonnull
	public String getUserAgent();

	/**
	 * Return the fake application URL.
	 * @return
	 */
	@Nonnull
	public String getApplicationHost();

	/**
	 * Return the web app context. This is either the empty string (for a ROOT context) or
	 * a string without any slashes.
	 * @return
	 */
	@Nonnull
	public String getWebappContext();

}

package to.etc.domui.server;

import java.io.*;

import to.etc.domui.state.*;


public interface RequestContext extends IParameterInfo {
	public DomApplication getApplication();

	public AppSession getSession();

	public WindowSession getWindowSession();

	public String getExtension();

	public String getInputPath();

	public String getUserAgent();

	public String getRemoteUser();

	public String getRelativePath(String rel);

	public Writer getOutputWriter() throws IOException;

	public String getRelativeThemePath(String frag);

	public String translateResourceName(String in);

	/**
	 * This checks if the currently logged on user has the named permission. This permission is
	 * what the J2EE code stupidly calls a "role", which it isn't of course.
	 * This should be very fast as it's called very often.
	 *
	 * @param permissionName
	 * @return
	 */
	public boolean hasPermission(String permissionName);
}

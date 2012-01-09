package to.etc.util;

/**
 * Used by the autobuilder [builderd] to save information about a build of
 * a module so that it can be retrieved at runtime.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class BuildInfo {
	public String	name;

	public String	buildnr;

	public String	hostname;

	public String	userid;

	public String	date;

	public String	userlist;

	public String[]	filelist;

	public BuildInfo() {
	}
}

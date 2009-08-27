package to.etc.server.servlet;

/**
 * Servlet information on the website.
 *
 * @author jal
 * Created on Jan 19, 2006
 */
public interface WebsiteInfo {
	/**
	 * Returns the complete website URL, including http://, hostname and port and any
	 * part of the URL that forms the site context. This is guaranteed to end in a slash.
	 * @return
	 */
	public String getSiteURL();

	/**
	 * Gets the relative URL of the website. If this is the ROOT website this returns the
	 * empty string. For normal webapps this returns the context name ending in a slash. So
	 * for the webapp 'navidemo' this function returns "navidemo/".
	 * 
	 * @return
	 */
	public String getSiteRURL();

	public String getInputPath();

	/**
	 * Returns the webapp context path. This is the name of the webapp without slashes
	 * or other stuff. If this is the root webapp then it will return null.
	 * @return
	 */
	public String getContextName();
}

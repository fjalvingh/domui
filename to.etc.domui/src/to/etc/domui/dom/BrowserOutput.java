package to.etc.domui.dom;

import java.io.*;

public interface BrowserOutput {
	public void	 writeRaw(String s) throws IOException;
	
	/**
	 * Writes string data. This escapes XML control characters to their entity
	 * equivalent. This does NOT indent data with newlines, because string data
	 * in a content block may not change.
	 */
	public void text(String s) throws IOException;
	
	public void	nl() throws IOException;
	public void	inc();
	public void	dec();
	public void	setIndentEnabled(boolean ind);

	/**
	 * Writes a tag start. It can be followed by attr() calls.
	 * @param tagname
	 */
	public void	tag(final String tagname) throws IOException;

	/**
	 * Ends a tag by adding a > only.
	 */
	public void	endtag() throws IOException;

	/**
	 * Ends a tag by adding />.
	 * @throws IOException
	 */
	public void	endAndCloseXmltag() throws IOException;

	/**
	 * Write the closing tag (&lt;/name&gt;).
	 * @param name
	 * @throws IOException
	 */
	public void	closetag(String name) throws IOException;

	/**
	 * Appends an attribute to the last tag. The value's characters that are invalid are quoted into
	 * entities.
	 *
	 * @param namespace
	 * @param name
	 * @param value
	 * @throws IOException
	 */
	public void		attr(String name, String value) throws IOException;
	public void		rawAttr(String name, String value) throws IOException;

	/**
	 * Write a simple numeric attribute thingy.
	 *
	 * @param namespace
	 * @param name
	 * @param value
	 * @throws IOException
	 */
	public void		attr(String name, long value) throws IOException;
	public void		attr(String name, int value) throws IOException;
	public void		attr(String name, boolean value) throws IOException;


}

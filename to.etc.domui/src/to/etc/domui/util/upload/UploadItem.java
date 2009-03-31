package to.etc.domui.util.upload;

import java.io.*;

/**
 * An item as read from a file upload request.
 *
 * <p>Created on Nov 21, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public interface UploadItem {
	public String	getValue();
	
	public String	getName();

	public String	getRemoteFileName();

	public String	getContentType();

	public int		getSize();
	
	public boolean	isEmpty();
	
	public File		getFile();
	
	public String	getCharSet();
	
	public void		discard();

	public boolean	isFile();
}

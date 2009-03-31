package to.etc.domui.component.htmleditor;

import java.io.*;

public interface IEditorFileRef {
	public String			getMimeType() throws Exception;
	public int				getSize() throws Exception;
	public void				copyTo(OutputStream os) throws Exception;
	public void				close();
}

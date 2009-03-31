package to.etc.domui.component.htmleditor;

import java.util.*;

/**
 * Represents the stuff needed to implement an editor file system for the editor.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 30, 2008
 */
public interface IEditorFileSystem {
	public List<?>					getFilesAndFolders(String type, String path) throws Exception;
	public boolean					hasThumbnails();
	public List<EditorResourceType>	getResourceTypes();
	public IEditorFileRef			getStreamRef(String type, String path) throws Exception;
}

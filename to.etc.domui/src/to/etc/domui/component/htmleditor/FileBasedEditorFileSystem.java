package to.etc.domui.component.htmleditor;

import java.io.*;
import java.util.*;

import to.etc.domui.util.*;
import to.etc.util.*;

public class FileBasedEditorFileSystem implements IEditorFileSystem {
	private File m_imageRoot;

	private List<EditorResourceType> m_resources;

	private Set<String> m_imageAllowed = new HashSet<String>();

	private Set<String> m_imageDenied = new HashSet<String>();

	public FileBasedEditorFileSystem() {
		m_imageAllowed.add("jpg");
		m_imageAllowed.add("jpeg");
		m_imageAllowed.add("gif");
		m_imageAllowed.add("png");
	}

	public FileBasedEditorFileSystem(File f) {
		this();
		m_imageRoot = f;
	}

	public List< ? > getFilesAndFolders(String type, String path) throws Exception {
		List<Object> res = new ArrayList<Object>();
		File root = new File(m_imageRoot, path);
		if(root.exists() && root.isDirectory()) {
			File[] far = root.listFiles();
			for(File f : far) {
				if(f.isDirectory()) {
					//-- Always add the dir
					EditorFolder ef = new EditorFolder(f.getName(), f.list().length > 0, 255);
					res.add(ef);
				} else if(f.isFile()) {
					String name = f.getName();
					String ext = FileTool.getFileExtension(name).toLowerCase();
					if(m_imageDenied.contains(ext))
						continue;
					if(m_imageAllowed.size() == 0 || m_imageAllowed.contains(ext)) {
						EditorFile ef = new EditorFile(name, (int) f.length(), new Date(f.lastModified()));
						res.add(ef);
					}
				}
			}
		}
		return res;
	}

	public List<EditorResourceType> getResourceTypes() {
		if(m_resources == null) {
			m_resources = new ArrayList<EditorResourceType>();
			m_resources.add(new EditorResourceType("Images", "key=Images", 255, m_imageAllowed, m_imageDenied));
		}
		return m_resources;
	}

	public boolean hasThumbnails() {
		return true;
	}

	/**
	 *
	 * @see to.etc.domui.component.htmleditor.IEditorFileSystem#getStreamRef(java.lang.String, java.lang.String)
	 */
	public IEditorFileRef getStreamRef(String type, String path) throws Exception {
		final File root = new File(m_imageRoot, path);
		if(!root.exists() || !root.isFile())
			return null;

		return new IEditorFileRef() {

			public int getSize() throws Exception {
				return (int) root.length();
			}

			public String getMimeType() throws Exception {
				String ext = FileTool.getFileExtension(root.getName());
				return ServerTools.getExtMimeType(ext);
			}

			public void copyTo(OutputStream os) throws Exception {
				InputStream is = null;
				try {
					is = new FileInputStream(root);
					FileTool.copyFile(os, is);
				} finally {
					try {
						if(is != null)
							is.close();
					} catch(Exception x) {}
				}
			}

			public void close() {}
		};
	}
}

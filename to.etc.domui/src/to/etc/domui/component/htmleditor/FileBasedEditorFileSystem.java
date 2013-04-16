/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.htmleditor;

import java.io.*;
import java.util.*;

import to.etc.util.*;
import to.etc.webapp.core.*;

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

	@Override
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

	@Override
	public List<EditorResourceType> getResourceTypes() {
		if(m_resources == null) {
			m_resources = new ArrayList<EditorResourceType>();
			m_resources.add(new EditorResourceType("Images", "key=Images", 255, m_imageAllowed, m_imageDenied));
		}
		return m_resources;
	}

	@Override
	public boolean hasThumbnails() {
		return true;
	}

	/**
	 *
	 * @see to.etc.domui.component.htmleditor.IEditorFileSystem#getStreamRef(java.lang.String, java.lang.String)
	 */
	@Override
	public IEditorFileRef getStreamRef(String type, String path) throws Exception {
		final File root = new File(m_imageRoot, path);
		if(!root.exists() || !root.isFile())
			return null;

		return new IEditorFileRef() {

			@Override
			public int getSize() throws Exception {
				return (int) root.length();
			}

			@Override
			public String getMimeType() throws Exception {
				String ext = FileTool.getFileExtension(root.getName());
				return ServerTools.getExtMimeType(ext);
			}

			@Override
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

			@Override
			public void close() {}
		};
	}
}

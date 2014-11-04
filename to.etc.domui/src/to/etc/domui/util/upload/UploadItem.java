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
package to.etc.domui.util.upload;

import java.io.*;

import javax.annotation.*;

/**
 * An item that was gotten through the multipart/form data code. This
 * can represent an uploaded file, and all of the metadata that the
 * browser has sent while it uploaded the file. When used from a page,
 * the actual file data will be deleted as soon as the page is destroyed,
 * you must make a copy if you want to retain the data.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 12, 2011
 */
final public class UploadItem {
	private String m_fieldName;

	private String m_contentType;

	private String m_charset;

	private String m_value;

	private String m_fileName;

	private File m_backingFile;

	private boolean m_file;

	public UploadItem(String fieldname, String contenttype, String charset, String filename, boolean isfile) {
		m_fieldName = fieldname;
		m_contentType = contenttype;
		m_fileName = filename;
		m_charset = charset;
		m_file = isfile;
	}

	/**
	 * T if this actually contains a file (it is not just a parameter).
	 * @return
	 */
	public boolean isFile() {
		return m_file;
	}

	/**
	 * Called when the item is resident in a file. When called the code
	 * takes ownership of the file, and deletes the file as soon as this
	 * item gets finalized or otherwise cleaned up.
	 *
	 * @param f
	 */
	public void setFile(File f) {
		m_backingFile = f;
	}

	//	@Override
	//	protected void finalize() throws Throwable {
	//		try {
	//			if(m_backingFile != null) {
	//				m_backingFile.delete();
	//				m_backingFile = null;
	//			}
	//		}
	//		catch(Exception x) {}
	//		super.finalize();
	//	}

	/**
	 * If known, the character set for how the data is to be decoded. Can be null.
	 */
	@Nullable
	public String getCharSet() {
		return m_charset;
	}

	//	private String getInternalCharset() {
	//		return getCharSet() == null ? "ISO-8859-1" : getCharSet();
	//	}
	//

	/**
	 * If this is a parameter and not a File, this contains the parameter's value, decoded
	 * using the browser-specified character encoding.
	 */
	public String getValue() {
		return m_value;
	}

	void setValue(String sa) {
		m_value = sa;
	}

	void setValue(File f) {
		m_backingFile = f;
	}

	/**
	 * The name of the input field.
	 * @return
	 */
	@Nonnull
	public String getName() {
		return m_fieldName;
	}

	/**
	 * If specified, the name that the browser provided as the "local file name", i.e. the name
	 * of the file on the browser's file system that was selected for upload. Will not usually
	 * contain a path.
	 * @return
	 */
	@Nullable
	public String getRemoteFileName() {
		return m_fileName;
	}

	/**
	 * If specified, the MIME content type the browser provided during the upload.
	 * @return
	 */
	@Nullable
	public String getContentType() {
		return m_contentType;
	}

	/**
	 * If this is a FILE item, this contains the size, in bytes, of the uploaded file.
	 * @return
	 */
	public int getSize() {
		if(isFile() && m_backingFile == null)
			throw new IllegalStateException("The file has already been closed (deleted)");
		if(m_backingFile != null)
			return (int) m_backingFile.length();
		return 0;
	}

	/**
	 * SILLY_INTERFACE? If this holds no value it returns true.
	 * @return
	 */
	public boolean isEmpty() {
		return m_fileName == null;
	}

	/**
	 * Return a file for this item. If the item is not yet file-based then a new
	 * file is generated for this item.
	 *
	 * @see to.etc.server.upload.UploadItem#getFile()
	 */
	public File getFile() {
		if(isFile() && m_backingFile == null)
			throw new IllegalStateException("The file has already been closed (deleted)");
		return m_backingFile;
	}

	/**
	 * When the request finishes and no-one has gotten this parameter the file must be discarded.
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "FindBugs definition is wrong for mkdirs, and delete() may fail in code here")
	void discard() {
		if(m_backingFile == null)
			return;
		try {
			System.out.println("Releasing unclaimed FILE upload: " + getName() + ", " + getSize() + " @" + getFile());
			m_backingFile.delete();
			m_backingFile = null;
		} catch(Exception x) {}
	}

	/**
	 * Called from user code to release the file attached to this item.
	 */
	public void	close() {
		if(m_backingFile == null)
			return;
		try {
			m_backingFile.delete();
			m_backingFile = null;
		} catch(Exception x) {}
	}
}

package to.etc.domui.util.upload;

import java.io.*;

public class ImplUploadItem implements UploadItem {
	private String m_fieldName;

	private String m_contentType;

	private String m_charset;

	private String	m_value;

	private String m_fileName;

	private File m_backingFile;

	private boolean	m_file;

	public ImplUploadItem(String fieldname, String contenttype, String charset, String filename, boolean isfile) {
		m_fieldName = fieldname;
		m_contentType = contenttype;
		m_fileName = filename;
		m_charset = charset;
		m_file = isfile;
	}

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
	void setFile(File f) {
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

	public String getCharSet() {
		return m_charset;
	}

//	private String getInternalCharset() {
//		return getCharSet() == null ? "ISO-8859-1" : getCharSet();
//	}
//
	public String getValue() {
		return m_value;
	}

	void setValue(String sa) {
		m_value = sa;
	}
	void setValue(File f) {
		m_backingFile = f;
	}

	public String getName() {
		return m_fieldName;
	}

	public String getRemoteFileName() {
		return m_fileName;
	}

	public String getContentType() {
		return m_contentType;
	}

	public int getSize() {
		if(m_backingFile != null)
			return (int) m_backingFile.length();
		return 0;
	}

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
		return m_backingFile;
	}

	public void discard() {
		if(m_backingFile == null)
			return;
		try {
			System.out.println("Releasing unclaimed FILE upload: "+getName()+", "+getSize()+" @"+getFile());
			m_backingFile.delete();
			m_backingFile = null;
		}
		catch(Exception x) {}
	}
}

package to.etc.server.upload;

import java.io.*;

import to.etc.util.*;

public class ImplUploadItem implements UploadItem {
	private String	m_fieldName;

	private String	m_contentType;

	private String	m_charset;

	private String	m_fileName;

	private boolean	m_isFile;

	/** The data, if buffered in memory */
	private byte[]	m_buffer;

	private File	m_backingFile;

	private File	m_repos;

	public ImplUploadItem(String fieldname, String contenttype, String charset, String filename, File repos, boolean isfileitem) {
		m_fieldName = fieldname;
		m_contentType = contenttype;
		m_fileName = filename;
		m_isFile = isfileitem;
		m_charset = charset;
		m_repos = repos;
	}

	/**
	 * Called when the data is memory-buffered.
	 * @param buffer
	 */
	void setBuffer(byte[] buffer) {
		m_buffer = buffer;
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
		//		FileTool.registerCleanup(this, f);				// Ask gc to delete when this object is discarded
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if(m_backingFile != null) {
				m_backingFile.delete();
				m_backingFile = null;
			}
		} catch(Exception x) {}
		super.finalize();
	}

	public boolean isFileItem() {
		return m_isFile;
	}

	public String getCharSet() {
		return m_charset;
	}

	private String getInternalCharset() {
		return getCharSet() == null ? "ISO-8859-1" : getCharSet();
	}

	public String getValue() {
		if(m_isFile)
			throw new IllegalStateException("Cannot get string value from File item.");
		String chset = getInternalCharset();
		if(m_buffer != null) {
			try {
				return new String(m_buffer, chset);
			} catch(UnsupportedEncodingException x) {
				throw new WrappedException("Unsupported encoding " + chset + ": " + x, x);
			}
		}

		try {
			return FileTool.readFileAsString(m_backingFile, chset);
		} catch(Exception x) {
			throw new WrappedException("Error reading tempfile " + x, x);
		}
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
		if(m_buffer != null)
			return m_buffer.length;
		if(m_backingFile != null)
			return (int) m_backingFile.length();
		return 0;
	}

	public boolean isEmpty() {
		return m_fileName == null;
	}

	public boolean inMemory() {
		return m_buffer != null;
	}

	public InputStream getInputStream() {
		if(m_buffer != null)
			return new ByteArrayInputStream(m_buffer);
		try {
			return new FileInputStream(m_backingFile);
		} catch(Exception x) {
			throw new WrappedException("Error reading tempfile " + x, x);
		}
	}

	/**
	 * Return a file for this item. If the item is not yet file-based then a new
	 * file is generated for this item.
	 *
	 * @see to.etc.server.upload.UploadItem#getFile()
	 */
	public File getFile() {
		if(!isFileItem())
			throw new IllegalStateException("This item is not a file item");
		if(m_backingFile != null)
			return m_backingFile;

		//-- Make a file for this item.
		FileOutputStream fos = null;
		try {
			File f = FileTool.makeTempFile(m_repos);
			fos = new FileOutputStream(f);
			fos.write(m_buffer);
			setFile(f);
			return m_backingFile;
		} catch(Exception x) {
			throw new WrappedException("Cannot create file from memory-buffer " + x, x);
		} finally {
			try {
				if(fos != null)
					fos.close();
			} catch(Exception x) {}
		}
	}

	public void discard() {
		if(m_backingFile == null)
			return;
		try {
			m_backingFile.delete();
			m_backingFile = null;
		} catch(Exception x) {}
	}
}

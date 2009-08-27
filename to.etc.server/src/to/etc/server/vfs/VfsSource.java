package to.etc.server.vfs;

import java.io.*;

/**
 * A point-in-time representation of a VFS stream source. This knows
 * the mime type, length and date-modified of a VFS object, and has
 * a method to retrieve a stream from an object. A VfsSource always
 * refers to an existing source; it cannot refer to a nonexisting
 * one (as a reference can).
 *
 * <p>Created on Dec 2, 2005
 * @author  <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
final public class VfsSource {
	/** The provider that created this source. */
	private VfsProvider	m_provider;

	/** The reference to the thing. */
	private VfsKey		m_reference;

	/** The date modified of the source at time of creation(!) */
	private long		m_modified;

	/** The mime type of the source at creation time. */
	private String		m_mimeType;

	/** The size, in bytes, of the source at creation time. */
	private int			m_size;

	/** The encoding. */
	private String		m_encoding;

	private boolean		m_directory;

	public VfsSource(VfsProvider p, VfsKey key) {
		m_provider = p;
		m_reference = key;
		m_directory = true;
		m_mimeType = "x-application/directory";
	}

	public VfsSource(VfsProvider p, VfsKey ref, String mime, String enc, int size, long modified) {
		m_provider = p;
		m_reference = ref;
		m_directory = false;
		m_mimeType = mime;
		m_encoding = enc;
		m_size = size;
		m_modified = modified;
	}

	final public String getVfsPath() {
		return m_provider.getVfsPath(this);
	}

	final public String getRealPath() {
		return m_provider.getRealPath(this);
	}

	/** 
	 * Gets the last date/time this document was modified 
	 */
	public long getDateModified() {
		return m_modified;
	}

	/**
	 * Gets the MIME type for this document
	 */
	public String getMimeType() {
		return m_mimeType;
	}

	/**
	 * Returns a valid Java encoding name for the resource *if* the resource is a  
	 * text type. For binary streams this returns null. This encoding will be used 
	 * when a reader is needed from the resource.
	 */
	public String getEncoding() {
		return m_encoding;
	}

	/**
	 * Returns the size of this resource, in bytes, or -1 if the length cannot
	 * be determined.
	 */
	public int size() {
		return m_size;
	}

	/**
	 * Get an inputstream from the resource.
	 */
	public InputStream getInputStream() throws Exception {
		return m_provider.getInputStream(this);
	}

	public boolean needCache() {
		return m_provider.needCache(this);
	}

	public int getExpiry() {
		return m_provider.getExpiry(this);
	}

	public boolean isDirectory() {
		return m_directory;
	}

	public VfsKey getReference() {
		return m_reference;
	}
}

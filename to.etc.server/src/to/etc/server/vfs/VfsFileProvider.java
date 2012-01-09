package to.etc.server.vfs;

import java.io.*;

import to.etc.server.*;
import to.etc.util.*;

/**
 * A resolver which uses the file system to resolve (partial) URL's.
 * <p>Created on Dec 5, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public class VfsFileProvider implements VfsProvider {
	private File	m_root;

	private String	m_encoding;

	private int		m_checkinterval;

	public VfsFileProvider(File root, String encoding, int checkinterval, int expiry) {
		m_root = root;
		m_encoding = encoding;
		m_checkinterval = checkinterval;
	}

	final public int getCheckInterval() {
		return m_checkinterval;
	}

	final public File getRoot() {
		return m_root;
	}

	String getEncoding() {
		return m_encoding;
	}

	public boolean accept(VfsKey key) {
		return (key instanceof VfsFileKey) && ((VfsFileKey) key).getEncoding() == m_encoding;
	}

	public File makeFile(VfsKey key) {
		VfsFileKey k = (VfsFileKey) key;
		return k.getFile();
	}

	public int getExpiry(VfsSource vs) {
		return 0;
	}

	public InputStream getInputStream(VfsSource vs) throws Exception {
		File f = makeFile(vs.getReference());
		try {
			return new FileInputStream(f);
		} catch(FileNotFoundException x) {
			return null;
		}
	}

	public String getRealPath(VfsSource sr) {
		return sr.getReference().toString();
	}

	public String getVfsPath(VfsSource sr) {
		return VFS.getInstance().resolveKey(sr.getReference());
	}

	public VfsSource makeSource(VfsKey key, VfsDependencyCollector deplist) throws Exception {
		long ts = -1;
		try {
			File f = makeFile(key);
			if(!f.exists())
				return null;
			if(f.isDirectory())
				return new VfsSource(this, key);

			//-- Is a file: get data from file name and the like,
			String ext = FileTool.getFileExtension(f.toString());
			String mime = ServerTools.getExtMimeType(ext);
			if(mime == null)
				mime = "application/octet-stream";
			ts = f.lastModified();
			return new VfsSource(this, key, mime, m_encoding, (int) f.length(), ts);
		} finally {
			if(deplist != null)
				deplist.add(key, ts);
		}
	}

	public boolean needCache(VfsSource vs) {
		return false;
	}
}

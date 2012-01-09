package to.etc.server.vfs;

import java.io.*;

import to.etc.util.*;

public class VfsFilePathResolver implements VfsPathResolver {
	/** The encoding of files on this path, */
	private String	m_encoding;

	private File	m_fsroot;

	private String	m_fspath;

	public VfsFilePathResolver(File root, String encoding) throws IOException {
		m_encoding = encoding;
		m_fsroot = root.getCanonicalFile();
		m_fspath = m_fsroot.toString();
	}

	public String resolveKey(VfsKey key) {
		VfsFileKey k = (VfsFileKey) key;
		String s = k.getFile().toString();
		if(s.length() < m_fspath.length())
			throw new IllegalStateException("Invalid input: " + s);
		return s.substring(m_fspath.length());
	}

	public VfsKey resolvePath(PathSplitter path) throws Exception {
		File f = new File(m_fsroot, path.getCurrentAndRest());
		return new VfsFileKey(f, m_encoding);
	}
}

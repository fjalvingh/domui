package to.etc.server.vfs;

import java.io.*;
import java.util.*;

import to.etc.util.*;

/**
 * Singleton base class containing the provider registry. Each
 * provider must register itself with the registry if it needs
 * serialization support.
 *
 * <p>Created on Dec 5, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public final class VFS {
	private static final VFS				m_instance		= new VFS();

	/** The root VFS path resolver. */
	private VfsSegmentResolver				m_root			= new VfsSegmentResolver();

	private Map<String, VfsFileProvider>	m_fsProviderMap	= new Hashtable<String, VfsFileProvider>();

	private String							m_encoding;

	class FSPathResolver implements VfsPathResolver {
		public String resolveKey(VfsKey key) {
			VfsFileKey k = (VfsFileKey) key;
			return k.getFile().toString().substring(1);
		}

		public VfsKey resolvePath(PathSplitter path) throws Exception {
			return new VfsFileKey(new File(path.getCurrentAndRest()), getDefaultEncoding());
		}
	}

	private VFS() {
		//-- Register the localhost file system path resolver.
		m_encoding = System.getProperty("file.encoding");
		if(m_encoding == null)
			m_encoding = "utf-8";
		register("[fs]", new FSPathResolver());
	}

	public String getDefaultEncoding() {
		return m_encoding;
	}

	static public VFS getInstance() {
		return m_instance;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Provider registry.									*/
	/*--------------------------------------------------------------*/
	final private List<VfsProvider>	m_providerList	= new ArrayList<VfsProvider>();

	public synchronized void register(VfsProvider p) {
		if(!m_providerList.contains(p))
			m_providerList.add(p);
	}

	public synchronized VfsProvider getProvider(VfsKey key) throws VfsNoProviderException {
		for(int i = m_providerList.size(); --i >= 0;) {
			VfsProvider p = m_providerList.get(i);
			if(p.accept(key))
				return p;
		}
		throw new VfsNoProviderException("No VFS provider accepts a key of type " + key.getClass().getCanonicalName());
	}

	/**
	 * Register a path resolver somewhere in the vfs directory tree.
	 * @param path
	 * @param res
	 */
	public void register(String path, VfsPathResolver res) {
		m_root.registerResolver(path, res);
	}

	/**
	 * Returns a source for a VFS key. If a value is passed for the dependency
	 * list then it gets filled with all of the dependencies that were found for
	 * the item. The list may be null, though.
	 *
	 * @param key
	 * @param deps
	 * @return
	 * @throws Exception
	 */
	public VfsSource get(VfsKey key, VfsDependencyCollector deps) throws Exception {
		VfsProvider p = getProvider(key);
		return p.makeSource(key, deps);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Path translator.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Resolves a VFS pathname to a VFS key.
	 */
	public VfsKey resolvePath(String path) throws Exception {
		PathSplitter ps = new PathSplitter(path);
		return resolvePath(ps);
	}

	/**
	 * Resolves a VFS pathname to a VFS key.
	 */
	public VfsKey resolvePath(PathSplitter path) throws Exception {
		return m_root.resolvePath(path);
	}

	public String resolveKey(VfsKey k) {
		return m_root.resolveKey(k);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	File system resolver								*/
	/*--------------------------------------------------------------*/
	/**
	 * Return a VFS path resolver for a given filesysteem root.
	 */
	public synchronized VfsPathResolver makeFilesystemResolver(File fsroot, String encoding) throws IOException {
		//-- Make sure a provider is registered for this encoding type.
		if(encoding == null)
			encoding = System.getProperty("file.encoding");
		encoding = encoding.toLowerCase();
		VfsFileProvider p = m_fsProviderMap.get(encoding);
		if(p == null) {
			p = new VfsFileProvider(new File("/"), encoding, -1, -1);
			m_fsProviderMap.put(encoding, p);
			register(p);
		}

		//-- Create a path resolver resolving a partial to a VfsKey.
		VfsPathResolver pr = new VfsFilePathResolver(fsroot, encoding);
		return pr;
	}
}

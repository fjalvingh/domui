package to.etc.server.vfs;

import to.etc.util.*;

public interface VfsPathResolver {
	/**
	 * This must resolve the input path into an opague VFS key object. If the path cannot
	 * be resolved this will throw a VfsNotFoundException.
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public VfsKey resolvePath(PathSplitter path) throws Exception;

	/**
	 * Returns the VFS pathname for the key passed. If the key is not recognised this
	 * returns null.
	 *
	 * @param key
	 * @return
	 */
	public String resolveKey(VfsKey key);
}

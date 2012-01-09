package to.etc.server.vfs;

import java.io.*;

/**
 * A provider can take a VfsPhysicalKey and create a StreamSource from it. <p>Created on Dec 2, 2005
 * @author  <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public interface VfsProvider {
	/**
	 * Returns T if this provider accepts the key passed. If so the provider
	 * must be able to resolve the key into a reference. This does *not* mean
	 * that the source referred to by the key <i>exists</i>, only that it could
	 * exist and that this provider knows how to make the thing.
	 * @param key
	 * @return
	 */
	public boolean accept(VfsKey key);

	public VfsSource makeSource(VfsKey ref, VfsDependencyCollector deps) throws Exception;

	/**
	 * Get an inputstream from the resource.
	 */
	public InputStream getInputStream(VfsSource vs) throws Exception;

	public boolean needCache(VfsSource vs);

	public int getExpiry(VfsSource vs);

	public String getVfsPath(VfsSource sr);

	public String getRealPath(VfsSource sr);

	public int getCheckInterval();
}

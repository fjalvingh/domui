package to.etc.server.cache;

import to.etc.server.vfs.*;

/**
 * Factory to create cached items when they do not exist in the
 * cache, or when they have changed.
 *
 * <p>Created on Dec 8, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public interface CacheObjectFactory {
	public CacheStats makeStatistics();

	public Object makeObject(ResourceRef ref, VfsPathResolver vr, Object pk, DependencySet depset, Object p1, Object p2, Object p3) throws Exception;
}

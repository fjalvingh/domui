package to.etc.server.vfs;

import java.util.*;

import to.etc.util.*;

/**
 * Resolves a single path segment and allows for "mounting" other
 * resolvers on a segment.
 *
 * <p>Created on Dec 27, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public class VfsSegmentResolver implements VfsPathResolver {
	private Map<String, VfsPathResolver>	m_segMap	= new Hashtable<String, VfsPathResolver>();

	private boolean							m_caseIndependent;

	public VfsSegmentResolver() {
	}

	public VfsSegmentResolver(boolean caseindependent) {
		m_caseIndependent = caseindependent;
	}

	public synchronized void registerResolver(String rpath, VfsPathResolver r) {
		PathSplitter ps = new PathSplitter(rpath);
		if(ps.isEmpty())
			throw new IllegalStateException("Invalid pathname: '" + rpath + "'");
		VfsSegmentResolver pr = this;
		String name = null;
		for(;;) {
			//-- Get placeholder name,
			name = ps.getCurrent();
			if(m_caseIndependent)
				name = name.toLowerCase();
			if(!ps.next())
				break;

			//-- There is another fragment! Get or add a segment resolver here
			Object o = pr.m_segMap.get(name);
			if(o == null) {
				VfsSegmentResolver sr = new VfsSegmentResolver(m_caseIndependent); // Add a new resolver there
				pr.m_segMap.put(name, sr);
				pr = sr;
			} else if(o instanceof VfsSegmentResolver) {
				pr = (VfsSegmentResolver) o;
			} else
				throw new IllegalStateException("A resolver for path=" + ps.getUptoCurrent() + " already exists!");
		}

		//-- At the last level.
		Object o = pr.m_segMap.get(name);
		if(o != null)
			throw new IllegalStateException("A resolver for path=" + rpath + " already exists");
		pr.m_segMap.put(name, r);
	}

	/**
	 * Walk the resolver tree to find any resolver that knows
	 * @see to.etc.server.vfs.VfsPathResolver#resolveKey(java.lang.Object)
	 */
	public synchronized String resolveKey(VfsKey key) {
		for(Map.Entry<String, VfsPathResolver> e : m_segMap.entrySet()) {
			String v = e.getValue().resolveKey(key);
			if(v != null)
				return e.getKey() + "/" + v;
		}
		return null;
	}

	public VfsKey resolvePath(PathSplitter path) throws Exception {
		if(path.isEmpty())
			throw new VfsNotFoundException("Missing path segments in " + path.getPath(), path.getPath());
		String name = path.getCurrent();
		if(m_caseIndependent)
			name = name.toLowerCase();
		VfsPathResolver r;
		synchronized(this) {
			r = m_segMap.get(name);
		}
		if(r == null)
			throw new VfsNotFoundException("No resolver found at '" + path.getUptoCurrent(), path.getPath());
		if(!path.next())
			throw new VfsNotFoundException("Missing path segments in " + path.getPath(), path.getPath());
		return r.resolvePath(path);
	}
}

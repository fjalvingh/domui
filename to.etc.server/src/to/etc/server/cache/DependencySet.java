package to.etc.server.cache;

import java.util.*;

import to.etc.server.vfs.*;

/**
 * Helper class which helps building a set of dependencies for
 * cached resources. It accepts keys plus timestamps and creates
 * cache dependencies for each pair passed. If a key has already
 * been added it does not get added twice, and only the first
 * timestamp gets seen.
 *
 * @author jal
 * Created on Dec 11, 2005
 */
final public class DependencySet implements VfsDependencyCollector {
	private List<CacheDependency>	m_list	= new ArrayList<CacheDependency>();

	public DependencySet() {
	}

	//	public void	add(Object key, long ts)
	//	{
	//		add(key, ts, m_defaultCheckInterval);
	//	}

	/**
	 * VfsDependencyCollector implementation.
	 */
	public void add(Object key, long ts) {
		add(key, ts, 0);
	}

	public void add(Object key, long ts, int checkinterval) {
		//-- Check if the dependency is already present,
		for(CacheDependency cd : m_list) {
			if(cd.getKey().equals(key)) // Already present?
				return;
		}
		m_list.add(new CacheDependency(key, ts, checkinterval));
	}

	public void add(Object key) {
		add(key, -1, 0);
	}

	CacheDependencies getDependencies() {
		//		if(m_list.size() == 0)
		//			throw new IllegalStateException("Dependency set is empty - it should at least contain the primary source!");
		return new CacheDependencies(m_list);
	}
}

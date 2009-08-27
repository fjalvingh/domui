package to.etc.server.vfs;

import java.util.*;

public class VfsDependencyList implements Iterable<VfsDependency>, VfsDependencyCollector {
	private List<VfsDependency>	m_list	= new ArrayList<VfsDependency>();

	private void add(VfsDependency d) {
		for(VfsDependency dep : m_list) {
			if(dep.getKey().equals(d.getKey()))
				return;
		}
		m_list.add(d);
	}

	public void add(Object key, long ts) {
		for(VfsDependency dep : m_list) {
			if(dep.getKey().equals(key))
				return;
		}
		m_list.add(new VfsDependency(key, ts));
	}

	public Iterator<VfsDependency> iterator() {
		return m_list.iterator();
	}

	public void addAll(Collection<VfsDependency> list) {
		for(VfsDependency d : list)
			add(d);
	}

	public void addAll(VfsDependencyList list) {
		for(VfsDependency d : list)
			add(d);
	}
}

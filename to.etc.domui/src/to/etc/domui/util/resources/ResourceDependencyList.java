package to.etc.domui.util.resources;

import java.util.*;

final public class ResourceDependencyList {
	private List<ResourceDependency> m_deplist = Collections.EMPTY_LIST;

	public void add(IModifyableResource c) {
		if(m_deplist == Collections.EMPTY_LIST)
			m_deplist = new ArrayList<ResourceDependency>(5);
		m_deplist.add(new ResourceDependency(c));
	}

	public boolean isModified() {
		for(ResourceDependency c : m_deplist) {
			if(c.isModified())
				return true;
		}
		return false;
	}
}

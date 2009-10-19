package to.etc.domui.util.resources;

import java.util.*;

/**
 * Contains a list of things that an "owner" depends on, and for each thing
 * a "timenstamp" of that thing at the time it was used (added) to this list.
 * By comparing the "actual" timestamp with the stored timestamp we can see
 * if the item changed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 19, 2009
 */
final public class ResourceDependencyList {
	static private class DependencyTimestamp {
		private IModifyableResource m_resource;

		private long m_timestamp;

		public DependencyTimestamp(IModifyableResource resource) {
			m_resource = resource;
			m_timestamp = resource.getLastModified();
		}

		public boolean isModified() {
			return m_timestamp != m_resource.getLastModified();
		}
	}

	private List<DependencyTimestamp> m_deplist = Collections.EMPTY_LIST;

	public void add(IModifyableResource c) {
		if(m_deplist == Collections.EMPTY_LIST)
			m_deplist = new ArrayList<DependencyTimestamp>(5);
		m_deplist.add(new DependencyTimestamp(c));
	}

	public boolean isModified() {
		for(DependencyTimestamp c : m_deplist) {
			if(c.isModified())
				return true;
		}
		return false;
	}
}

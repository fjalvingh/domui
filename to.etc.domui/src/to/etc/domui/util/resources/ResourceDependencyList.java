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
	private List<ResourceTimestamp> m_deplist = Collections.EMPTY_LIST;

	public void add(IModifyableResource c) {
		if(m_deplist == Collections.EMPTY_LIST)
			m_deplist = new ArrayList<ResourceTimestamp>(5);
		m_deplist.add(new ResourceTimestamp(c, c.getLastModified()));
	}

	public boolean isModified() {
		for(ResourceTimestamp c : m_deplist) {
			if(c.isModified())
				return true;
		}
		return false;
	}
}

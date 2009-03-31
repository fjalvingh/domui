package to.etc.domui.util.resources;

import java.util.*;

final public class ResourceDependencyList implements IWithModifiedCalculator {
	private List<IWithModifiedCalculator>	m_deplist = Collections.EMPTY_LIST;

	public void		add(IWithModifiedCalculator c) {
		if(m_deplist == Collections.EMPTY_LIST)
			m_deplist = new ArrayList<IWithModifiedCalculator>(5);
		m_deplist.add(c);
	}

	public boolean	isModified() {
		for(IWithModifiedCalculator c : m_deplist) {
			if(c.isModified())
				return true;
		}
		return false;
	}
}

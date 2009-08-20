package to.etc.domui.component.lookup;

import java.util.*;

import to.etc.domui.component.meta.*;

/**
 * Default Registry of Lookup control factories.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 23, 2008
 */
public class LookupControlRegistry {
	private List<LookupControlFactory> m_factoryList = new ArrayList<LookupControlFactory>();

	public LookupControlRegistry() {
		register(new LookupFactoryString());
		register(new LookupFactoryDate());
		register(new LookupFactoryNumber());
		register(new LookupFactoryRelation());
		register(new LookupFactoryEnumAndBool());
		register(new LookupFactoryRelationCombo());
	}

	public synchronized List<LookupControlFactory> getFactoryList() {
		return m_factoryList;
	}

	public synchronized void register(LookupControlFactory f) {
		m_factoryList = new ArrayList<LookupControlFactory>(m_factoryList);
		m_factoryList.add(f);
	}

	public LookupControlFactory findFactory(PropertyMetaModel pmm) {
		LookupControlFactory best = null;
		int score = 0;
		for(LookupControlFactory cf : getFactoryList()) {
			int v = cf.accepts(pmm);
			if(v > score) {
				score = v;
				best = cf;
			}
		}
		return best;
	}

	public LookupControlFactory getControlFactory(PropertyMetaModel pmm) {
		LookupControlFactory cf = findFactory(pmm);
		if(cf == null)
			throw new IllegalStateException("Cannot get a Lookup Control factory for " + pmm);
		return cf;
	}
}

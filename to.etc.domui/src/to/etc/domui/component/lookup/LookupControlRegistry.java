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
	private List<ILookupControlFactory> m_factoryList = new ArrayList<ILookupControlFactory>();

	public LookupControlRegistry() {
		register(new LookupFactoryString());
		register(new LookupFactoryDate());
		register(new LookupFactoryNumber());
		register(new LookupFactoryNumber2());
		register(new LookupFactoryRelation());
		register(new LookupFactoryEnumAndBool());
		register(new LookupFactoryRelationCombo());
	}

	public synchronized List<ILookupControlFactory> getFactoryList() {
		return m_factoryList;
	}

	public synchronized void register(ILookupControlFactory f) {
		m_factoryList = new ArrayList<ILookupControlFactory>(m_factoryList);
		m_factoryList.add(f);
	}

	public ILookupControlFactory findFactory(SearchPropertyMetaModel pmm) {
		ILookupControlFactory best = null;
		int score = 0;
		for(ILookupControlFactory cf : getFactoryList()) {
			int v = cf.accepts(pmm);
			if(v > score) {
				score = v;
				best = cf;
			}
		}
		return best;
	}

	public ILookupControlFactory getControlFactory(SearchPropertyMetaModel pmm) {
		ILookupControlFactory cf = findFactory(pmm);
		if(cf == null)
			throw new IllegalStateException("Cannot get a Lookup Control factory for " + pmm);
		return cf;
	}
}

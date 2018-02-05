package to.etc.domui.component.searchpanel.lookupcontrols;

import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.PropertyRelationType;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.util.Constants;
import to.etc.domui.util.DomUtil;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 13-12-17.
 */
@DefaultNonNull
public class LookupControlRegistry2 {
	static public final LookupControlRegistry2 INSTANCE = new LookupControlRegistry2();

	private static class RegEntry {
		private final IAcceptScore<PropertyMetaModel<?>> m_acceptor;

		private final ILookupFactory<?> m_factory;

		public RegEntry(IAcceptScore<PropertyMetaModel<?>> acceptor, ILookupFactory<?> factory) {
			m_acceptor = acceptor;
			m_factory = factory;
		}

		public IAcceptScore<PropertyMetaModel<?>> getAcceptor() {
			return m_acceptor;
		}

		public ILookupFactory<?> getFactory() {
			return m_factory;
		}
	}

	private List<RegEntry> m_list = Collections.emptyList();

	public LookupControlRegistry2() {
		//-- Register all default controls
		register(new DateLookupFactory2(), a -> Date.class.isAssignableFrom(a.getActualType()) ? 10 : 0);
		register(new EnumAndBoolLookupFactory2<>(), LookupControlRegistry2::scoreEnumerable);
		register(new NumberLookupFactory2(), pmm -> DomUtil.isIntegerType(pmm.getActualType()) || DomUtil.isRealType(pmm.getActualType()) || pmm.getActualType() == BigDecimal.class ? 10 : 0);
		register(new RelationLookupFactory2<>(), pmm -> pmm.getRelationType() ==  PropertyRelationType.UP ? 10 : 0);
		register(new RelationComboLookupFactory2<>(), pmm -> pmm.getRelationType() == PropertyRelationType.UP && Constants.COMPONENT_COMBO.equals(pmm.getComponentTypeHint()) ? 10 : 0);
		register(new StringLookupFactory2<>(), pmm -> 1);			// Accept all
	}

	/**
	 * FIXME Should actually ask the metadata for domain value list.
	 */
	private static int scoreEnumerable(PropertyMetaModel<?> pmm) {
		return pmm.getActualType() == Boolean.class
			|| pmm.getActualType() == Boolean.TYPE
			|| Enum.class.isAssignableFrom(pmm.getActualType()) ? 10 : 0
			;
	}

	public synchronized void register(ILookupFactory<?> factory, IAcceptScore<PropertyMetaModel<?>> acceptor) {
		ArrayList<RegEntry> list = new ArrayList<>(m_list);
		list.add(new RegEntry(acceptor, factory));
		m_list = list;
	}

	private synchronized List<RegEntry> getList() {
		return m_list;
	}

	@Nullable
	public ILookupFactory<?> findFactory(SearchPropertyMetaModel spm) {
		PropertyMetaModel< ? > pmm = spm.getProperty();
		ILookupFactory<?> best = null;
		int bestScore = -1;
		for(RegEntry re : getList()) {
			int score = re.getAcceptor().score(pmm);
			if(score > 0) {
				if(best == null || score > bestScore) {
					bestScore = score;
					best = re.getFactory();
				}
			}
		}
		return best;
	}

	@Nullable
	public FactoryPair<?> findControlPair(SearchPropertyMetaModel spm) {
		ILookupFactory<?> factory = findFactory(spm);
		if(null == factory)
			return null;
		FactoryPair<?> controlPair = factory.createControl(spm);
		return controlPair;
	}
}

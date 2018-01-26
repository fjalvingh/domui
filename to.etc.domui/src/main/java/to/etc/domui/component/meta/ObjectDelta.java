package to.etc.domui.component.meta;

import to.etc.domui.component.meta.ObjectDelta.Delta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Create delta's by comparing two objects's values. Currently only supports non-deep
 * comparisons.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 31, 2013
 */
final public class ObjectDelta implements Iterable<Delta<?>> {
	@Nonnull
	final private Map<String, Delta< ? >> m_deltaMap;

	/**
	 * The dickhead Java "designers" apparently misunderstood DRY to mean: DO repeat yourself 8-(
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Dec 31, 2013
	 */
	@Immutable
	static public class Delta<V> {
		@Nonnull
		final private PropertyMetaModel<V> m_pmm;

		@Nullable
		final private V m_oldValue;

		@Nullable
		final private V m_newValue;

		@Nonnull
		final private String m_name;

		public Delta(@Nonnull PropertyMetaModel<V> pmm, @Nullable V oldValue, @Nullable V newValue, @Nonnull String name) {
			m_pmm = pmm;
			m_oldValue = oldValue;
			m_newValue = newValue;
			m_name = name;
		}

		@Nonnull
		public PropertyMetaModel<V> getPmm() {
			return m_pmm;
		}

		@Nullable
		public V getOldValue() {
			return m_oldValue;
		}

		@Nullable
		public V getNewValue() {
			return m_newValue;
		}

		@Nonnull
		public String getName() {
			return m_name;
		}
	}

	private ObjectDelta(@Nonnull Map<String, Delta< ? >> deltaMap) {
		m_deltaMap = deltaMap;
	}

	@Nonnull
	static public <T> ObjectDelta compare(@Nonnull T a, @Nonnull T b, Object... ignored) throws Exception {
		ClassMetaModel acmm = MetaManager.findClassMeta(a.getClass());
		ClassMetaModel bcmm = MetaManager.findClassMeta(b.getClass());
		if(acmm != bcmm)
			throw new IllegalStateException("Class " + a + " and class " + b + " are not the same class.");
		Set<String> ignoreSet = getPropertyList(ignored);

		Map<String, Delta< ? >> deltaMap = new HashMap<String, Delta< ? >>();
		for(PropertyMetaModel< ? > pmm : acmm.getProperties()) {
			if(ignoreSet.contains(pmm.getName()))
				continue;

			if(Collection.class.isAssignableFrom(pmm.getActualType())) {
				//-- todo
			} else if(Map.class.isAssignableFrom(pmm.getActualType())) {
				//-- todo
			} else {
				compare(deltaMap, a, b, pmm);
			}
		}

		return new ObjectDelta(deltaMap);
	}

	@Nonnull @Override public Iterator<Delta<?>> iterator() {
		return m_deltaMap.values().iterator();
	}

	static private <T, V> void compare(@Nonnull Map<String, Delta< ? >> deltaMap, @Nonnull T a, @Nonnull T b, @Nonnull PropertyMetaModel<V> pmm) throws Exception {
		V aval = pmm.getValue(a);
		V bval = pmm.getValue(b);
		if(MetaManager.areObjectsEqual(aval, bval))
			return;
		deltaMap.put(pmm.getName(), new Delta<V>(pmm, aval, bval, pmm.getName()));
	}

	/**
	 * Return the #of changes in the set.
	 * @return
	 */
	public int size() {
		return m_deltaMap.size();
	}

	@Nonnull
	public ObjectDelta remove(@Nonnull Object... things) {
		Map<String, Delta< ? >> deltaMap = new HashMap<String, Delta< ? >>();
		for(String name : getPropertyList(things)) {
			Delta< ? > delta = m_deltaMap.remove(name);
			if(delta != null)
				deltaMap.put(name, delta);
		}
		return new ObjectDelta(deltaMap);
	}

	/**
	 * Create a list of properties from the things passed.
	 * @param things
	 * @return
	 */
	@Nonnull
	static private Set<String> getPropertyList(@Nonnull Object... things) {
		Set<String> res = new HashSet<String>();
		for(Object v : things) {
			if(v instanceof String) {
				String s = (String) v;
				if(s.startsWith("-")) {
					res.remove(s.substring(1));
				} else {
					res.add(s);
				}
			} else if(v instanceof Class) {
				ClassMetaModel cmm = MetaManager.findClassMeta((Class< ? >) v);
				for(PropertyMetaModel< ? > pmm : cmm.getProperties())
					res.add(pmm.getName());
			} else
				throw new IllegalArgumentException("Expecting either a String (property name) or a Class (as a set of properties)");
		}
		return res;
	}

}

package to.etc.domui.component.binding;

import to.etc.domui.component.meta.*;

import javax.annotation.*;
import java.util.*;

/**
 * EXPERIMENTAL
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/22/14.
 */
@DefaultNonNull
public class BindablePropertiesChecker<T> {
	/** The last-saved property values. */
	final private Map<String, Object> m_lastMap = new HashMap<>();

	final private List<String> m_propertyList;

	public BindablePropertiesChecker(List<String> propertyList) {
		m_propertyList = propertyList;
	}

	/**
	 * Update this value set with a new value.
	 * @param value
	 * @return		T if any of the property values have changed.
	 */
	public boolean set(@Nullable T value) {
		if(value == null) {
			boolean changed = m_lastMap.size() > 0;
			m_lastMap.clear();
			return changed;
		}

		ClassMetaModel cmm = MetaManager.findClassMeta(value.getClass());
		boolean changed = false;
		for(String propName: m_propertyList) {
			PropertyMetaModel<?> pmm = cmm.getProperty(propName);
			if(set(value, pmm, propName))
				changed = true;
		}
		return changed;
	}

	/**
	 * Compare the stored values of the properties with the current ones, without storing.
	 * @param instance
	 * @return	T if the values have changed.
	 */
	public boolean compare(@Nullable T instance) {
		if(instance == null) {
			return m_lastMap.size() > 0;
		}

		ClassMetaModel cmm = MetaManager.findClassMeta(instance.getClass());
		boolean changed = false;
		for(String propName: m_propertyList) {
			PropertyMetaModel<?> pmm = cmm.getProperty(propName);
			if(compare(instance, pmm, propName))
				changed = true;
		}
		return changed;
	}

	private <V> boolean compare(T instance, PropertyMetaModel<V> pmm, String propName) {
		V value = null;
		try {
			value = pmm.getValue(instance);
		} catch(Exception x) {
		}

		V oldval = (V) m_lastMap.get(propName);
		return ! MetaManager.areObjectsEqual(oldval, value);
	}

	private <V> boolean set(T instance, PropertyMetaModel<V> pmm, String propName) {
		V value = null;
		try {
			value = pmm.getValue(instance);
		} catch(Exception x) {
		}

		V oldval = (V) m_lastMap.put(propName, value);
		return ! MetaManager.areObjectsEqual(oldval, value);
	}
}

package to.etc.domui.component.misc;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.input.ComboFixed.*;
import to.etc.domui.component.meta.*;
import to.etc.webapp.nls.*;

/**
 * Helps creating controls.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 18, 2009
 */
final public class UIControlUtil {
	private UIControlUtil() {
	}

	/**
	 * Create a combo for all members of an enum. It uses the enums labels as description. Since this has no known property it cannot
	 * use per-property translations!!
	 *
	 * @param <T>
	 * @param clz
	 * @return
	 */
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(Class<T> clz) {
		ClassMetaModel cmm = MetaManager.findClassMeta(clz);
		List<Pair<T>> l = new ArrayList<Pair<T>>();
		T[] ar = clz.getEnumConstants();
		for(T v : ar) {
			String label = cmm.getDomainLabel(NlsContext.getLocale(), v);
			if(label == null)
				label = v.name();
			l.add(new Pair<T>(v, label));
		}
		return new ComboFixed<T>(l);
	}

	/**
	 * Returns a combo for all of the list-of-value items for the specified property.
	 *
	 * @param <T>
	 * @param base		The class
	 * @param property	The property on the class.
	 * @return
	 */
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(Class< ? > base, String property) {
		return createEnumCombo(MetaManager.getPropertyMeta(base, property));
	}

	/**
	 * Returns a combo for all of the list-of-value items for the specified property.
	 * @param <T>
	 * @param pmm
	 * @return
	 */
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(PropertyMetaModel pmm) {
		T[] var = (T[]) pmm.getDomainValues();
		if(var == null)
			throw new IllegalArgumentException(pmm + " is not a list-of-values domain property");
		List<Pair<T>> l = new ArrayList<Pair<T>>();
		for(T v : var) {
			String label = pmm.getDomainValueLabel(NlsContext.getLocale(), v);
			if(label == null)
				label = v.toString();
			l.add(new Pair<T>(v, label));
		}
		return new ComboFixed<T>(l);
	}



	/**
	 * Create a combobox having only the specified enum labels.
	 * @param <T>
	 * @param items
	 * @return
	 */
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(T... items) {
		if(items.length == 0)
			throw new IllegalArgumentException("Missing parameters");

		ClassMetaModel cmm = MetaManager.findClassMeta(items[0].getClass());
		List<Pair<T>> l = new ArrayList<Pair<T>>();
		for(T v : items) {
			String label = cmm.getDomainLabel(NlsContext.getLocale(), v);
			if(label == null)
				label = v.name();
			l.add(new Pair<T>(v, label));
		}
		return new ComboFixed<T>(l);
	}

	/**
	 * Create a combobox having only the specified enum labels.
	 * @param <T>
	 * @param base
	 * @param property
	 * @param domainvalues
	 * @return
	 */
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(Class< ? > base, String property, T... domainvalues) {
		return createEnumCombo(MetaManager.getPropertyMeta(base, property), domainvalues);
	}

	/**
	 * Create a combobox having only the specified enum labels.
	 * @param <T>
	 * @param pmm
	 * @param domainvalues
	 * @return
	 */
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(PropertyMetaModel pmm, T... domainvalues) {
		if(domainvalues.length == 0)
			throw new IllegalArgumentException("Missing parameters");
		List<Pair<T>> l = new ArrayList<Pair<T>>();
		for(T v : domainvalues) {
			String label = pmm.getDomainValueLabel(NlsContext.getLocale(), v);
			if(label == null)
				label = v.toString();
			l.add(new Pair<T>(v, label));
		}
		return new ComboFixed<T>(l);
	}

}

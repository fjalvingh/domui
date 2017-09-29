/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component2.combo;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

import javax.annotation.*;
import java.util.*;

/**
 * Simple combobox handling [String, Object] pairs where the string is the
 * presented label value and the Object represents the values selected.
 * Please see {@link UIControlUtil} for factory methods that help you to
 * create ComboFixed instances easily.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 26, 2009
 */
public class ComboFixed2<T> extends ComboComponentBase2<ValueLabelPair<T>, T> {
	static private final IRenderInto<ValueLabelPair<Object>> STATICRENDERER = new IRenderInto<ValueLabelPair<Object>>() {
		@Override
		public void render(@Nonnull NodeContainer node, @Nullable ValueLabelPair<Object> object) throws Exception {
			if(object != null)
				node.setText(object.getLabel());
		}
	};

	/**
	 * Generic constructor.
	 */
	public ComboFixed2() {
		initRenderer();
	}

	public ComboFixed2(Class< ? extends IComboDataSet<ValueLabelPair<T>>> set, IRenderInto<ValueLabelPair<T>> r) {
		super(set, r);
	}

	public ComboFixed2(Class< ? extends IComboDataSet<ValueLabelPair<T>>> dataSetClass) {
		super(dataSetClass);
		initRenderer();
	}

	public ComboFixed2(IComboDataSet<ValueLabelPair<T>> dataSet) {
		super(dataSet);
		initRenderer();
	}

	public ComboFixed2(IListMaker<ValueLabelPair<T>> maker) {
		super(maker);
		initRenderer();
	}

	/**
	 * Use the specified list of pairs directly.
	 * @param in
	 */
	public ComboFixed2(List<ValueLabelPair<T>> in) {
		super(in);
		initRenderer();
	}

	@Override
	protected T listToValue(ValueLabelPair<T> in) throws Exception {
		return in.getValue();
	}

	@SuppressWarnings({"unchecked"})
	private void initRenderer() {
		IRenderInto< ? > r = STATICRENDERER;
		setContentRenderer((IRenderInto<ValueLabelPair<T>>) r);
	}
	// 20100502 jal Horrible bug! This prevents setting customized option rendering from working!!
	//	@Override
	//	protected void renderOptionLabel(SelectOption o, ValueLabelPair<T> object) throws Exception {
	//		o.add(object.getLabel());
	//	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Utilities to quickly create combo's.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Create a combo for all members of an enum, except for specified exceptions. It uses the enums labels as description. Since this has no known property it cannot
	 * use per-property translations!!
	 * @param <T>
	 * @param clz
	 * @param exceptions
	 * @return
	 */
	static public <T extends Enum<T>> ComboFixed2<T> createEnumCombo(Class<T> clz, T... exceptions) {
		ClassMetaModel cmm = MetaManager.findClassMeta(clz);
		List<ValueLabelPair<T>> l = new ArrayList<ValueLabelPair<T>>();
		T[] ar = clz.getEnumConstants();
		for(T v : ar) {
			if(!DomUtil.contains(exceptions, v)) {
				String label = cmm.getDomainLabel(NlsContext.getLocale(), v);
				if(label == null)
					label = v.name();
				l.add(new ValueLabelPair<T>(v, label));
			}
		}
		Collections.sort(l, (a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));
		return new ComboFixed2<T>(l);
	}

	/**
	 * Returns a combo for all of the list-of-value items for the specified property.
	 *
	 * @param <T>
	 * @param base		The class
	 * @param property	The property on the class.
	 * @return
	 */
	static public <T extends Enum<T>> ComboFixed2<T> createEnumCombo(Class< ? > base, String property) {
		return createEnumCombo(MetaManager.getPropertyMeta(base, property));
	}

	/**
	 * Returns a combo for all of the list-of-value items for the specified property.
	 * @param <T>
	 * @param pmm
	 * @return
	 */
	static public <T extends Enum<T>> ComboFixed2<T> createEnumCombo(PropertyMetaModel< ? > pmm) {
		T[] var = (T[]) pmm.getDomainValues();
		if(var == null)
			throw new IllegalArgumentException(pmm + " is not a list-of-values domain property");
		List<ValueLabelPair<T>> l = new ArrayList<ValueLabelPair<T>>();
		for(T v : var) {
			String label = MetaManager.getEnumLabel(pmm, v);
			l.add(new ValueLabelPair<T>(v, label));
		}
		return new ComboFixed2<T>(l);
	}

	/**
	 * Create a combobox having only the specified enum labels.
	 * @param <T>
	 * @param items
	 * @return
	 */
	static public <T extends Enum<T>> ComboFixed2<T> createEnumCombo(T... items) {
		List<ValueLabelPair<T>> l = createEnumValueList(items);
		return new ComboFixed2<T>(l);
	}

	@Nonnull
	public static <T extends Enum<T>> List<ValueLabelPair<T>> createEnumValueList(T... items) {
		if(items.length == 0)
			throw new IllegalArgumentException("Missing parameters");

		ClassMetaModel cmm = MetaManager.findClassMeta(items[0].getClass());
		List<ValueLabelPair<T>> l = new ArrayList<ValueLabelPair<T>>();
		for(T v : items) {
			String label = cmm.getDomainLabel(NlsContext.getLocale(), v);
			if(label == null)
				label = v.name();
			l.add(new ValueLabelPair<T>(v, label));
		}
		return l;
	}

	/**
	 * Create a combobox having only the specified enum labels.
	 * @param <T>
	 * @param base
	 * @param property
	 * @param domainvalues
	 * @return
	 */
	static public <T extends Enum<T>> ComboFixed2<T> createEnumCombo(Class< ? > base, String property, T... domainvalues) {
		return createEnumCombo(MetaManager.getPropertyMeta(base, property), domainvalues);
	}

	/**
	 * Create a combobox having only the specified enum labels.
	 * @param <T>
	 * @param pmm
	 * @param domainvalues
	 * @return
	 */
	static public <T extends Enum<T>> ComboFixed2<T> createEnumCombo(PropertyMetaModel< ? > pmm, T... domainvalues) {
		if(domainvalues.length == 0)
			throw new IllegalArgumentException("Missing parameters");
		List<ValueLabelPair<T>> l = new ArrayList<ValueLabelPair<T>>();
		for(T v : domainvalues) {
			String label = MetaManager.getEnumLabel(pmm, v);
			l.add(new ValueLabelPair<T>(v, label));
		}
		return new ComboFixed2<T>(l);
	}

	/**
	 * Default tostring converter.
	 */
	@Nonnull
	static private final IObjectToStringConverter<Object> TOSTRING_CV = new IObjectToStringConverter<Object>() {
		@Override
		public String convertObjectToString(Locale loc, Object in) throws UIException {
			if(null == in)
				return "";
			return String.valueOf(in);
		}
	};

	/**
	 * Create a combo for a manually specified list of objects. It calls toString on them to
	 * get a String value.
	 * @param <T>
	 * @param items
	 * @return
	 */
	static public <T> ComboFixed2<T>	createCombo(T... items) {
		return createCombo((IObjectToStringConverter<T>) TOSTRING_CV, items);
	}

	/**
	 * Create a combo for a manually specified list of objects. Use the specified converter
	 * to convert to a string.
	 *
	 * @param <T>
	 * @param converter
	 * @param items
	 * @return
	 */
	static public <T> ComboFixed2<T> createCombo(@Nonnull IObjectToStringConverter<T> converter, T... items) {
		List<ValueLabelPair<T>> values = new ArrayList<ValueLabelPair<T>>();
		for(T item : items) {
			String v = converter.convertObjectToString(NlsContext.getLocale(), item);
			if(null == v)
				v = "";
			values.add(new ValueLabelPair<T>(item, v));
		}
		return new ComboFixed2<T>(values);
	}

	@Nonnull
	static public <T> ComboFixed2<T> createComboFor(PropertyMetaModel<T> pmm, boolean editable) {
		if(pmm == null)
			throw new IllegalArgumentException("propertyMeta cannot be null");
		Object[] vals = pmm.getDomainValues();
		if(vals == null || vals.length == 0)
			throw new IllegalArgumentException("The type of property " + pmm + " (" + pmm.getActualType() + ") is not known as a fixed-size domain type");

		ClassMetaModel ecmm = null;
		List<ValueLabelPair<T>> vl = new ArrayList<ValueLabelPair<T>>();
		for(Object o : vals) {
			String label = pmm.getDomainValueLabel(NlsContext.getLocale(), o); // Label known to property?
			if(label == null) {
				if(ecmm == null)
					ecmm = MetaManager.findClassMeta(pmm.getActualType()); // Try to get the property's type.
				label = ecmm.getDomainLabel(NlsContext.getLocale(), o);
				if(label == null)
					label = o == null ? "" : o.toString();
			}
			vl.add(new ValueLabelPair<T>((T) o, label));
		}

		ComboFixed2<T> c = new ComboFixed2<T>(vl);
		if(pmm.isRequired())
			c.setMandatory(true);
		if(!editable || pmm.getReadOnly() == YesNoType.YES)
			c.setDisabled(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			c.setTitle(s);
		return c;
	}


}

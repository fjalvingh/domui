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
package to.etc.domui.component.input;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.misc.UIControlUtil;
import to.etc.domui.converter.IObjectToStringConverter;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.trouble.UIException;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IComboDataSet;
import to.etc.domui.util.IListMaker;
import to.etc.domui.util.IRenderInto;
import to.etc.webapp.nls.NlsContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Simple combobox handling [String, Object] pairs where the string is the
 * presented label value and the Object represents the values selected.
 * Please see {@link UIControlUtil} for factory methods that help you to
 * create ComboFixed instances easily.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 26, 2009
 */
public class ComboFixed<T> extends ComboComponentBase<ValueLabelPair<T>, T> {
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
	public ComboFixed() {
		initRenderer();
	}

	public ComboFixed(Class< ? extends IComboDataSet<ValueLabelPair<T>>> set, IRenderInto<ValueLabelPair<T>> r) {
		super(set, r);
	}

	public ComboFixed(Class< ? extends IComboDataSet<ValueLabelPair<T>>> dataSetClass) {
		super(dataSetClass);
		initRenderer();
	}

	public ComboFixed(IComboDataSet<ValueLabelPair<T>> dataSet) {
		super(dataSet);
		initRenderer();
	}

	public ComboFixed(IListMaker<ValueLabelPair<T>> maker) {
		super(maker);
		initRenderer();
	}

	/**
	 * Use the specified list of pairs directly.
	 * @param in
	 */
	public ComboFixed(List<ValueLabelPair<T>> in) {
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
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(Class<T> clz, T... exceptions) {
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
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(PropertyMetaModel< ? > pmm) {
		T[] var = (T[]) pmm.getDomainValues();
		if(var == null)
			throw new IllegalArgumentException(pmm + " is not a list-of-values domain property");
		List<ValueLabelPair<T>> l = new ArrayList<ValueLabelPair<T>>();
		for(T v : var) {
			String label = MetaManager.getEnumLabel(pmm, v);
			l.add(new ValueLabelPair<T>(v, label));
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
		List<ValueLabelPair<T>> l = createEnumValueList(items);
		return new ComboFixed<T>(l);
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
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(PropertyMetaModel< ? > pmm, T... domainvalues) {
		if(domainvalues.length == 0)
			throw new IllegalArgumentException("Missing parameters");
		List<ValueLabelPair<T>> l = new ArrayList<ValueLabelPair<T>>();
		for(T v : domainvalues) {
			String label = MetaManager.getEnumLabel(pmm, v);
			l.add(new ValueLabelPair<T>(v, label));
		}
		return new ComboFixed<T>(l);
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
	static public <T> ComboFixed<T>	createCombo(T... items) {
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
	static public <T> ComboFixed<T> createCombo(@Nonnull IObjectToStringConverter<T> converter, T... items) {
		List<ValueLabelPair<T>> values = new ArrayList<ValueLabelPair<T>>();
		for(T item : items) {
			String v = converter.convertObjectToString(NlsContext.getLocale(), item);
			if(null == v)
				v = "";
			values.add(new ValueLabelPair<T>(item, v));
		}
		return new ComboFixed<T>(values);
	}
}

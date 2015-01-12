package to.etc.domui.component.combobox;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

public class SelectFixed<T> extends ComboBoxBase<ValueLabelPair<T>, T> {
	static private final INodeContentRenderer<ValueLabelPair<Object>> STATICRENDERER = new INodeContentRenderer<ValueLabelPair<Object>>() {
		@Override
		public void renderNodeContent(@Nonnull NodeBase component, @Nonnull NodeContainer node, @Nullable ValueLabelPair<Object> object, @Nullable Object parameters) throws Exception {
			if(object != null)
				node.setText(object.getLabel());
		}
	};

	/**
	 * Generic constructor.
	 */
	public SelectFixed() {
		initRenderer();
	}

	public SelectFixed(IComboDataSet<ValueLabelPair<T>> dataSet) {
		super(dataSet);
		initRenderer();
	}

	public SelectFixed(IListMaker<ValueLabelPair<T>> maker) {
		super(maker);
		initRenderer();
	}

	/**
	 * Use the specified list of pairs directly.
	 * @param in
	 */
	public SelectFixed(List<ValueLabelPair<T>> in) {
		super(in);
		initRenderer();
	}

	@Override
	protected T listToValue(ValueLabelPair<T> in) throws Exception {
		return in.getValue();
	}

	@SuppressWarnings({"unchecked"})
	private void initRenderer() {
		INodeContentRenderer< ? > r = STATICRENDERER;
		setContentRenderer((INodeContentRenderer<ValueLabelPair<T>>) r);
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
	static public <T extends Enum<T>> SelectFixed<T> createEnumCombo(Class<T> clz, T... exceptions) {
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
		return new SelectFixed<T>(l);
	}

	/**
	 * Returns a combo for all of the list-of-value items for the specified property.
	 *
	 * @param <T>
	 * @param base		The class
	 * @param property	The property on the class.
	 * @return
	 */
	static public <T extends Enum<T>> SelectFixed<T> createEnumCombo(Class< ? > base, String property) {
		return createEnumCombo(MetaManager.getPropertyMeta(base, property));
	}

	/**
	 * Returns a combo for all of the list-of-value items for the specified property.
	 * @param <T>
	 * @param pmm
	 * @return
	 */
	static public <T extends Enum<T>> SelectFixed<T> createEnumCombo(PropertyMetaModel< ? > pmm) {
		T[] var = (T[]) pmm.getDomainValues();
		if(var == null)
			throw new IllegalArgumentException(pmm + " is not a list-of-values domain property");
		List<ValueLabelPair<T>> l = new ArrayList<ValueLabelPair<T>>();
		for(T v : var) {
			String label = MetaManager.getEnumLabel(pmm, v);
			l.add(new ValueLabelPair<T>(v, label));
		}
		return new SelectFixed<T>(l);
	}

	/**
	 * Create a combobox having only the specified enum labels.
	 * @param <T>
	 * @param items
	 * @return
	 */
	static public <T extends Enum<T>> SelectFixed<T> createEnumCombo(T... items) {
		List<ValueLabelPair<T>> l = createEnumValueList(items);
		return new SelectFixed<T>(l);
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
	static public <T extends Enum<T>> SelectFixed<T> createEnumCombo(Class< ? > base, String property, T... domainvalues) {
		return createEnumCombo(MetaManager.getPropertyMeta(base, property), domainvalues);
	}

	/**
	 * Create a combobox having only the specified enum labels.
	 * @param <T>
	 * @param pmm
	 * @param domainvalues
	 * @return
	 */
	static public <T extends Enum<T>> SelectFixed<T> createEnumCombo(PropertyMetaModel< ? > pmm, T... domainvalues) {
		if(domainvalues.length == 0)
			throw new IllegalArgumentException("Missing parameters");
		List<ValueLabelPair<T>> l = new ArrayList<ValueLabelPair<T>>();
		for(T v : domainvalues) {
			String label = MetaManager.getEnumLabel(pmm, v);
			l.add(new ValueLabelPair<T>(v, label));
		}
		return new SelectFixed<T>(l);
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
	static public <T> SelectFixed<T> createCombo(T... items) {
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
	static public <T> SelectFixed<T> createCombo(@Nonnull IObjectToStringConverter<T> converter, T... items) {
		List<ValueLabelPair<T>> values = new ArrayList<ValueLabelPair<T>>();
		for(T item : items) {
			String v = converter.convertObjectToString(NlsContext.getLocale(), item);
			if(null == v)
				v = "";
			values.add(new ValueLabelPair<T>(item, v));
		}
		return new SelectFixed<T>(values);
	}
}

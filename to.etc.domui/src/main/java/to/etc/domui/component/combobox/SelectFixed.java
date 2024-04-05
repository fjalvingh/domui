package to.etc.domui.component.combobox;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.input.ValueLabelPair;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.converter.IObjectToStringConverter;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.trouble.UIException;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IComboDataSet;
import to.etc.domui.util.IListMaker;
import to.etc.domui.util.IRenderInto;
import to.etc.webapp.nls.NlsContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SelectFixed<T> extends ComboBoxBase<ValueLabelPair<T>, T> {
	static private final IRenderInto<ValueLabelPair<Object>> STATICRENDERER = new IRenderInto<ValueLabelPair<Object>>() {
		@Override
		public void render(@NonNull NodeContainer node, @Nullable ValueLabelPair<Object> object) throws Exception {
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
	 */
	public SelectFixed(List<ValueLabelPair<T>> in) {
		super(in);
		initRenderer();
	}

	@Override
	protected T listToValue(ValueLabelPair<T> in) throws Exception {
		return in.getValue();
	}

	private void initRenderer() {
		IRenderInto<?> r = STATICRENDERER;
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
	 * @param base     The class
	 * @param property The property on the class.
	 */
	static public <T extends Enum<T>> SelectFixed<T> createEnumCombo(Class<?> base, String property) {
		return createEnumCombo(MetaManager.getPropertyMeta(base, property));
	}

	/**
	 * Returns a combo for all of the list-of-value items for the specified property.
	 */
	static public <T extends Enum<T>> SelectFixed<T> createEnumCombo(PropertyMetaModel<?> pmm) {
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
	 */
	static public <T extends Enum<T>> SelectFixed<T> createEnumCombo(T... items) {
		List<ValueLabelPair<T>> l = createEnumValueList(items);
		return new SelectFixed<T>(l);
	}

	@NonNull
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
	 */
	static public <T extends Enum<T>> SelectFixed<T> createEnumCombo(Class<?> base, String property, T... domainvalues) {
		return createEnumCombo(MetaManager.getPropertyMeta(base, property), domainvalues);
	}

	/**
	 * Create a combobox having only the specified enum labels.
	 */
	static public <T extends Enum<T>> SelectFixed<T> createEnumCombo(PropertyMetaModel<?> pmm, T... domainvalues) {
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
	@NonNull
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
	 */
	static public <T> SelectFixed<T> createCombo(T... items) throws Exception {
		return createCombo((IObjectToStringConverter<T>) TOSTRING_CV, items);
	}

	/**
	 * Create a combo for a manually specified list of objects. Use the specified converter
	 * to convert to a string.
	 */
	static public <T> SelectFixed<T> createCombo(@NonNull IObjectToStringConverter<T> converter, T... items) throws Exception {
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

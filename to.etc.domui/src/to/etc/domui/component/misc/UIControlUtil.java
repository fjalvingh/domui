package to.etc.domui.component.misc;

import java.math.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.css.*;
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

	/*--------------------------------------------------------------*/
	/*	CODING:	Creating ComboFixed controls for enum's				*/
	/*--------------------------------------------------------------*/
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
		List<ValueLabelPair<T>> l = new ArrayList<ValueLabelPair<T>>();
		T[] ar = clz.getEnumConstants();
		for(T v : ar) {
			String label = cmm.getDomainLabel(NlsContext.getLocale(), v);
			if(label == null)
				label = v.name();
			l.add(new ValueLabelPair<T>(v, label));
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
		List<ValueLabelPair<T>> l = new ArrayList<ValueLabelPair<T>>();
		for(T v : var) {
			String label = getEnumLabel(pmm, var);
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
		List<ValueLabelPair<T>> l = new ArrayList<ValueLabelPair<T>>();
		for(T v : domainvalues) {
			String label = getEnumLabel(pmm, v);
			l.add(new ValueLabelPair<T>(v, label));
		}
		return new ComboFixed<T>(l);
	}

	static public String getEnumLabel(Enum< ? > label) {
		if(label == null)
			return null;
		ClassMetaModel cmm = MetaManager.findClassMeta(label.getClass());
		String s = cmm.getDomainLabel(NlsContext.getLocale(), label);
		if(s == null)
			s = String.valueOf(label);
		return s;
	}

	static public String getEnumLabel(Class< ? > clz, String property, Object value) {
		if(value == null)
			return null;
		return getEnumLabel(MetaManager.findPropertyMeta(clz, property), value);
	}

	static public String getEnumLabel(PropertyMetaModel pmm, Object value) {
		if(value == null)
			return null;
		Locale loc = NlsContext.getLocale();
		String v = pmm.getDomainValueLabel(loc, value);
		if(v == null) {
			ClassMetaModel cmm = MetaManager.findClassMeta(pmm.getActualType());
			v = cmm.getDomainLabel(loc, value);
			if(v == null) {
				if(value.getClass() != cmm.getActualClass()) {
					cmm = MetaManager.findClassMeta(value.getClass());
					v = cmm.getDomainLabel(loc, value);
				}
				if(v == null)
					v = String.valueOf(value);
			}
		}
		return v;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Creating monetary input controls.					*/
	/*--------------------------------------------------------------*/
	/**
	 * Create a control to input a monetary value proper for the specified property.
	 * @param clz
	 * @param property
	 * @return
	 */
	@Nonnull
	static public Text<Double> createDoubleMoneyInput(@Nonnull Class< ? > clz, @Nonnull String property, boolean editable) {
		return createDoubleMoneyInput(MetaManager.findPropertyMeta(clz, property), editable);
	}

	static public Text<BigDecimal> createBDMoneyInput(Class< ? > clz, String property, boolean editable) {
		return createBDMoneyInput(MetaManager.findPropertyMeta(clz, property), editable);
	}

	static public Text<BigDecimal> createBDMoneyInput(PropertyMetaModel pmm, boolean editable) {
		if(pmm == null)
			throw new NullPointerException("Null property model not allowed");
		Text<BigDecimal> txt = new Text<BigDecimal>(BigDecimal.class);
		configureNumericInput(txt, pmm, editable);
		assignMonetaryConverter(pmm, editable, txt);
		return txt;
	}

	@Nonnull
	static public Text<Double> createDoubleMoneyInput(@Nonnull PropertyMetaModel pmm, boolean editable) {
		if(pmm == null)
			throw new NullPointerException("Null property model not allowed");
		Text<Double> txt = new Text<Double>(Double.class);
		configureNumericInput(txt, pmm, editable);
		assignMonetaryConverter(pmm, editable, txt);
		return txt;
	}

	static private void configureNumericInput(Text< ? > txt, PropertyMetaModel pmm, boolean editable) {
		if(!editable)
			txt.setReadOnly(true);

		/*
		 * Length calculation using the metadata. This uses the "length" field as LAST, because it is often 255 because the
		 * JPA's column annotation defaults length to 255 to make sure it's usability is bloody reduced. Idiots.
		 */
		if(pmm.getDisplayLength() > 0)
			txt.setSize(pmm.getDisplayLength());
		else if(pmm.getPrecision() > 0) {
			// FIXME This should be localized somehow...
			//-- Calculate a size using scale and precision.
			int size = pmm.getPrecision();
			int d = size;
			if(pmm.getScale() > 0) {
				size++; // Inc size to allow for decimal point or comma
				d -= pmm.getScale(); // Reduce integer part,
				if(d >= 4) { // Can we get > 999? Then we can have thousand-separators
					int nd = (d - 1) / 3; // How many thousand separators could there be?
					size += nd; // Increment input size with that
				}
			}
			txt.setSize(size);

			//-- 20100318 Since we have precision and scale, add a range check to this control.
			assignPrecisionValidator(txt, pmm);
		} else if(pmm.getLength() > 0) {
			txt.setSize(pmm.getLength() < 40 ? pmm.getLength() : 40);
		}
		if(pmm.getLength() > 0)
			txt.setMaxLength(pmm.getLength());
		if(pmm.isRequired())
			txt.setMandatory(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			txt.setTitle(s);
		for(PropertyMetaValidator mpv : pmm.getValidators())
			txt.addValidator(mpv);
		txt.setTextAlign(TextAlign.RIGHT);
	}

	@SuppressWarnings("unchecked")
	static public void assignMonetaryConverter(final PropertyMetaModel pmm, boolean editable, final IConvertable< ? > node) {
		if(pmm.getConverter() != null)
			node.setConverter((IConverter) pmm.getConverter());
		else {
			NumericPresentation np = null;
			if(!editable)
				np = pmm.getNumericPresentation();
			if(np == null)
				np = NumericPresentation.MONEY_NUMERIC;

			if(pmm.getActualType() == Double.class || pmm.getActualType() == double.class) {
				node.setConverter((IConverter) MoneyConverterFactory.createDoubleMoneyConverters(np));
			} else if(pmm.getActualType() == BigDecimal.class) {
				node.setConverter((IConverter) MoneyConverterFactory.createBigDecimalMoneyConverters(np));
			} else
				throw new IllegalStateException("Cannot handle type=" + pmm.getActualType() + " for monetary types");
		}
	}

	@SuppressWarnings("unchecked")
	static private <T extends Number> void assignNumericConverter(final PropertyMetaModel pmm, boolean editable, final IConvertable<T> node, Class<T> type) {
		if(pmm.getConverter() != null)
			node.setConverter((IConverter) pmm.getConverter());
		else {
			NumericPresentation np = null;
			//			if(!editable)
			np = pmm.getNumericPresentation();
			int scale = pmm.getScale();
			IConverter<T> c = NumericUtil.createNumberConverter(type, np, scale);
			node.setConverter(c);
		}
	}

	static private final void	assignPrecisionValidator(@Nonnull Text<?> control, @Nonnull PropertyMetaModel pmm) {
		assignPrecisionValidator(control, pmm.getPrecision(), pmm.getScale());
	}

	static private final void assignPrecisionValidator(@Nonnull Text< ? > control, int precision, int scale) {
		if(precision > 0) {
			int d = precision;
			if(scale > 0)
				d -= scale;
			if(d < 0)
				return;
			BigDecimal bd = BigDecimal.valueOf(10);
			bd = bd.pow(d); // 10^n, this is the EXCLUSIVE max/min value.
			bd = bd.subtract(BigDecimal.valueOf(1)); // Inclusive now;
			control.addValidator(new MaxMinValidator(bd.negate(), bd));
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Numeric Text inputs for base types.					*/
	/*--------------------------------------------------------------*/
	/**
	 * Create an int input control, properly configured for the specified property.
	 * @param clz
	 * @param property
	 * @param editable
	 * @return
	 */
	static public Text<Integer> createIntInput(Class< ? > clz, String property, boolean editable) {
		return createIntInput(MetaManager.findPropertyMeta(clz, property), editable);
	}

	static public Text<Integer> createIntInput(PropertyMetaModel pmm, boolean editable) {
		if(pmm == null)
			throw new NullPointerException("Null property model not allowed");
		Text<Integer> txt = new Text<Integer>(Integer.class);
		configureNumericInput(txt, pmm, editable);
		assignNumericConverter(pmm, editable, txt, Integer.class);
		return txt;
	}

	static public Text<Long> createLongInput(Class< ? > clz, String property, boolean editable) {
		return createLongInput(MetaManager.findPropertyMeta(clz, property), editable);
	}

	static public Text<Long> createLongInput(PropertyMetaModel pmm, boolean editable) {
		if(pmm == null)
			throw new NullPointerException("Null property model not allowed");
		Text<Long> txt = new Text<Long>(Long.class);
		configureNumericInput(txt, pmm, editable);
		assignNumericConverter(pmm, editable, txt, Long.class);
		return txt;
	}

	static public Text<Double> createDoubleInput(Class< ? > clz, String property, boolean editable) {
		return createDoubleInput(MetaManager.findPropertyMeta(clz, property), editable);
	}

	static public Text<Double> createDoubleInput(PropertyMetaModel pmm, boolean editable) {
		if(pmm == null)
			throw new NullPointerException("Null property model not allowed");
		Text<Double> txt = new Text<Double>(Double.class);
		configureNumericInput(txt, pmm, editable);
		assignNumericConverter(pmm, editable, txt, Double.class);
		return txt;
	}

	static public Text<BigDecimal> createBigDecimalInput(Class< ? > clz, String property, boolean editable) {
		return createBigDecimalInput(MetaManager.findPropertyMeta(clz, property), editable);
	}

	static public Text<BigDecimal> createBigDecimalInput(PropertyMetaModel pmm, boolean editable) {
		if(pmm == null)
			throw new NullPointerException("Null property model not allowed");
		Text<BigDecimal> txt = new Text<BigDecimal>(BigDecimal.class);
		configureNumericInput(txt, pmm, editable);
		assignNumericConverter(pmm, editable, txt, BigDecimal.class);
		return txt;
	}
}


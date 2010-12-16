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
package to.etc.domui.component.misc;

import java.math.*;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.css.*;

/**
 * PLEASE LOOK IN THE CONTROL CLASS YOU WANT TO CREATE FOR MORE METHODS!
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
	 * FIXME Replace with {@link ComboFixed#createEnumCombo(Class)}.
	 * Create a combo for all members of an enum. It uses the enums labels as description. Since this has no known property it cannot
	 * use per-property translations!!
	 *
	 * @param <T>
	 * @param clz
	 * @return
	 */
	@Deprecated
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(Class<T> clz) {
		return ComboFixed.createEnumCombo(clz);
	}

	/**
	 * Returns a combo for all of the list-of-value items for the specified property.
	 * FIXME Replace with {@link ComboFixed#createEnumCombo(Class, String)}.
	 *
	 * @param <T>
	 * @param base		The class
	 * @param property	The property on the class.
	 * @return
	 */
	@Deprecated
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(Class< ? > base, String property) {
		return ComboFixed.createEnumCombo(MetaManager.getPropertyMeta(base, property));
	}

	/**
	 * Returns a combo for all of the list-of-value items for the specified property.
	 * FIXME Replace with {@link ComboFixed#createEnumCombo(PropertyMetaModel)}.
	 * @param <T>
	 * @param pmm
	 * @return
	 */
	@Deprecated
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(PropertyMetaModel pmm) {
		return ComboFixed.createEnumCombo(pmm);
	}

	/**
	 * Create a combobox having only the specified enum labels.
	 * FIXME Replace with {@link ComboFixed#createEnumCombo(Enum...)}.
	 * @param <T>
	 * @param items
	 * @return
	 */
	@Deprecated
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(T... items) {
		return ComboFixed.createEnumCombo(items);
	}

	/**
	 * Create a combobox having only the specified enum labels.
	 * FIXME Replace with {@link ComboFixed#createEnumCombo(Class, String, Enum...)}
	 * @param <T>
	 * @param base
	 * @param property
	 * @param domainvalues
	 * @return
	 */
	@Deprecated
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(Class< ? > base, String property, T... domainvalues) {
		return ComboFixed.createEnumCombo(MetaManager.getPropertyMeta(base, property), domainvalues);
	}

	/**
	 * Create a combobox having only the specified enum labels.
	 * FIXME Replace with {@link ComboFixed#createEnumCombo(PropertyMetaModel, Enum...)}.
	 * @param <T>
	 * @param pmm
	 * @param domainvalues
	 * @return
	 */
	@Deprecated
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(PropertyMetaModel pmm, T... domainvalues) {
		return ComboFixed.createEnumCombo(pmm, domainvalues);
	}

	/**
	 * Replace with method in {@link MetaManager}
	 * @param label
	 * @return
	 */
	@Deprecated
	static public String getEnumLabel(Enum< ? > label) {
		return MetaManager.getEnumLabel(label);
	}

	/**
	 * Replace with method in {@link MetaManager}
	 *
	 * @param clz
	 * @param property
	 * @param value
	 * @return
	 */
	@Deprecated
	static public String getEnumLabel(Class< ? > clz, String property, Object value) {
		return MetaManager.getEnumLabel(clz, property, value);
	}

	/**
	 * Replace with method in {@link MetaManager}
	 *
	 * @param pmm
	 * @param value
	 * @return
	 */
	@Deprecated
	static public String getEnumLabel(PropertyMetaModel pmm, Object value) {
		return MetaManager.getEnumLabel(pmm, value);
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

	@SuppressWarnings({"unchecked", "rawtypes"})
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

	@SuppressWarnings({"unchecked", "rawtypes"})
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

	static public Text< ? > createText(Class< ? > clz, String property, boolean editable) {
		PropertyMetaModel pmm = MetaManager.findPropertyMeta(clz, property);
		return createText(pmm.getActualType(), pmm, editable);
	}

	static public <T> Text<T> createText(Class<T> iclz, PropertyMetaModel pmm, boolean editable) {
		Class< ? > aclz = pmm.getActualType();
		if(!iclz.isAssignableFrom(aclz))
			throw new IllegalStateException("Invalid class type=" + iclz + " for property " + pmm);
		Text<T> txt = new Text<T>(iclz);

		//-- Get simple things to do out of the way.
		if(!editable)
			txt.setReadOnly(true);
		if(pmm.getConverter() != null)
			txt.setConverter((IConverter<T>) pmm.getConverter());
		if(pmm.isRequired())
			txt.setMandatory(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			txt.setTitle(s);
		for(PropertyMetaValidator mpv : pmm.getValidators())
			txt.addValidator(mpv);

		txt.setRegexpUserString(pmm.getRegexpUserString());
		txt.setValidationRegexp(pmm.getRegexpValidator());

		/*
		 * Start calculating maxlength and display length. Display length means the presented size on the
		 * UI (size= attribute); maxlength means just that - no data longer than maxlength can be entered.
		 * The calculation is complex and depends on the input type; the common types are handled here; other
		 * types should be handled by their own control factory.
		 *
		 * Length calculation is made fragile because the JPA @Column annotation's length attribute defaults
		 * to 255 (a decision made by some complete and utter idiot), so we take some special care with it
		 * if it has this value - it is not really used in the decision process anymore.
		 *
		 */
		//-- Precalculate some sizes for well-known types like numerics.
		int calcmaxsz = -1; // Calculated max input size
		int calcsz = -1; // Calculated display size,

		if(pmm.getPrecision() > 0) {
			// FIXME This should be localized somehow...
			//-- Calculate a size using scale and precision.
			int size = pmm.getPrecision();
			int d = size;
			String hint = pmm.getComponentTypeHint();
			if(hint != null) {
				hint = hint.toLowerCase();
			}
			if(hint == null || !hint.contains(MetaUtils.NO_MINUS)) {
				size++; // Allow minus
			}
			if(hint == null || !hint.contains(MetaUtils.NO_SEPARATOR)) {
				if(pmm.getScale() > 0) {
					size++; // Inc size to allow for decimal point or comma
					d -= pmm.getScale(); // Reduce integer part,
					if(d >= 4) { // Can we get > 999? Then we can have thousand-separators
						int nd = (d - 1) / 3; // How many thousand separators could there be?
						size += nd; // Increment input size with that
					}
				} else {
					if(d >= 4) { // Can we get > 999? Then we can have thousand-separators
						int nd = (d - 1) / 3; // How many thousand separators could there be?
						size += nd; // Increment input size with that
					}
				}
			}

			//-- If this is some form of money allow extra room for the currency indicator + space.
			if(NumericPresentation.isMonetary(pmm.getNumericPresentation())) {
				size += 2; // For now allow 2 extra characters
			}
			calcsz = size;
			calcmaxsz = size;
		} else if(NumericPresentation.isMonetary(pmm.getNumericPresentation())) {
			//-- Monetary amount with unclear precision- do a reasonable default. Allow for E 1.000.000.000,00 input size and way bigger max size
			calcsz = 18;
			calcmaxsz = 30;
		}

		//-- When a display length *is* present it *always* overrides any calculated value,
		if(pmm.getDisplayLength() > 0)
			calcsz = pmm.getDisplayLength();

		if(pmm.getLength() > 0 && pmm.getLength() != 255) { // Handle non-jpa-blundered lengths, if present
			//-- A length is present. It only defines the max. input size if no converter is present...
			if(pmm.getConverter() == null) {
				calcmaxsz = pmm.getLength(); // Defined max length always overrides anything else
				if(calcsz <= 0 && calcmaxsz < 40)
					calcsz = calcmaxsz; // Set the display size provided it is reasonable
			}
		}

		//-- Wrap it up...
		if(calcmaxsz > 0)
			txt.setMaxLength(calcmaxsz);
		if(calcsz <= 0) {
			if(calcmaxsz <= 0 || calcmaxsz > 40)
				calcsz = 40;
			else
				calcsz = calcmaxsz;
		}
		txt.setSize(calcsz);
		return txt;
	}


}


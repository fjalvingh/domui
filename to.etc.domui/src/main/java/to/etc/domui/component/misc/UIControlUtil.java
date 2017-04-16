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

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

import javax.annotation.*;
import java.math.*;

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
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(PropertyMetaModel< ? > pmm) {
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
	static public <T extends Enum<T>> ComboFixed<T> createEnumCombo(PropertyMetaModel< ? > pmm, T... domainvalues) {
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
	static public String getEnumLabel(PropertyMetaModel< ? > pmm, Object value) {
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
	 * @deprecated Use {@link Text#createDoubleMoneyInput(Class<?>,String,boolean)} instead
	 */
	@Deprecated
	@Nonnull
	static public Text<Double> createDoubleMoneyInput(@Nonnull Class< ? > clz, @Nonnull String property, boolean editable) {
		return Text.createDoubleMoneyInput(clz, property, editable);
	}

	/**
	 * @deprecated Use {@link Text#createBDMoneyInput(Class<?>,String,boolean)} instead
	 */
	@Deprecated
	static public Text<BigDecimal> createBDMoneyInput(Class< ? > clz, String property, boolean editable) {
		return Text.createBDMoneyInput(clz, property, editable);
	}

	/**
	 * @deprecated Use {@link Text#createBDMoneyInput(PropertyMetaModel<?>,boolean)} instead
	 */
	@Deprecated
	static public Text<BigDecimal> createBDMoneyInput(PropertyMetaModel<BigDecimal> pmm, boolean editable) {
		return Text.createBDMoneyInput(pmm, editable);
	}

	/**
	 * @deprecated Use {@link Text#createDoubleMoneyInput(PropertyMetaModel<?>,boolean)} instead
	 */
	@Deprecated
	@Nonnull
	static public Text<Double> createDoubleMoneyInput(@Nonnull PropertyMetaModel<Double> pmm, boolean editable) {
		return Text.createDoubleMoneyInput(pmm, editable);
	}

	/**
	 * @deprecated Use {@link Text#configureNumericInput(Text<?>,PropertyMetaModel<?>,boolean)} instead
	 */
	@Deprecated
	public static void configureNumericInput(Text< ? > txt, PropertyMetaModel< ? > pmm, boolean editable) {
		Text.configureNumericInput(txt, pmm, editable);
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
	 * @deprecated Use {@link Text#createIntInput(Class<?>,String,boolean)} instead
	 */
	@Deprecated
	static public Text<Integer> createIntInput(Class< ? > clz, String property, boolean editable) {
		return Text.createIntInput(clz, property, editable);
	}

	/**
	 * @deprecated Use {@link Text#createNumericInput(PropertyMetaModel<Integer>,boolean)} instead
	 */
	@Deprecated
	static public Text<Integer> createIntInput(PropertyMetaModel<Integer> pmm, boolean editable) {
		return Text.createNumericInput(pmm, editable);
	}

	/**
	 * @deprecated Use {@link Text#createLongInput(Class<?>,String,boolean)} instead
	 */
	@Deprecated
	static public Text<Long> createLongInput(Class< ? > clz, String property, boolean editable) {
		return Text.createLongInput(clz, property, editable);
	}

	/**
	 * @deprecated Use {@link Text#createNumericInput(PropertyMetaModel<Long>,boolean)} instead
	 */
	@Deprecated
	static public Text<Long> createLongInput(PropertyMetaModel<Long> pmm, boolean editable) {
		return Text.createNumericInput(pmm, editable);
	}

	/**
	 * @deprecated Use {@link Text#createDoubleInput(Class<?>,String,boolean)} instead
	 */
	@Deprecated
	static public Text<Double> createDoubleInput(Class< ? > clz, String property, boolean editable) {
		return Text.createDoubleInput(clz, property, editable);
	}

	/**
	 * @deprecated Use {@link Text#createNumericInput(PropertyMetaModel<Double>,boolean)} instead
	 */
	@Deprecated
	static public Text<Double> createDoubleInput(PropertyMetaModel<Double> pmm, boolean editable) {
		return Text.createNumericInput(pmm, editable);
	}

	/**
	 * @deprecated Use {@link Text#createBigDecimalInput(Class<?>,String,boolean)} instead
	 */
	@Deprecated
	static public Text<BigDecimal> createBigDecimalInput(Class< ? > clz, String property, boolean editable) {
		return Text.createBigDecimalInput(clz, property, editable);
	}

	/**
	 * @deprecated Use {@link Text#createNumericInput(PropertyMetaModel<BigDecimal>,boolean)} instead
	 */
	@Deprecated
	static public Text<BigDecimal> createBigDecimalInput(PropertyMetaModel<BigDecimal> pmm, boolean editable) {
		return Text.createNumericInput(pmm, editable);
	}

	/**
	 * @deprecated Use {@link Text#createText(Class<?>,String,boolean)} instead
	 */
	@Deprecated
	static public <T> Text< ? > createText(Class< ? > clz, String property, boolean editable) {
		return Text.createText(clz, property, editable);
	}

	/**
	 * @deprecated Use {@link Text#createText(Class<T>,PropertyMetaModel<T>,boolean)} instead
	 */
	@Deprecated
	static public <T> Text<T> createText(Class<T> iclz, PropertyMetaModel<T> pmm, boolean editable) {
		return Text.createText(iclz, pmm, editable);
	}

	public static String getRgbHex(java.awt.Color color, boolean prefixHash) {
		String hexString = Integer.toHexString(color.getRGB() & 0x00FFFFFF);
		while(hexString.length() < 6)
			hexString = "0" + hexString;
		if(prefixHash) {
			hexString = "#" + hexString;
		}
		return hexString;
	}

	/**
	 * Returns resource text, but with excluded action key (exclamation mark symbol).
	 * That enables to reuse button captions on link buttons i.e. 
	 *   
	 * @param bundle
	 * @param key
	 * @return
	 */
	@Nonnull 
	public static String removeActionKeyStr(@Nonnull BundleRef bundle, @Nonnull String key) {
		return DomUtil.nullChecked(bundle.getString(key).replace("!", ""));
	}

}


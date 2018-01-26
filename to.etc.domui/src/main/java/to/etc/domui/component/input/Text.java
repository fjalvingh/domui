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

import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.MetaUtils;
import to.etc.domui.component.meta.NumericPresentation;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.PropertyMetaValidator;
import to.etc.domui.component.meta.impl.MetaPropertyValidatorImpl;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.IConvertable;
import to.etc.domui.converter.IConverter;
import to.etc.domui.converter.IValueValidator;
import to.etc.domui.converter.MoneyUtil;
import to.etc.domui.converter.NumericUtil;
import to.etc.domui.converter.ValidatorRegistry;
import to.etc.domui.dom.css.TextAlign;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IHasModifiedIndication;
import to.etc.domui.dom.html.Input;
import to.etc.domui.trouble.UIException;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.Msgs;
import to.etc.util.RuntimeConversionException;
import to.etc.util.RuntimeConversions;
import to.etc.webapp.nls.NlsContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Use {@code Text2<T>} instead.
 *
 * A single-line input box. This extends the "input" tag with validation ability
 * and methods to handle conversions and labels.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 11, 2008
 */
@Deprecated
public class Text<T> extends Input implements IControl<T>, IHasModifiedIndication, IConvertable<T>, ITypedControl<T> {
	/** The type of class that is expected. This is the return type of the getValue() call for a validated item */
	@Nonnull
	private Class<T> m_inputClass;

	/**
	 * If the value is to be converted use this converter for it.
	 */
	private IConverter<T> m_converter;

	/** Defined value validators on this field. */
	private List<IValueValidator< ? >> m_validators = Collections.emptyList();

	private T m_value;

	/**
	 * This flag gets T if the validate method has been called on the current
	 * input for a control. It gets reset when a control receives a new value
	 * that differs from it's previous value (raw).
	 */
	private boolean m_validated;

	/** If validated this contains the last validation result. */
	private UIException m_validationResult;

	/**
	 * T when this input value is a REQUIRED value.
	 */
	private boolean m_mandatory;

	/**
	 * When T the raw value in the text input thing does not get space trimmed before
	 * it's being returned.
	 */
	private boolean m_untrimmed;

	public enum NumberMode {
		NONE, DIGITS, FLOAT,
	}

	@Nonnull
	private NumberMode m_numberMode = NumberMode.NONE;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	private boolean m_modifiedByUser;

	private String m_validationRegexp;

	private String m_regexpUserString;

	private String m_placeHolder;

	public Text(@Nonnull Class<T> inputClass) {
		m_inputClass = inputClass;
		addCssClass("ui-otxt");

		NumberMode nm = NumberMode.NONE;
		if(BigDecimal.class.isAssignableFrom(inputClass) || DomUtil.isRealType(inputClass))
			nm = NumberMode.FLOAT;
		else if(DomUtil.isIntegerType(inputClass))
			nm = NumberMode.DIGITS;
		setNumberMode(nm);
	}

	/**
	 * Handle the input from the request for this component.
	 * @see to.etc.domui.dom.html.Input#acceptRequestParameter(java.lang.String[])
	 */
	@Override
	public boolean acceptRequestParameter(@Nonnull String[] values) {
		String oldValue = getRawValue();									// Retain previous value,
		super.acceptRequestParameter(values);								// Set the new one;
		oldValue = oldValue == null ? "" : m_untrimmed ? oldValue : oldValue.trim();
		String newValue = getRawValue() == null ? "" : m_untrimmed ? getRawValue() : getRawValue().trim();
		if(oldValue.equals(newValue)) {
			return false;
		}
		m_validated = false;
		DomUtil.setModifiedFlag(this);

		//-- Handle data updates.
// jal 2014/11/08 This is obviously wrong because there is no way to pass in the bind-time validation failure. Disable for now as hard binding is probably not useful.
//		T old = m_value;
//		if(validate(true)) {
//			fireModified("value", old, m_value);
//		}

		return true;
	}

	/**
	 * Main handler to validate input. This gets called whenever new input is
	 * present in the control (unvalidated) and:
	 * <ul>
	 * 	<li>The converted value in this control is requested by a call to getValue() or</li>
	 *	<li>Someone has called for the validation of a whole container (parent node)</li>
	 * </ul>
	 * If validation fails then this throws UIException containing the exact validation problem.
	 */
	private void validate(boolean clearvalidate) {
		UIException result = m_validationResult;
		if(m_validated) {
			if(null == result)
				return;
			throw result;
		}
		try {
			m_validated = true;
			validatePrimitive();

			if(clearvalidate)
				clearValidationFailure(result);				// jal 20160216 You cannot just do this: this clears the error message associated with the component!!!
			m_validationResult = null;
		} catch(ValidationException vx) {
			m_validationResult = vx;
			throw vx;
		}
	}

	/**
	 * Does all validations unconditionally, and throws the appropriate ValidationException on trouble.
	 */
	private void validatePrimitive() {
		//-- 1. Get the appropriate raw value && trim
		String raw = getRawValue();
		if(raw != null && !m_untrimmed)
			raw = raw.trim();

		//-- Do mandatory checking && exit if value is missing.
		if(raw == null || raw.length() == 0) {
			//-- Field is empty.
			if(isMandatory()) {
				throw new ValidationException(Msgs.MANDATORY);
			}

			//-- Empty field always results in null object.
			m_value = null;
			return;
		}

		//-- If a pattern validation is present apply it to the raw string value.
		if(getValidationRegexp() != null) {
			if(!Pattern.matches(getValidationRegexp(), raw)) {
				//-- We have a validation error.
				if(getRegexpUserString() != null)
					throw new ValidationException(Msgs.V_NO_RE_MATCH, getRegexpUserString());		// Input format must be {0}
				else
					throw new ValidationException(Msgs.V_INVALID);
			}
		}

		//-- Handle conversion and validation.
		try {
			IConverter<T> c = m_converter;
			if(c == null)
				c = ConverterRegistry.findConverter(getActualType());

			Object converted;
			if(c != null)
				converted = c.convertStringToObject(NlsContext.getLocale(), raw);
			else
				converted = RuntimeConversions.convertTo(raw, m_inputClass);

			for(IValueValidator< ? > vv : m_validators)
				((IValueValidator<Object>) vv).validate(converted);
			m_value = (T) converted;
		} catch(UIException x) {
			throw new ValidationException(x.getBundle(), x.getCode(), x.getParameters());
		} catch(RuntimeConversionException x) {
			throw new ValidationException(Msgs.NOT_VALID, raw);
		} catch(Exception x) {
			x.printStackTrace();
			throw new ValidationException(Msgs.UNEXPECTED_EXCEPTION, x);
		}
	}

	private void handleValidationException(@Nullable ValidationException x) {
		UIMessage message = null;
		if(null != x) {
			message = UIMessage.error(x);
		}
		setMessage(message);
		messageNotifier(message);
	}

	@Nullable
	private String m_errclass;

	private void messageNotifier(@Nullable UIMessage msg) {
		String errclass = m_errclass;
		if(errclass != null) {
			removeCssClass(errclass);
			m_errclass = null;
			setTitle("");
		}
		if(null != msg) {
			errclass = m_errclass = "ui-text-" + msg.getType().name().toLowerCase();
			addCssClass(errclass);
			setTitle(msg.getMessage());
		}
	}

	/**
	 * Returns the datatype of the value of this control, as passed in the constructor.
	 * @return
	 */
	@Override
	@Nonnull
	public Class<T> getActualType() {
		return m_inputClass;
	}

	/**
	 * See {@link IConvertable#getConverter()}.
	 * This returns null if no converter has been set. It also returns null if a default converter is used.
	 *
	 * @return
	 */
	@Override
	public IConverter<T> getConverter() {
		return m_converter;
	}

	/**
	 * See {@link IConvertable#setConverter(IConverter)}.
	 * Sets the Converter to use to convert the string value to a T and vice versa. It is the programmer's
	 * responsibility to ensure that the converter actually converts to a T; if not the code will throw
	 * ClassCastExceptions.
	 *
	 * @param converter
	 */
	@Override
	public void setConverter(IConverter<T> converter) {
		m_converter = converter;
		//-- 20171005 jal Very, very wrong. If you want to clear defaults do it manually.
		//if(m_numberMode != NumberMode.NONE && converter != null) {
		//	m_numberMode = NumberMode.NONE;
		//	setOnKeyPressJS(null);
		//}
	}

	/**
	 * Bind-capable version of getValue(). If called (usually from binding) this will act as follows:
	 * <ul>
	 * 	<li>If this component has an input error: throw the ValidationException for that error</li>
	 * 	<li>On no error this returns the value.</li>
	 * </ul>
	 * @return
	 */
	public T getBindValue() {
		validate(false);												// Validate, and throw exception without UI change on trouble.
		return m_value;
	}

	public void setBindValue(@Nullable T value) {
		if(MetaManager.areObjectsEqual(m_value, value)) {
			return;
		}
		setValue(value);
	}


	/**
	 * @see to.etc.domui.dom.html.IControl#getValue()
	 */
	@Override
	public T getValue() {
		try {
			validate(true);
			return m_value;
		} catch(ValidationException x) {
			handleValidationException(x);
			throw x;
		}
	}

	/**
	 * Clear message and reset validated flag, so next getValue would result with new validation check.
	 * @see to.etc.domui.dom.html.NodeBase#clearMessage()
	 */
	@Override
	public void clearMessage() {
		super.clearMessage();
		m_validated = false;
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#getValueSafe()
	 */
	@Override
	public T getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#hasError()
	 */
	@Override
	public boolean hasError() {
		getValueSafe();
		return super.hasError();
	}

	/**
	 * Set a new value in this component. The value will be converted to a string representation by
	 * any converter set or by one of the default converters. This string value will then be shown
	 * to the users.
	 *
	 * @see to.etc.domui.dom.html.IControl#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(@Nullable T value) {
		m_value = value;
		String converted;
		try {
			IConverter<T> c = m_converter;
			if(c == null)
				c = ConverterRegistry.findConverter(getActualType());

			if(c != null)
				converted = c.convertObjectToString(NlsContext.getLocale(), value);
			else
				converted = RuntimeConversions.convertTo(value, String.class);
		} catch(UIException x) {
			setMessage(UIMessage.error(x.getBundle(), x.getCode(), x.getParameters()));
			return;
		} catch(Exception x) {
			x.printStackTrace();
			setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.UNEXPECTED_EXCEPTION, x));
			return;
		}
		setRawValue(converted == null ? "" : converted); // jal 20090821 If set to null for empty the value attribute will not be renderered, it must render a value as empty string

		clearMessage();

		// jal 20081021 Clear validated als inputwaarde leeg is en de control is mandatory.
		if((converted == null || converted.trim().length() == 0) && isMandatory())
			m_validated = false;
		else {
			m_validated = true;
			m_validationResult = null;
		}
	}

	/**
	 * Returns T if this control is mandatory.
	 * @see to.etc.domui.dom.html.IControl#isMandatory()
	 */
	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	/**
	 * Set the control as mandatory. A mandatory control expects the value filled in to be non-whitespace.
	 *
	 * @see to.etc.domui.dom.html.IControl#setMandatory(boolean)
	 */
	@Override
	public void setMandatory(boolean mandatory) {
		if(mandatory && !m_mandatory) {
			//vmijic 20100326 - m_validated flag must be reset in case that component dynamically becomes mandatory (since it can happen that was setValue(null) while it not mandatory)
			m_validated = false;
		}
		m_mandatory = mandatory;
	}

	/**
	 * Returns T if the input is to be left untrimmed.
	 * @return
	 */
	public boolean isUntrimmed() {
		return m_untrimmed;
	}

	/**
	 * Specify whether the input is to be space-trimmed before being used. This defaults to TRUE, causing
	 * values to be trimmed before being returned to the converter code.
	 * @param untrimmed
	 */
	public void setUntrimmed(boolean untrimmed) {
		m_untrimmed = untrimmed;
	}

	/**
	 * Returns the current numeric mode in effect. This mode prevents letters from being input on the screen.
	 * @return
	 */
	@Nonnull
	public NumberMode getNumberMode() {
		return m_numberMode;
	}

	/**
	 * Sets the current numeric mode in effect. This mode prevents letters from being input on the screen.
	 * @param numberMode
	 */
	public void setNumberMode(@Nonnull NumberMode numberMode) {
		m_numberMode = numberMode;

		switch(numberMode){
			default:
				throw new IllegalStateException(numberMode + "?");

			case NONE:
				setOnKeyPressJS("");
				break;
			case DIGITS:
				setOnKeyPressJS("WebUI.isNumberKey(event)");
				break;
			case FLOAT:
				setOnKeyPressJS("WebUI.isFloatKey(event)");
				break;
		}

	}

	public void addValidator(IValueValidator< ? > v) {
		if(m_validators == Collections.EMPTY_LIST)
			m_validators = new ArrayList<IValueValidator< ? >>(5);
		m_validators.add(v);
	}

	public void addValidator(PropertyMetaValidator v) {
		IValueValidator<T> vi = ValidatorRegistry.getValueValidator((Class< ? extends IValueValidator<T>>) v.getValidatorClass(), v.getParameters());
		addValidator(vi);
	}

	public void addValidator(Class< ? extends IValueValidator<T>> clz) {
		IValueValidator<T> vi = ValidatorRegistry.getValueValidator(clz, null);
		addValidator(vi);
	}

	public void addValidator(Class< ? extends IValueValidator<T>> clz, String[] parameters) {
		addValidator(new MetaPropertyValidatorImpl(clz, parameters));
	}

	public String getValidationRegexp() {
		return m_validationRegexp;
	}

	public void setValidationRegexp(String validationRegexp) {
		m_validationRegexp = validationRegexp;
	}

	public String getRegexpUserString() {
		return m_regexpUserString;
	}

	public void setRegexpUserString(String regexpUserString) {
		m_regexpUserString = regexpUserString;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IHasModifiedIndication impl							*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the modified-by-user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#isModified()
	 */
	@Override
	public boolean isModified() {
		return m_modifiedByUser;
	}

	/**
	 * Set or clear the modified by user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#setModified(boolean)
	 */
	@Override
	public void setModified(boolean as) {
		m_modifiedByUser = as;
	}

	/**
	 * This adds a validator for the maximal and minimal value for an input, gotten from the property metamodel.
	 * @param control
	 * @param pmm
	 */
	public static final void assignPrecisionValidator(@Nonnull Text< ? > control, @Nonnull PropertyMetaModel< ? > pmm) {
		IValueValidator<?> validator = MetaManager.calculatePrecisionValidator(pmm);
		if(null != validator)
			control.addValidator(validator);
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
		return Text.createDoubleMoneyInput((PropertyMetaModel<Double>) MetaManager.getPropertyMeta(clz, property), editable);
	}

	@Nonnull
	static public Text<BigDecimal> createBDMoneyInput(Class< ? > clz, String property, boolean editable) {
		return Text.createBDMoneyInput((PropertyMetaModel<BigDecimal>) MetaManager.findPropertyMeta(clz, property), editable);
	}

	@Nonnull
	static public Text<BigDecimal> createBDMoneyInput(PropertyMetaModel<BigDecimal> pmm, boolean editable) {
		if(pmm == null)
			throw new NullPointerException("Null property model not allowed");
		Text<BigDecimal> txt = new Text<BigDecimal>(BigDecimal.class);
		Text.configureNumericInput(txt, pmm, editable);
		MoneyUtil.assignMonetaryConverter(pmm, editable, txt);
		return txt;
	}

	@Nonnull
	static public Text<Double> createDoubleMoneyInput(@Nonnull PropertyMetaModel<Double> pmm, boolean editable) {
		if(pmm == null)
			throw new NullPointerException("Null property model not allowed");
		Text<Double> txt = new Text<Double>(Double.class);
		Text.configureNumericInput(txt, pmm, editable);
		MoneyUtil.assignMonetaryConverter(pmm, editable, txt);
		return txt;
	}

	public static void configureNumericInput(@Nonnull Text< ? > txt, @Nonnull PropertyMetaModel< ? > pmm, boolean editable) {
		if(!editable)
			txt.setReadOnly(true);

		/*
		 * Length calculation using the metadata. This uses the "length" field as LAST, because it is often 255 because the
		 * JPA's column annotation defaults length to 255 to make sure it's usability is bloody reduced. Idiots.
		 */
		int size = MetaManager.calculateTextSize(pmm);
		if(size > 0)
			txt.setSize(size);

		//-- 20100318 Since we have precision and scale, add a range check to this control.
		//-- 20110721 jal Move it globally: it does not work when an explicit display size is set.
		IValueValidator<?> validator = MetaManager.calculatePrecisionValidator(pmm);
		if(null != validator)
			txt.addValidator(validator);
		Text.assignPrecisionValidator(txt, pmm);

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

	/*--------------------------------------------------------------*/
	/*	CODING:	Numeric Text inputs for base types.					*/
	/*--------------------------------------------------------------*/

	@Nonnull
	static public <T extends Number> Text<T> createNumericInput(PropertyMetaModel<T> pmm, boolean editable) {
		if(pmm == null)
			throw new NullPointerException("Null property model not allowed");
		Text<T> txt = new Text<T>(pmm.getActualType());
		Text.configureNumericInput(txt, pmm, editable);
		NumericUtil.assignNumericConverter(pmm, editable, txt, pmm.getActualType());
		return txt;
	}

	/**
	 * Create an int input control, properly configured for the specified property.
	 * @param clz
	 * @param property
	 * @param editable
	 * @return
	 */
	@Nonnull
	static public Text<Integer> createIntInput(Class< ? > clz, String property, boolean editable) {
		return Text.createNumericInput((PropertyMetaModel<Integer>) MetaManager.findPropertyMeta(clz, property), editable);
	}

	@Nonnull
	static public Text<Long> createLongInput(Class< ? > clz, String property, boolean editable) {
		return Text.createNumericInput((PropertyMetaModel<Long>) MetaManager.findPropertyMeta(clz, property), editable);
	}

	@Nonnull
	static public Text<Double> createDoubleInput(Class< ? > clz, String property, boolean editable) {
		return Text.createNumericInput((PropertyMetaModel<Double>) MetaManager.findPropertyMeta(clz, property), editable);
	}

//	@Nonnull
//	static public <N extends Number> Text<N> createNumericInput(PropertyMetaModel<N> pmm, boolean editable) {
//		if(pmm == null)
//			throw new NullPointerException("Null property model not allowed");
//		Text<N> txt = new Text<N>(pmm.getActualType());
//		Text.configureNumericInput(txt, pmm, editable);
//		NumericUtil.assignNumericConverter(pmm, editable, txt, pmm.getActualType());
//		return txt;
//	}
//

	@Nonnull
	static public Text<BigDecimal> createBigDecimalInput(Class< ? > clz, String property, boolean editable) {
		return Text.createNumericInput((PropertyMetaModel<BigDecimal>) MetaManager.findPropertyMeta(clz, property), editable);
	}

	@Nonnull
	static public <T> Text< ? > createText(Class< ? > clz, String property, boolean editable) {
		PropertyMetaModel<T> pmm = (PropertyMetaModel<T>) MetaManager.getPropertyMeta(clz, property);
		return Text.createText(pmm.getActualType(), pmm, editable);
	}

	@Nonnull
	static public <T> Text<T> createText(Class<T> iclz, PropertyMetaModel<T> pmm, boolean editable) {
		return createText(iclz, pmm, editable, false);
	}

	@Nonnull
	static public <T> Text<T> createText(Class<T> iclz, PropertyMetaModel<T> pmm, boolean editable, boolean setDefaultErrorLocation) {
		Class< ? > aclz = pmm.getActualType();
		if(!iclz.isAssignableFrom(aclz))
			throw new IllegalStateException("Invalid class type=" + iclz + " for property " + pmm);
		Text<T> txt = new Text<T>(iclz);

		//-- Get simple things to do out of the way.
		if(!editable)
			txt.setReadOnly(true);
		if(pmm.getConverter() != null)
			txt.setConverter(pmm.getConverter());
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
			Text.assignPrecisionValidator(txt, pmm);

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
		if(setDefaultErrorLocation) {
			txt.setErrorLocation(pmm.getDefaultLabel());
		}
		return txt;
	}

	/**
	 * Append appropriate JS based on current {@link NumberMode}
	 */
	private void renderMode() {
		switch(m_numberMode){
			default:
				break;
			case DIGITS:
				setOnKeyPressJS("WebUI.isNumberKey(event)");
				break;
			case FLOAT:
				setOnKeyPressJS("WebUI.isFloatKey(event)");
				break;
		}
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		renderMode();
	}
}

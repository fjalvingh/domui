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

import java.math.*;
import java.util.*;
import java.util.regex.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.parts.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

/**
 * A single-line input box. This extends the "input" tag with validation ability
 * and methods to handle conversions and labels.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 11, 2008
 */
public class Text<T> extends Input implements IControl<T>, IHasModifiedIndication, IConvertable<T> {
	/** The properties bindable for this component. */
	@Nonnull
	static private final Set<String> BINDABLE_SET = createNameSet("value", "disabled");

	/** The type of class that is expected. This is the return type of the getValue() call for a validated item */
	private Class<T> m_inputClass;

	/**
	 * If the value is to be converted use this converter for it.
	 */
	private IConverter<T> m_converter;

	/** Defined value validators on this field. */
	private List<IValueValidator< ? >> m_validators = Collections.EMPTY_LIST;

	private T m_value;

	/**
	 * This flag gets T if the validate method has been called on the current
	 * input for a control. It gets reset when a control receives a new value
	 * that differs from it's previous value (raw).
	 */
	private boolean m_validated;

	/** If validated this contains the last validation result. */
	private boolean m_wasvalid;

	/**
	 * T when this input value is a REQUIRED value.
	 */
	private boolean m_mandatory;

	/**
	 * When T the raw value in the text input thing does not get space trimmed before
	 * it's being returned.
	 */
	private boolean m_untrimmed;


	/**
	 * @see Text#getEmptyMarker()
	 */
	private String m_emptyMarker;

	public static enum NumberMode {
		NONE, DIGITS, FLOAT,
	}

	private NumberMode m_numberMode = NumberMode.NONE;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	private boolean m_modifiedByUser;

	private String m_validationRegexp;

	private String m_regexpUserString;

	public Text(Class<T> inputClass) {
		m_inputClass = inputClass;

		if(BigDecimal.class.isAssignableFrom(inputClass) || DomUtil.isRealType(inputClass))
			m_numberMode = NumberMode.FLOAT;
		else if(DomUtil.isIntegerType(inputClass))
			m_numberMode = NumberMode.DIGITS;

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
	@Nonnull
	public Set<String> getBindableProperties() {
		return BINDABLE_SET;
	}

	/**
	 * Handle the input from the request for this component.
	 * @see to.etc.domui.dom.html.Input#acceptRequestParameter(java.lang.String[])
	 */
	@Override
	public boolean acceptRequestParameter(@Nonnull String[] values) {
		String oldValue = getRawValue();									// Retain previous value,
		super.acceptRequestParameter(values);								// Set the new one;
		String oldTrimmed = oldValue == null ? "" : oldValue.trim();
		String newTrimmed = getRawValue() == null ? "" : getRawValue().trim();
		if(oldTrimmed.equals(newTrimmed)) {
			return false;
		}
		m_validated = false;
		DomUtil.setModifiedFlag(this);

		//-- Handle data updates.
		T old = m_value;
		if(validate(false)) {
			fireModified("value", old, m_value);
		}

		return true;
	}

	/**
	 * Main handler to validate input. This gets called whenever new input is
	 * present in the control (unvalidated) and:
	 * <ul>
	 * 	<li>The converted value in this control is requested by a call to getValue() or</li>
	 *	<li>Someone has called for the validation of a whole container (parent node)</li>
	 * </ul>
	 * If validation fails it sets this control in ERROR status, and it registers the error
	 * message into the Page. When in ERROR state an input control will add an "invalidValue"
	 * class to it's HTML class, and it may expose error labels on it.
	 */
	public boolean validate(boolean seterror) {
		if(m_validated)
			return m_wasvalid;

		//-- 1. Get the appropriate raw value && trim
		String raw = getRawValue();
		if(raw != null && !m_untrimmed)
			raw = raw.trim();

		//-- Do mandatory checking && exit if value is missing.
		m_validated = true;
		m_wasvalid = false;
		if(raw == null || raw.length() == 0) {
			if(isMandatory()) {
				handleValidationError(UIMessage.error(Msgs.BUNDLE, Msgs.MANDATORY), seterror);
				return false;
			}

			//-- Empty field always results in null object.
			m_value = null;
			handleValidationError(null, seterror);
			m_wasvalid = true;
			return true;
		}

		//-- If a pattern validation is present apply it to the raw string value.
		if(getValidationRegexp() != null) {
			if(!Pattern.matches(getValidationRegexp(), raw)) {
				//-- We have a validation error.
				if(getRegexpUserString() != null)
					handleValidationError(UIMessage.error(Msgs.BUNDLE, Msgs.V_NO_RE_MATCH, getRegexpUserString()), seterror);// Input format must be {0}
				else
					handleValidationError(UIMessage.error(Msgs.BUNDLE, Msgs.V_INVALID), seterror);
				m_wasvalid = false;
				return false;
			}
		}

		//-- Handle conversion and validation.
		Object converted;
		try {
			IConverter<T> c = m_converter;
			if(c == null)
				c = ConverterRegistry.findConverter(getInputClass());

			if(c != null)
				converted = c.convertStringToObject(NlsContext.getLocale(), raw);
			else
				converted = RuntimeConversions.convertTo(raw, m_inputClass);

			for(IValueValidator< ? > vv : m_validators)
				((IValueValidator<Object>) vv).validate(converted);

			m_wasvalid = true;
		} catch(UIException x) {
			handleValidationError(UIMessage.error(x.getBundle(), x.getCode(), x.getParameters()), seterror);
			return false;
		} catch(RuntimeConversionException x) {
			handleValidationError(UIMessage.error(Msgs.BUNDLE, Msgs.NOT_VALID, raw), seterror);
			return false;
		} catch(Exception x) {
			x.printStackTrace();
			handleValidationError(UIMessage.error(Msgs.BUNDLE, Msgs.UNEXPECTED_EXCEPTION, x), seterror);
			return false;
		}

		//-- Conversion ok. Handle any validator in the validation chain
		m_value = (T) converted;
		handleValidationError(null, seterror);
		return true;
	}

	private void handleValidationError(@Nullable UIMessage message, boolean seterror) {
		if(null == message) {
			clearMessage();
		} else {
			if(seterror) {
				setMessage(message);
			}
		}
		messageNotifier(message);
	}

	private String m_errclass;

	private void messageNotifier(@Nullable UIMessage msg) {
		if(m_errclass != null) {
			removeCssClass(m_errclass);
			m_errclass = null;
			setTitle("");
		}
		if(null != msg) {
			m_errclass = "ui-text-" + msg.getType().name().toLowerCase();
			addCssClass(m_errclass);
			setTitle(msg.getMessage());
		}
	}


	@Nullable
	private Div m_mnot;

	private void messageNotifier2(@Nullable UIMessage msg) {
		Div mn = m_mnot;
		if(mn != null)
			mn.remove();
		if(null == msg) {
			m_mnot = null;
		} else {
			mn = m_mnot = new Div();
			appendAfterMe(mn);
			mn.setCssClass("ui-mesi ui-mesi-" + msg.getType().name().toLowerCase());
			mn.setTitle(msg.getMessage());
		}
	}

	/**
	 * Returns the datatype of the value of this control, as passed in the constructor.
	 * @return
	 */
	public Class<T> getInputClass() {
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
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#getValue()
	 */
	@Override
	public T getValue() {
		if(!validate(true))
			throw new ValidationException(Msgs.NOT_VALID, getRawValue());
		return m_value;
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
		T old = m_value;
		m_value = value;
		try {
			String converted;
			try {
				IConverter<T> c = m_converter;
				if(c == null)
					c = ConverterRegistry.findConverter(getInputClass());

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
				m_wasvalid = true;
			}
		} finally {
			fireModified("value", old, value);
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

	//	private boolean isValidated() {
	//		return m_validated;
	//	}

	private void setEmptyMarker(String emptyMarker) {
		if(DomUtil.isBlank(emptyMarker)) {
			setSpecialAttribute("marker", null);
		} else {
			setSpecialAttribute("marker", emptyMarker);
		}
		m_emptyMarker = emptyMarker;
	}

	/**
	 * Returns assigned empty marker.
	 *
	 * @see Text#setEmptyMarker(String)
	 */
	public String getEmptyMarker() {
		return m_emptyMarker;
	}

	/**
	 * This sets a marker image to be used as the background image for an empty text box. It should contain the URL to a fully-constructed
	 * background image. To create such an image from an icon plus text use one of the setMarkerXxx methods. This method should be used
	 * only for manually-constructed images.
	 * @param emptyMarker
	 */
	public void setMarkerImage(String emptyMarker) {
		if(DomUtil.isBlank(emptyMarker)) {
			setSpecialAttribute("marker", null);
		} else {
			setSpecialAttribute("marker", emptyMarker);
		}
		m_emptyMarker = emptyMarker;
	}

	/**
	 * Returns assigned empty marker.
	 *
	 * @see Text#setMarkerImage(String)
	 */
	public String getMarkerImage() {
		return m_emptyMarker;
	}


	/**
	 * Method can be used to show default marker icon (THEME/icon-search.png) with magnifier image in background of input. Image is hidden when input have focus or has any content.
	 * @return
	 */
	public void setMarker() {
		setMarkerImage(MarkerImagePart.getBackgroundIconOnly());
	}

	/**
	 * Method can be used to show custom marker icon as image in background of input. Image is hidden when input have focus or has any content.
	 *
	 * @param iconUrl
	 * @return
	 */
	public void setMarker(String iconUrl) {
		setMarkerImage(MarkerImagePart.getBackgroundIconOnly(iconUrl));
	}

	/**
	 * Method can be used to show default marker icon (THEME/icon-search.png) with magnifier and custom label as image in background of input. Image is hidden when input have focus or has any content.
	 *
	 * @param caption
	 * @return
	 */
	public void setMarkerText(String caption) {
		setMarkerImage(MarkerImagePart.getBackgroundImage(caption));
	}

	/**
	 * Method can be used to show custom marker icon and custom label as image in background of input. Image is hidden when input have focus or has any content.
	 *
	 * @param iconUrl
	 * @param caption
	 * @return
	 */
	public void setMarker(String iconUrl, String caption) {
		setMarkerImage(MarkerImagePart.getBackgroundImage(iconUrl, caption));
	}

	/**
	 * Returns the current numeric mode in effect. This mode prevents letters from being input on the screen.
	 * @return
	 */
	public NumberMode getNumberMode() {
		return m_numberMode;
	}

	/**
	 * Sets the current numeric mode in effect. This mode prevents letters from being input on the screen.
	 * @param numberMode
	 */
	public void setNumberMode(NumberMode numberMode) {
		m_numberMode = numberMode;
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

	/*--------------------------------------------------------------*/
	/*	CODING:	IBindable interface (EXPERIMENTAL)					*/
	/*--------------------------------------------------------------*/

	/** When this is bound this contains the binder instance handling the binding. */
	private SimpleBinder m_binder;

	/**
	 * Return the binder for this control.
	 * @see to.etc.domui.component.input.IBindable#bind()
	 */
	@Override
	public @Nonnull IBinder bind() {
		SimpleBinder binder = m_binder;
		if(binder == null)
			binder = m_binder = new SimpleBinder(this);
		return binder;
	}

	/**
	 * Returns T if this control is bound to some data value.
	 *
	 * @see to.etc.domui.component.input.IBindable#isBound()
	 */
	@Override
	public boolean isBound() {
		return m_binder != null && m_binder.isBound();
	}


	/**
	 * This adds a validator for the maximal and minimal value for an input, gotten from the property metamodel.
	 * @param control
	 * @param pmm
	 */
	public static final void assignPrecisionValidator(@Nonnull Text< ? > control, @Nonnull PropertyMetaModel< ? > pmm) {
		Text.assignPrecisionValidator(control, pmm.getPrecision(), pmm.getScale());
	}

	/**
	 * This adds a validator for the maximal and minimal value for a numeric input, depending on the precision
	 * and scale.
	 * @param control
	 * @param precision
	 * @param scale
	 */
	public static final void assignPrecisionValidator(@Nonnull Text< ? > control, int precision, int scale) {
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

		} else if(pmm.getLength() > 0) {
			txt.setSize(pmm.getLength() < 40 ? pmm.getLength() : 40);
		}
		//-- 20100318 Since we have precision and scale, add a range check to this control.
		//-- 20110721 jal Move it globally: it does not work when an explicit display size is set.
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
	/**
	 * Create an int input control, properly configured for the specified property.
	 * @param clz
	 * @param property
	 * @param editable
	 * @return
	 */
	@Nonnull
	static public Text<Integer> createIntInput(Class< ? > clz, String property, boolean editable) {
		return Text.createIntInput((PropertyMetaModel<Integer>) MetaManager.findPropertyMeta(clz, property), editable);
	}

	@Nonnull
	static public Text<Integer> createIntInput(PropertyMetaModel<Integer> pmm, boolean editable) {
		if(pmm == null)
			throw new NullPointerException("Null property model not allowed");
		Text<Integer> txt = new Text<Integer>(Integer.class);
		Text.configureNumericInput(txt, pmm, editable);
		NumericUtil.assignNumericConverter(pmm, editable, txt, Integer.class);
		return txt;
	}

	@Nonnull
	static public Text<Long> createLongInput(Class< ? > clz, String property, boolean editable) {
		return Text.createLongInput((PropertyMetaModel<Long>) MetaManager.findPropertyMeta(clz, property), editable);
	}

	@Nonnull
	static public Text<Long> createLongInput(PropertyMetaModel<Long> pmm, boolean editable) {
		if(pmm == null)
			throw new NullPointerException("Null property model not allowed");
		Text<Long> txt = new Text<Long>(Long.class);
		Text.configureNumericInput(txt, pmm, editable);
		NumericUtil.assignNumericConverter(pmm, editable, txt, Long.class);
		return txt;
	}

	@Nonnull
	static public Text<Double> createDoubleInput(Class< ? > clz, String property, boolean editable) {
		return Text.createDoubleInput((PropertyMetaModel<Double>) MetaManager.findPropertyMeta(clz, property), editable);
	}

	@Nonnull
	static public Text<Double> createDoubleInput(PropertyMetaModel<Double> pmm, boolean editable) {
		if(pmm == null)
			throw new NullPointerException("Null property model not allowed");
		Text<Double> txt = new Text<Double>(Double.class);
		Text.configureNumericInput(txt, pmm, editable);
		NumericUtil.assignNumericConverter(pmm, editable, txt, Double.class);
		return txt;
	}

	@Nonnull
	static public Text<BigDecimal> createBigDecimalInput(Class< ? > clz, String property, boolean editable) {
		return Text.createBigDecimalInput((PropertyMetaModel<BigDecimal>) MetaManager.findPropertyMeta(clz, property), editable);
	}

	@Nonnull
	static public Text<BigDecimal> createBigDecimalInput(PropertyMetaModel<BigDecimal> pmm, boolean editable) {
		if(pmm == null)
			throw new NullPointerException("Null property model not allowed");
		Text<BigDecimal> txt = new Text<BigDecimal>(BigDecimal.class);
		Text.configureNumericInput(txt, pmm, editable);
		NumericUtil.assignNumericConverter(pmm, editable, txt, BigDecimal.class);
		return txt;
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
}

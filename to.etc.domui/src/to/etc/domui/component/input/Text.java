package to.etc.domui.component.input;

import java.math.*;
import java.util.*;
import java.util.regex.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
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
public class Text<T> extends Input implements IInputNode<T>, IHasModifiedIndication {
	/** The type of class that is expected. This is the return type of the getValue() call for a validated item */
	private Class<T> m_inputClass;

	/**
	 * If the value is to be converted use this converter for it.
	 */
	private IConverter<T> m_converter;

	/** Defined value validators on this field. */
	private List<PropertyMetaValidator> m_validators = Collections.EMPTY_LIST;

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
				setOnKeyPressJS("return WebUI.isNumberKey(event)");
				break;
			case FLOAT:
				setOnKeyPressJS("return WebUI.isFloatKey(event)");
				break;
		}
	}

	/**
	 * Handle the input from the request for this component.
	 * @see to.etc.domui.dom.html.Input#acceptRequestParameter(java.lang.String[])
	 */
	@Override
	public boolean acceptRequestParameter(String[] values) {
		String value = getRawValue(); // Retain previous value,
		super.acceptRequestParameter(values); // Set the new one;

		//-- when string is rendered into Input html tag, it is rendered as trimmed, so old raw value for comparasion has also to be trimmed
		//vmijic 20091124 - when no input is done, empty string is returned as request parameter, so if old raw value was null it has to be replaced with empty string
		if(value != null) {
			value = value.trim();
		} else {
			value = "";
		}
		if(DomUtil.isEqual(value, getRawValue()))
			return false;
		m_validated = false;
		DomUtil.setModifiedFlag(this);
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
	public boolean validate() {
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
				setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.MANDATORY));
				return false;
			}

			//-- Empty field always results in null object.
			m_value = null;
			clearMessage();
			m_wasvalid = true;
			return true;
		}

		//-- If a pattern validation is present apply it to the raw string value.
		if(getValidationRegexp() != null) {
			if(!Pattern.matches(getValidationRegexp(), raw)) {
				//-- We have a validation error.
				if(getRegexpUserString() != null)
					setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.V_NO_RE_MATCH, getRegexpUserString()));// Input format must be {0}
				else
					setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.V_INVALID));
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

			if(m_validators.size() != 0)
				ValidatorRegistry.validate(converted, m_validators);

			m_wasvalid = true;
		} catch(UIException x) {
			setMessage(UIMessage.error(x.getBundle(), x.getCode(), x.getParameters()));
			return false;
		} catch(RuntimeConversionException x) {
			setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.NOT_VALID, raw));
			return false;
		} catch(Exception x) {
			x.printStackTrace();
			setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.UNEXPECTED_EXCEPTION, x));
			return false;
		}

		//-- Conversion ok. Handle any validator in the validation chain
		m_value = (T) converted;
		clearMessage();
		return true;
	}

	//	/**
	//	 * Returns TRUE if the input for this control is currently valid. This does NOT call the validator if needed!!!!
	//	 * @return
	//	 */
	//	private boolean isValid() {
	//		return m_validated && (getMessage() == null || getMessage().getType() != MsgType.ERROR);
	//	}

	/**
	 * Returns the datatype of the value of this control, as passed in the constructor.
	 * @return
	 */
	public Class<T> getInputClass() {
		return m_inputClass;
	}

	/**
	 * Returns the class of the converter for this control. This returns null if no converter has been set. It also
	 * returns null if a default converter is used.
	 *
	 * @return
	 */
	public IConverter<T> getConverter() {
		return m_converter;
	}

	/**
	 * Sets the Converter to use to convert the string value to a T and vice versa. It is the programmer's
	 * responsibility to ensure that the converter actually converts to a T; if not the code will throw
	 * ClassCastExceptions.
	 *
	 * @param converter
	 */
	public void setConverter(IConverter<T> converter) {
		m_converter = converter;
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#getValue()
	 */
	@Override
	public T getValue() {
		if(!validate())
			throw new ValidationException(Msgs.NOT_VALID, getRawValue());
		return m_value;
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#getValueSafe()
	 */
	@Override
	public T getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#hasError()
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
	 * @see to.etc.domui.dom.html.IInputNode#setValue(java.lang.Object)
	 */
	public void setValue(T value) {
		// jal 20080930 Onderstaande code aangepast. Dit levert als bug op dat "wissen" van een niet-gevalideerde waarde niet werkt. Dat
		// wordt veroorzaakt als volgt: als de control een niet-gevalideerde tekst bevat dan is m_rawValue de string maar m_value staat nog
		// op null. Onderstaande code returnt dan onmiddelijk waardoor de rawvalue blijft bestaan.
		// jal 20091002 If the value is currently INVALID but set from code we need to update always. This is needed
		// because 'invalid' can mean that rawvalue is set but value is not. In this case setting value to null (which
		// should clear the error)
		// jal 20091002 Better yet: WHY IS THIS TEST HERE!? Removing this test means that errors will be set every time
		// a setValue() is done with an incorrect value, but if the same value is set multiple times the rawValue
		// will not change, so no delta will be generated....
		//		if(isValidated() && DomUtil.isEqual(m_value, value)) // FIXME Removed pending explanation:  && DomUtil.isEqual(getRawValue(), value)
		//			return;
		m_value = value;
		String converted;
		try {
			IConverter<T> c = m_converter;
			if(c == null)
				c = ConverterRegistry.findConverter(getInputClass());

			if(c != null)
				converted = c.convertObjectToString(NlsContext.getLocale(), value);
			else
				converted = (String) RuntimeConversions.convertTo(value, String.class);
		} catch(UIException x) {
			setMessage(UIMessage.error(x.getBundle(), x.getCode(), x.getParameters()));
			return;
		} catch(Exception x) {
			x.printStackTrace();
			setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.UNEXPECTED_EXCEPTION, x));
			return;
		}
		setRawValue(converted == null ? "" : converted); // jal 20090821 If set to null for empty the value attribute will not be renderered, it must render a value as empty string

		// jal 20081021 Clear validated als inputwaarde leeg is en de control is mandatory.
		if((converted == null || converted.trim().length() == 0) && isMandatory())
			m_validated = false;
		else {
			m_validated = true;
			m_wasvalid = true;
		}
		clearMessage();
	}

	/**
	 * Returns T if this control is mandatory.
	 * @see to.etc.domui.dom.html.IInputNode#isMandatory()
	 */
	public boolean isMandatory() {
		return m_mandatory;
	}

	/**
	 * Set the control as mandatory. A mandatory control expects the value filled in to be non-whitespace.
	 *
	 * @see to.etc.domui.dom.html.IInputNode#setMandatory(boolean)
	 */
	public void setMandatory(boolean mandatory) {
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

	public void addValidator(PropertyMetaValidator v) {
		if(m_validators == Collections.EMPTY_LIST)
			m_validators = new ArrayList<PropertyMetaValidator>(5);
		m_validators.add(v);
	}

	public void setValidators(List<PropertyMetaValidator> validators) {
		m_validators = validators;
	}

	public void addValidator(Class< ? extends IValueValidator<T>> clz) {
		addValidator(new MetaPropertyValidatorImpl(clz));
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
	public boolean isModified() {
		return m_modifiedByUser;
	}

	/**
	 * Set or clear the modified by user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#setModified(boolean)
	 */
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
	public IBinder bind() {
		if(m_binder == null)
			m_binder = new SimpleBinder(this);
		return m_binder;
	}

	/**
	 * Returns T if this control is bound to some data value.
	 *
	 * @see to.etc.domui.component.input.IBindable#isBound()
	 */
	public boolean isBound() {
		return m_binder != null && m_binder.isBound();
	}
}

package to.etc.domui.component.lookup;

import java.math.*;

import to.etc.domui.component.input.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.webapp.query.*;

/**
 * Base utility class when lookups of custom numeric conditions should be implemented.
 * Provides buitin expression parser and range checks, only custom conditions have to be implemented in descendant classes. 
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Feb 11, 2010
 */
public abstract class AbstractNumericLookupControlImpl<T> extends AbstractLookupControlImpl {
	final private Text<String> m_input;

	private MiniScanner m_s;

	private Class<T> m_valueType;

	private T m_minValue;

	private T m_maxValue;

	public AbstractNumericLookupControlImpl(final Class<T> valueType, Text<String> node, T minValue, T maxValue) {
		super(node);
		m_input = node;
		m_valueType = valueType;
		m_minValue = minValue;
		m_maxValue = maxValue;

	}

	private void checkNumber(T value) {
		if(value instanceof Double) {
			if(m_maxValue != null && ((Double) value).doubleValue() > ((Double) m_maxValue).doubleValue() || m_minValue != null && ((Double) value).doubleValue() < ((Double) m_minValue).doubleValue())
				throw new ValidationException(Msgs.BUNDLE, Msgs.V_OUT_OF_RANGE, value);
		} else if(value instanceof BigDecimal) {
			if(m_maxValue != null && ((BigDecimal) value).doubleValue() > ((BigDecimal) m_maxValue).doubleValue() || m_minValue != null
				&& ((BigDecimal) value).doubleValue() < ((BigDecimal) m_minValue).doubleValue())
				throw new ValidationException(Msgs.BUNDLE, Msgs.V_OUT_OF_RANGE, value);
		} else if(value instanceof Long) {
			if(m_maxValue != null && ((Long) value).longValue() > ((Long) m_maxValue).longValue() || m_minValue != null && ((Long) value).longValue() < ((Long) m_minValue).longValue())
				throw new ValidationException(Msgs.BUNDLE, Msgs.V_OUT_OF_RANGE, value);
		} else if(value instanceof Integer) {
			if(m_maxValue != null && ((Integer) value).intValue() > ((Integer) m_maxValue).intValue() || m_minValue != null && ((Integer) value).intValue() < ((Integer) m_minValue).intValue())
				throw new ValidationException(Msgs.BUNDLE, Msgs.V_OUT_OF_RANGE, value);
		} else
			throw new IllegalStateException("Unsupported value type: " + value.getClass());
	}

	public abstract boolean appendCriteria(QCriteria< ? > crit, QOperation op, T val, QOperation op2, T val2) throws Exception;

	@Override
	public boolean appendCriteria(QCriteria< ? > crit) throws Exception {
		try {
			m_input.clearMessage(); // Remove any earlier validation failure.

			//-- Get value and bail out if it's empty.
			String in = m_input.getValue();
			if(in == null)
				return true;
			in = in.trim();
			if(in.length() == 0)
				return true;

			//-- Handle single operators: currently only the '!' to indicate 'not-null'
			if("!".equals(in)) {
				return appendCriteria(crit, QOperation.ISNULL, null, null, null);
			} else if("*".equals(in)) {
				return appendCriteria(crit, QOperation.ISNOTNULL, null, null, null);
			}

			//-- We need to separate into [operator, value] pairs; there can be max. 2.
			m_s = new MiniScanner();
			m_s.init(m_input.getValue());
			m_s.skipWs();

			if(Character.isDigit(m_s.LA())) {
				//-- This is just a number and cannot have operators. Parse and create equality test.
				T value = parseNumber(in);
				if(value == null)
					return false;
				return appendCriteria(crit, QOperation.EQ, value, null, null);
			}

			//-- We need to parse the input which can be numeric input with operators. We must have an operator now.
			QOperation op = scanOperation();

			//-- 2nd part MUST be numeric, so scan a value
			String v = scanNumeric();
			if(v == null || "".equals(v))
				throw new ValidationException(Msgs.BUNDLE, "ui.lookup.invalid");
			T value = parseNumber(v); // Convert to appropriate type,
			checkNumber(value);

			//-- Ok: is there a 2nd part?
			m_s.skipWs();
			if(m_s.eof()) {
				return appendCriteria(crit, op, value, null, null);
			}

			QOperation op2 = scanOperation();
			m_s.skipWs();
			if(m_s.eof())
				throw new ValidationException(Msgs.BUNDLE, "ui.lookup.invalid");

			//-- 2nd fragment of 2nd part MUST be numeric, so scan a value
			v = scanNumeric();
			if(v == null)
				throw new ValidationException(Msgs.BUNDLE, "ui.lookup.invalid");
			T value2 = parseNumber(v); // Convert to appropriate type,
			checkNumber(value2);

			//-- Now: construct the between proper
			if(((op == QOperation.GE || op == QOperation.GT) && (op2 == QOperation.LT || op2 == QOperation.LE))
				|| ((op2 == QOperation.GE || op2 == QOperation.GT) && (op == QOperation.LT || op == QOperation.LE))) {
				return appendCriteria(crit, op, value, op2, value2);
			} else
				throw new ValidationException(Msgs.BUNDLE, Msgs.UI_LOOKUP_BAD_OPERATOR_COMBI);
		} catch(UIException x) {
			m_input.setMessage(UIMessage.error(x));
			return false;
		}
	}

	private String scanNumeric() {
		m_s.skipWs();
		m_s.getStringResult(); // Clear old result
		for(;;) {
			int c = m_s.LA();
			if(c != '-' && c != '+' && c != 'E' && c != 'e' && c != ',' && c != '.' && c != 0x20ac && c != '$' && !Character.isDigit(c))
				break;
			m_s.copy();
		}
		return m_s.getStringResult();
	}


	/**
	 * Checks the current position for a supported operation. If OK the appropriate operation code is
	 * returned and the current pos is advanced after it.
	 * @return
	 */
	private QOperation scanOperation() {
		m_s.skipWs();
		if(m_s.eof())
			throw new IllegalStateException("eof at scanning operations");
		if(m_s.match(">="))
			return QOperation.GE;
		else if(m_s.match("<="))
			return QOperation.LE;
		else if(m_s.match("<>") || m_s.match("!=") || m_s.match("!"))
			return QOperation.NE;
		else if(m_s.match("<"))
			return QOperation.LT;
		else if(m_s.match(">"))
			return QOperation.GT;
		else {
			m_s.getStringResult(); // Clear content
			for(;;) {
				int c = m_s.LA();
				if(Character.isWhitespace(c) || Character.isDigit(c) || c == '-' || c == '.' || c == ',' || c == -1)
					break;
				m_s.copy();
			}
			throw new ValidationException(Msgs.V_INVALID_OPERATOR, m_s.getStringResult());
		}
	}

	/**
	 * This delivers a number of the same type as the property by scanning the input string. I use
	 * the monetary amount scanner to allow for max flexibility in input, and the resulting BigDecimal
	 * will be converted to the appropriate type afterwards.
	 * @param in
	 * @return
	 */
	private T parseNumber(String in) {
		BigDecimal bd = MoneyUtil.parseEuroToBigDecimal(in);
		if(m_valueType == BigDecimal.class)
			return (T) bd;
		if(DomUtil.isLongOrWrapper(m_valueType))
			return (T) Long.valueOf(bd.longValue());
		if(DomUtil.isIntegerOrWrapper(m_valueType))
			return (T) Integer.valueOf(bd.intValue());
		else if(DomUtil.isDoubleOrWrapper(m_valueType))
			return (T) Double.valueOf(bd.doubleValue());
		else if(DomUtil.isFloatOrWrapper(m_valueType))
			return (T) Float.valueOf(bd.floatValue());
		else
			throw new IllegalStateException("Unknown value type for control: " + m_valueType);
	}
}

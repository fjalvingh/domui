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
package to.etc.domui.component.searchpanel.lookupcontrols;

import to.etc.domui.component.input.Text2;
import to.etc.domui.converter.MiniScanner;
import to.etc.domui.converter.MoneyUtil;
import to.etc.domui.converter.NumericUtil;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;
import to.etc.webapp.query.QOperation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Number lookup control. This is a Text2 input box which allows the following:
 * <ul>
 *	<li>Entering just a number: look for the exact value of the number</li>
 *	<li>Entering operator number, like "&gt; 200", looks for that. Operators supported are: =, !=, &lt;&gt; &gt; &gt;=, &lt; &lt;=, !, </></li>
 *	<li>Two operators, two numbers to handle between, like "&gt; 12 &lt; 100 </li>
 *	<li>Just entering '*' means look for a nonnull value</li>
 *	<li>Entering just a ! means: look for a null only</li>
 *	<li>You can also search for numbers with like which will try to issue a like query with the number converted to a string using '%' as the like value.</li>
 * </ul>
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Feb 11, 2010
 */
public class NumberLookupControl<T extends Number> extends Div implements IControl<NumberLookupValue> {
	final private Text2<String> m_input = new Text2<>(String.class);

	private final int m_scale;

	private MiniScanner m_s;

	private Class<T> m_valueType;

	private Number m_minValue;

	private Number m_maxValue;

	private boolean m_monetary;

	private boolean m_allowLike;

	@Nullable
	private NumberLookupValue m_value;

	@Nullable
	private String m_lastString;

	//FIXME: check how other databases will match with this numbers range limits?
	//Oracle reference: The NUMBER data type is used to store zero, negative, positive, fixed, and floating point numbers with up to 38 digits of precision. Numbers range between 1.0x10 -126 and 1.0x10 126.
	//Max NUMBER data type limitation.
	private static final Double m_max_jdbc_column_value = Double.valueOf(1e126d);

	//Min NUMBER data type limitation.
	private static final Double m_min_jdbc_column_value = Double.valueOf(-1e126d);

	public NumberLookupControl(final Class<T> valueType, Number minValue, Number maxValue, boolean monetary, boolean allowLike, int scale) {
		m_valueType = valueType;
		m_minValue = minValue;
		m_maxValue = maxValue;
		m_monetary = monetary;
		m_allowLike = allowLike;
		m_scale = scale;
	}

	@Override public void createContent() throws Exception {
		add(m_input);
	}

	@Nullable @Override public NumberLookupValue getValue() {
		String string = m_input.getValue();
		if(null == string || string.trim().isEmpty())
			return null;
		if(Objects.equals(m_lastString, string))
			return m_value;

		m_value = decodeStringValue(string);
		m_lastString = string;
		return m_value;
	}

	@Override public void setValue(@Nullable NumberLookupValue value) {
		if(Objects.equals(m_value, value))
			return;
		m_value = value;
		renderValue(value);

	}

	private void renderValue(NumberLookupValue value) {
		if(null == value) {
			m_input.setValue(null);
			return;
		}
		StringBuilder sb = new StringBuilder();
		QOperation from = value.getFromOperation();
		if(from != null) {
			switch(from) {
				default:
					throw new IllegalStateException("Unsupported operation: " + from);
				case LE:
					sb.append("<=");
					break;
				case LT:
					sb.append("<");
					break;
				case GT:
					sb.append(">");
					break;
				case GE:
					sb.append(">=");
					break;
				case ISNOTNULL:
					m_input.setValue("*");
					return;
				case ISNULL:
					m_input.setValue("!");
					return;
			}
		}

		Number number = value.getFrom();
		sb.append(renderNumber(number));

		QOperation to = value.getToOperation();
		if(null != to) {
			sb.append(" ");
			switch(from) {
				default:
					throw new IllegalStateException("Unsupported operation: " + from);
				case LE:
					sb.append("<=");
					break;
				case LT:
					sb.append("<");
					break;
				case GT:
					sb.append(">");
					break;
				case GE:
					sb.append(">=");
					break;
			}
			number = value.getFrom();
			sb.append(renderNumber(number));
		}
		m_input.setValue(sb.toString());
	}

	private String renderNumber(Number number) {
		if(null == number)
			return "";

		return number.toString();
	}

	@Nullable
	private NumberLookupValue decodeStringValue(String in) {
		in = in.trim();

		//-- Handle single operators:
		if("!".equals(in)) {
			return new NumberLookupValue(QOperation.ISNULL, null);
		} else if("*".equals(in)) {
			return new NumberLookupValue(QOperation.ISNOTNULL, null);
		}

		if(m_allowLike) {
			in = in.replace("*", "%");
		}

		//-- We need to separate into [operator, value] pairs; there can be max. 2.
		m_s = new MiniScanner();
		m_s.init(in);
		m_s.skipWs();

		if(Character.isDigit(m_s.LA()) || m_s.LA() == '-' || m_s.LA() == '+' || m_s.LA() == '%') {
			//-- Does not start with operation: can only be number or a number with like
			String v = scanNumeric(true);
			if(v == null || "".equals(v))
				throw new ValidationException(Msgs.BUNDLE, Msgs.UI_LOOKUP_INVALID);
			if(v.contains("%") && m_allowLike) {
				m_s.skipWs();
				if(!m_s.eof()) 									// Must have eof
					throw new ValidationException(Msgs.BUNDLE, Msgs.UI_LOOKUP_INVALID);
				return new NumberLookupValue(v);
			}

			T value = parseNumber(v);
			if(value == null)
				throw new ValidationException(Msgs.BUNDLE, Msgs.UI_LOOKUP_INVALID);
			checkNumber(value);
			return new NumberLookupValue(QOperation.EQ, value);
		}

		//-- We need to parse the input which can be numeric input with operators. We must have an operator now.
		QOperation op = scanOperation();

		//-- 2nd part MUST be numeric, so scan a value
		String v = scanNumeric(false);
		if(v.isEmpty())
			throw new ValidationException(Msgs.BUNDLE, Msgs.UI_LOOKUP_INVALID);
		T value = parseNumber(v); 								// Convert to appropriate type,
		checkNumber(value);

		//-- Ok: is there a 2nd part?
		m_s.skipWs();
		if(m_s.eof()) {
			return new NumberLookupValue(op, value);
		}

		QOperation op2 = scanOperation();
		m_s.skipWs();
		if(m_s.eof())
			throw new ValidationException(Msgs.BUNDLE, Msgs.UI_LOOKUP_INVALID);

		//-- 2nd fragment of 2nd part MUST be numeric, so scan a value
		v = scanNumeric(false);
		T value2 = parseNumber(v); 								// Convert to appropriate type,
		checkNumber(value2);

		//-- Now: construct the between proper
		if(((op == QOperation.GE || op == QOperation.GT) && (op2 == QOperation.LT || op2 == QOperation.LE))
			|| ((op2 == QOperation.GE || op2 == QOperation.GT) && (op == QOperation.LT || op == QOperation.LE))) {
			return new NumberLookupValue(op, value, op2, value2);
		}
		throw new ValidationException(Msgs.BUNDLE, Msgs.UI_LOOKUP_BAD_OPERATOR_COMBI);
	}

	protected void checkNumber(T value) {
		if(value instanceof Double || value instanceof BigDecimal) { // FIXME BigDecimal is wrongly compared here (vmijic - what would be right compare ?)
			if(m_maxValue != null && value.doubleValue() > m_maxValue.doubleValue())
				throw new ValidationException(Msgs.BUNDLE, Msgs.V_TOOLARGE, m_maxValue);
			if(m_minValue != null && value.doubleValue() < m_minValue.doubleValue())
				throw new ValidationException(Msgs.BUNDLE, Msgs.V_TOOSMALL, m_minValue);

			// In case that other validations pass, we need to check for implicit JDBC parameter validation range (for Oracle it is 10^126 and -10^126)
			if(value.doubleValue() >= m_max_jdbc_column_value.doubleValue())
				throw new ValidationException(Msgs.BUNDLE, Msgs.V_TOOLARGE, m_max_jdbc_column_value);
			if(value.doubleValue() <= m_min_jdbc_column_value.doubleValue())
				throw new ValidationException(Msgs.BUNDLE, Msgs.V_TOOSMALL, m_min_jdbc_column_value);
		} else if(value instanceof Long || value instanceof Integer) {
			if(m_maxValue != null && value.longValue() > m_maxValue.longValue())
				throw new ValidationException(Msgs.BUNDLE, Msgs.V_TOOLARGE, m_maxValue);
			if(m_minValue != null && value.longValue() < m_minValue.longValue())
				throw new ValidationException(Msgs.BUNDLE, Msgs.V_TOOSMALL, m_minValue);
		} else
			throw new IllegalStateException("Unsupported value type: " + value.getClass());
	}

	@Nonnull
	private String scanNumeric(boolean allowpct) {
		m_s.skipWs();
		m_s.getStringResult(); // Clear old result
		for(;;) {
			int c = m_s.LA();
			if(c != '-' && c != '+' && c != 'E' && c != 'e' && c != ',' && c != '.' && c != 0x20ac && c != '$' && !Character.isDigit(c) && !(allowpct && c == '%'))
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
	protected QOperation scanOperation() {
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
	 * This delivers a number of the value type by scanning the input string.
	 */
	protected T parseNumber(String in) {
		try {
			if(isMonetary())
				return MoneyUtil.parseMoney(m_valueType, in);
			else {
				return NumericUtil.parseNumber(m_valueType, in, m_scale);
			}
		} catch(ValidationException vx) {
			/*
			 * Partial fix for bug 682:
			 * Oddity: if the value entered is too big for the target data type (like too big to fit in an int) the parse
			 * routine will throw a validation exception with toolarge or toosmall. But that value there will be maxint or
			 * minint (for integer) while this field can have other max/min constraints. To properly show the actual value
			 * constraint we need to check if a max/min exception was thrown and the replace the max/min value in that
			 * exception with the actual ones defined here. If not the user gets a different maximum value for large values
			 * than for smaller ones.
			 */
			if(vx.getCode().equals(Msgs.V_TOOLARGE)) {
				if(m_maxValue != null)
					throw new ValidationException(Msgs.V_TOOLARGE, m_maxValue);
			} else if(vx.getCode().equals(Msgs.V_TOOSMALL)) {
				if(m_minValue != null)
					throw new ValidationException(Msgs.V_TOOSMALL, m_minValue);
			}
			throw vx;
		}
	}

	/**
	 * T if this control handles a monetary amount.
	 */
	final public boolean isMonetary() {
		return m_monetary;
	}

	@Override public NumberLookupValue getValueSafe() {
		return getValue();
	}

	@Override public boolean isReadOnly() {
		return m_input.isReadOnly();
	}

	@Override public void setReadOnly(boolean ro) {
		m_input.setReadOnly(ro);
	}

	@Override public boolean isDisabled() {
		return m_input.isDisabled();
	}

	@Override public boolean isMandatory() {
		return m_input.isMandatory();
	}

	@Override public void setMandatory(boolean ro) {
		m_input.setMandatory(ro);
	}

	@Override public void setDisabled(boolean d) {
		m_input.setDisabled(d);
	}

	@Nullable @Override public NodeBase getForTarget() {
		return m_input;
	}

	@Override public IValueChanged<?> getOnValueChanged() {
		return m_input.getOnValueChanged();
	}

	@Override public void setOnValueChanged(IValueChanged<?> onValueChanged) {
		m_input.setOnValueChanged(onValueChanged);
	}

	public void setSize(int size) {
		m_input.setSize(size);
	}

}

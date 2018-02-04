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
package to.etc.domui.component.lookup;

import to.etc.domui.component.input.Text2;
import to.etc.domui.converter.MiniScanner;
import to.etc.domui.converter.MoneyUtil;
import to.etc.domui.converter.NumericUtil;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.trouble.UIException;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QLiteral;
import to.etc.webapp.query.QOperation;
import to.etc.webapp.query.QPropertyComparison;
import to.etc.webapp.query.QUnaryProperty;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Base utility class when lookups of custom numeric conditions should be implemented.
 * Provides buitin expression parser and range checks, only custom conditions have to be implemented in descendant classes.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Feb 11, 2010
 */
@Deprecated
public class LookupNumberControl<T extends Number> extends BaseAbstractLookupControlImpl<String> {
	static public final Set<QOperation> UNARY_OPS;

	static public final Set<QOperation> BINARY_OPS;

	final private String m_propertyName;

	final private Text2<String> m_input;

	private final int m_scale;

	private MiniScanner m_s;

	private Class<T> m_valueType;

	private Number m_minValue;

	private Number m_maxValue;

	private boolean m_monetary;

	private boolean m_allowLike;

	//FIXME: check how other databases will match with this numbers range limits?
	//Oracle reference: The NUMBER data type is used to store zero, negative, positive, fixed, and floating point numbers with up to 38 digits of precision. Numbers range between 1.0x10 -126 and 1.0x10 126.
	//Max NUMBER data type limitation.
	private static final Double m_max_jdbc_column_value = Double.valueOf(1e126d);

	//Min NUMBER data type limitation.
	private static final Double m_min_jdbc_column_value = Double.valueOf(-1e126d);

	static {
		UNARY_OPS = new HashSet<>();
		UNARY_OPS.add(QOperation.ISNOTNULL);
		UNARY_OPS.add(QOperation.ISNULL);

		BINARY_OPS = new HashSet<>();
		BINARY_OPS.add(QOperation.EQ);
		BINARY_OPS.add(QOperation.NE);
		BINARY_OPS.add(QOperation.LT);
		BINARY_OPS.add(QOperation.LE);
		BINARY_OPS.add(QOperation.GT);
		BINARY_OPS.add(QOperation.GE);
		BINARY_OPS.add(QOperation.LIKE);
		BINARY_OPS.add(QOperation.ILIKE);
	}

	public LookupNumberControl(final Class<T> valueType, Text2<String> node, String propertyName, Number minValue, Number maxValue, boolean monetary, int scale) {
		this(valueType, node, propertyName, minValue, maxValue, monetary, true, scale);
	}

	public LookupNumberControl(final Class<T> valueType, Text2<String> node, String propertyName, Number minValue, Number maxValue, boolean monetary, boolean allowLike, int scale) {
		super(node);
		m_input = node;
		m_valueType = valueType;
		m_propertyName = propertyName;
		m_minValue = minValue;
		m_maxValue = maxValue;
		m_monetary = monetary;
		m_allowLike = allowLike;
		m_scale = scale;
	}

	/**
	 * T if this control handles a monetary amount.
	 */
	final public boolean isMonetary() {
		return m_monetary;
	}

	protected AppendCriteriaResult appendCriteria(QCriteria< ? > crit, QOperation op, T val) throws Exception {
		if(UNARY_OPS.contains(op)) {
			if(val != null)
				throw new IllegalStateException("Unused value"+val+" for unary operation "+op);
			crit.add(new QUnaryProperty(op, m_propertyName));
		} else if(BINARY_OPS.contains(op)) {
			if(val == null)
				throw new IllegalStateException("Missing value for binary operation "+op);
			crit.add(new QPropertyComparison(op, m_propertyName, new QLiteral(val)));
		} else
			return AppendCriteriaResult.INVALID;
		return AppendCriteriaResult.VALID;
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

	@Override
	public @Nonnull AppendCriteriaResult appendCriteria(@Nonnull QCriteria< ? > crit) throws Exception {
		try {
			m_input.clearMessage(); // Remove any earlier validation failure.

			//-- Get value and bail out if it's empty.
			String in = m_input.getValue();
			if(in == null)
				return AppendCriteriaResult.EMPTY;
			in = in.trim();
			if(in.length() == 0)
				return AppendCriteriaResult.EMPTY;

			//-- Handle single operators: currently only the '!' to indicate 'not-null'
			if("!".equals(in)) {
				return appendCriteria(crit, QOperation.ISNULL, null);
			} else if("*".equals(in)) {
				return appendCriteria(crit, QOperation.ISNOTNULL, null);
			}

			if(m_allowLike) {
				in = in.replace("*", "%");
			}
			//-- We need to separate into [operator, value] pairs; there can be max. 2.
			m_s = new MiniScanner();
			m_s.init(in);
			m_s.skipWs();

			if(Character.isDigit(m_s.LA()) || m_s.LA() == '-' || m_s.LA() == '%') {
				//-- Does not start with operation: can only be number [%].
				String v = scanNumeric(true);
				if(v == null || "".equals(v))
					throw new ValidationException(Msgs.BUNDLE, "ui.lookup.invalid");
				if(v.contains("%") && m_allowLike) {
					m_s.skipWs();
					if(!m_s.eof()) // Must have eof
						throw new ValidationException(Msgs.BUNDLE, "ui.lookup.invalid");
					crit.add(new QPropertyComparison(QOperation.LIKE, m_propertyName, new QLiteral(v)));
					return AppendCriteriaResult.VALID;
				}

				T value = parseNumber(v);
				if(value == null)
					return AppendCriteriaResult.INVALID;
				checkNumber(value);
				return appendCriteria(crit, QOperation.EQ, value);
			}

			//-- We need to parse the input which can be numeric input with operators. We must have an operator now.
			QOperation op = scanOperation();

			//-- 2nd part MUST be numeric, so scan a value
			String v = scanNumeric(false);
			if(v == null || "".equals(v))
				throw new ValidationException(Msgs.BUNDLE, "ui.lookup.invalid");
			T value = parseNumber(v); // Convert to appropriate type,
			checkNumber(value);

			//-- Ok: is there a 2nd part?
			m_s.skipWs();
			if(m_s.eof()) {
				return appendCriteria(crit, op, value);
			}

			QOperation op2 = scanOperation();
			m_s.skipWs();
			if(m_s.eof())
				throw new ValidationException(Msgs.BUNDLE, "ui.lookup.invalid");

			//-- 2nd fragment of 2nd part MUST be numeric, so scan a value
			v = scanNumeric(false);
//			if(v == null)
//				throw new ValidationException(Msgs.BUNDLE, "ui.lookup.invalid");
			T value2 = parseNumber(v); // Convert to appropriate type,
			checkNumber(value2);

			//-- Now: construct the between proper
			if(((op == QOperation.GE || op == QOperation.GT) && (op2 == QOperation.LT || op2 == QOperation.LE))
				|| ((op2 == QOperation.GE || op2 == QOperation.GT) && (op == QOperation.LT || op == QOperation.LE))) {
				AppendCriteriaResult r1 = appendCriteria(crit, op, value);
				AppendCriteriaResult r2 = appendCriteria(crit, op2, value2); // Do not merge in return because || is short-circuit
				if(r1 == AppendCriteriaResult.VALID || r2 == AppendCriteriaResult.VALID) {
					return AppendCriteriaResult.VALID;
				} else if(r1 == AppendCriteriaResult.EMPTY || r2 == AppendCriteriaResult.EMPTY) {
					return AppendCriteriaResult.EMPTY;
				} else {
					return AppendCriteriaResult.INVALID;
				}
			} else
				throw new ValidationException(Msgs.BUNDLE, Msgs.UI_LOOKUP_BAD_OPERATOR_COMBI);
		} catch(UIException x) {
			m_input.setMessage(UIMessage.error(x));
			return AppendCriteriaResult.INVALID;
		}
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
	 * This delivers a number of the same type as the property by scanning the input string. I use
	 * the monetary amount scanner to allow for max flexibility in input, and the resulting BigDecimal
	 * will be converted to the appropriate type afterwards.
	 * @param in
	 * @return
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

	@Override
	public String getValue() {
		return m_input.getValue();
	}

	@Override
	public void setValue(String value) {
		m_input.setValue(value);
	}
}

package to.etc.domui.component.lookup;

import java.math.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.webapp.query.*;

/**
 * This is a factory for numeric entry of values, where the value entered can be preceded with some kind of
 * operator. If a value is entered verbatim it will be scanned as-is and used in an "equals" query. If the
 * value is preceded by either &gt;, &lt;, &gt;=, &lt;= the query will be done using the appropriate operator. In addition
 * the field can also contain '!' to indicate that the field MUST be empty (db null). Between or not-between
 * queries can be done by entering two operator-value pairs, like "&gt; 10 &lt; 100" (between [10..100&lt;)
 * or "&lt; 10 &gt; 100" (meaning NOT between [10..100&lt;).
 *
 * This control is the default numeric input control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 28, 2009
 */
public class LookupFactoryNumber2 implements ILookupControlFactory {
	/**
	 * We accept
	 * @see to.etc.domui.component.lookup.ILookupControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel)
	 */
	public int accepts(SearchPropertyMetaModel spm) {
		final PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);
		return DomUtil.isIntegerType(pmm.getActualType()) || DomUtil.isRealType(pmm.getActualType()) || pmm.getActualType() == BigDecimal.class ? 4 : -1;
	}

	/**
	 * Create the input control which is a text input.
	 *
	 * @see to.etc.domui.component.lookup.ILookupControlFactory#createControl(to.etc.domui.component.meta.SearchPropertyMetaModel, to.etc.domui.component.meta.PropertyMetaModel)
	 */
	public ILookupControlInstance createControl(final SearchPropertyMetaModel spm) {
		final PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);
		final Text<String> numText = new Text<String>(String.class);

		/*
		 * Calculate a "size=" for entering this number. We cannot assign a "maxlength" because not only the number but
		 * operators can be added to the string too. By default we size the field some 5 characters wider than the max size
		 * for the number as defined by scale and precision.
		 */
		if(pmm.getDisplayLength() > 0)
			numText.setSize(pmm.getDisplayLength() + 5);
		else if(pmm.getPrecision() > 0) {
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
			numText.setSize(size + 5);
		} else if(pmm.getLength() > 0) {
			numText.setSize(pmm.getLength() < 40 ? pmm.getLength() + 5 : 40);
		}
		String s = pmm.getDefaultHint();
		if(s != null)
			numText.setTitle(s);
		return new NumberInputImpl(spm.getPropertyName(), pmm, numText);
	}

	private class NumberInputImpl extends AbstractLookupControlImpl {
		final private Text<String> m_input;

		final private PropertyMetaModel m_pmm;

		private MiniScanner m_s;

		private String m_propertyName;

		public NumberInputImpl(String propname, PropertyMetaModel pmm, Text<String> node) {
			super(node);
			m_propertyName = propname;
			m_input = node;
			m_pmm = pmm;
		}

		private double calcMaxValue(PropertyMetaModel pmm) {
			int prec = pmm.getPrecision();
			if(prec > 0) {
				int scale = pmm.getScale();
				if(scale > 0 && scale < prec)
					prec -= scale;
				double val = Math.pow(10, prec);
				return val;
			}
			return Double.MAX_VALUE;
		}

		private void checkNumber(PropertyMetaModel pmm, Object value) {
			double max = calcMaxValue(pmm);
			if(value instanceof Double) {
				if(((Double) value).doubleValue() > max || ((Double) value).doubleValue() < -max)
					throw new ValidationException(Msgs.BUNDLE, Msgs.V_OUT_OF_RANGE, value);
			} else if(value instanceof BigDecimal) {
				if(((BigDecimal) value).doubleValue() > max || ((BigDecimal) value).doubleValue() < -max)
					throw new ValidationException(Msgs.BUNDLE, Msgs.V_OUT_OF_RANGE, value);
			} else if(value instanceof Long) {
				Long v = (Long) value;
				if(v.doubleValue() > max || v.doubleValue() < -max)
					throw new ValidationException(Msgs.BUNDLE, Msgs.V_OUT_OF_RANGE, value);
			} else if(value instanceof Integer) {
				Integer v = (Integer) value;
				if(v.doubleValue() > max || v.doubleValue() < -max)
					throw new ValidationException(Msgs.BUNDLE, Msgs.V_OUT_OF_RANGE, value);
			} else
				throw new IllegalStateException("Unsupported value type: " + value.getClass());
		}

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
					crit.isnull(m_propertyName);
					return true;
				} else if("*".equals(in)) {
					crit.isnotnull(m_propertyName);
					return true;
				}

				//-- We need to separate into [operator, value] pairs; there can be max. 2.
				m_s = new MiniScanner();
				m_s.init(m_input.getValue());
				m_s.skipWs();

				if(Character.isDigit(m_s.LA())) {
					//-- This is just a number and cannot have operators. Parse and create equality test.
					Object value = parseNumber(in);
					if(value == null)
						return false;
					checkNumber(m_pmm, value);
					crit.eq(m_propertyName, value);
					return true;
				}

				//-- We need to parse the input which can be numeric input with operators. We must have an operator now.
				QOperation op = scanOperation();

				//-- 2nd part MUST be numeric, so scan a value
				String v = scanNumeric();
				if(v == null)
					throw new ValidationException(Msgs.BUNDLE, "ui.lookup.invalid");
				Object value = parseNumber(v); // Convert to appropriate type,
				checkNumber(m_pmm, value);

				//-- Ok: is there a 2nd part?
				m_s.skipWs();
				if(m_s.eof()) {
					//-- Just add the appropriate operation.
					QPropertyComparison r = new QPropertyComparison(op, m_propertyName, new QLiteral(value));
					crit.add(r);
					return true;
				}

				QOperation op2 = scanOperation();
				m_s.skipWs();
				if(m_s.eof())
					throw new ValidationException(Msgs.BUNDLE, "ui.lookup.invalid");

				//-- 2nd fragment of 2nd part MUST be numeric, so scan a value
				v = scanNumeric();
				if(v == null)
					throw new ValidationException(Msgs.BUNDLE, "ui.lookup.invalid");
				Object value2 = parseNumber(v); // Convert to appropriate type,
				checkNumber(m_pmm, value2);

				//-- Now: construct the between proper
				if((op == QOperation.GE || op == QOperation.GT) && (op2 == QOperation.LT || op2 == QOperation.LE)) {
					crit.add(new QPropertyComparison(op, m_propertyName, new QLiteral(value)));
					crit.add(new QPropertyComparison(op2, m_propertyName, new QLiteral(value2)));
				} else if((op2 == QOperation.GE || op2 == QOperation.GT) && (op == QOperation.LT || op == QOperation.LE)) {
					crit.add(new QPropertyComparison(op, m_propertyName, new QLiteral(value)));
					crit.add(new QPropertyComparison(op2, m_propertyName, new QLiteral(value2)));
				} else
					throw new ValidationException(Msgs.BUNDLE, Msgs.UI_LOOKUP_BAD_OPERATOR_COMBI);

				return true;
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
		private Object parseNumber(String in) {
			BigDecimal bd = MoneyUtil.parseEuroToBigDecimal(in);
			Class< ? > icl = m_pmm.getActualType();
			if(icl == BigDecimal.class)
				return bd;
			if(DomUtil.isLongOrWrapper(icl))
				return Long.valueOf(bd.longValue());
			if(DomUtil.isIntegerOrWrapper(icl))
				return Integer.valueOf(bd.intValue());
			else if(DomUtil.isDoubleOrWrapper(icl))
				return Double.valueOf(bd.doubleValue());
			else if(DomUtil.isFloatOrWrapper(icl))
				return Float.valueOf(bd.floatValue());
			else
				throw new IllegalStateException("Unknown value type for control: " + icl);
		}
	}
}

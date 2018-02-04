package to.etc.domui.component.searchpanel.lookupcontrols;

import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QLiteral;
import to.etc.webapp.query.QOperation;
import to.etc.webapp.query.QPropertyComparison;
import to.etc.webapp.query.QUnaryProperty;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-12-17.
 */
public class NumberLookupQueryBuilder implements ILookupQueryBuilder<NumberLookupValue> {
	static private final Set<QOperation> UNARY_OPS;

	static private final Set<QOperation> BINARY_OPS;

	private final String m_propertyName;

	public NumberLookupQueryBuilder(String propertyName) {
		m_propertyName = propertyName;
	}
	@Override public <T> LookupQueryBuilderResult appendCriteria(QCriteria<T> criteria, @Nullable NumberLookupValue lookupValue) {
		if(null == lookupValue)
			return LookupQueryBuilderResult.EMPTY;
		appendCriteria(criteria, lookupValue.getFromOperation(), lookupValue.getFrom());
		QOperation op = lookupValue.getToOperation();
		if(null != op)
			appendCriteria(criteria, op, lookupValue.getTo());
		return LookupQueryBuilderResult.VALID;
	}

	private <V> LookupQueryBuilderResult appendCriteria(QCriteria<?> crit, QOperation op, V val) {
		if(UNARY_OPS.contains(op)) {
			if(val != null)
				throw new IllegalStateException("Unused value" + val + " for unary operation " + op);
			crit.add(new QUnaryProperty(op, m_propertyName));
		} else if(BINARY_OPS.contains(op)) {
			if(val == null)
				throw new IllegalStateException("Missing value for binary operation " + op);
			crit.add(new QPropertyComparison(op, m_propertyName, new QLiteral(val)));
		} else
			return LookupQueryBuilderResult.INVALID;
		return LookupQueryBuilderResult.VALID;
	}

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
}

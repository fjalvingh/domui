package to.etc.domui.component.searchpanel.lookupcontrols;

import to.etc.webapp.query.QCriteria;

import java.util.Date;

/**
 * Given a {@link DatePeriod}, this adds the parts to the query that represent
 * that period.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-12-17.
 */
public class DateLookupQueryBuilder implements ILookupQueryBuilder<DatePeriod> {
	private final String m_propertyName;

	public DateLookupQueryBuilder(String propertyName) {
		m_propertyName = propertyName;
	}

	@Override public <T> LookupQueryBuilderResult appendCriteria(QCriteria<T> criteria, DatePeriod lookupValue) {
		if(null == lookupValue)
			return LookupQueryBuilderResult.EMPTY;

		Date from = lookupValue.getFrom();
		Date till = lookupValue.getTo();

		if(from == null && till == null)
			return LookupQueryBuilderResult.EMPTY;
		if(from != null && till != null) {
			if(from.getTime() > till.getTime()) {
				Date tmp = from;
				from = till;
				till = tmp;
			}

			//-- Between query
			criteria.ge(m_propertyName, from);
			criteria.lt(m_propertyName, till);
		} else if(from != null) {
			criteria.ge(m_propertyName, from);
		} else if(till != null) {
			criteria.lt(m_propertyName, till);
		} else
			throw new IllegalStateException("Logic error");
		return LookupQueryBuilderResult.VALID;
	}
}

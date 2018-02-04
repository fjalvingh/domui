package to.etc.domui.component.searchpanel.lookupcontrols;

import java.util.Date;
import java.util.Objects;

/**
 * A search pair for dates inside a given period.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-12-17.
 */
final public class DatePeriod {
	/** Inclusive from boundary */
	private final Date m_from;

	/** Exclusive outer boundary */
	private final Date m_to;

	public DatePeriod(Date from, Date to) {
		m_from = from;
		m_to = to;
	}

	public Date getFrom() {
		return m_from;
	}

	public Date getTo() {
		return m_to;
	}

	@Override public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;
		DatePeriod that = (DatePeriod) o;
		return Objects.equals(m_from, that.m_from) &&
			Objects.equals(m_to, that.m_to);
	}

	@Override public int hashCode() {
		return Objects.hash(m_from, m_to);
	}
}

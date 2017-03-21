package to.etc.domui.component.lookup.filter;

import java.util.*;

import javax.annotation.*;

/**
 * This is a simple class that only holds two date values
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/12/16.
 */
public final class DateFromTo {

	@Nullable
	private final Date m_dateFrom;

	@Nullable
	private final Date m_dateTo;

	public DateFromTo(@Nullable Date dateFrom, @Nullable Date dateTo) {
		m_dateFrom = dateFrom;
		m_dateTo = dateTo;
	}

	@Nullable
	public Date getDateFrom() {
		return m_dateFrom;
	}

	@Nullable
	public Date getDateTo() {
		return m_dateTo;
	}
}

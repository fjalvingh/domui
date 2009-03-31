package to.etc.domui.component.agenda;

import java.util.Date;
import java.util.Locale;

/**
 * A single holiday
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 27, 2007
 */
public interface ScheduleHoliday {
	/** The day the holiday occurs on */
	public Date		getDate();

	/** A short name for the holyday */
	public String	getHolidayName(Locale loc);

	/** An URL to an image describing this holiday */
	public String	getImageURL();
}

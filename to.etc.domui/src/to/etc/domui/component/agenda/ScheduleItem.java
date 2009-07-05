package to.etc.domui.component.agenda;

import java.util.*;

/**
 * A single "appointment" in the schedule interface.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 27, 2007
 */
public interface ScheduleItem {
	/** An unique identifier that can be used to locate this appointment. */
	public String getID();

	/** The start of the appointment. */
	public Date getStart();

	/** The end of the appointment. */
	public Date getEnd();

	/** The short text describing this appointment (mandatory) */
	public String getName();

	/** A short string describing the appointment type */
	public String getType();

	public String getImageURL();

	public String getDetails();
}

package to.etc.domui.component.agenda;

import java.util.Date;

public class BasicScheduleWorkHour implements ScheduleWorkHour {
	private Date		m_start;
	private Date		m_end;
	
	public BasicScheduleWorkHour(Date start, Date end) {
		m_start = start;
		m_end = end;
	}
	public Date getEnd() {
		return m_end;
	}
	public Date getStart() {
		return m_start;
	}
}

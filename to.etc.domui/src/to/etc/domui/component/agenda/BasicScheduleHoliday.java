package to.etc.domui.component.agenda;

import java.util.Date;
import java.util.Locale;

public class BasicScheduleHoliday implements ScheduleHoliday {
	private Date		m_date;
	private String		m_name;
	private String		m_imageURL;

	
	public BasicScheduleHoliday(Date date, String name, String imageURL) {
		m_date = date;
		m_name = name;
		m_imageURL = imageURL;
	}
	public Date getDate() {
		return m_date;
	}
	public String getHolidayName(Locale loc) {
		return m_name;
	}
	public String getImageURL() {
		return m_imageURL;
	}
}

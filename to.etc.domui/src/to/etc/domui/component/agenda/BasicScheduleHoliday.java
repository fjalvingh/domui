package to.etc.domui.component.agenda;

import java.util.*;

public class BasicScheduleHoliday implements ScheduleHoliday {
	private Date m_date;

	private String m_name;

	private String m_imageURL;


	public BasicScheduleHoliday(Date date, String name, String imageURL) {
		m_date = date;
		m_name = name;
		m_imageURL = imageURL;
	}

	@Override
	public Date getDate() {
		return m_date;
	}

	@Override
	public String getHolidayName(Locale loc) {
		return m_name;
	}

	@Override
	public String getImageURL() {
		return m_imageURL;
	}
}

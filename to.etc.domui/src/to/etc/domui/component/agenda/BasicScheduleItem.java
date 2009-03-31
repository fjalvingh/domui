package to.etc.domui.component.agenda;

import java.util.Date;

public class BasicScheduleItem implements ScheduleItem {
	private String		m_details;
	private String		m_imageURL;
	private String		m_name;
	private Date		m_start;
	private Date		m_end;
	private String		m_id;
	private String		m_type;

	public BasicScheduleItem(String id, Date start, Date end, String name, String details, String type, String imageURL) {
		m_id = id;
		m_start = start;
		m_end = end;
		m_name = name;
		m_details = details;
		m_type = type;
		m_imageURL = imageURL;
	}

	public String getDetails() {
		return m_details;
	}

	public Date getEnd() {
		return m_end;
	}

	public String getID() {
		return m_id;
	}

	public String getImageURL() {
		return m_imageURL;
	}

	public String getName() {
		return m_name;
	}

	public Date getStart() {
		return m_start;
	}

	public String getType() {
		return m_type;
	}
}

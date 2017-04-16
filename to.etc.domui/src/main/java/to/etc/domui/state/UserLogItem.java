package to.etc.domui.state;

import javax.annotation.*;

import to.etc.util.*;

/**
 * Log item for action logging inside a user's session.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 26, 2014
 */
public class UserLogItem {
	/** The CID (session and conversation) this took place on */
	private String m_cid;

	private long m_timestamp;

	private String m_page;

	/** If applicable, the action from the request */
	private String m_action;

	/** If applicable, the component description */
	private String m_component;

	private String m_text;

	public UserLogItem(String cid, String page, String action, String component, String text) {
		m_cid = cid;
		m_timestamp = System.currentTimeMillis();
		m_action = action;
		m_component = component;
		m_text = text;
		m_page = page;
	}

	public String getPage() {
		return m_page;
	}

	public String getCid() {
		return m_cid;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	@Nonnull
	public String getRelaTime(long currt) {
		return StringTool.strDurationMillis(currt - m_timestamp);
	}

	public String time() {
		return getRelaTime(System.currentTimeMillis());
	}

	public String getAction() {
		return m_action;
	}

	public String getComponent() {
		return m_component;
	}

	public String getText() {
		return m_text;
	}
}

package to.etc.domui.component.plotly.traces;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
final public class SunburstValue {
	private final String m_id;

	private final String m_label;

	private final double m_value;

	private final String m_parentId;

	public SunburstValue(String id, String label, double value, String parentId) {
		m_id = id;
		m_label = label;
		m_value = value;
		m_parentId = parentId;
	}

	public String getId() {
		return m_id;
	}

	public String getLabel() {
		return m_label;
	}

	public double getValue() {
		return m_value;
	}

	public String getParentId() {
		return m_parentId;
	}
}

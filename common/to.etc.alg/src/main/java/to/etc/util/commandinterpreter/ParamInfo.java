package to.etc.util.commandinterpreter;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-02-22.
 */
final class ParamInfo {
	private final String m_name;

	private final Class<?> m_type;

	private final int m_index;

	private boolean m_used;

	public ParamInfo(String name, Class<?> type, int index) {
		m_name = name;
		m_type = type;
		m_index = index;
	}

	public String getName() {
		return m_name;
	}

	public Class<?> getType() {
		return m_type;
	}

	public int getIndex() {
		return m_index;
	}

	public boolean isUsed() {
		return m_used;
	}

	public void setUsed(boolean used) {
		m_used = used;
	}
}

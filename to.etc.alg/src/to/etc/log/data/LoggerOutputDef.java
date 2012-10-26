package to.etc.log.data;

public class LoggerOutputDef {
	private String	m_key;

	private String	m_output;

	public LoggerOutputDef(String key, String output) {
		super();
		m_key = key;
		m_output = output;
	}

	public String getKey() {
		return m_key;
	}

	public void setKey(String key) {
		m_key = key;
	}

	public String getOutput() {
		return m_output;
	}

	public void setOutput(String output) {
		m_output = output;
	}

}

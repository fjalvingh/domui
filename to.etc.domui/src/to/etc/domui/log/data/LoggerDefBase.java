package to.etc.domui.log.data;

import to.etc.domui.component.meta.*;

public class LoggerDefBase {
	private String m_key;

	public static final String pKEY = "key";

	public LoggerDefBase(String key) {
		super();
		m_key = key;
	}

	@MetaProperty(length = 255, required = YesNoType.YES)
	public String getKey() {
		return m_key;
	}

	public void setKey(String key) {
		m_key = key;
	}
}

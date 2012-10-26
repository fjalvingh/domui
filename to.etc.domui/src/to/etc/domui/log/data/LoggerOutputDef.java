package to.etc.domui.log.data;

import to.etc.domui.component.meta.*;

public class LoggerOutputDef extends LoggerDefBase {
	private String m_output;

	public static final String pOUTPUT = "output";

	public LoggerOutputDef(String key, String output) {
		super(key);
		m_output = output;
	}

	@MetaProperty(length = 25, required = YesNoType.YES)
	public String getOutput() {
		return m_output;
	}

	public void setOutput(String output) {
		m_output = output;
	}

}

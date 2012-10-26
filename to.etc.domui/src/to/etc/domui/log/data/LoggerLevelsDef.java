package to.etc.domui.log.data;

import to.etc.domui.component.meta.*;
import to.etc.log.*;

public class LoggerLevelsDef extends LoggerDefBase {
	private MyLogger.Level m_level;

	public static final String pLEVEL = "level";

	public LoggerLevelsDef(String key, MyLogger.Level level) {
		super(key);
		m_level = level;
	}

	@MetaProperty(required = YesNoType.YES)
	public MyLogger.Level getLevel() {
		return m_level;
	}

	public void setLevel(MyLogger.Level level) {
		m_level = level;
	}

}

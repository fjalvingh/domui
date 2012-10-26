package to.etc.domui.log.data;

import to.etc.domui.component.meta.*;
import to.etc.log.MyLogger.Level;
import to.etc.log.MyLogger.*;

public class LoggerRootDef {
	/**
	 * Root dir for cofig.
	 */
	private String	m_rootDir;

	public static final String pROOTDIR = "rootDir";

	/**
	 * Root dir for all 'per logger' log files created.
	 */
	private String	m_logDir;

	public static final String pLOGDIR = "logDir";

	/**
	 * Default logger level.
	 */
	private Level	m_level;

	public static final String pLEVEL = "level";

	/**
	 * Questionable if needed? Disables all logging!?.
	 */
	private boolean	m_disabled	= false;

	public static final String pDISABLED = "disabled";

	public LoggerRootDef(Level level, boolean disabled, String rootDir, String logDir) {
		super();
		m_level = level;
		m_disabled = disabled;
		m_rootDir = rootDir;
		m_logDir = logDir;
	}

	@MetaProperty(length = 255, required = YesNoType.YES, readOnly = YesNoType.YES)
	public String getRootDir() {
		return m_rootDir;
	}

	public void setRootDir(String rootDir) {
		m_rootDir = rootDir;
	}

	@MetaProperty(length = 255, required = YesNoType.YES, readOnly = YesNoType.YES)
	public String getLogDir() {
		return m_logDir;
	}

	public void setLogDir(String logDir) {
		m_logDir = logDir;
	}

	@MetaProperty(required = YesNoType.YES)
	public Level getLevel() {
		return m_level;
	}

	public void setLevel(Level level) {
		m_level = level;
	}

	@MetaProperty(required = YesNoType.YES)
	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(boolean disabled) {
		m_disabled = disabled;
	}

}

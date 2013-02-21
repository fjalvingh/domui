package to.etc.domui.log.data;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

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
	 * Root dir for all 'per logger' log files created.
	 */
	private String m_logDirAbsolute;

	public static final String pLOGDIRABSOLUTE = "logDirAbsolute";

	public LoggerRootDef(@Nonnull String rootDir, @Nonnull String logDir, @Nonnull String logDirAbsolute) {
		super();
		m_rootDir = rootDir;
		m_logDir = logDir;
		m_logDirAbsolute = logDirAbsolute;
	}

	@MetaProperty(length = 255, required = YesNoType.YES, readOnly = YesNoType.YES)
	public String getRootDir() {
		return m_rootDir;
	}

	public void setRootDir(String rootDir) {
		m_rootDir = rootDir;
	}

	@MetaProperty(length = 255, required = YesNoType.YES)
	public String getLogDir() {
		return m_logDir;
	}

	public void setLogDir(String logDir) {
		m_logDir = logDir;
	}

	@MetaProperty(length = 255, required = YesNoType.YES, readOnly = YesNoType.YES)
	public String getLogDirAbsolute() {
		return m_logDirAbsolute;
	}

	public void setLogDirAbsolute(String logDirAbsolute) {
		m_logDirAbsolute = logDirAbsolute;
	}
}

package to.etc.server.janitor;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:
 * @author
 * @version 1.0
 */

public class JanitorException extends Exception {
	/// The janitor that threw this
	protected Janitor		m_j;

	/// The task that threw this
	protected JanitorTask	m_jt;

	public JanitorException() {
		super("?? Unknown janitor error");
	}

	public JanitorException(JanitorTask jt, String msg) {
		super(msg);
		m_j = jt.m_j;
		m_jt = jt;
	}

}

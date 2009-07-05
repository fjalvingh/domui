package to.etc.iocular.container;

import java.io.*;

import to.etc.util.*;

/**
 * A single alternative in a failure that was tried.
 *
 * @author jal
 * Created on Mar 28, 2007
 */
public class FailedAlternative {
	private String m_failureString;

	public FailedAlternative(String failureString) {
		m_failureString = failureString;
	}

	public String getFailureString() {
		return m_failureString;
	}

	public void dump(IndentWriter iw) throws IOException {
		iw.println(m_failureString);
	}
}

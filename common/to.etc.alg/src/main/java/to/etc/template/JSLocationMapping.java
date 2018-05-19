package to.etc.template;

final public class JSLocationMapping {
	private int	m_sline, m_scol, m_tline, m_tcol;

	public JSLocationMapping(int tline, int tcol, int sline, int scol) {
		m_tline = tline;
		m_tcol = tcol;
		m_sline = sline;
		m_scol = scol;
	}

	public int getSline() {
		return m_sline;
	}

	public int getScol() {
		return m_scol;
	}

	public int getTline() {
		return m_tline;
	}

	public int getTcol() {
		return m_tcol;
	}
}

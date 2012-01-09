package to.etc.server.syslogger;

public class LogItemResultPage extends InfoResultPage {
	private LogMessage[]	m_m_ar;

	public LogItemResultPage(int count, int npp, int se, int ee, LogMessage[] ar) {
		m_count = count;
		m_ix_first = se;
		m_n_onpage = ee - se;
		m_m_ar = ar;
		m_n_perpage = npp;
	}

	public LogMessage iteratorTypeHint() {
		return null;
	}

	/// Called by the enumerator to retrieve an instance.
	@Override
	public Object getItem(int rix) {
		return m_m_ar[rix];
	}


}

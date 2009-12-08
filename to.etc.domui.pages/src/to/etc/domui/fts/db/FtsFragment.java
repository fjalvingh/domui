package to.etc.domui.fts.db;

public class FtsFragment {
	private long			m_id;
	private String			m_word;

	FtsFragment(long id, String word) {
		m_id = id;
		m_word = word;
	}
	public long getId() {
		return m_id;
	}
	public String getName() {
		return m_word;
	}
}

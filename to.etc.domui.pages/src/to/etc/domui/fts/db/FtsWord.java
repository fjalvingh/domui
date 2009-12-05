package to.etc.domui.fts.db;

public class FtsWord {
	private long			m_id;
	private String			m_word;

	FtsWord(long id, String word) {
		m_id = id;
		m_word = word;
	}
	public long getId() {
		return m_id;
	}
	public String getWord() {
		return m_word;
	}
}

package to.etc.domui.fts;

/**
 * A reusable token which is fast.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 11, 2008
 */
public class FtsToken implements CharSequence {
	private CharBuf			m_content = new CharBuf();
	private int				m_textOffset;
	private int				m_tokenNumber;
	private int				m_sentenceNumber;

	void	init(CharBuf b, int textoffset, int sentence, int tokennr) {
		m_content = b;
		m_textOffset = textoffset;
		m_tokenNumber = tokennr;
		m_sentenceNumber = sentence;
	}
	void	init(int textoffset, int sentence, int tokennr) {
		m_textOffset = textoffset;
		m_tokenNumber = tokennr;
		m_sentenceNumber = sentence;
	}
	public char charAt(int index) {
		return m_content.charAt(index);
	}
	
	public int length() {
		return m_content.length();
	}
	public CharBuf getContent() {
		return m_content;
	}
	public CharSequence subSequence(int start, int end) {
		throw new IllegalStateException("Not implemented");
	}
	public int getTextOffset() {
		return m_textOffset;
	}
	public int getTokenNumber() {
		return m_tokenNumber;
	}
	public int getSentenceNumber() {
		return m_sentenceNumber;
	}
	public void setTokenNumber(int tokenNumber) {
		m_tokenNumber = tokenNumber;
	}
	public void setSentenceNumber(int sentenceNumber) {
		m_sentenceNumber = sentenceNumber;
	}
	@Override
	public String toString() {
		return m_content.toString();
	}
}

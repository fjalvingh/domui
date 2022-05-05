package to.etc.syntaxer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.IExecute;

/**
 * Split a line into tokens using general (and configurable) rules
 * for a language.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-05-22.
 */
@NonNullByDefault
abstract public class HiParser {
	private String m_line = "";

	private int m_ix;

	private int m_len;

	/** The current token being built. */
	private StringBuilder m_sb = new StringBuilder();

	private IExecute m_state = this::sWhitespace;

	private boolean m_eoln;

	@Nullable
	private String m_tokenEnd;

	private HighlightTokenType m_lastType = HighlightTokenType.whitespace;

	private String m_lastToken = "";

	abstract protected void tokenFound(HighlightTokenType type, CharSequence text);

	public LineContext start(String line, @Nullable LineContext startContext) throws Exception {
		m_line = line;
		m_ix = 0;
		m_len = line.length();
		m_sb.setLength(0);
		m_eoln = false;
		if(null != startContext) {
			m_state = startContext.getLexerState();
		} else {
			m_state = this::sWhitespace;
		}
		m_lastType = HighlightTokenType.whitespace;
		m_lastToken = "";
		while(la() != -1) {
			m_state.execute();
		}



		flush(HighlightTokenType.newline);
		return new LineContext(m_state);
	}

	/**
	 * If the current char is whitespace then append, else move to the next state.
	 */
	protected void sWhitespace() {
		int c = la();
		if(Character.isWhitespace(c)) {
			copy();
			return;
		}

		//-- We have finished a whitespace run -> flush
		flush(HighlightTokenType.whitespace);
		calculatePhaseFor();
	}

	protected void sIdentifier() {
		int c = la();
		if(m_sb.length() == 0) {
			if(isIdentifierStart(c)) {
				copy();
				return;
			}
		} else {
			if(isIdentifierNext(c)) {
				copy();
				return;
			}
		}

		//-- No longer an identifier ->
		flush(HighlightTokenType.id);
		calculatePhaseFor();
	}

	protected void calculatePhaseFor() {
		if(isWsStart())
			return;
		if(isIdentifierStart())
			return;
		if(la() == '\n') {
			flush(HighlightTokenType.newline);
			accept();
			m_state = this::sWhitespace;
			return;
		}
		if(isStringStart())
			return;
		if(isNumberStart())
			return;
		if(isComment())
			return;

		//-- Must be punctuation
		m_state = this::sPunctuation;
		copy();
	}

	protected void sPunctuation() {
		int c = la();



	}

	private boolean isComment() {
		if(is("/*")) {
			m_state = this::sComment;
			m_tokenEnd = "*/";
			return true;
		}

		if(is("//")) {
			//-- Just copy everything
			lineComment();
			return true;
		}
		return false;
	}

	protected void lineComment() {
		m_sb.append(m_line.substring(m_ix));
		m_ix = m_len;
		flush(HighlightTokenType.comment);
		calculatePhaseFor();
	}

	protected void sComment() {
		if(isTokenEnd()) {
			flush(HighlightTokenType.comment);
			calculatePhaseFor();
			return;
		}
		copy();
	}

	protected boolean isNumberStart() {
		int c = la();
		if(Character.isDigit(c)) {
			if(isHexNumber() || isOctalNumber() || isBinaryNumber() || isDecimalNumber()) {
				return true;
			}
			return true;
		}
		return false;
	}

	protected boolean isHexNumber() {
		if(la() == '0') {
			if(Character.toUpperCase(la(1)) == 'X') {
				m_state = this::sHexNumber;
				copy(2);
				return true;
			}
		}
		return false;
	}

	protected boolean isBinaryNumber() {
		if(la() == '0') {
			if(Character.toUpperCase(la(1)) == 'B') {
				m_state = this::sBinaryNumber;
				copy(2);
				return true;
			}
		}
		return false;
	}

	protected boolean isOctalNumber() {
		if(la() == '0') {
			m_state = this::sOctalNumber;
			copy(1);
			return true;
		}
		return false;
	}

	protected boolean isDecimalNumber() {
		m_state = this::sDecimalNumber;
		copy();
		return true;
	}

	protected void sHexNumber() {
		int c = la();
		if(Character.isDigit(c) || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f') || c == '_') {
			copy();
		} else {
			flush(HighlightTokenType.number);
			calculatePhaseFor();
		}
	}

	protected void sOctalNumber() {
		int c = la();
		if(Character.isDigit(c) || c == '_') {
			copy();
		} else {
			flush(HighlightTokenType.number);
			calculatePhaseFor();
		}
	}

	protected void sBinaryNumber() {
		int c = la();
		if(Character.isDigit(c) || c == '_') {
			copy();
		} else {
			flush(HighlightTokenType.number);
			calculatePhaseFor();
		}
	}

	/**
	 * Scans decimals
	 */
	protected void sDecimalNumber() {
		int c = la();
		if(Character.isDigit(c) || c == '_') {
			copy();
			return;
		}

		if(c == '.') {
			copy();
			return;
		}
		if(c == 'e' || c == 'E') {
			copy();
			return;
		}
		if(c == '+' || c == '-') {
			int lc = getLastCopied();
			if(lc == 'e' || lc == 'E') {
				copy();
				return;
			}
		}
		if(c == 'L' || c == 'l' || c == 'D' || c == 'd') {
			copy();
			return;
		}

		//-- End of number
		flush(HighlightTokenType.number);
		calculatePhaseFor();
	}

	protected boolean isStringStart() {
		int c = la();
		if(c == '\"' || c == '\'') {
			copy();
			m_state = this::sString;
			m_tokenEnd = "\"";
			return true;
		}
		return false;
	}

	protected void sString() {
		if(isStringEscape()) {
			return;
		}
		if(isTokenEnd()) {
			flush(HighlightTokenType.string);
			calculatePhaseFor();
		} else {
			copy();
		}
	}

	/**
	 * Checks whether the current position contains the tokenEnd value. If so
	 * it copies the tokenEnd value and returns true, else it returns false.
	 */
	protected boolean isTokenEnd() {
		String te = m_tokenEnd;
		if(null == te)
			throw new IllegalStateException("Token end is not set");
		for(int i = 0; i < te.length(); i++) {
			if(la(i) != te.charAt(i)) {
				return false;
			}
		}

		//-- Token end found
		copy(te.length());
		return true;
	}

	protected boolean is(String te) {
		for(int i = 0; i < te.length(); i++) {
			if(la(i) != te.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * If the current char (and possibly following chars) represent an escape sequence then
	 * report that and skip over it, else return false.
	 */
	protected boolean isStringEscape() {
		if(la() != '\\')
			return false;
		if(remainder() < 1) {			// Have at least \x?
			return false;
		}
		int c = la(1);
		if(c == 'u' || c == 'U') {
			//-- would be  backslash uxxxx;
			if(remainder() >= 6) {
				flush(HighlightTokenType.string);			// Flush part so far
				copy(6);								// Copy bs uxxxx
				flush(HighlightTokenType.stringEscape);		// And send that
				return true;
			}
			return false;
		} else {
			flush(HighlightTokenType.string);			// Flush part so far
			copy(2);								// Copy \c
			flush(HighlightTokenType.stringEscape);		// And send that
			return true;
		}
	}

	protected boolean isWsStart() {
		if(Character.isWhitespace(la())) {
			m_state = this::sWhitespace;
			copy();
			return true;
		}
		return false;
	}

	protected boolean isIdentifierStart() {
		if(isIdentifierStart(la())) {
			copy();
			m_state = this::sIdentifier;
			return true;
		}
		return false;
	}


	protected boolean isIdentifierStart(int c) {
		return Character.isJavaIdentifierStart(c);
	}

	protected boolean isIdentifierNext(int c) {
		return Character.isJavaIdentifierPart(c);
	}

	/**
	 * Called when a token seems to have ended. If the new token type differs from
	 * the previous token type then flush the previous type.
	 */
	protected void flush(HighlightTokenType type) {
		if(m_lastType != type) {
			tokenFound(m_lastType, m_lastToken);
			m_sb.setLength(0);
			m_lastType = type;
			m_lastToken = sb().toString();
		} else {
			m_lastToken = m_lastToken + sb();
		}
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Boilerplate													*/
	/*----------------------------------------------------------------------*/


	protected StringBuilder sb() {
		return m_sb;
	}

	protected char getLastCopied() {
		int l = m_sb.length();
		if(l == 0)
			return (char) -1;
		return m_sb.charAt(l - 1);
	}

	protected int la() {
		if(m_ix >= m_len)
			return -1;
		return m_line.charAt(m_ix) & 0xffff;
	}

	protected int la(int offset) {
		int o = offset + m_ix;
		if(o >= m_len)
			return -1;
		return m_line.charAt(o);
	}

	protected void accept() {
		m_ix++;
	}

	protected void copy() {
		if(m_ix < m_len) {
			m_sb.append(m_line.charAt(m_ix++));
		}
	}

	protected int remainder() {
		return m_len - m_ix;
	}

	protected void copy(int count) {
		while(count-- > 0 && m_ix < m_len) {
			m_sb.append(m_line.charAt(m_ix++));
		}
	}

}

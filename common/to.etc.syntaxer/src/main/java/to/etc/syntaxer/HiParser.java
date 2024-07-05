package to.etc.syntaxer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.WrappedException;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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

	/** The current token type being parsed */
	private HighlightTokenType m_state;

	private int m_tokenStart;

	@Nullable
	private String m_tokenEnd;

	//private HighlightTokenType m_lastType = HighlightTokenType.whitespace;

	abstract protected void tokenFound(HighlightTokenType type, String text, int characterIndex);

	private Map<String, HighlightTokenType> m_keywordMap = new HashMap<>();

	protected IHighlightRenderer m_renderer = new IHighlightRenderer() {
		@Override
		public void renderToken(HighlightTokenType tokenType, String token, int characterIndex) {
			//-- Dummy
		}
	};

	public LineContext start(String line, @Nullable LineContext startContext) {
		m_line = line;
		m_ix = 0;
		m_len = line.length();
		m_sb.setLength(0);
		m_tokenStart = 0;
		if(null != startContext) {
			m_state = startContext.getLexerState();
		} else {
			calculatePhaseFor();
		}
		int eolct = 0;							// The eol char is always offered to the phase once.
		for(;;) {
			int c = la();
			if(c == -1) {
				eolct++;
				if(eolct >= 2)
					break;
			}

			try {
				switch(m_state) {
					default:
						throw new IllegalStateException("Unhandled token type " + m_state);

					case comment:
						sComment();
						break;

					case whitespace:
						sWhitespace();
						break;

					case character:
					case string:
						sString();
						break;

					case id:
						sIdentifier();
						break;

					case punctuation:
						sPunctuation();
						break;
				}
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
		}

		//-- Flush the current token
		if(m_sb.length() > 0) {
			flush(m_state);
		}

		//-- Flush the newline
		m_tokenStart = m_ix;
		flush(HighlightTokenType.newline);
		return new LineContext(m_state);
	}

	/**
	 * If the current char is whitespace then append, else move to the next state.
	 */
	protected void sWhitespace() {
		while(isWhitespace(la()))
			copy();

		//-- We have finished a whitespace run -> flush
		flush(HighlightTokenType.whitespace);
		calculatePhaseFor();
	}

	private boolean isWhitespace(int c) {
		return c != -1 && Character.isWhitespace(c);
	}

	protected void sIdentifier() {
		m_state = HighlightTokenType.id;
		for(;;) {
			int c = la();
			if(isEoln(c))
				break;
			if(m_sb.length() == 0) {
				if(! isIdentifierStart(c))
					break;
			} else {
				if(! isIdentifierNext(c))
					break;
			}

			//-- Id char -> add
			copy();
		}

		//-- No longer an identifier ->
		flush(HighlightTokenType.id);
		calculatePhaseFor();
	}

	private boolean isEoln(int c) {
		return c == -1 || c == '\n';
	}

	protected void calculatePhaseFor() {
		m_tokenStart = m_ix;

		if(isWsStart()) {
			m_state = HighlightTokenType.whitespace;
			return;
		}
		if(isIdentifierStart(la())) {
			m_state = HighlightTokenType.id;
			return;
		}
		if(la() == '\n') {
			flush(HighlightTokenType.newline);
			accept();
			m_state = HighlightTokenType.whitespace;
			return;
		}
		int nc = isStringStart();
		if(nc > 0) {
			m_tokenEnd = get(nc);
			copy(nc);
			m_state = HighlightTokenType.string;
			return;
		}
		if(checkNumber())
			return;
		if(isComment())
			return;

		//-- Must be punctuation
		m_state = HighlightTokenType.punctuation;
		copy();
	}

	protected void sPunctuation() {
		for(;;) {
			int c = la();
			if(! isPunctuation(c) || isCommentStarter() || isStringStart() != 0 || c == -1) {
				break;
			}
			copy();
		}
		flush(HighlightTokenType.punctuation);
		calculatePhaseFor();
	}

	private boolean isCommentStarter() {
		if(is("/*") || is("//"))
			return true;
		return false;
	}

	private boolean isPunctuation(int c) {
		return ! Character.isLetterOrDigit(c)
			&& ! Character.isWhitespace(c)
			&& c != -1;
	}

	private boolean isComment() {
		if(is("/*")) {
			m_state = HighlightTokenType.comment;
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
		for(;;) {
			if(isTokenEnd()) {
				flush(HighlightTokenType.comment);
				calculatePhaseFor();
				return;
			} else if(la() == -1) {
				return;
			}
			copy();
		}
	}

	protected boolean checkNumber() {
		int c = la();
		int base = 0;
		if(c == '0') {
			//-- octal/hex/binary?
			copy();
			c = la();
			if(c == 'x' || c == 'X') {
				base = 16;
				copy();
			} else if(c == 'b' || c == 'B') {
				base = 2;
				copy();
			} else {
				base = 8;
				// Let the copy be done in the body as we check there that it is actually a digit
			}
		} else if(Character.isDigit(c)) {
			m_state = HighlightTokenType.number;
			copy();
			base = 10;
		} else {
			return false;
		}

		//-- Copy all digits, and allow for scientific notation where necessary
		m_state = HighlightTokenType.number;
		for(;;) {
			c = la();
			if(c != '_') {
				int value = digitValue(c);
				if(value == -1 || value >= base) {
					break;
				}
			}
			copy();
		}

		//-- If the base is 10 we can have scientific notation, so bail out for other bases
		if(base != 10) {
			//-- We do accept 'l' or 'L' here
			if(c == 'l' || c == 'L') {
				copy();
			}
			flush(m_state);
			calculatePhaseFor();
			return true;
		}

		//-- Base 10. Do we have a dot?
		if(c == '.') {
			copy();

			//-- Second copy digit loop
			while(Character.isDigit(la()))
				copy();
		}

		//-- Can now be followed by 'e' 'E'
		c = la();
		if(c == 'e' || c == 'E') {
			copy();

			//-- Then + or -, optionally,
			c = la();
			if(c == '+' || c == '-')
				copy();

			//-- Copy exponent
			while(Character.isDigit(la()))
				copy();
		}
		c = la();
		if(c == 'd' || c == 'D')
			copy();
		flush(m_state);
		calculatePhaseFor();
		return true;
	}

	private int digitValue(int c) {
		if(c >= '0' && c <= '9') {
			return c - '0';
		} else if(c >= 'A' && c <= 'Z') {
			return 10 + c - 'A';
		} else if(c >= 'a' && c <= 'z') {
			return 10 + c - 'a';
		} else
			return -1;
	}

	protected int isStringStart() {
		int c = la();
		if(c == '\"' || c == '\'') {
			return 1;
			//copy();
			//m_state = this::sString;
			//m_tokenEnd = "" + (char) c;
			//return true;
		}
		return 0;
	}

	protected void sString() {
		for(;;) {
			if(! isStringEscape()) {
				if(isTokenEnd() || la() == -1) {
					break;
				}
				copy();
			}
		}
		flush(m_state);
		calculatePhaseFor();
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

	protected String get(int count) {
		int ep = m_ix + count;
		if(ep > m_len)
			ep = m_len;
		return m_line.substring(m_ix, ep);
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
		return Character.isWhitespace(la());
	}

	//protected boolean isIdentifierStart() {
	//	if(isIdentifierStart(la())) {
	//		copy();
	//		m_state = this::sIdentifier;
	//		return true;
	//	}
	//	return false;
	//}

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
		//-- Is this a keyword?
		String token = m_sb.toString();
		if(type == HighlightTokenType.id) {
			HighlightTokenType alt = m_keywordMap.get(token);
			if(null != alt) {
				type = alt;
			}
		}

		tokenFound(type, token, m_tokenStart);
		m_sb.setLength(0);
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

	protected void setKeywordMap(Map<String, HighlightTokenType> keywordMap) {
		m_keywordMap = keywordMap;
	}

	protected void setKeywordCaseIndependent() {
		Map<String, HighlightTokenType> map = m_keywordMap;
		m_keywordMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		m_keywordMap.putAll(map);
	}

	protected void addKeywords(HighlightTokenType type, String... list) {
		for(String s : list) {
			m_keywordMap.put(s, type);
		}
	}
}

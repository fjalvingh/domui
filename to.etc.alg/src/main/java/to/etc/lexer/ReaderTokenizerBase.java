/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.lexer;

import java.io.*;

/**
 * Created on Sep 9, 2004
 * @author jal
 */
public class ReaderTokenizerBase extends ReaderScannerBase {
	/** If T the tokenizer will return whitespace as a token too. All whitespace is collated and returned as a single token */
	private boolean	m_return_ws;

	/** If T the tokenizer will treat newline as a token too - it will be returned. */
	private boolean	m_return_nl;

	private boolean	m_returnComment;

	private boolean m_keepQuotes;

	private boolean m_scanUndottedNumbers;

	//	private StringBuffer	m_sb = new StringBuffer();

	private int		m_lastToken	= T_EOF;

	public ReaderTokenizerBase(Object source, Reader r) {
		super(source, r);
	}

	public void setReturnWhitespace(boolean ws) {
		m_return_ws = ws;
	}

	public void setReturnNewline(boolean nl) {
		m_return_nl = nl;
	}

	public boolean isKeepQuotes() {
		return m_keepQuotes;
	}

	public void setKeepQuotes(boolean keepQuotes) {
		m_keepQuotes = keepQuotes;
	}

	public void setReturnComment(boolean returnComment) {
		m_returnComment = returnComment;
	}

	public String getTokenString() {
		int type = getLastToken();
		switch(type){
			default:
				return "'" + Character.toString((char) type) + "'";
			case T_EOF:
				return "<<eof>>";
			case T_STRING:
				return "string:" + getCopied();
			case T_COMMENT:
				return "comment";
			case T_IDENT:
				return "identifier:" + getCopied();
			case T_IPADDR:
				return "ip address:+" + getCopied();
			case T_NUMBER:
				return "number:" + getCopied();
		}
	}

	protected int scanString() throws IOException, SourceErrorException {
		scanSimpleString(isKeepQuotes());
		return T_STRING;
	}

	protected int scanToken() throws IOException {
		return T_EOF;
	}

	public int getLastToken() {
		return m_lastToken;
	}

	public int nextToken() throws IOException, SourceErrorException {
		return (m_lastToken = nextTokenPrimitive());
	}

	private int nextTokenPrimitive() throws IOException, SourceErrorException {
		for(;;) // Till a non-filtered token is found
		{
			if(!m_return_ws && !m_return_nl)
				skipWs();
			else if(m_return_nl && !m_return_ws)
				skipWsNoNL();
			startToken();
			int c = LA(); // Get current character
			int token = scanToken();
			if(token != T_EOF)
				return token;

			switch(c){
				default:
					if(Character.isWhitespace((char) c)) {
						for(;;) {
							if(m_return_nl) // Must we treat NL differently?
							{
								if(c == '\n') // Is newline-> end loop
									break;
							}
							if(c == -1)
								break;
							else if(!Character.isWhitespace((char) c))
								break;

							//-- This is whitespace- accept and continue
							append((char) c);
							accept(); // Accept whitespace char,
							c = LA(); // NEXT
						}

						//-- End of whitespace loop
						return ' ';
					}

					if(isIdStart((char) c))
						return scanIdentifier();
					append((char) c);
					accept();
					return c;

				case '\n':
					if(m_return_nl) {
						append((char) c);
						accept();
						return c;
					} else {
						for(;;) {
							if(c == -1)
								break;
							else if(!Character.isWhitespace((char) c))
								break;

							//-- This is whitespace- accept and continue
							append((char) c);
							accept(); // Accept whitespace char,
							c = LA(); // NEXT
						}

						//-- End of whitespace loop
						return ' ';
					}

				case -1:
					return T_EOF;

				case '"':
				case '\'':
					//-- String constant
					return scanString();

				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					if(m_scanUndottedNumbers)
						return scanUndottedNumber();
					return scanNumber(); // Scan a number OR an IP address

				case '/':
					//-- Line-based comment?
					if(LA(1) == '/') {
						copy(2);
						for(;;) {
							c = LA();
							if(c == '\n' || c == -1) { // Eof/eoln?
								if(m_returnComment)
									return T_COMMENT;
								break;
							}
							copy(); // Always accept
						}
					} else if(LA(1) == '*') // C style multiline comment?
					{
						copy(2); // accept /*
						int lc = 0;
						for(;;) {
							c = LA();
							copy();
							if(c == -1)
								throw new IllegalStateException("Unexpected EOF in multiline comment started at line " + getTokenLine() + ":" + getTokenColumn());
							if(c == '/') {
								if(lc == '*') {
									if(m_returnComment)
										return T_COMMENT;
									break;
								}
							}
							lc = c;
						}
					} else {
						accept(); // Single slash
						append((char) c);
						return c;
					}
					break;
			}
		}
	}

	public void setScanUndottedNumbers(boolean scanUndottedNumbers) {
		m_scanUndottedNumbers = scanUndottedNumbers;
	}
}

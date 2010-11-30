/*
 * DomUI Java User Interface library
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
package to.etc.domui.fts;

import java.io.*;

import to.etc.util.*;

/**
 * This tokenizes HTML text for the fulltext scanner. This is a state machine handling several HTML
 * constructs like the entities and stuff. The scanner skips all HTML tags, and only reads the content
 * from reasonable tags (non-script, content tags).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 11, 2008
 */
public class HtmlTokenizer extends BufferedTokenizer implements ISourceTokenizer {
	private char[]		m_dest;
	private int			m_dix;
	private int			m_dsz;
	private boolean		m_hidden;
	private int			m_nsilly;
	private char		m_lastsilly;

	public HtmlTokenizer(Reader reader, int blocksz) {
		super(reader, blocksz);
	}
	public HtmlTokenizer(Reader r) {
		super(r);
	}

	public boolean nextToken(FtsToken token) throws IOException {
		//-- Init for the next thingy.
		m_dest = token.getContent().getData();
		m_dix = 0;
		m_dsz = m_dest.length;
		for(;;) {
			int six = m_fullOffset + m_bix;
			scan();
			if(m_dix > 0) {
				if(m_hidden) {
					m_dix = 0;
				} else {
					//-- Return the token collected so-far
					token.getContent().setLength(m_dix);
					token.init(six, m_sentenceNumber, m_tokenNumber++);
					return true;
				}
			} else if(atEof()) {
//				System.out.println("\n:GOT EOF:");
				return false;
			}
		}
	}

	private void	append(char c) {
		if(m_dix < m_dsz)
			m_dest[m_dix++] = c;
	}

	/**
	 * Determines the next thingy at the current location.
	 * @return
	 * @throws IOException
	 */
	private void	scan() throws IOException {
		if(m_bix >= m_bend) {
			if(! nextBuffer())
				return;							// EOF: no token collected
		}
		char c = m_buffer[m_bix++];				// Collect the 1st char of the next construct;

		if(c == '<') {
			scanTag();
			return;
		}
		if(c != '&' && isWsChar(c)) {
			skipWhitespace();
			return;
		}
		m_bix--;
		scanWord();
	}
	@Override
	protected boolean isWsChar(char c) {
		if(c == '&')
			return false;
		return super.isWsChar(c);
	}

	/**
	 * This scans a word in a normal text block of a tag. This accepts words formed by non-ws, possibly containing
	 * entities (that are translated to normal unicode inside the word). The word can still be skipped if it
	 * is later decided it is part of a "hidden" tag.
	 */
	private void	scanWord() throws IOException {
		for(;;) {
			if(m_bix >= m_bend) {
				if(! nextBuffer())
					return;
			}
			char c = m_buffer[m_bix];
			if(c == '&') {
				m_bix++;
				if(! scanEntity())
					return;
				continue;
			}
			if(c == '<')						// At tag start?
				return;							// Exit, usually with token.
			if(isWsChar(c)) {
				if(m_nsilly == 1)
					append(m_lastsilly);
				m_nsilly = 0;
				return;
			}
			if(! Character.isLetterOrDigit(c)) {	// Silly character?
				m_nsilly++;
				m_lastsilly = c;
			} else {
				if(m_nsilly == 1) {				// Exactly 1 silly character before?
					append(m_lastsilly);		// Include it,
					m_nsilly = 0;
				} else if(m_nsilly > 1 && m_dix > 0) {
					m_nsilly = 0;
					return;
				}
				append(c);
			}
			m_bix++;
		}
	}

	private char[]		m_ebuf = new char[32];

	/**
	 * Scans a &xxxx; entity. This tries to be forgiving for entity fuckups (Internet Exploder websites *cough*). When
	 * called the ampersand has been skipped already.
	 * @return
	 * @throws IOException
	 */
	private boolean	scanEntity() throws IOException {
		int c = nextChar();
		if(Character.isWhitespace(c)) {				// Loose and wild ampersand
			append((char)c);
			return true;
		}
		if(c == '#')
			return scanNumericEntity();

		//-- Most probably a character entity; it's name must end in ; or whitespace and it cannot be too long.
		int	eix = 0;
		int	esz	= m_ebuf.length;
		m_ebuf[eix++] = (char) c;
		while(eix < esz) {
			c = nextChar();
			if(c == ';' || isWsChar((char)c) || c == -1) {
				//-- If we ended on ws we need to backspace
				if(isWsChar((char)c))
					m_bix--;

				//-- Done.
				String ename = new String(m_ebuf, 0, eix);
				int val = HtmlEntityTables.findCode(ename);
				if(val == -1)
					return true;
				append((char) val);
				return true;
			}
			m_ebuf[eix++] = (char)c;
		}

		//-- Buffer has overflown- skip the whole shebang.
		for(int i = 0; i < esz; i++)
			append(m_ebuf[i]);
		return true;
	}

	private boolean	scanNumericEntity() throws IOException {
		int c = nextChar();
		int	base = 10;
		if(c == 'x' || c == 'X') {							// Decimal thingy?
			base = 16;
			c = nextChar();
		}

		int val = 0;
		for(;;) {
			//-- Include the char 1st
			c = Character.toLowerCase(c);
			if(c >= '0' && c <= '9') {
				val = val*10 + (c-'0');
			} else if(base == 16 && c >= 'a' && c <= 'f') {
				val = val * 10 + (c - 'a' + 10);
			} else
				break;
			c = nextChar();
		}
		if(c != ';')
			m_bix--;
		if(isWsChar((char) val))
			return false;
		append((char) val);
		return true;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Tag skipping code									*/
	/*--------------------------------------------------------------*/

	/** T when the last tag scanned was &lt;/xxx&gt; */
	private boolean		m_isCloseTag;

	/** T when the last tag scanned was &lt;xxxx/&gt; */
	private boolean		m_isClosedTag;

	private String		m_tagName;

	/**
	 * Called when a < is found. This code starts by determining the tag name, it then at least handles it to it's ending >. The tag's
	 * attributes are fully ignored; only the tag name is used to determine if the tag's content is to be indexed. This is the case for
	 * only a few tags, like script, style, object, head, comment etc.
	 * @throws IOException
	 */
	private void	scanTagInternal() throws IOException {
		int	eix = 0;
		int esz = m_ebuf.length;
		m_tagName = null;

		int c = nextChar();
		m_isCloseTag = false;
		if(c == '/') {
			m_isCloseTag = true;
			c = nextChar();
		} else if(c == '!') {
			c = nextChar();
			if(c == '-') {
				//-- Comment. Skip till -->
				int nd = 0;
				for(;;) {
					c = nextChar();
					if(c == -1)
						return;						// Unexpected eof in comment
					if(c == '-')
						nd++;
					else if(c == '>') {
						if(nd >= 2) {
							m_tagName = "!--";
							return;					// Proper end of comment -->
						}
					} else
						nd = 0;
				}
			}
			
			//-- DOCTYPE??
			while(c != '>' && c != -1)
				c = nextChar();
			m_tagName = "DOCTYPE?";
			return;
		}

		//-- Tag name scan
		for(;;) {
			if(c == -1)
				return;							// Unexpected eof
			if(! Character.isLetterOrDigit(c))
				break;
			if(eix < esz)
				m_ebuf[eix++] = (char)c;
			c = nextChar();
		}

		//-- Name is known. Now move to the full end of the tag (the > or />)
		int pc = 0;
		for(;;) {
			if(c == '>')
				break;
			else if(c == -1)
				return;							// Unexpected eof while scanning for end of tag
			pc = c;
			c = nextChar();
		}
		m_isClosedTag = (pc == '/');			// Set if /> was found
		m_tagName = new String(m_ebuf, 0, eix).toLowerCase();
	}

	/**
	 * Called @ tag start
	 * @throws Exception
	 */
	private void	scanTag() throws IOException {
		scanTagInternal();
		if(m_tagName == null)
			return;

		//-- Special tags we skip fully
//		System.out.println("\nTAG="+m_tagName);
		if("head".equals(m_tagName) || "script".equals(m_tagName) || "style".equals(m_tagName) || "object".equals(m_tagName)) {
			if(m_isClosedTag)						// Closed immediately?
				return;
			skipUntilClosed(m_tagName);				// Skip until a matching end tag is found...
			return;
		}
	}

	/**
	 * Skips input until a close tag of the specified type is found. This does not do nesting.
	 * @param tag
	 */
	private void	skipUntilClosed(String tag) throws IOException {
		for(;;) {
			if(m_bix >= m_bend) {
				if(! nextBuffer())
					return;							// Unexpected EOF
			}
			char c = m_buffer[m_bix++];				// Next thingy.
			if(c == '<') {							// Start of a tag thing?
				scanTagInternal();					// Scan the thingy.
//				System.out.println("ISEND "+m_tagName+" "+m_isCloseTag);
				if(m_tagName == null)
					return;							// Unexpected eof
				if(m_tagName.equals(tag) && m_isCloseTag)
					return;
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Testing code.										*/
	/*--------------------------------------------------------------*/


	static public void	tokenize(Reader r) throws Exception {
		HtmlTokenizer	tt	= new HtmlTokenizer(r);
		int i = 1;
		FtsToken	t = new FtsToken();
		while(! tt.atEof()) {
			if(! tt.nextToken(t))
				break;
			if(t == null)
				System.out.print("(skipped token) ");
			else {
				System.out.print("<<"+t.toString()+">> ");
			}
			if(i++ % 10 == 0)
				System.out.println("");
		}
		System.out.println("EOF");
	}

	static public void	tokenize(String res) throws Exception {
		InputStream	is	= null;
		try {
			is	= HtmlTokenizer.class.getResourceAsStream(res);
			if(is == null)
				throw new IllegalStateException("Resource "+res+" not found");
			Reader	r	= new InputStreamReader(is, "utf-8");
			tokenize(r);
		} finally {
			try { if(is != null) is.close(); } catch(Exception x) {}
		}
	}

	public static void main(String[] args) {
		try {
			tokenize("verne.html");
//			
//			IFtsLanguage	lang = new DutchLanguage();
//
//			Analyzer.analyze("testnl.txt", new IAnalyzerListener() {
//				public void nextWord(String txt, int sentence, int word) throws Exception {
//					System.out.println("WORD: "+txt+" @"+sentence+", "+word);
//				}
//				public void stopWord(String word, int sentence, int wordindex) throws Exception {
//				}
//			}, lang);
//			
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

}

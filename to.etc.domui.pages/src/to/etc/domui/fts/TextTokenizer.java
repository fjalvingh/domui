package to.etc.domui.fts;

import java.io.*;

import to.etc.domui.fts.nl.*;

/**
 * This tokenizes normal text files.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 11, 2008
 */
public class TextTokenizer extends BufferedTokenizer implements ISourceTokenizer {
	public TextTokenizer(Reader r) throws IOException {
		super(r);
		nextBuffer();
	}

	/**
	 * Scan for the next word token in a textfile. This merely skips all whitespace
	 * @return
	 */
	public boolean		nextToken(FtsToken token) throws IOException {
		skipWhitespace();
		return scanWord(token);
	}

	protected boolean	scanWord(FtsToken t) throws IOException {
		int	six	= m_bix;							// Start of the thingy.
		int dix = 0;								// Destination (token buffer) index,
		char[] dest = t.getContent().getData();		// Get backing char buffer (destination)
		int dsz	= dest.length;
		char[]	data = m_buffer;
		int nsilly = 0;
		char lastsilly = 0;
		for(;;) {
			while(m_bix < m_bend) {					// While still in *this* buffer
				char c = data[m_bix];				// Get the thingy.

				//-- We accept anything but punctuation and whitespace in a word, including hyphens etc...
				if(isWsChar(c)) {
					//-- Handle silly characters,
					if(nsilly == 1) {				// Only if it ends in ONE silly character add the silly character, (this does not allow c++)
						if(dix < dsz)
							dest[dix++] = lastsilly;
					}
					t.getContent().setLength(dix);
					t.init(m_fullOffset+six, m_sentenceNumber, m_tokenNumber++);
					return true;
				}

				//-- If this is a silly char, increment the silly count and do NOT store it..
				if(! Character.isLetterOrDigit(c)) {
					lastsilly = c;
					nsilly++;
				} else {
					if(nsilly == 1) {
						if(dix < dsz)
							dest[dix++] = lastsilly;
						nsilly = 0;
					} else if(nsilly > 1 && dix > 0) {
						//-- Thingy ending in lots of sillyness: return the token if there is one;
						t.getContent().setLength(dix);
						t.init(m_fullOffset+six, m_sentenceNumber, m_tokenNumber++);
						return true;
					}
					if(dix < dsz)
						dest[dix++] = data[m_bix];
				}
				m_bix++;
			}

			if(! nextBuffer()) {
				if(dix == 0)
					return false;

				//-- Return the current allocation.
				t.getContent().setLength(dix);
				t.init(m_fullOffset+six, m_sentenceNumber, m_tokenNumber++);
				return true;
			}
		}
	}

	static public void	tokenize(Reader r) throws Exception {
		TextTokenizer	tt	= new TextTokenizer(r);
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
//			if(i++ > 1000)
//				break;
		}
		System.out.println("EOF");
	}

	static public void	tokenize(String res) throws Exception {
		InputStream	is	= null;
		try {
			is	= TextTokenizer.class.getResourceAsStream(res);
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
//			tokenize("testnl.txt");

			IFtsLanguage	lang = new DutchLanguage();

			Analyzer.analyze("testnl.txt", new IAnalyzerListener() {
				public void nextWord(String txt, int sentence, int word) throws Exception {
					System.out.println("WORD: "+txt+" @"+sentence+", "+word);
				}
				public void stopWord(String word, int sentence, int wordindex) throws Exception {
				}
			}, lang, "text/text");

		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}

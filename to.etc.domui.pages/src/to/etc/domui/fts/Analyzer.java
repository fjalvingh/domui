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

/**
 * Analyzes a token stream and calls a listener for each indexable stem.
 * 
 * Some statistics:
 * <h3>Met round-robin complexe tokenscanner met reuse van TokenBuffer ruimte</h3>
 * <pre>
 *	--- FTS indexing testnl.txt ------
 *	Indexer completed in 4s 56ms
 *	Commit completed in 5ms 531us
 *	IndexWriter: 49562 words, 19823 stopwords, 29739 indexable words, 24517 from cache, 5222 from db, 0 inserted
 *             : 29739 word occurences inserted
 *	--- FTS indexing verne.txt ------
 *	Indexer completed in 4s 231ms
 *	Commit completed in 4ms 454us
 *	IndexWriter: 75881 words, 31293 stopwords, 44588 indexable words, 41421 from cache, 3167 from db, 0 inserted
 *	           : 44588 word occurences inserted
 * </pre>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 12, 2008
 */
public class Analyzer {
	private IAnalyzerListener	m_listener;

	public Analyzer() {
	}
	public Analyzer(IAnalyzerListener l) {
		m_listener = l;
	}
	
	public void		analyze(ISourceTokenizer t, IFtsLanguage lang) throws Exception {
		IStemmer	stemmer	= lang.getStemmer();
		FtsToken	token = new FtsToken();
		while(t.nextToken(token)) {
			String s = token.toString();
			if(s.length() == 0)
				continue;
			if(lang.isStopWord(s)) {			// Got a STOP word
				m_listener.stopWord(s, token.getSentenceNumber(), token.getTokenNumber());
			} else {
				s = stemmer.stem(s);				// Convert to stem
				if(s != null)
					foundWord(token.getSentenceNumber(), token.getTokenNumber(), s);
			}
		}
	}

	public void	foundWord(int sentence, int word, String txt) throws Exception {
		if(m_listener != null)
			m_listener.nextWord(txt, sentence, word);
	}

	public IAnalyzerListener getListener() {
		return m_listener;
	}

	public void setListener(IAnalyzerListener listener) {
		m_listener = listener;
	}

	static public void	analyze(Reader r, IAnalyzerListener l, IFtsLanguage lang, String mime) throws Exception {
		ISourceTokenizer	tt;
		if("text/text".equals(mime))
			tt	= new TextTokenizer(r);
		else if("text/html".equals(mime))
			tt	= new HtmlTokenizer(r);
		else
			throw new IllegalStateException("Unknown tokenizer for "+mime);
		Analyzer	a = new Analyzer(l);
		a.analyze(tt, lang);
	}

	static public void	analyze(String res, IAnalyzerListener l, IFtsLanguage lang, String mime) throws Exception {
		InputStream	is	= null;
		try {
			is	= TextTokenizer.class.getResourceAsStream(res);
			if(is == null)
				throw new IllegalStateException("Resource "+res+" not found");
			Reader	r	= new InputStreamReader(is, "utf-8");
			analyze(r, l, lang, mime);
		} finally {
			try { if(is != null) is.close(); } catch(Exception x) {}
		}
	}
}

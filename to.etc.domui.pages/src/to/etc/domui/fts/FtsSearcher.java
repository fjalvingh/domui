package to.etc.domui.fts;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Search module for the FTS index.
 * 
 * <h2>Search algorithm</h2>
 * <ul>
 *	<li>Take the input string, and decompose it into separate words</li>
 *	<li>Remove all stopwords, but let the remaining words remember their original index</li>
 *	<li>If no words remain we have an illegal search for only stopwords - cancel</li>
 *	<li>Lookup the remaining words in the database. If any word is unknown we return 0 results immediately</li>
 *	<li>Take the word with the smallest occurence and use it to prime the result vector.</li>
 * </ul>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 16, 2008
 */
public class FtsSearcher {
	static public class WordInfo {
		/** The actual word in the search command */
		String word;

		boolean	isStopWord;

		String	stemmedWord;

		long	wordPK = -1;
		int		wordOccurences;

		public WordInfo() {
		}
		public long getWordPK() {
			return wordPK;
		}
	}

//	static private class OccEntry {
//		public long 	docPK;
//		public OccEntry	next;
//		public int[][]	wordIndexes;
//		public int[]	wordIndexCount;
//		public short	hitmask;
//		public double	score = 0.0;
//
//		public OccEntry() {
//		}
//		public void addWordIndex(int nword, int wix) {
//			if(wordIndexes[nword] == null)
//				wordIndexes[nword] = new int[10];
//			else if(wordIndexCount[nword] >= wordIndexes[nword].length) {
//				int[] ar = new int[wordIndexCount[nword]+20];
//				System.arraycopy(wordIndexes[nword], 0, ar, 0, wordIndexCount[nword]);
//				wordIndexes[nword] = ar;
//			}
//			wordIndexes[nword][wordIndexCount[nword]++] = wix;
//			hitmask |= 1 << nword;
//		}
//	}

	private Connection			m_dbc;
	private List<WordInfo>		m_wordList = new ArrayList<WordInfo>();
	private String				m_errorCode;
	private String				m_errorArgument;
	private List<WordInfo>		m_searchList = new ArrayList<WordInfo>();
	private List<String>		m_filterFragments = new ArrayList<String>();
	private List<Long>			m_fragmentIds = new ArrayList<Long>();
	private List<String>		m_filterDocTypes = new ArrayList<String>();
//	private OccEntry[]			m_occMap;

	public FtsSearcher(Connection dbc) {
		m_dbc = dbc;
	}
	
	/**
	 * Search main entrypoint.
	 *
	 * @param in
	 * @return			T if results were found.
	 * @throws Exception
	 */
//	public boolean		search(IFtsLanguage lang, String in) throws Exception {
//		clearInternal();								// Discard any previous search results.
//		int nstemmed = createWordTable(lang, in);		// Create the stemmed word table
//		if(nstemmed == 0) {
//			m_errorCode = "fts.stops";					// Stop words only.
//			return false;
//		}
//		List<String> failed = lookupWords();
//		if(failed != null) {
//			m_errorCode = "fts.wordsnotfound";			// At least one of the search words was not found.
//			m_errorArgument = failed.toString();
//			return false;
//		}
//		orderSearchList();								// Sort the least-occuring word as 1st element;
////		mergingLookup();
//		return false;
//	}

	public String	createQuery(IFtsLanguage lang, String in) throws Exception {
		clearInternal();								// Discard any previous search results.
		int nstemmed = createWordTable(lang, in);		// Create the stemmed word table
		if(nstemmed == 0) {
			m_errorCode = "fts.stops";					// Stop words only.
			return null;
		}
		List<String> failed = lookupWords();
		if(failed != null) {
			m_errorCode = "fts.wordsnotfound";			// At least one of the search words was not found.
			m_errorArgument = failed.toString();
			return null;
		}

		//-- Lookup all fragments, if needed,
		lookupFragments();
		if(m_filterFragments.size() > 0 && m_fragmentIds.size() == 0) {
			//-- Will not have a result (missing fragments)
			m_errorCode = "fts.missingfragments";
			return null;
		}
		return createSQL();
	}

	private String	createSQL() {
		StringBuilder sb = new StringBuilder();
		sb.append("select a0.fdo_id from ");

		for(int i = 0; i < getSearchList().size(); i++) {
			if(i > 0)
				sb.append(",");
			sb.append("fts_occurence a");
			sb.append(i);
		}
		sb.append(" where ");
		for(int i = 0; i < getSearchList().size(); i++) {
			FtsSearcher.WordInfo wi= getSearchList().get(i);
			if(i > 0)
				sb.append(" and ");
			sb.append("a");
			sb.append(i);
			sb.append(".fwd_id=");
			sb.append(wi.getWordPK());
			if(m_filterDocTypes.size() > 0) {
				sb.append(" and (");
				int j = 0;
				for(String type: m_filterDocTypes) {
					if(j++ != 0)
						sb.append(" and ");
					sb.append("a");
					sb.append(i);
					sb.append(".fdo_type='");
					sb.append(type);
					sb.append("'");
				}
				sb.append(")");
			}
			//-- Fragments
			if(m_fragmentIds.size() > 0) {
				sb.append(" and (");
				int j = 0;
				for(Long type: m_fragmentIds) {
					if(j++ != 0)
						sb.append(" and ");
					sb.append("a");
					sb.append(i);
					sb.append(".ffr_id=");
					sb.append(type.toString());
				}
				sb.append(")");
			}
			if(i > 0) {
				sb.append(" and a");
				sb.append(i-1);
				sb.append(".fdo_id=a");
				sb.append(i);
				sb.append(".fdo_id");
			}
		}
		return sb.toString();
	}

	public List<WordInfo> getSearchList() {
		return m_searchList;
	}
	public void	addDocumentType(String type) {
		m_filterDocTypes.add(type);
	}
	public void	addFragment(String fragment) {
		m_filterFragments.add(fragment);
	}

//	/**
//	 * Create the proper occurence lookup statement for a single word.
//	 * @return
//	 * @throws Exception
//	 */
//	private PreparedStatement	createOccurenceStatement() throws Exception {
//		StringBuilder	sb = new StringBuilder(256);
//		sb.append("select fdo_id,foc_wordindex,ffr_id,foc_sentence from fts_occurence where fwd_id=?");
//
//		//-- Append any further limitations.
//		if(m_filterFragments.size() > 0) {
//			sb.append(" and ffr_id in (");
//			for(int i = 0; i < m_filterFragments.size(); i++) {
//				FtsFragment ff = m_filterFragments.get(i);
//				if(i > 0)
//					sb.append(',');
//				sb.append(ff.getId());
//			}
//			sb.append(')');
//		}
//		if(m_filterDocTypes.size() > 0) {
//			sb.append(" and fdo_type in (");
//			for(int i = 0; i < m_filterDocTypes.size(); i++) {
//				String s = m_filterDocTypes.get(i);
//				if(i > 0)
//					sb.append(',');
//				sb.append('\'');
//				sb.append(s);
//				sb.append('\'');
//			}
//			sb.append(')');
//		}
//		return m_dbc.prepareStatement(sb.toString());
//	}
	
//	/**
//	 * Sort the least-occuring word as 1st element.
//	 */
//	private void	orderSearchList() {
//		Collections.sort(m_searchList, new Comparator<WordInfo>() {
//			public int compare(WordInfo w0, WordInfo w1) {
//				return w1.wordOccurences - w0.wordOccurences;
//			}
//		});
//	}
	
	
	/**
	 * For each valid stemmed word in the word table, lookup the word. If a
	 * word lookup fails exit false.
	 * @return
	 * @throws Exception
	 */
	public List<String>	lookupWords() throws Exception {
		List<String>	failed = new ArrayList<String>();
		PreparedStatement	ps = null;
		try {
			ps	= m_dbc.prepareStatement("select fwd_id,fwd_noccurences from fts_word where fwd_word=?");
			for(WordInfo wi: m_searchList) {
				if(wi.stemmedWord != null) {
					if(! lookupWord(ps, wi)) {
						failed.add(wi.word);
					}
				}
			}
			return failed.size() == 0 ? null : failed;
		} finally {
			try { if(ps != null) ps.close(); } catch(Exception x){}
		}
	}

	protected boolean lookupWord(PreparedStatement ps, WordInfo wi) throws Exception {
		ResultSet rs = null;
		try {
			ps.setString(1, wi.stemmedWord);
			rs	= ps.executeQuery();
			if(! rs.next())
				return false;
			wi.wordPK = rs.getLong(1);
			wi.wordOccurences = rs.getInt(2);
			return true;
		} finally {
			try { if(rs != null) rs.close(); } catch(Exception x){}
		}
	}

	/**
	 * If fragments were added look up their key.
	 * @throws Exception
	 */
	private void	lookupFragments() throws Exception {
		m_fragmentIds.clear();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps	= m_dbc.prepareStatement("select ffr_id from fts_frag where ffr_name=?");
			for(int i = m_filterFragments.size(); --i >= 0;) {
				String name = m_filterFragments.get(i);
				ps.setString(1, name.toLowerCase());
				rs	= ps.executeQuery();
				if(rs.next()) {
					Long id = Long.valueOf(rs.getLong(1));
					m_fragmentIds.add(id);
				}
				rs.close();
			}
		} finally {
			try { if(rs != null) rs.close(); } catch(Exception x){}
			try { if(ps != null) ps.close(); } catch(Exception x){}
		}
	}

	/**
	 * Divide the search string into a word table, check for stopwords and stem the remaining words.
	 *
	 * @param lang
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public int	createWordTable(IFtsLanguage lang, String in) throws Exception {
		//-- Decompose into word list, remove stopwords and stem the sjhtuff
		IStemmer	stmr	= lang.getStemmer();
		TextTokenizer	tt = new TextTokenizer(new StringReader(in));
		FtsToken	token = new FtsToken();
		while(tt.nextToken(token)) {
			WordInfo	wi = new WordInfo();
			m_wordList.add(wi);
			wi.word = token.toString();
			if(lang.isStopWord(wi.word))
				wi.isStopWord = true;
			else {
				wi.stemmedWord = stmr.stem(wi.word);
				if(wi.stemmedWord != null) {
					if(m_searchList.size() < 16)
						m_searchList.add(wi);
				}
			}
		}
		return m_searchList.size();
	}
	
	private void	clearInternal() {
		m_wordList.clear();
		m_searchList.clear();
	}
	public String getErrorCode() {
		return m_errorCode;
	}
	public String getErrorArgument() {
		return m_errorArgument;
	}

//	private int			m_occSize;
//
//	private int		calcHash(long k) {
//		int k1 = (int) (k >> 32);
//		int k2 = (int) k;
//		return (k1 ^ k2) & 0x7fffffff;
//	}
//
//	private OccEntry	addOcc(long pk) {
//		int hash = calcHash(pk);
//		int index = hash % m_occMap.length;
//		for(OccEntry oe = m_occMap[index]; oe != null; oe = oe.next) {
//			if(oe.docPK == pk)
//				return oe;
//		}
//		m_occSize++;
//		OccEntry oe = new OccEntry();
//		oe.docPK = pk;
//		oe.next = m_occMap[index];
//		m_occMap[index] = oe;
//		return oe;
//	}
//	private OccEntry	findOcc(long pk) {
//		int hash = calcHash(pk);
//		int index = hash % m_occMap.length;
//		for(OccEntry oe = m_occMap[index]; oe != null; oe = oe.next) {
//			if(oe.docPK == pk)
//				return oe;
//		}
//		return null;
//	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Actual lookup code.									*/
	/*--------------------------------------------------------------*/
//	/**
//	 * Do the actual document merge
//	 * @throws Exception
//	 */
//	private void mergingLookup() throws Exception {
//		PreparedStatement	ps	= null;
//		try {
//			ps	= createOccurenceStatement();			// Database per-word occurence lookup.
//			loadInitialOccurences(ps);
//			int mask = 1;
//			for(int i = 1; i < m_searchList.size(); i++) {
//				mergeWord(ps, m_searchList.get(i), i-1);
//				mask = (mask << 1) | 1;					// Add another bit
//				reap(mask);
//			}
//		} finally {
//			try { if(ps != null) ps.close(); } catch(Exception x) {}
//		}
//	}
//
//	private void loadInitialOccurences(PreparedStatement ps) throws Exception {
//		WordInfo	wi = m_searchList.get(0);			// Start with least-used word
//		m_occMap = new OccEntry[8191];
//		m_occSize= 0;
//
//		ResultSet rs = null;
//		try {
//			ps.setString(1, wi.stemmedWord);
//			rs	= ps.executeQuery();
//			while(rs.next()) {
//				long docpk = rs.getLong(1);
//				OccEntry	oe = addOcc(docpk);
//				int wix = rs.getInt(2);					// foc_wordindex.
//				oe.addWordIndex(0, wix);
//			}
//		} finally {
//			try { if(rs != null) rs.close(); } catch(Exception x){}
//		}
//	}
//
//	private void	mergeWord(PreparedStatement ps, WordInfo wi, int windex) throws Exception {
//		int mask = 1 << windex;
//		ResultSet rs = null;
//		try {
//			ps.setString(1, wi.stemmedWord);
//			rs	= ps.executeQuery();
//			while(rs.next()) {
//				long docpk = rs.getLong(1);
//				OccEntry	oe = findOcc(docpk);
//				if(oe == null)
//					continue;
//				int wix = rs.getInt(2);					// foc_wordindex.
//				oe.addWordIndex(windex, wix);
//			}
//		} finally {
//			try { if(rs != null) rs.close(); } catch(Exception x){}
//		}
//	}
//
//	/**
//	 * Remove all entries that do not have the specified mask.
//	 * @param mask
//	 */
//	private void	reap(int mask) {
//		for(int i = m_occMap.length; --i >= 0;) {
//			OccEntry	prevoe = null;
//			for(OccEntry oe = m_occMap[i]; oe != null;) {
//				if(oe.hitmask == mask) {
//					prevoe = oe;
//					oe = oe.next;
//				} else {
//					//-- Remove this one: unchain,
//					if(prevoe == null)
//						m_occMap[i] = oe.next;		
//					else
//						prevoe.next = oe.next;
//					oe	= oe.next;
//				}
//			}
//		}
//	}
}

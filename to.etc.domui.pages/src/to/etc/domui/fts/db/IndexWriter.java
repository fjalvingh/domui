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
package to.etc.domui.fts.db;

import java.sql.*;
import java.util.*;

import to.etc.dbutil.*;
import to.etc.domui.fts.*;


/**
 * Handles the callbacks from an Analyzer and writes the database from it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 12, 2008
 */
public class IndexWriter implements IAnalyzerListener {
	private Connection					m_dbc;
	private Map<String, FtsWord>		m_cachedWordMap = new HashMap<String, FtsWord>();
	private PreparedStatement			m_lookupWordPS;
	private PreparedStatement			m_insertWordPS;
	private int							m_nWordAccesses;
	private int							m_nWordsInserted;
	private int							m_nWordsLookup;
	private int							m_nWordCacheHits;

	/*** Document handling ***/
	private String						m_docType;
	private long						m_docKey;

	/*** Document fragment handling ***/
	private Map<String, FtsFragment>	m_cachedFragmentMap = new HashMap<String, FtsFragment>();
	private long						m_fragPK = -1;
	private PreparedStatement			m_lookupFragPS;
	private PreparedStatement			m_insertFragPS;
	private int							m_nFragAccesses, m_nFragLookup, m_nFragInserted, m_nFragCacheHits;

	private PreparedStatement			m_occInsertPS;
	private int							m_nOccurenceInserts;
	private boolean						m_batch = true;
	private int							m_batchcount;
	private int							m_nStopWords;
	private Set<FtsWord>				m_occuredWordSet = new HashSet<FtsWord>();
	private boolean						m_inhibitMulti = true;

	public IndexWriter(Connection c) {
		m_dbc = c;
	}

	/**
	 * Defines the document fragment to use.
	 * @param name
	 */
	public void	setFragment(String name) throws Exception {
		name	= name.trim().toLowerCase();
//		m_fragName = name;
		m_fragPK = -1;
		m_nFragAccesses++;
		FtsFragment	ff = m_cachedFragmentMap.get(name);
		if(ff != null)
			m_nFragCacheHits++;
		else {
			//-- Lookup the frag by name
			if(m_lookupFragPS == null)
				m_lookupFragPS = m_dbc.prepareStatement("select ffr_id from fts_frag where ffr_name=?");
			ResultSet	rs = null;
			try {
				m_nFragLookup++;
				m_lookupFragPS.setString(1, name);
				rs	= m_lookupFragPS.executeQuery();
				if(rs.next()) {
					ff = new FtsFragment(rs.getLong(1), name);
				} else {
					rs.close();
					rs = null;
					long id = GenericDB.getSequenceID(m_dbc, "fts_frag");
					if(m_insertFragPS == null)
						m_insertFragPS = m_dbc.prepareStatement("insert into fts_frag(ffr_id,ffr_name) values(?,?)");
					m_insertFragPS.setLong(1, id);
					m_insertFragPS.setString(2, name);
					m_insertFragPS.executeUpdate();
					ff	= new FtsFragment(id, name);
					m_nFragInserted++;
				}
				m_cachedFragmentMap.put(name, ff);
			} finally {
				try { if(rs != null) rs.close(); } catch(Exception x) {}
			}
		}
		m_fragPK = ff.getId();
	}

	/**
	 * Set the document the next scan will pertain to. This forces a lookup or insert of the current document.
	 *
	 * @param type
	 * @param id1
	 * @param id2
	 */
	public void	setDocument(String type, long key) throws Exception {
		m_docType = type;
		m_docKey = key;
		m_fragPK = -1;

//		//-- Lookup the document by it's keys
//		if(m_docLookupPS == null)
//			m_docLookupPS = m_dbc.prepareStatement("select fdo_id from fts_doc where fdo_type=? and fdo_key=?");
//		ResultSet	rs	= null;
//		try {
//			m_docLookupPS.setString(1, m_docType);
//			m_docLookupPS.setString(2, m_docKey);
//			rs = m_docLookupPS.executeQuery();
//			if(rs.next())
//				m_docPK = rs.getLong(1);
//			else {
//				rs.close();
//				rs = null;
//
//				//-- Lookup failed; insert.
//				if(m_docInsertPS == null)
//					m_docInsertPS = m_dbc.prepareStatement("insert into fts_doc(fdo_id,fdo_type,fdo_key) values(?,?,?)");
//				m_docPK	= GenericDB.getSequenceID(m_dbc, "fts_doc");
//				m_docInsertPS.setLong(1, m_docPK);
//				m_docInsertPS.setString(2, m_docType);
//				m_docInsertPS.setString(3, m_docKey);
//				int rc= m_docInsertPS.executeUpdate();
//				if(rc != 1)
//					throw new SQLException("Failed to insert a DOC: resultcount="+rc);
//			}
//		} finally {
//			try { if(rs != null) rs.close(); } catch(Exception x) {}
//		}
	}

	public FtsWord		findWord(String word) throws Exception {
		m_nWordAccesses++;
		FtsWord	w	= m_cachedWordMap.get(word);
		if(w != null)
			m_nWordCacheHits++;
		else {
			if(m_lookupWordPS == null)
				m_lookupWordPS = m_dbc.prepareStatement("select fwd_id from fts_word where fwd_word=?");
			ResultSet	rs = null;
			try {
				m_nWordsLookup++;
				m_lookupWordPS.setString(1, word);
				rs	= m_lookupWordPS.executeQuery();
				if(rs.next()) {
					w	= new FtsWord(rs.getLong(1), word);
				} else {
					rs.close();
					rs = null;
					long id = GenericDB.getSequenceID(m_dbc, "fts_word");
					if(m_insertWordPS == null)
						m_insertWordPS = m_dbc.prepareStatement("insert into fts_word(fwd_id,fwd_word) values(?,?)");
					m_insertWordPS.setLong(1, id);
					m_insertWordPS.setString(2, word);
					m_insertWordPS.executeUpdate();
					w	= new FtsWord(id, word);
					m_nWordsInserted++;
				}
				m_cachedWordMap.put(word, w);
			} finally {
				try { if(rs != null) rs.close(); } catch(Exception x) {}
			}
		}
		return w;
	}

	private void	addOccurence(FtsWord word, int sentence, int wordindex) throws Exception {
		if(m_inhibitMulti) {
			if(! m_occuredWordSet.add(word))			// Add, exit if already present
				return;
		}
		if(m_docKey == -1)
			throw new IllegalStateException("No document has been set for this index run");
		if(m_fragPK == -1)
			setFragment("default");
		if(m_occInsertPS == null)
			m_occInsertPS = m_dbc.prepareStatement("insert into fts_occurence(fwd_id,foc_wordindex,foc_sentence,fdo_id,ffr_id,fdo_type) values(?,?,?,?,?,?)");
		m_occInsertPS.setLong(1, word.getId());
		m_occInsertPS.setLong(2, wordindex);
		m_occInsertPS.setLong(3, sentence);
		m_occInsertPS.setLong(4, m_docKey);
		m_occInsertPS.setLong(5, m_fragPK);
		m_occInsertPS.setString(6, m_docType);
		if(m_batch) {
			m_occInsertPS.addBatch();
			if(m_batchcount++ >= 100) {
				m_occInsertPS.executeBatch();
				m_batchcount = 0;
			}
		} else {
			int rc = m_occInsertPS.executeUpdate();
			if(rc != 1)
				throw new SQLException("Word occurence insert failed: rc="+rc);
		}
		m_nOccurenceInserts++;
	}

	/**
	 * Callback from text analyzer.
	 *
	 * @see to.etc.bugduster.fts.IAnalyzerListener#nextWord(java.lang.String, int, int)
	 */
	public void nextWord(String word, int sentence, int wordindex) throws Exception {
		FtsWord	w	= findWord(word);
		addOccurence(w, sentence, wordindex);
	}
	public void stopWord(String word, int sentence, int wordindex) throws Exception {
		m_nStopWords++;
	}

	public void	stats() {
		System.out.println("IndexWriter: "+(m_nWordAccesses+m_nStopWords)+" words, "+m_nStopWords+" stopwords, "+m_nWordAccesses+" indexable words, "+m_nWordCacheHits+" from cache, "+m_nWordsLookup+" from db, "+m_nWordsInserted+" inserted");
		System.out.println("           : "+m_nOccurenceInserts+" word occurences inserted");
	}
	public void	resetStats() {
		m_nFragAccesses = 0;
		m_nFragCacheHits = 0;
		m_nFragInserted = 0;
		m_nFragLookup = 0;
		m_nOccurenceInserts = 0;
		m_nStopWords = 0;
		m_nWordAccesses = 0;
		m_nWordCacheHits = 0;
		m_nWordsInserted = 0;
		m_nWordsLookup = 0;
	}

	public void flush() throws SQLException {
		if(m_batch && m_batchcount > 0)
			m_occInsertPS.executeBatch();
	}

	public void close() {
		try { if(m_insertWordPS != null) m_insertWordPS.close(); } catch(Exception x) {}
		try { if(m_lookupWordPS != null) m_lookupWordPS.close(); } catch(Exception x) {}
		try { if(m_insertFragPS != null) m_insertFragPS.close(); } catch(Exception x) {}
		try { if(m_lookupFragPS != null) m_lookupFragPS.close(); } catch(Exception x) {}
//		try { if(m_docLookupPS != null) m_docLookupPS.close(); } catch(Exception x) {}
//		try { if(m_docInsertPS != null) m_docInsertPS.close(); } catch(Exception x) {}
		try { if(m_occInsertPS != null) m_occInsertPS.close();  } catch(Exception x) {}
	}

}

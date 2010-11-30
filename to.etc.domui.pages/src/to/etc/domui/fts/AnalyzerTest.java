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

import java.sql.*;

import to.etc.dbpool.*;
import to.etc.domui.fts.db.*;
import to.etc.domui.fts.nl.*;
import to.etc.util.*;

public class AnalyzerTest {
	private Connection			m_dbc;
	private IFtsLanguage		m_lang;

	public static void main(String[] args) {
		try {
			new AnalyzerTest().run();
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	private void	indexOne(IndexWriter iw, String res, long dockey, String mime) throws Exception {
		iw.setDocument("TEST", dockey);
		iw.resetStats();
		System.out.println("--- FTS indexing "+res+" ------");
		long ts = System.nanoTime();
		Analyzer.analyze(res, iw, m_lang, mime);
		iw.flush();										// Force all cached stuff to the database.
		ts	= System.nanoTime() - ts;
		System.out.println("Indexer completed in "+StringTool.strNanoTime(ts));
		ts = System.nanoTime();
		m_dbc.commit();
		ts	= System.nanoTime() - ts;
		System.out.println("Commit completed in "+StringTool.strNanoTime(ts));
		iw.stats();
	}

	private void	run() throws Exception {
		openDB();
		m_lang	= new DutchLanguage();
		IndexWriter	iw = null;
		try {
			iw	= new IndexWriter(m_dbc);
			indexOne(iw, "testnl.txt", 1, "text/text");
//			indexOne(iw, "verne.txt", 2, "text/text");
			indexOne(iw, "verne.html", 3, "text/html");
		} finally {
			try { if(iw != null) iw.close(); } catch(Exception x) {}
			try { if(m_dbc != null) m_dbc.close(); } catch(Exception x) {}
		}
	}

	private void	openDB() throws Exception {
		ConnectionPool	pool = PoolManager.getInstance().definePool("jalpg");
//		ConnectionPool	pool = PoolManager.getInstance().definePool("vpdemo");
		m_dbc = pool.getUnpooledDataSource().getConnection();
		m_dbc.setAutoCommit(false);
	}
}

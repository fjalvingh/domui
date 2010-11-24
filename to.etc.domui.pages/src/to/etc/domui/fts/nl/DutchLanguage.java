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
package to.etc.domui.fts.nl;

import java.util.*;

import to.etc.domui.fts.*;

public class DutchLanguage implements IFtsLanguage {
	/**
	 * List of typical Dutch stopwords.
	 */
	private final static String[] DUTCH_STOP_WORDS = {"de", "en", "van", "ik", "te", "dat", "die", "in", "een", "hij", "het", "niet", "zijn", "is",
			"was", "op", "aan", "met", "als", "voor", "had", "er", "maar", "om", "hem", "dan", "zou", "of", "wat", "mijn", "men", "dit", "zo",
			"door", "over", "ze", "zich", "bij", "ook", "tot", "je", "mij", "uit", "der", "daar", "haar", "naar", "heb", "hoe", "heeft", "hebben",
			"deze", "u", "want", "nog", "zal", "me", "zij", "nu", "ge", "geen", "omdat", "iets", "worden", "toch", "al", "waren", "veel", "meer",
			"doen", "toen", "moet", "ben", "zonder", "kan", "hun", "dus", "alles", "onder", "ja", "eens", "hier", "wie", "werd", "altijd", "doch",
			"wordt", "wezen", "kunnen", "ons", "zelf", "tegen", "na", "reeds", "wil", "kon", "niets", "uw", "iemand", "geweest", "andere"
	};

	private static Set<String>	m_stopset;

	static {
		m_stopset = new HashSet<String>(DUTCH_STOP_WORDS.length*3);
		for(String s: DUTCH_STOP_WORDS)
			m_stopset.add(s);
	}

	private DutchStemmer			m_stemmer;

	public DutchLanguage() {

	}

	public boolean		isStopWord(String s) {
		return m_stopset.contains(s);
	}

	public IStemmer		getStemmer() {
		if(m_stemmer == null)
			m_stemmer = new DutchStemmer();
		return m_stemmer;
	}
}

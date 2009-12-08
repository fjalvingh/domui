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

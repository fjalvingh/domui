package to.etc.util;

import javax.annotation.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4/10/15.
 */
@DefaultNonNull
final public class Lorem {
	@Nullable
	static private String m_lorem;

	static private int	m_lastIndex;

	private Lorem() {}

	/**
	 * Get a full lorem ipsum text containing 200 paragraphs.
	 * @return
	 */
	static private synchronized String getLorem() {
		String lorem = m_lorem;
		if(null == lorem) {
			try {
				lorem = m_lorem = FileTool.readResourceAsString(Lorem.class, "Lorem.txt", "utf-8");
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
		}
		return lorem;
	}

	/**
	 * Get the specified number of lorum sentences (ending in '.'). Sentences are separated
	 * by space. A \n will be inserted every few sentences. The sentences always have the same
	 * start (Lorum ipsum ....).
	 *
	 * @param sentences
	 * @return
	 */
	static public String	getLorumSentences(int sentences) {
		String lorum = getLorem();

		StringBuilder sb = new StringBuilder();
		int pos = 0;
		while(sentences > 0) {
			int epos = lorum.indexOf('.', pos);
			if(epos == -1) {
				pos = 0;						// Start over.
			} else {
				epos++;							// Past .

				sb.append(lorum, pos, epos);
				pos = epos;
				sentences--;
			}
		}
		return sb.toString();
	}

	/**
	 *
	 * @param sentences
	 * @return
	 */
	static synchronized public String getRandomLorumSentences(int sentences) {
		String lorum = getLorem();

		StringBuilder sb = new StringBuilder();
		int pos = m_lastIndex;

		while(sentences > 0) {
			int epos = lorum.indexOf('.', pos);
			if(epos == -1) {
				pos = 0;						// Start over.
			} else {
				epos++;							// Past .
				while(pos < epos && lorum.charAt(pos) == ' ')
					pos++;

				sb.append(lorum, pos, epos);
				pos = epos;
				sentences--;
			}
		}
		m_lastIndex = pos;
		return sb.toString();
	}



	public static void main(String[] args) {
		System.out.println("1: "+getLorumSentences(1));

		System.out.println("2: "+getLorumSentences(2));

		System.out.println("30: "+getLorumSentences(30));

		System.out.println(" ---\n\n");

		for(int i = 0; i < 5; i++) {
			System.out.println("#"+i+" "+getRandomLorumSentences(2));
		}


	}


}

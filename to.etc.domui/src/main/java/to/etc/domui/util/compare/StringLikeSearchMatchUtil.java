package to.etc.domui.util.compare;

import java.util.*;
import java.util.regex.*;

import javax.annotation.*;

/**
 * Encapsulates functionality that simulates 'like' kind of search as in databases.
 *
 * Created by vmijic on 19.8.15..
 */
@DefaultNonNull
public class StringLikeSearchMatchUtil {

	private Map<String, Matcher> m_likeMatcherCache = new HashMap<>();

	public StringLikeSearchMatchUtil(){}

	/**
	 * Does case sensitive match. In case that you need case insensitive match, please do toLowerCases() on input params
	 * @param val
	 * @param match
	 * @return
	 */
	public boolean compareLike(@Nonnull String val, @Nonnull String match) {
		Matcher m = m_likeMatcherCache.get(match);
		if(null == m) {
			//-- Convert to regexp
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < match.length(); i++) {
				char c = match.charAt(i);
				switch(c){
					default:
						sb.append(c);
						break;
					case '.':
					case '*':
					case '+':
					case '\\':
					case '?':
					case '(':
					case ')':
					case '[':
					case ']':
					case '|':
					case '-':
					case '{':
					case '}':
						sb.append("\\");				// Escape
						sb.append(c);
						break;

					case '%':
						sb.append(".*");				// Replace % with .* meta
						break;
				}

			}
			m = Pattern.compile(sb.toString()).matcher("");		// What incredible idiot made this matcher() function!?
			m_likeMatcherCache.put(match, m);
		}
		return m.reset(val).matches();
	}
}

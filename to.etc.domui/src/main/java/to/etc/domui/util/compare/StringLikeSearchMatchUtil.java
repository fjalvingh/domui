package to.etc.domui.util.compare;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates functionality that simulates 'like' kind of search as in databases.
 *
 * Created by vmijic on 19.8.15..
 */
@NonNullByDefault
public class StringLikeSearchMatchUtil {

	private Map<String, Matcher> m_likeMatcherCache = new HashMap<>();

	public StringLikeSearchMatchUtil(){}

	/**
	 * Does case sensitive match. In case that you need case insensitive match, please do toLowerCases() on input params
	 * @param val
	 * @param match
	 * @return
	 */
	public boolean compareLike(@NonNull String val, @NonNull String match) {
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

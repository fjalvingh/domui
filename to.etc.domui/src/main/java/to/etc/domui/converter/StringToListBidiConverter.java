package to.etc.domui.converter;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.binding.IBidiBindingConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converts an input string into a list of strings by separating with spaces.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-02-20.
 */
final public class StringToListBidiConverter implements IBidiBindingConverter<String, List<String>> {
	@Nullable
	@Override
	public String modelToControl(@Nullable List<String> value) throws Exception {
		if(null == value)
			return null;
		StringBuilder sb = new StringBuilder();
		for(String s : value) {
			if(sb.length() > 0)
				sb.append(" ");
			if(hasWhiteSpace(s)) {
				s = s.replace("\"", "\"\"");				// Quote all "" marks
				sb.append("\"").append(s).append("\"");						// And quote the whole thingy.
			} else {
				sb.append(s);
			}
		}
		return sb.toString();
	}

	static private boolean hasWhiteSpace(String s) {
		for(int i = 0; i < s.length(); i++) {
			if(Character.isWhitespace(s.charAt(i)))
				return true;
		}
		return false;
	}

	@Nullable
	@Override
	public List<String> controlToModel(@Nullable String value) throws Exception {
		if(value == null || value.trim().length() == 0)
			return Collections.emptyList();

		//-- Parse the input, and keep quoted strings together
		List<String> res = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		int qc = 0;
		int i = 0;
		while(i < sb.length()) {
			char c = value.charAt(i++);
			if(qc == 0) {
				//-- Not in quote
				if(c == '\"' || c == '\'') {								// Quoting?
					qc = c;
				} else if(Character.isWhitespace(c)) {
					//-- Separator. Add a string if we have one.
					if(sb.length() > 0) {
						String s = sb.toString();
						if(! s.isBlank())
							res.add(s);
						sb.setLength(0);
					}
				} else {
					sb.append(c);
				}
			} else {
				//-- We're inside quotes. Same quote here?
				if(c == qc) {
					//-- Found a quote. Does it terminate the string or is it a double one?
					if(i < value.length() && value.charAt(i) == qc) {
						//-- Double quote -> not terminating.
						i++;						// Skip 2nd quote
						sb.append(c);
					} else {
						qc = 0;
					}
				} else {
					sb.append(c);
				}
			}
		}

		if(sb.length() > 0) {
			String s = sb.toString();
			if(! s.isBlank())
				res.add(s);
		}
		return res;
	}
}

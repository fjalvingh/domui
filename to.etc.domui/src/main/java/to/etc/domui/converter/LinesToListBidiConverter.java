package to.etc.domui.converter;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.binding.IBidiBindingConverter;
import to.etc.util.LineIterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converts an input string with multiple lines (separated by NL) into a list of strings.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-02-20.
 */
final public class LinesToListBidiConverter implements IBidiBindingConverter<String, List<String>> {
	@Nullable
	@Override
	public String modelToControl(@Nullable List<String> value) throws Exception {
		if(null == value)
			return null;
		StringBuilder sb = new StringBuilder();
		for(String s : value) {
			sb.append(s);
			sb.append("\n");
		}
		return sb.toString();
	}

	@Nullable
	@Override
	public List<String> controlToModel(@Nullable String value) throws Exception {
		if(value == null || value.trim().isEmpty())
			return Collections.emptyList();

		//-- Parse the input, and keep quoted strings together
		List<String> res = new ArrayList<>();
		new LineIterator(value).forEach(line -> res.add(line));
		return res;
	}
}

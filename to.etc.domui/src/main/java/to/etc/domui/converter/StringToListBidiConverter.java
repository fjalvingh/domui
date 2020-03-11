package to.etc.domui.converter;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.binding.IBidiBindingConverter;

import java.util.Arrays;
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
		return String.join(" ", value);
	}

	@Nullable
	@Override
	public List<String> controlToModel(@Nullable String value) throws Exception {
		if(value == null || value.trim().length() == 0)
			return Collections.emptyList();
		return Arrays.asList(value.split("\\s+"));
	}
}

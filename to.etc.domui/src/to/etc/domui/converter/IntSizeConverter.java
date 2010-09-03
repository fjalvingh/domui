package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.util.*;

/**
 * Converter to convert a computer "size" to a size in bytes, KB, MB, GB etc.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 30, 2009
 */
public class IntSizeConverter implements IConverter<Integer> {
	@Override
	public String convertObjectToString(Locale loc, Integer in) throws UIException {
		Integer val = in;
		return StringTool.strSize(val.longValue());
	}

	@Override
	public Integer convertStringToObject(Locale loc, String in) throws UIException {
		throw new IllegalStateException("Not implemented yet");
	}
}

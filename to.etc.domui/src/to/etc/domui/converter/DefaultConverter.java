package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.trouble.*;

/**
 * The converter of last resort, accepting anything.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 30, 2008
 */
final public class DefaultConverter implements IConverter<Object> {
	@Override
	public String convertObjectToString(Locale loc, Object in) throws UIException {
		if(in == null)
			return "";
		return String.valueOf(in);
	}

	@Override
	public Object convertStringToObject(Locale loc, String in) throws UIException {
		throw new IllegalStateException("Cannot convert this string '" + in + "' because no IConverterfactory is registered for the type to convert to");
	}
}

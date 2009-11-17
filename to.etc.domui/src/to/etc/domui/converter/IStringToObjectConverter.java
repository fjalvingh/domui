package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.trouble.*;

/**
 * Convert a presentation string to an object (value) instance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 17, 2009
 */
public interface IStringToObjectConverter<T> {
	T convertStringToObject(Locale loc, String in) throws UIException;
}

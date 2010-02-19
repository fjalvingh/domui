package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.trouble.*;

/**
 * Converts any object of type T to a presentation string.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 17, 2009
 */
public interface IObjectToStringConverter<T> {
	String convertObjectToString(Locale loc, T in) throws UIException;
}

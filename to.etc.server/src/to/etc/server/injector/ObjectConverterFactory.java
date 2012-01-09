package to.etc.server.injector;

import java.lang.annotation.*;

import to.etc.util.*;

/**
 * Generic object conversions. This gets used by the JSON calls to convert the
 * map to a structure expected by a call.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 20, 2006
 */
public class ObjectConverterFactory implements InjectorConverterFactory {
	public InjectorConverter accepts(final Class totype, Class fromtype, Annotation[] anar) throws Exception {
		if(fromtype != Object.class)
			return null;

		/**
		 * We accept ANYTHING if a source is Object!
		 */
		return new InjectorConverter() {
			public Object convertValue(Object source) throws Exception {
				return RuntimeConversions.convertToComplex(source, totype);
			}
		};
	}

}

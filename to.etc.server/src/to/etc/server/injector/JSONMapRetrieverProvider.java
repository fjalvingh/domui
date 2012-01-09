package to.etc.server.injector;

import java.lang.annotation.*;
import java.util.*;

import to.etc.server.ajax.*;

/**
 * This provides parameters from a JSON parameter map: a Map<Object, Object>.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 20, 2006
 */
public class JSONMapRetrieverProvider extends AnnotatedRetrieverProvider {

	@Override
	protected Retriever accepts(Class sourcecl, final Class targetcl, final String name, Annotation[] ann, final AjaxParam ap) {
		if(Map.class.isAssignableFrom(sourcecl)) {
			return new AnnotatedRetriever(Object.class, name, ap) {
				@Override
				protected Object retrieveValuePrimitive(Object source) throws Exception {
					assert (source instanceof Map);
					Map map = (Map) source;
					Object val = map.get(name);
					if(val == null) {
						if(map.containsKey(name))
							return null;
						else
							return NO_VALUE;
					}

					//-- FIXME Basic convertions anyone?
					return val;
				}

				@Override
				public void releaseObject(Object o) {
				}

				public String getDisplayName() {
					return "json[" + name + "]";
				}
			};
		}
		return null;
	}
}

package to.etc.server.injector;

import java.lang.annotation.*;
import java.lang.reflect.*;

import to.etc.server.ajax.*;

/**
 * This checks to see if the bean passed has a getter for the
 * specified property. If so it returns a retriever which uses 
 * that getter to obtain a value for the object.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 11, 2006
 */
public class BeanRetrieverProvider extends AnnotatedRetrieverProvider {
	static final Class[]	NOARGS	= new Class[0];

	@Override
	protected Retriever accepts(Class sourcecl, Class targetcl, final String name, Annotation[] ann, AjaxParam ap) {
		String methodname = Injector.makeMethodName("get", name);
		try {
			final Method m = sourcecl.getMethod(methodname, (Class[]) null);

			//-- Getter was found.. If proper then use,
			if(!Modifier.isPublic(m.getModifiers()))
				return null;

			//-- Return a new bean getter.
			return new AnnotatedRetriever(m.getReturnType(), name, ap) {
				@Override
				protected Object retrieveValuePrimitive(Object source) throws Exception {
					try {
						return m.invoke(source, (Object[]) NOARGS);
					} catch(InvocationTargetException itx) {
						throw new ParameterException("The retriever for the injector parameter '" + name + "' caused an exception", itx.getCause()).setParameterName(name);
					} catch(Exception itx) {
						throw new ParameterException("The retriever for the injector parameter '" + name + "' caused an exception", itx).setParameterName(name);
					}
				}

				@Override
				public void releaseObject(Object o) {
				}

				public String getDisplayName() {
					return m.toGenericString();
				}
			};
		} catch(Exception x) {
			return null;
		}
	}
}

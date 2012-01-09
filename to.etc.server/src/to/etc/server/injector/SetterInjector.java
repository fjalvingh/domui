package to.etc.server.injector;

import java.lang.reflect.*;

/**
 * Encapsulates a call to set a value onto an object by using a
 * setter method.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 12, 2006
 */
final public class SetterInjector implements ObjectReleaser {
	/** The method to call to set the value */
	private Method				m_setterMethod;

	/** The retriever which gets a value from the source object. */
	private Retriever			m_retriever;

	/** The provider which returns the value. */
	private InjectorConverter	m_provider;

	public SetterInjector(Method m, Retriever r, InjectorConverter p) {
		m_retriever = r;
		m_setterMethod = m;
		m_provider = p;
	}

	public void releaseObject(Object v) {
		m_retriever.releaseObject(v);
	}

	public Object apply(Object source, Object obj) throws Exception {
		//-- Get a value from the retriever
		Object value = null;
		boolean ok = false;
		try {
			try {
				value = m_retriever.retrieveValue(source); // Retrieve a value
			} catch(Exception x) {
				x.printStackTrace();
				throw new ParameterException("Can't get value from '" + m_retriever.getDisplayName() + "' to set " + m_setterMethod, x).setParameterValue(value).setParameterName(
					m_setterMethod.getName());
			}

			// Call the converter if needed,
			Object conv;
			try {
				conv = m_provider == null ? value : m_provider.convertValue(value);
			} catch(Exception x) {
				x.printStackTrace();
				throw new ParameterException("Can't convert value from '" + m_retriever.getDisplayName() + "' to set " + m_setterMethod).setParameterValue(value).setParameterName(
					m_setterMethod.getName()).setParameterValue(value);
			}

			//-- Then call the setter.
			try {
				m_setterMethod.invoke(obj, conv);
				ok = true;
				return value;
			} catch(InvocationTargetException itx) {
				Throwable t = itx.getCause();
				if(t instanceof Exception)
					throw (Exception) t;
				throw itx;
			}
		} finally {
			try {
				if(!ok)
					m_retriever.releaseObject(value);
			} catch(Exception x) {}
		}
	}
}

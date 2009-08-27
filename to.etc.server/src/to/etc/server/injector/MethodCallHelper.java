package to.etc.server.injector;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import to.etc.server.ajax.*;

/**
 * This helps with actually calling a method and binding it's
 * parameters.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 17, 2006
 */
public class MethodCallHelper {
	static private final Logger			LOG	= Logger.getLogger(MethodCallHelper.class.getName());

	private List<ParameterInjectorSet>	m_pisar;

	/** The method to call. */
	private Method						m_method;

	/** The converters that provide for the method's parameters, indexed by parameter#. */
	private InjectorConverter[]			m_providers;

	/** The retrievers that get the raw values. */
	private Retriever[]					m_retrievers;

	/** The values returned by the retrievers; these are the ones that need to be discarded by the retriever after the call. */
	private Object[]					m_values;

	/** The actual values to use for the call; the result of a converter if there was one. */
	private Object[]					m_params;

	public MethodCallHelper(Method m, List<ParameterInjectorSet> l) {
		m_method = m;
		m_pisar = l;

		//-- Allot all arrays.
		int pc = m.getParameterTypes().length;
		m_providers = new InjectorConverter[pc];
		m_retrievers = new Retriever[pc];
		m_values = new Object[pc];
		m_params = new Object[pc];
	}

	public int getParameterCount() {
		return m_providers.length;
	}

	public void setParameter(int ix, Object value) {
		m_params[ix] = value;
	}

	/**
	 * Walks the parameter injector set list and retrieves all parameters needed for that.
	 * @param sr
	 */
	public void calculateParameters(InjectorSourceRetriever sr) throws Exception {
		boolean ok = false;
		int param = 0;
		try {
			for(int i = m_pisar.size(); --i >= 0;) {
				param = -1;
				ParameterInjectorSet pis = m_pisar.get(i);

				//-- Walk all of the thingies in this set,
				Object source = sr.getInjectorSource(pis.getSourceClass()); // Get the object representing a source
				for(int j = getParameterCount(); --j >= 0;) {
					param = j;
					if(m_retrievers[i] == null) {
						Retriever re = pis.getRetrievers()[j];
						if(re != null) {
							//-- This one provides for this parameter.
							m_retrievers[j] = re;
							Object val = re.retrieveValue(source);
							m_values[j] = val;
							InjectorConverter co = pis.getConverters()[j];
							if(co != null) {
								m_providers[j] = co;
								m_params[j] = co.convertValue(val);
							} else
								m_params[j] = val;
						}
					}
				}
			}
			ok = true;
		} catch(Exception x) {
			if(x instanceof InvocationTargetException) {
				Throwable t = x.getCause();
				if(t instanceof Exception)
					x = (Exception) t;
			}
			throw new ParameterException("Exception in provider for parameter " + param + "\nof " + m_method.toString() + "\n" + x, x).setParameterIndex(param).setHandlerMethod(m_method);
		} finally {
			if(!ok)
				release();
		}
	}

	public void release() {
		for(int i = m_retrievers.length; --i >= 0;) {
			if(m_retrievers[i] != null) {
				if(m_values[i] != null) {
					try {
						m_retrievers[i].releaseObject(m_values[i]);
					} catch(Exception x) {
						LOG.log(Level.SEVERE, "Exception while releasing retrieved object", x);
					}
					m_values[i] = null;
					m_retrievers[i] = null;
					m_providers[i] = null;
					m_params[i] = null;
				}
			}
		}
	}

	/**
	 * Called when the prepare call has been done manually.
	 *
	 * @param serviceinstance
	 * @return
	 * @throws Exception
	 */
	public Object invoke(Object serviceinstance) throws Exception {
		try {
			return m_method.invoke(serviceinstance, m_params);
		} catch(IllegalArgumentException x) {
			//-- Somehow the parameters were mismatched - dump them....
			StringBuilder sb = new StringBuilder();
			sb.append(x.toString());
			sb.append("\nThe parameter assignments were:\n");
			Class<Object>[] far = (Class<Object>[]) m_method.getParameterTypes();
			for(int i = 0; i < far.length; i++) {
				sb.append("#");
				sb.append(i + 1);
				sb.append(": formal=");
				sb.append(far[i].getName());
				sb.append(", actual=");
				if(m_params[i] == null)
					sb.append("null");
				else
					sb.append(m_params[i].getClass().getName());
				sb.append("\n");
			}
			throw new ServiceException(sb.toString());
		} catch(Exception x) {
			if(x instanceof InvocationTargetException) {
				Throwable t = x.getCause();
				if(t instanceof Exception)
					x = (Exception) t;
			}
			throw new AjaxHandlerException("Exception in call " + m_method.toString() + ": " + x, x);
		} finally {
			release();
		}
	}

	public Object invoke(Object serviceinstance, InjectorSourceRetriever sr) throws Exception {
		calculateParameters(sr);
		return invoke(serviceinstance);
	}
}

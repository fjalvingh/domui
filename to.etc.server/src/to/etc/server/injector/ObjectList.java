package to.etc.server.injector;

/**
 * Encapsulates a set of allocated objects, and the way to discard them.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 13, 2006
 */
public class ObjectList {
	/** The allocated objects returned by the retriever. */
	private Object[]			m_values;

	/** The allocated objects returned by the converter. */
	private Object[]			m_parameters;

	private ObjectReleaser[]	m_releasers;

	ObjectList(Object[] va, Object[] pa, ObjectReleaser[] pro) {
		m_values = va;
		m_parameters = pa;
		m_releasers = pro;
	}

	public int size() {
		return m_parameters.length;
	}

	public Object getParameter(int ix) {
		return m_parameters[ix];
	}

	public Object[] getParameters() {
		return m_parameters;
	}

	public Object[] getValues() {
		return m_values;
	}

	/**
	 * Can be called with a (partially) filled parameter array. This calls all of
	 * the "release" handlers on the providers and cleans out the array.
	 * @param params
	 */
	public void release() {
		if(m_parameters == null)
			return;
		for(int i = m_values.length; --i >= 0;) {
			try {
				if(m_values[i] != null) {
					m_releasers[i].releaseObject(m_values[i]);
					m_values[i] = null;
				}
			} catch(Exception x) {}
		}
		m_parameters = null;
		m_releasers = null;
	}
}

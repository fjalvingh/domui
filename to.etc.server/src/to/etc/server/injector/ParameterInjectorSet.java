package to.etc.server.injector;

/**
 * A set of injector definitions: all parameters that need to be injected from
 * a single source class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 17, 2006
 */
final public class ParameterInjectorSet {
	/** The source class that provides values for these setters. */
	private Class				m_sourceClass;

	/** The converters that provide for the method's parameters, indexed by parameter#. */
	private InjectorConverter[]	m_converters;

	/** The retrievers that get the raw values. A null retriever at a given index means this does not provide for that parameter. */
	private Retriever[]			m_retrievers;

	public ParameterInjectorSet(Class source, Retriever[] r, InjectorConverter[] co) {
		m_sourceClass = source;
		m_retrievers = r;
		m_converters = co;
	}

	public Class getSourceClass() {
		return m_sourceClass;
	}

	public InjectorConverter[] getConverters() {
		return m_converters;
	}

	public Retriever[] getRetrievers() {
		return m_retrievers;
	}
}

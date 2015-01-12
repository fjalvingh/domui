package to.etc.domui.logic.errors;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

/**
 * An actual occurrence of a {@link Problem} that was found to occur on some instance
 * or some property of the model. Problem instances are reported when {@link Problem#on(ProblemModel, Object)}
 * or one of it's alternatives is called, and are registered inside the {@link ProblemModel} that was
 * used to call the logic.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 8, 2014
 */
public class ProblemInstance {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		Object instance = m_instance;
		result = prime * result + ((instance == null) ? 0 : instance.hashCode());
		Problem problem = m_problem;
		result = prime * result + ((problem == null) ? 0 : problem.hashCode());
		PropertyMetaModel< ? > property = m_property;
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		return result;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		ProblemInstance other = (ProblemInstance) obj;
		Object instance = m_instance;
		if(instance == null) {
			if(other.m_instance != null)
				return false;
		} else if(!instance.equals(other.m_instance))
			return false;
		if(m_problem == null) {
			if(other.m_problem != null)
				return false;
		} else if(!m_problem.equals(other.m_problem))
			return false;
		PropertyMetaModel< ? > property = m_property;
		if(property == null) {
			if(other.m_property != null)
				return false;
		} else if(!property.equals(other.m_property))
			return false;
		return true;
	}

	static private final Object[] NONE = new Object[0];

	final private Problem m_problem;

	final private Object m_instance;

	@Nullable
	final private PropertyMetaModel< ? > m_property;

	/** Any message parameters, if applicable. */
	private Object[] m_parameters = NONE;

	ProblemInstance(Problem problem, Object instance, PropertyMetaModel< ? > property) {
		m_problem = problem;
		m_instance = instance;
		m_property = property;
	}

	ProblemInstance(Problem problem, Object instance) {
		m_problem = problem;
		m_instance = instance;
		m_property = null;
	}

	public Problem getProblem() {
		return m_problem;
	}

	public Object getInstance() {
		return m_instance;
	}

	@Nullable
	public PropertyMetaModel< ? > getProperty() {
		return m_property;
	}

	public Object[] getParameters() {
		return m_parameters;
	}

	/**
	 * Add message parameters to the error.
	 * @param arguments
	 * @return
	 */
	public ProblemInstance using(Object... arguments) {
		if(m_parameters.length == 0)
			m_parameters = arguments;
		else if(arguments.length > 0) {
			Object[] initial = new Object[arguments.length + m_parameters.length];
			int index = 0;
			for(Object o : m_parameters)
				initial[index++] = o;
			for(Object o : arguments)
				initial[index++] = o;
			m_parameters = initial;
		}
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(MetaManager.identify(m_instance));
		PropertyMetaModel< ? > pmm = m_property;
		if(null != pmm) {
			sb.append(".").append(pmm.getName());
		}
		sb.append(' ');
		sb.append(m_problem.toString());
		return sb.toString();
	}
}

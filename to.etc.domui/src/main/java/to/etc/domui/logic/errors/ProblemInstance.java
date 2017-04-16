package to.etc.domui.logic.errors;

import java.util.*;

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
@DefaultNonNull
final public class ProblemInstance {
	static private final Object[] NONE = new Object[0];

	final private Problem m_problem;

	final private Object m_instance;

	@Nullable
	final private PropertyMetaModel< ? > m_property;

	/** The set of non-identifying parameters. These are used to "parameterize" the message, but are not part of the instance's identity. */
	private Object[] m_parameters = NONE;

	private Object[] m_identifyingParameters = NONE;

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
	public void using(Object... arguments) {
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
		if(m_problem.isRepeatable())
			m_identifyingParameters = m_parameters;
		//return this;
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

	@Nonnull
	public String getMessage() {
		return getProblem().getBundle().formatMessage(getProblem().getCode(), getParameters());
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;

		ProblemInstance that = (ProblemInstance) o;

		if(!m_problem.equals(that.m_problem))
			return false;
		if(!m_instance.equals(that.m_instance))
			return false;
		if(m_property != null ? !m_property.equals(that.m_property) : that.m_property != null)
			return false;
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		return Arrays.equals(m_identifyingParameters, that.m_identifyingParameters);

	}

	@Override
	public int hashCode() {
		int result = m_problem.hashCode();
		result = 31 * result + m_instance.hashCode();
		result = 31 * result + (m_property != null ? m_property.hashCode() : 0);
		result = 31 * result + Arrays.hashCode(m_identifyingParameters);
		return result;
	}
}

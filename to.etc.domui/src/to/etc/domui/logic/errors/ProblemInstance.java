package to.etc.domui.logic.errors;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;

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
	static private final Object[] NONE = new Object[0];

	final private Problem m_problem;

	final private Object m_instance;

	@Nullable
	final private PropertyMetaModel< ? > m_property;

	/** Any message parameters, if applicable. */
	private Object[] m_parameters = NONE;

	private MsgType m_severity = MsgType.ERROR;

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

	public PropertyMetaModel< ? > getProperty() {
		return m_property;
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

	/**
	 * Set the severity to warning.
	 * @return
	 */
	public ProblemInstance warning() {
		m_severity = MsgType.WARNING;
		return this;
	}

	/**
	 * Set the severity to error (it is that by default).
	 * @return
	 */
	public ProblemInstance error() {
		m_severity = MsgType.ERROR;
		return this;
	}

	/**
	 * Set the severity to info.
	 * @return
	 */
	public ProblemInstance info() {
		m_severity = MsgType.INFO;
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
		sb.append(m_severity);
		sb.append(' ');
		sb.append(m_problem.toString());
		return sb.toString();
	}
}

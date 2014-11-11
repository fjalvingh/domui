package to.etc.domui.logic.errors;

import javax.annotation.*;
import javax.annotation.concurrent.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.webapp.nls.*;

/**
 * EXPERIMENTAL
 * Definition of a possible error/message. The message is uniquely identifyable by a package name combined
 * with a message code. The package name is obtained from a class passed in when defining the problem, and
 * is used to retrieve the resource bundle for obtaining the message's string. The bundle is hard defined
 * as residing in the specified package with a name of "messages".
 *
 * <p>A problem is switched "on" or "off" on any part of a model. Any model part (model, instance, instance-property)
 * can have any list of switched-on problems, but every Problem instance can exist only once in each of those.
 * A problem can be reported as-is, but can also have parameters that will be used to construct the message. These
 * parameters however are <b>not</b> part of the identity of the problem; even when parameters differ can you not
 * report the same problem multiple times on the same model part.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 7, 2014
 */
@Immutable
final public class Problem {
	final private BundleRef m_bundle;

	final private String m_code;

	/** Interned unique key for this error. */
	final private String m_key;

	final private MsgType m_severity;

	public Problem(Class< ? > anchor, String code) {
		m_bundle = BundleRef.create(anchor, "messages");				// All problem messages must be in a bundle called messages.
		m_key = m_bundle.getBundleKey() + "#" + code;
		m_code = code;
		m_severity = MsgType.ERROR;
	}

	public String getCode() {
		return m_code;
	}

	/**
	 * Return a unique key for the message as "bundle name" '#' "code". The bundle name is defined as the bundle's class package + '.' + message file name without extension
	 * @return
	 */
	public String getMessageKey() {
		return m_key;
	}

	public MsgType getSeverity() {
		return m_severity;
	}

	public BundleRef getBundle() {
		return m_bundle;
	}

	/**
	 * Switch off this error: remove it from the error(s) list.
	 * @param errors
	 */
	public <T> void off(@Nonnull ProblemModel errors, @Nonnull T instance) {
		errors.clear(this, instance, null);
	}

	public <T, P> void off(@Nonnull ProblemModel errors, @Nonnull T instance, @Nonnull PropertyMetaModel<P> property) {
		errors.clear(this, instance, property);
	}

	public <T, P> void off(@Nonnull ProblemModel errors, @Nonnull T instance, @Nonnull String property) {
		errors.clear(this, instance, MetaManager.findPropertyMeta(instance.getClass(), property));
	}

	/**
	 * Report this error on a specific instance only.
	 * @param errors
	 * @param instance
	 * @return
	 */
	public <T> ProblemInstance on(@Nonnull ProblemModel errors, @Nonnull T instance) {
		ProblemInstance pi = new ProblemInstance(this, instance);
		errors.addProblem(pi);
		return pi;										// Allow specialization using builder pattern.
	}

	/**
	 * Report this error on the specified instance's property.
	 * @param errors
	 * @param instance
	 * @param property
	 * @return
	 */
	public <T, P> ProblemInstance on(@Nonnull ProblemModel errors, @Nonnull T instance, @Nonnull PropertyMetaModel<P> property) {
		ProblemInstance pi = new ProblemInstance(this, instance, property);
		errors.addProblem(pi);
		return pi;										// Allow specialization using builder pattern.
	}

	public <T> ProblemInstance on(@Nonnull ProblemModel errors, @Nonnull T instance, @Nonnull String property) {
		ProblemInstance pi = new ProblemInstance(this, instance, MetaManager.getPropertyMeta(instance.getClass(), property));
		errors.addProblem(pi);
		return pi;										// Allow specialization using builder pattern.
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_key == null) ? 0 : m_key.hashCode());
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
		Problem other = (Problem) obj;
		if(m_key == null) {
			if(other.m_key != null)
				return false;
		} else if(!m_key.equals(other.m_key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return m_severity + " " + m_key;
	}
}

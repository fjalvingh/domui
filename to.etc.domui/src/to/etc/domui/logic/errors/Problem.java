package to.etc.domui.logic.errors;

import javax.annotation.*;
import javax.annotation.concurrent.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.logic.*;

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
	final private String m_anchor;

	final private String m_code;

	/** Interned unique key for this error. */
	final private String m_key;

	public Problem(Class< ? > anchor, String code) {
		String name = anchor.getName();
		m_anchor = name.substring(name.lastIndexOf('.') + 1);
		m_code = code;
		m_key = (m_anchor + "/" + m_code).intern();
	}

	/**
	 * Get the problem anchor (the reference to whatever class is reporting the error).
	 * @return
	 */
	public String getAnchor() {
		return m_anchor;
	}

	public String getCode() {
		return m_code;
	}

	/**
	 * Switch off this error: remove it from the error(s) list.
	 * @param errors
	 */
	public <T> void off(@Nonnull LogiErrors errors, @Nonnull T instance) {
		// errors.remove(this, instance, null);			INCO Must fix
	}

	public <T, P> void off(@Nonnull LogiErrors errors, @Nonnull T instance, @Nonnull PropertyMetaModel<P> property) {
		// errors.remove(this, instance, null);			INCO Must fix
	}

	public <T, P> void off(@Nonnull LogiErrors errors, @Nonnull T instance, @Nonnull String property) {
		// errors.remove(this, instance, null);			INCO Must fix
	}

	/**
	 * Report this error on a specific instance only.
	 * @param errors
	 * @param instance
	 * @return
	 */
	public <T> ProblemInstance on(@Nonnull LogiErrors errors, @Nonnull T instance) {
		ProblemInstance pi = new ProblemInstance(this, instance);
		// errors.addProblem(pi);			INCO Report on model
		return pi;										// Allow specialization using builder pattern.
	}

	/**
	 * Report this error on the specified instance's property.
	 * @param errors
	 * @param instance
	 * @param property
	 * @return
	 */
	public <T, P> ProblemInstance on(@Nonnull LogiErrors errors, @Nonnull T instance, @Nonnull PropertyMetaModel<P> property) {
		ProblemInstance pi = new ProblemInstance(this, instance, property);
		// errors.addProblem(pi);			INCO Report on model
		return pi;										// Allow specialization using builder pattern.
	}

	public <T> ProblemInstance on(@Nonnull LogiErrors errors, @Nonnull T instance, @Nonnull String property) {
		ProblemInstance pi = new ProblemInstance(this, instance, MetaManager.findPropertyMeta(instance.getClass(), property));
		// errors.addProblem(pi);			INCO Report on model
		return pi;										// Allow specialization using builder pattern.
	}

}

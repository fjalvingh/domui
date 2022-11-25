package to.etc.domui.logic.errors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.errors.MsgType;
import to.etc.webapp.nls.IBundleCode;

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
@NonNullByDefault
public class Problem {
	final private IBundleCode m_code;

	/**
	 * Interned unique key for this error.
	 */
	final private String m_key;

	final private MsgType m_severity;

	/**
	 * When set, this same problem can be reported multiple times on a single target.
	 */
	final private boolean m_repeatable;

	public Problem(IBundleCode code, MsgType severity, boolean repeatable) {
		m_code = code;
		m_severity = severity;
		m_repeatable = repeatable;
		m_key = code.getBundle().getBundleKey() + "#" + code;
	}

	public Problem(IBundleCode code) {
		this(code, MsgType.ERROR, false);
	}

	//static public Problem warning(Class<?> anchor, String code) {
	//	return new Problem(anchor, code, MsgType.WARNING, false);
	//}

	//static public Problem warningList(Class<?> anchor, String code) {
	//	return new Problem(anchor, code, MsgType.WARNING, true);
	//}

	//static public Problem error(Class<?> anchor, String code) {
	//	return new Problem(anchor, code, MsgType.ERROR, false);
	//}

	//static public Problem errorList(Class<?> anchor, String code) {
	//	return new Problem(anchor, code, MsgType.ERROR, true);
	//}

	public boolean isRepeatable() {
		return m_repeatable;
	}

	public IBundleCode getCode() {
		return m_code;
	}

	/**
	 * Return a unique key for the message as "bundle name" '#' "code". The bundle name is defined as the bundle's class package + '.' + message file name without extension
	 */
	public String getMessageKey() {
		return m_key;
	}

	public MsgType getSeverity() {
		return m_severity;
	}

	/**
	 * Switch off this error: remove it from the error(s) list.
	 */
	public <T> void off(@NonNull ProblemModel errors, @NonNull T instance) {
		errors.clear(this, instance, null);
	}

	public <T, P> void off(@NonNull ProblemModel errors, @NonNull T instance, @NonNull PropertyMetaModel<P> property) {
		errors.clear(this, instance, property);
	}

	public <T, P> void off(@NonNull ProblemModel errors, @NonNull T instance, @NonNull String property) {
		errors.clear(this, instance, MetaManager.findPropertyMeta(instance.getClass(), property));
	}

	/**
	 * Report this error on a specific instance only.
	 */
	public <T> ProblemInstance on(@NonNull ProblemModel errors, @NonNull T instance) {
		//System.out.println("error " + toString());
		ProblemInstance pi = new ProblemInstance(this, instance);
		errors.addProblem(pi);
		return pi;                                        // Allow specialization using builder pattern.
	}

	/**
	 * Report this error on the specified instance's property.
	 */
	@NonNull
	public <T, P> ProblemInstance on(@NonNull ProblemModel errors, @NonNull T instance, @NonNull PropertyMetaModel<P> property) {
		ProblemInstance pi = new ProblemInstance(this, instance, property);
		errors.addProblem(pi);
		return pi;                                        // Allow specialization using builder pattern.
	}

	@NonNull
	public <T> ProblemInstance on(@NonNull ProblemModel errors, @NonNull T instance, @NonNull String property) {
		ProblemInstance pi = new ProblemInstance(this, instance, MetaManager.getPropertyMeta(instance.getClass(), property));
		errors.addProblem(pi);
		return pi;                                        // Allow specialization using builder pattern.
	}

	public <T> ProblemInstance when(@NonNull ProblemModel errors, @NonNull T instance, @NonNull String property, boolean condition) {
		if(condition) {
			return on(errors, instance, property);
		} else {
			off(errors, instance, property);
			return new ProblemInstance(this, instance); // To prevent returning null, this returns a 'dummy' ProblemInstance which will never be used
		}
	}

	public <T> ProblemInstance when(@NonNull ProblemModel errors, @NonNull T instance, boolean condition) {
		if(condition) {
			return on(errors, instance);
		} else {
			off(errors, instance);
			return new ProblemInstance(this, instance); // To prevent returning null, this returns a 'dummy' ProblemInstance which will never be used.
		}
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
			return other.m_key == null;
		} else
			return m_key.equals(other.m_key);
	}

	@Override
	public String toString() {
		return m_severity + " " + m_key;
	}
}

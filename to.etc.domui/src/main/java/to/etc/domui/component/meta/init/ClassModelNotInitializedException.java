package to.etc.domui.component.meta.init;

/**
 * Thrown when a metamodel enhancer wants to use a class that is as yet
 * undefined. When thrown from metamodel initialization this will cause
 * the initializer to first try to initialize the class, then return to
 * the failed action.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-10-17.
 */
final public class ClassModelNotInitializedException extends RuntimeException {
	final private Object m_target;

	public ClassModelNotInitializedException(Object target) {
		super("ClassMetaModel for " + target + " not yet initialized - the operation must be retried later");
		m_target = target;
	}

	public Object getTarget() {
		return m_target;
	}
}

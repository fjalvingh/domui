package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNull;

import java.io.Serializable;

/**
 * A saved page from a destroyed {@link WindowSession}, suitable to be resurrected
 * inside a new window session after a development time reload.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 27, 2013
 */
//@Immutable
final public class SavedPage implements Serializable {
	@NonNull
	final private String m_className;

	@NonNull
	final private IPageParameters m_parameters;

	SavedPage(@NonNull String className, @NonNull IPageParameters parameters) {
		m_className = className;
		m_parameters = parameters;
	}

	@NonNull
	public String getClassName() {
		return m_className;
	}

	@NonNull
	public IPageParameters getParameters() {
		return m_parameters;
	}

	@Override
	public String toString() {
		return getClassName() + "[" + getParameters() + "]";
	}
}

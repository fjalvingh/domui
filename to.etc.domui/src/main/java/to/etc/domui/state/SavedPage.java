package to.etc.domui.state;

import java.io.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;

/**
 * A saved page from a destroyed {@link WindowSession}, suitable to be resurrected
 * inside a new window session after a development time reload.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 27, 2013
 */
@Immutable
final public class SavedPage implements Serializable {
	@Nonnull
	final private String m_className;

	@Nonnull
	final private IPageParameters m_parameters;

	SavedPage(@Nonnull String className, @Nonnull IPageParameters parameters) {
		m_className = className;
		m_parameters = parameters;
	}

	@Nonnull
	public String getClassName() {
		return m_className;
	}

	@Nonnull
	public IPageParameters getParameters() {
		return m_parameters;
	}

	@Override
	public String toString() {
		return getClassName() + "[" + getParameters() + "]";
	}
}

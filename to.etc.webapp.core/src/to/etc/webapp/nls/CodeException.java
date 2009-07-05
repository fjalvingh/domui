package to.etc.webapp.nls;

import java.text.*;
import java.util.*;

/**
 * FIXME Must move to a separate NLS package.
 * Base class for all code-based exceptions.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 31, 2009
 */
public class CodeException extends RuntimeException {
	private final BundleRef m_bundle;

	private final String m_code;

	private final Object[] m_parameters;

	public CodeException(final BundleRef bundle, final String code, final Object... parameters) {
		m_bundle = bundle;
		m_code = code;
		m_parameters = parameters;
	}

	public CodeException(final String code, final Object... parameters) {
		m_code = code;
		m_parameters = parameters;
		m_bundle = null;
	}

	public CodeException(final Throwable t, final BundleRef bundle, final String code, final Object... parameters) {
		super(t);
		m_bundle = bundle;
		m_code = code;
		m_parameters = parameters;
	}

	public CodeException(final Throwable t, final String code, final Object... parameters) {
		super(t);
		m_code = code;
		m_parameters = parameters;
		m_bundle = null;
	}

	public BundleRef getBundle() {
		return m_bundle;
	}

	public String getCode() {
		return m_code;
	}

	public Object[] getParameters() {
		return m_parameters;
	}

	@Override
	public String getMessage() {
		if(m_bundle == null)
			return NlsContext.getGlobalMessage(m_code, m_parameters);
		Locale loc = NlsContext.getLocale();
		String msg = m_bundle.getString(m_code);
		MessageFormat temp = new MessageFormat(msg, loc); // SUN people are dumb. It's idiotic to have to create an object for this.
		return temp.format(m_parameters);
	}
}

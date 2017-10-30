package to.etc.domui.parts;

import to.etc.domui.server.BrowserVersion;
import to.etc.domui.server.IExtendedParameterInfo;
import to.etc.domui.server.IParameterInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-5-17.
 */
public class ParameterInfoProxy implements IExtendedParameterInfo {
	private final IParameterInfo m_delegate;

	public ParameterInfoProxy(IParameterInfo delegate) {
		m_delegate = delegate;
	}

	@Override @Nullable public String getParameter(@Nonnull String name) {
		return m_delegate.getParameter(name);
	}

	@Override @Nonnull public String[] getParameters(@Nonnull String name) {
		return m_delegate.getParameters(name);
	}

	@Override @Nonnull public String[] getParameterNames() {
		return m_delegate.getParameterNames();
	}

	@Override @Nonnull public String getInputPath() {
		return m_delegate.getInputPath();
	}

	@Override public BrowserVersion getBrowserVersion() {
		if(m_delegate instanceof IExtendedParameterInfo)
			return ((IExtendedParameterInfo) m_delegate).getBrowserVersion();
		return null;
	}

	@Nullable @Override public String getThemeName() {
		if(m_delegate instanceof IExtendedParameterInfo)
			return ((IExtendedParameterInfo) m_delegate).getThemeName();
		return null;
	}
}

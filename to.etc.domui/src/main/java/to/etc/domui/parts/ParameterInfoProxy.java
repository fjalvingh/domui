package to.etc.domui.parts;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.server.BrowserVersion;
import to.etc.domui.server.IExtendedParameterInfo;
import to.etc.domui.server.IParameterInfo;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-5-17.
 */
public class ParameterInfoProxy implements IExtendedParameterInfo {
	private final IParameterInfo m_delegate;

	public ParameterInfoProxy(IParameterInfo delegate) {
		m_delegate = delegate;
	}

	@Override @Nullable public String getParameter(@NonNull String name) {
		return m_delegate.getParameter(name);
	}

	@Override @NonNull public String[] getParameters(@NonNull String name) {
		return m_delegate.getParameters(name);
	}

	@Override @NonNull public String[] getParameterNames() {
		return m_delegate.getParameterNames();
	}

	@Override @NonNull public String getInputPath() {
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

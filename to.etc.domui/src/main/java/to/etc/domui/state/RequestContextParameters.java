package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.server.BrowserVersion;
import to.etc.domui.server.IExtendedParameterInfo;
import to.etc.domui.server.RequestContextImpl;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-10-19.
 */
final public class RequestContextParameters extends PageParameterWrapper implements IExtendedParameterInfo {
	private final RequestContextImpl m_ctx;

	public RequestContextParameters(RequestContextImpl ctx) {
		super(new RequestContextParameterContainer(ctx));
		m_ctx = ctx;
	}

	@NonNull
	@Override
	public String getInputPath() {
		return m_ctx.getInputPath();
	}

	@NonNull
	@Override
	public BrowserVersion getBrowserVersion() {
		return m_ctx.getBrowserVersion();
	}

	@Nullable
	@Override
	public String getThemeName() {
		return m_ctx.getThemeName();
	}
}

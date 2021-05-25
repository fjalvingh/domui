package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.dom.html.Page;
import to.etc.domui.server.RequestContextImpl;

@NonNullByDefault
public class UIRedirectContext {
	private final RequestContextImpl m_ctx;

	private final MoveMode m_targetMode;

	private final Page m_to;

	private final boolean m_ajax;

	public UIRedirectContext(RequestContextImpl ctx, MoveMode targetMode, Page to, boolean ajax) {
		m_ctx = ctx;
		m_targetMode = targetMode;
		m_to = to;
		m_ajax = ajax;
	}

	public RequestContextImpl getCtx() {
		return m_ctx;
	};

	public MoveMode getTargetMode() {
		return m_targetMode;
	};

	public Page getTo() {
		return m_to;
	}

	public boolean isAjax() {
		return m_ajax;
	};
}

package to.etc.domui.server;

import javax.annotation.*;
import javax.servlet.http.*;

public class HttpServerSession implements IServerSession {
	@Nonnull
	final private HttpSession m_session;

	public HttpServerSession(@Nonnull HttpSession session) {
		m_session = session;
	}

	@Override
	@Nullable
	public Object getAttribute(@Nonnull String name) {
		return m_session.getAttribute(name);
	}

	@Override
	public void setAttribute(@Nonnull String name, @Nullable Object value) {
		if(null == value)
			m_session.removeAttribute(name);
		else
			m_session.setAttribute(name, value);
	}
}

package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import javax.servlet.http.HttpSession;

public class HttpServerSession implements IServerSession {
	@NonNull
	final private HttpSession m_session;

	public HttpServerSession(@NonNull HttpSession session) {
		m_session = session;
	}

	@Override
	@NonNull
	public String getId() {
		return m_session.getId();
	}

	@Override
	@Nullable
	public Object getAttribute(@NonNull String name) {
		return m_session.getAttribute(name);
	}

	@Override
	public void setAttribute(@NonNull String name, @Nullable Object value) {
		if(null == value)
			m_session.removeAttribute(name);
		else
			m_session.setAttribute(name, value);
	}

	@Override
	public void invalidate() {
		m_session.invalidate();
	}

	@Override
	public boolean isNew() {
		return m_session.isNew();
	}
}

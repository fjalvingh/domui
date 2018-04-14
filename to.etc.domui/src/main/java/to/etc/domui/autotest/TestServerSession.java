package to.etc.domui.autotest;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.server.IServerSession;

import java.util.HashMap;
import java.util.Map;

public class TestServerSession implements IServerSession {
	static private int m_idcount;

	@NonNull
	final private String m_id;

	@NonNull
	private final Map<String, Object> m_data = new HashMap<String, Object>();

	public TestServerSession() {
		m_id = "sess" + nextID();
	}

	@Override
	@NonNull
	public String getId() {
		return m_id;
	}

	static synchronized int nextID() {
		return ++m_idcount;
	}

	@Override
	@Nullable
	public Object getAttribute(@NonNull String name) {
		return m_data.get(name);
	}

	@Override
	public void setAttribute(@NonNull String name, @Nullable Object value) {
		m_data.put(name, value);
	}

	@Override
	public void invalidate() {
		m_data.clear();
	}

	@Override
	public boolean isNew() {
		return false;
	}
}

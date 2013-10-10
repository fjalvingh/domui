package to.etc.domui.autotest;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.server.*;

public class TestServerSession implements IServerSession {
	static private int m_idcount;

	@Nonnull
	final private String m_id;

	@Nonnull
	private final Map<String, Object> m_data = new HashMap<String, Object>();

	public TestServerSession() {
		m_id = "sess" + nextID();
	}

	@Override
	@Nonnull
	public String getId() {
		return m_id;
	}

	static synchronized int nextID() {
		return ++m_idcount;
	}

	@Override
	@Nullable
	public Object getAttribute(@Nonnull String name) {
		return m_data.get(name);
	}

	@Override
	public void setAttribute(@Nonnull String name, @Nullable Object value) {
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

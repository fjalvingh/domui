package to.etc.domui.logic;

import javax.annotation.*;

public abstract class ObjectEvent {
	private final @Nonnull
	ObjectIdentifier< ? > m_key;

	public ObjectEvent(@Nonnull ObjectIdentifier< ? > key) {
		super();
		m_key = key;
	}

	public @Nonnull
	ObjectIdentifier< ? > getKey() {
		return m_key;
	}
}

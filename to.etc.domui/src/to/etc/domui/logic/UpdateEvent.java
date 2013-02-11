package to.etc.domui.logic;

import java.util.*;

import javax.annotation.*;

public class UpdateEvent extends ObjectEvent {
	private final @Nonnull
	List<String> m_changedProperties;

	public UpdateEvent(@Nonnull ObjectIdentifier< ? > key, @Nonnull List<String> changedProperties) {
		super(key);
		m_changedProperties = changedProperties;
	}

	public @Nonnull
	List<String> getChangedProperties() {
		return m_changedProperties;
	}
}

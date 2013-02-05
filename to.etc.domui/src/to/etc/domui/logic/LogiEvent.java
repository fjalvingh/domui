package to.etc.domui.logic;

import java.util.*;

import javax.annotation.*;

import to.etc.webapp.query.*;

public class LogiEvent {
	Map<ObjectIdentifier< ? >, ObjectEvent> m_events = new HashMap<ObjectIdentifier< ? >, ObjectEvent>();

	void add(@Nonnull ObjectEvent event) {
		m_events.put(event.getKey(), event);
	}

	private <T> ObjectEvent getEventForSource(IIdentifyable<T> source) {
		T id = source.getId();
		ObjectIdentifier< ? > key = new ObjectIdentifier<T>(source.getClass(), id);
		ObjectEvent event = m_events.get(key);
		return event;
	}

	public @Nonnull
	<T> List<String> getChanges(@Nonnull IIdentifyable<T> source) {
		ObjectEvent event = getEventForSource(source);
		if(event != null && event instanceof UpdateEvent) {
			return ((UpdateEvent) event).getChangedProperties();
		}
		return Collections.EMPTY_LIST;
	}

	public <T> boolean isDeleted(@Nonnull IIdentifyable<T> source) {
		ObjectEvent event = getEventForSource(source);
		if(event != null && event instanceof DeleteEvent) {
			return true;
		}
		return false;
	}

	public <T> boolean isInserted(@Nonnull IIdentifyable<T> source) {
		ObjectEvent event = getEventForSource(source);
		if(event != null && event instanceof InsertEvent) {
			return true;
		}
		return false;
	}

	public <T> boolean isUpdated(@Nonnull IIdentifyable<T> source) {
		ObjectEvent event = getEventForSource(source);
		if(event != null && event instanceof UpdateEvent) {
			return ((UpdateEvent) event).getChangedProperties().size() > 0;
		}
		return false;
	}

	public <T> boolean isChanged(@Nonnull IIdentifyable<T> source) {
		ObjectEvent event = getEventForSource(source);
		return event != null;
	}
}

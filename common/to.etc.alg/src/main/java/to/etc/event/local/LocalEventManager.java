package to.etc.event.local;

import org.eclipse.jdt.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager can be used to fire events that will be handled on the same server that fires them.
 *
 *
 * @author <a href="mailto:menno.van.eijk@itris.nl">Menno van Eijk</a>
 * Created on Feb 6, 2013
 */
public class LocalEventManager {

	private static final LocalEventManager					m_instance	= new LocalEventManager();

	//@GuardedBy("this")
	@NonNull
	private final Map<Class< ? >, List<ILocalEventListener< ? >>>	m_listeners	= new HashMap<Class< ? >, List<ILocalEventListener< ? >>>();



	/**
	 * Method will add an listener, listening for the given event.
	 * @param eventObject
	 * @param l
	 */
	public <T> void addListenerForEvent(@NonNull ILocalEventListener<T> l, @NonNull Class< ? extends T> eventClass) {
		List<ILocalEventListener< ? >> existingListeners;
		synchronized(this) {
			existingListeners = m_listeners.get(eventClass);
		}
		List<ILocalEventListener< ? >> newListeners;
		if(existingListeners == null) {
			newListeners = new ArrayList<ILocalEventListener< ? >>();
		} else {
			newListeners = new ArrayList<ILocalEventListener< ? >>(existingListeners);
		}

		newListeners.add(l);
		synchronized(this) {
			m_listeners.put(eventClass, newListeners);
		}
	}

	/**
	 * fire an event. All the registered listeners will be informed of the fired event.
	 * @param firedEvent
	 * @throws Exception
	 */
	public <T> void eventFired(@NonNull T firedEvent) throws Exception {
		List<ILocalEventListener< ? >> listeners;
		synchronized(this) {
			listeners = m_listeners.get(firedEvent.getClass());
		}
		if(listeners == null) {
			return;
		}
		for(ILocalEventListener< ? > listener : listeners) {
			((ILocalEventListener<T>) listener).eventFired(firedEvent);
		}
	}

	public static LocalEventManager getInstance() {
		return m_instance;
	}


}

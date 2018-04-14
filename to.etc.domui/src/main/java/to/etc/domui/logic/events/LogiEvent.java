package to.etc.domui.logic.events;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.meta.PropertyMetaModel;

import java.util.List;
import java.util.Map;

final public class LogiEvent {
	@NonNull
	final private List<LogiEventBase> m_allEvents;

	@NonNull
	final private Map<Object, LogiEventInstanceChange> m_instanceEventMap;

	public LogiEvent(@NonNull List<LogiEventBase> allEvents, @NonNull Map<Object, LogiEventInstanceChange> instanceEventMap) {
		m_allEvents = allEvents;
		m_instanceEventMap = instanceEventMap;

	}

	/**
	 * Return T if this event contains a property value change on the specified instance and property.
	 * @param base
	 * @param propertyName
	 * @return
	 */
	public <T> boolean propertyChanged(@NonNull T base, @NonNull String propertyName) {
		LogiEventInstanceChange ic = m_instanceEventMap.get(base);
		if(null == ic)
			return false;
		return ic.isPropertyChanged(propertyName);
	}

	/**
	 * Return T if this event contains a property value change on the specified instance and property.
	 * @param base
	 * @param pmm
	 * @return
	 */
	public <T, V> boolean propertyChanged(@NonNull T base, @NonNull PropertyMetaModel<T> pmm) {
		LogiEventInstanceChange ic = m_instanceEventMap.get(base);
		if(null == ic)
			return false;
		return ic.isPropertyChanged(pmm);
	}

}

package to.etc.domui.logic.events;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

final public class LogiEvent {
	@Nonnull
	final private List<LogiEventBase> m_allEvents;

	@Nonnull
	final private Map<Object, LogiEventInstanceChange> m_instanceEventMap;

	public LogiEvent(@Nonnull List<LogiEventBase> allEvents, @Nonnull Map<Object, LogiEventInstanceChange> instanceEventMap) {
		m_allEvents = allEvents;
		m_instanceEventMap = instanceEventMap;

	}

	/**
	 * Return T if this event contains a property value change on the specified instance and property.
	 * @param base
	 * @param propertyName
	 * @return
	 */
	public <T> boolean propertyChanged(@Nonnull T base, @Nonnull String propertyName) {
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
	public <T, V> boolean propertyChanged(@Nonnull T base, @Nonnull PropertyMetaModel<T> pmm) {
		LogiEventInstanceChange ic = m_instanceEventMap.get(base);
		if(null == ic)
			return false;
		return ic.isPropertyChanged(pmm);
	}

}

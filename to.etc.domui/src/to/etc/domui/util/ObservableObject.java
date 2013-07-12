package to.etc.domui.util;

import javax.annotation.*;

import to.etc.domui.databinding.*;
import to.etc.webapp.annotations.*;

public class ObservableObject implements IObservableEntity {
	@Nullable
	private ObserverSupport<ObservableObject> m_osupport;

	@Nonnull
	protected ObserverSupport<ObservableObject> getObserverSupport() {
		ObserverSupport<ObservableObject> osupport = m_osupport;
		if(null == osupport) {
			osupport = m_osupport = new ObserverSupport<ObservableObject>(this);
		}
		return osupport;
	}

	protected <V> void firePropertyChange(@Nonnull String propertyName, V old, V nw) {
		ObserverSupport< ? > osupport = m_osupport;
		if(null == osupport)					// Nothing observing?
			return;
		osupport.fireModified(propertyName, old, nw);
	}

	@Override
	@Nonnull
	public IObservableValue< ? > observableProperty(@Nonnull @GProperty String property) {
		return getObserverSupport().getValueObserver(property);
	}
}

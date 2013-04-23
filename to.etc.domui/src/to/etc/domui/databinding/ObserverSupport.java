package to.etc.domui.databinding;

import java.beans.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

/**
 * Like {@link PropertyChangeSupport}, this class handles the {@link IObservable} support for DomUI for
 * directly implementing classes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 23, 2013
 */
public class ObserverSupport<C> {
	@Nonnull
	final private C m_instance;

	@Nonnull
	final private ClassMetaModel m_model;

	@Nonnull
	private Map<String, IObservable< ? , ? , ? >> m_propertyMap = Collections.EMPTY_MAP;

	public ObserverSupport(@Nonnull C instance) {
		m_instance = instance;
		m_model = MetaManager.findClassMeta(instance.getClass());
	}

	@Nonnull
	public <T> PropertyObservableValue<C, T> getValueObserver(@Nonnull String property) {
		IObservable< ? , ? , ? > po = m_propertyMap.get(property);
		if(null == po) {
			if(m_propertyMap.size() == 0)
				m_propertyMap = new HashMap<String, IObservable< ? , ? , ? >>(10);
			PropertyMetaModel<T> pmm = (PropertyMetaModel<T>) m_model.getProperty(property);
			po = new PropertyObservableValue<C, T>(m_instance, pmm);
			m_propertyMap.put(property, po);
		}
		return (PropertyObservableValue<C, T>) po;
	}

	protected <T> void firePropertyChange(@Nonnull String propertyName, @Nullable T oldValue, @Nullable T newValue) {
		IObservable< ? , ? , ? > po = m_propertyMap.get(propertyName);
		if(po instanceof PropertyObservableValue) {
			((PropertyObservableValue<C, T>) po).notifyIfChanged(oldValue, newValue);
		}
	}
}

package to.etc.domui.databinding.observables;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.databinding.*;

public class ObservableObject implements IObservableEntity {
	@Nullable
	private ClassMetaModel m_model;

	@Nonnull
	private Map<String, IObservable< ? , ? , ? >> m_propertyMap = Collections.EMPTY_MAP;

	@Nonnull
	public ClassMetaModel classMetaModel() {
		ClassMetaModel cmm = m_model;
		if(null == cmm) {
			cmm = m_model = MetaManager.findClassMeta(getClass());
		}
		return cmm;
	}

	/**
	 * Create a reference to the property that can be used to observe changes.
	 * @see to.etc.domui.databinding.observables.IObservableEntity#observableValue(java.lang.String)
	 */
	@Override
	@Nonnull
	public ObservablePropertyValue<ObservableObject, ? > observableValue(@Nonnull String property) {
		IObservable< ? , ? , ? > po = m_propertyMap.get(property);
		if(null == po) {
			if(m_propertyMap.size() == 0)
				m_propertyMap = new HashMap<String, IObservable< ? , ? , ? >>(10);
			po = createObservableValue(property);
			m_propertyMap.put(property, po);
		}
		return (ObservablePropertyValue<ObservableObject, ? >) po;
	}

	/**
	 * Needed to fix lobotomized java generics.
	 * @param property
	 * @return
	 */
	@Nonnull
	private <T> ObservablePropertyValue<ObservableObject, T> createObservableValue(@Nonnull String property) {
		PropertyMetaModel<T> pmm = (PropertyMetaModel<T>) classMetaModel().getProperty(property);
		return new ObservablePropertyValue<ObservableObject, T>(this, pmm);
	}

	@Nonnull
	public ObservablePropertyList<ObservableObject, ? > observableList(@Nonnull String property) {
		IObservable< ? , ? , ? > po = m_propertyMap.get(property);
		if(null == po) {
			if(m_propertyMap.size() == 0)
				m_propertyMap = new HashMap<String, IObservable< ? , ? , ? >>(10);
			po = createObservableList(property);
			m_propertyMap.put(property, po);
		}
		return (ObservablePropertyList<ObservableObject, ? >) po;
	}

	/**
	 * Needed to fix lobotomized java generics.
	 * @param property
	 * @return
	 */
	@Nonnull
	private <T> ObservablePropertyList<ObservableObject, T> createObservableList(@Nonnull String property) {
		PropertyMetaModel<List<T>> pmm = (PropertyMetaModel<List<T>>) classMetaModel().getProperty(property);
		if(!List.class.isAssignableFrom(pmm.getActualType()))
			throw new IllegalStateException("Property " + property + " is not of type List<T>");
		return new ObservablePropertyList<ObservableObject, T>(this, pmm);
	}

	public <T> void firePropertyChange(@Nonnull String propertyName, @Nullable T oldValue, @Nullable T newValue) {
		IObservable< ? , ? , ? > po = m_propertyMap.get(propertyName);
		if(po instanceof IPropertyChangeNotifier) {
			((IPropertyChangeNotifier) po).notifyIfChanged(oldValue, newValue);
		}
	}
}

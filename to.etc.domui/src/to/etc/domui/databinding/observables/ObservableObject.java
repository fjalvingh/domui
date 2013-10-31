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

	@Override
	@Nonnull
	public PropertyObservableValue<ObservableObject, ? > observableValue(@Nonnull String property) {
		IObservable< ? , ? , ? > po = m_propertyMap.get(property);
		if(null == po) {
			if(m_propertyMap.size() == 0)
				m_propertyMap = new HashMap<String, IObservable< ? , ? , ? >>(10);
			po = createObservable(property);
			m_propertyMap.put(property, po);
		}
		return (PropertyObservableValue<ObservableObject, ? >) po;
	}

	/**
	 * Needed to fix lobotomized java generics.
	 * @param property
	 * @return
	 */
	@Nonnull
	private <T> PropertyObservableValue<ObservableObject, T> createObservable(@Nonnull String property) {
		PropertyMetaModel<T> pmm = (PropertyMetaModel<T>) classMetaModel().getProperty(property);
		return new PropertyObservableValue<ObservableObject, T>(this, pmm);
	}

	//	@Nonnull
	//	public <T>

	public <T> void fireModified(@Nonnull String propertyName, @Nullable T oldValue, @Nullable T newValue) {
		IObservable< ? , ? , ? > po = m_propertyMap.get(propertyName);
		if(po instanceof PropertyObservableValue) {
			((PropertyObservableValue<ObservableObject, T>) po).notifyIfChanged(oldValue, newValue);
		}
	}
}

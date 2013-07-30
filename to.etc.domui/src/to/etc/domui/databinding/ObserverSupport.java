/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.databinding;

import java.beans.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;

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

	public <T> void fireModified(@Nonnull String propertyName, @Nullable T oldValue, @Nullable T newValue) {
		IObservable< ? , ? , ? > po = m_propertyMap.get(propertyName);
		if(po instanceof PropertyObservableValue) {
			((PropertyObservableValue<C, T>) po).notifyIfChanged(oldValue, newValue);
		}
	}

	public void fireError(@Nonnull String propertyName, @Nullable UIMessage error) {
		IObservable< ? , ? , ? > po = m_propertyMap.get(propertyName);
		if(po instanceof IObservable) {
			((IObservable< ? , ? , ? >) po).setError(error);
			;
		}
	}
}

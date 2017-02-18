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
package to.etc.domui.databinding.observables;

import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.databinding.ListenerList;
import to.etc.domui.databinding.value.IObservableValue;
import to.etc.domui.databinding.value.IValueChangeListener;
import to.etc.domui.databinding.value.ValueChangeEvent;
import to.etc.domui.databinding.value.ValueDiff;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Just some observable value container. This is meant to be used directly.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 20, 2013
 */
public class ObservableValue<T> extends ListenerList<T, ValueChangeEvent<T>, IValueChangeListener<T>> implements IObservableValue<T> {
	private T m_value;

	@Nonnull
	final private Class<T> m_valueType;

	/**
	 * Create a null-holding value of the specified type.
	 * @param valueType
	 */
	public ObservableValue(@Nonnull Class<T> valueType) {
		m_valueType = valueType;
	}

	/**
	 * Create a new value with the specified initial non-null value.
	 * @param value
	 */
	public ObservableValue(@Nonnull T value) {
		m_value = value;
		m_valueType = (Class<T>) value.getClass();
	}

	@Override
	@Nonnull
	public Class<T> getValueType() {
		return m_valueType;
	}

	@Override
	@Nullable
	public T getValue() throws Exception {
		return m_value;
	}

	@Override
	public void setValue(@Nullable T value) throws Exception {
		T oldValue = m_value;
		m_value = value;
		notifyIfChanged(oldValue, value);
	}

	private void notifyIfChanged(@Nullable T old, @Nullable T value) {
		if(MetaManager.areObjectsEqual(old, value))
			return;
		ValueDiff<T> vd = new ValueDiff<T>(old, value);
		fireEvent(new ValueChangeEvent<T>(this, vd));
	}
}

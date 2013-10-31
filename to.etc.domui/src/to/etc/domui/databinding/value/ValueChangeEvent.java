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
package to.etc.domui.databinding.value;

import javax.annotation.*;

import to.etc.domui.databinding.*;

/**
 * An event due to a change in some observable value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 23, 2013
 */
public class ValueChangeEvent<T> extends ObservableEvent<T, ValueChangeEvent<T>, IValueChangeListener<T>> {
	@Nonnull
	final private ValueDiff<T> m_diff;

	public ValueChangeEvent(@Nonnull IObservableValue<T> source, @Nonnull ValueDiff<T> diff) {
		super(source);
		m_diff = diff;
	}

	/**
	 * The observable that this event sprung from.
	 * @return
	 */
	@Override
	@Nonnull
	public IObservableValue<T> getSource() {
		return (IObservableValue<T>) super.getSource();
	}

	@Nonnull
	public ValueDiff<T> getDiff() {
		return m_diff;
	}
}

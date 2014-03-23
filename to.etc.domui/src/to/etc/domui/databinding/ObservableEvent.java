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

import javax.annotation.*;

public class ObservableEvent<V, E extends IChangeEvent<V, E, L>, L extends IChangeListener<V, E, L>> implements IChangeEvent<V, E, L> {
	@Nonnull
	final private IObservable<V, E, L> m_source;

	public ObservableEvent(@Nonnull IObservable<V, E, L> source) {
		if(null == source)
			throw new IllegalArgumentException("Source cannot be null");
		m_source = source;
	}

	@Override
	@Nonnull
	public IObservable<V, E, L> getSource() {
		return m_source;
	}
}

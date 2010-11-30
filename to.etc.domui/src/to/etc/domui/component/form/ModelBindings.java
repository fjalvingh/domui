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
package to.etc.domui.component.form;

import java.util.*;

import javax.annotation.*;

public class ModelBindings implements IModelBinding, Iterable<IModelBinding> {
	@Nonnull
	final private List<IModelBinding> m_bindings = new ArrayList<IModelBinding>();

	public void add(@Nonnull IModelBinding b) {
		m_bindings.add(b);
	}

	@Override
	public void moveControlToModel() throws Exception {
		Exception cx = null;
		for(IModelBinding b : m_bindings) {
			try {
				b.moveControlToModel();
			} catch(Exception x) {
				if(cx == null)
					cx = x;
			}
		}
		if(cx != null)
			throw cx;
	}

	@Override
	public void moveModelToControl() throws Exception {
		for(IModelBinding b : m_bindings)
			b.moveModelToControl();
	}

	public int size() {
		return m_bindings.size();
	}

	@Override
	public void setControlsEnabled(boolean on) {
		for(IModelBinding b : m_bindings)
			b.setControlsEnabled(on);
	}

	@Override
	@Nonnull
	public Iterator<IModelBinding> iterator() {
		return m_bindings.iterator();
	}

	@Nonnull
	public IModelBinding get(int ix) {
		return m_bindings.get(ix);
	}
}

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
package to.etc.domui.component.controlfactory;

import java.util.*;

import javax.annotation.*;

/**
 * This represents a list of {@link IModelBinding}s. Each binding connects a UI Control
 * (the input component) with a given property of the data model. This list can be used
 * to collect all of the component-property bindings for an entire form, so that the
 * data from the model can be moved into the controls that <i>edit</i> that model in one
 * call. And of course the reverse, moving all edited control data back to the model
 * is possible too.
 * <p>A ModelBindings list is itself an IModelBinding too, so you can add this list to
 * <i>another</i> list of bindings too.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 20, 2011
 */
public class ModelBindings implements IModelBinding, Iterable<IModelBinding> {
	@Nonnull
	final private List<IModelBinding> m_bindings = new ArrayList<IModelBinding>();

	/**
	 * Add a binding to the list.
	 * @param b
	 */
	public void add(@Nonnull IModelBinding b) {
		m_bindings.add(b);
	}

	/**
	 * Move the data from the controls to the data model. If any control validation fails the
	 * move stops at the failing control, so this reports at most one validation error.
	 * @see to.etc.domui.component.controlfactory.IModelBinding#moveControlToModel()
	 */
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

	/**
	 * Move the data from model to the controls for all controls added to the list.
	 * @see to.etc.domui.component.controlfactory.IModelBinding#moveModelToControl()
	 */
	@Override
	public void moveModelToControl() throws Exception {
		for(IModelBinding b : m_bindings)
			b.moveModelToControl();
	}

	/**
	 * Return the #of bindings present in the list. Watch out: a ModelBinding that is <i>itself</i>
	 * added to the list counts as one item, even though it has many items inside itself.
	 * @return
	 */
	public int size() {
		return m_bindings.size();
	}

	/**
	 * Remove all bindings from this list.
	 */
	public void clear() {
		m_bindings.clear();
	}

	/**
	 * Enable or disable all bound components.
	 * @see to.etc.domui.component.controlfactory.IModelBinding#setControlsEnabled(boolean)
	 */
	@Override
	public void setControlsEnabled(boolean on) {
		for(IModelBinding b : m_bindings)
			b.setControlsEnabled(on);
	}

	/**
	 * Get an iterator to pass over all bindings in this list.
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	@Nonnull
	public Iterator<IModelBinding> iterator() {
		return m_bindings.iterator();
	}

	/**
	 * Get the nth binding stored in this list.
	 * @param ix
	 * @return
	 */
	@Nonnull
	public IModelBinding get(int ix) {
		return m_bindings.get(ix);
	}
}

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
package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

abstract public class TableModelTableBase<T> extends Div implements ITableModelListener<T> {
	@Nonnull
	private ITableModel<T> m_model;

	protected TableModelTableBase(@Nonnull ITableModel<T> model) {
		m_model = model;
		model.addChangeListener(this);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Model updates.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Return the current model being used.
	 */
	@Nonnull
	public ITableModel<T> getModel() {
		return m_model;
	}

	/**
	 * Set a new model for this table. This discards the entire presentation
	 * and causes a full build at render time.
	 */
	public void setModel(@Nonnull ITableModel<T> model) {
		if(model == null)
			throw new IllegalArgumentException("Cannot set a table model to null");

		ITableModel<T> itm = model; // Stupid Java Generics need cast here
		if(m_model == itm) // If the model did not change at all begone
			return;
		ITableModel<T> old = m_model;
		if(m_model != null)
			m_model.removeChangeListener(this); // Remove myself from listening to my old model
		m_model = itm;
		if(itm != null)
			itm.addChangeListener(this); // Listen for changes on the new model
		forceRebuild(); // Force a rebuild of all my nodes
		fireModelChanged(old, model);
	}

	@Nonnull
	protected T getModelItem(int index) throws Exception {
		List<T> res = m_model.getItems(index, index + 1);
		if(res.size() == 0)
			throw new IllegalStateException("Model did not return a row at index=" + index);
		T val = res.get(0);
		if(val == null)
			throw new IllegalStateException("Model item at index=" + index + " is null");
		return val;
	}

	@Override
	protected void onShelve() throws Exception {
		super.onShelve();
		if(m_model instanceof IShelvedListener) {
			((IShelvedListener) m_model).onShelve();
		}
	}

	@Override
	protected void onUnshelve() throws Exception {
		super.onUnshelve();
		if(m_model instanceof IShelvedListener) {
			//			System.out.println("Unshelving the model: refreshing it's contents");
			((IShelvedListener) m_model).onUnshelve();
			forceRebuild();
			firePageChanged();
		}
	}

	@Override
	protected void onRefresh() throws Exception {
		if(m_model != null)
			m_model.refresh();
	}

	protected void firePageChanged() {}

	protected void fireModelChanged(@Nullable ITableModel<T> old, @Nonnull ITableModel<T> model) {}
}

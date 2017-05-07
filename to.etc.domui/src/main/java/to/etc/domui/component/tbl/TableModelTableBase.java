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

import to.etc.domui.databinding.list.*;
import to.etc.domui.databinding.list2.*;
import to.etc.domui.databinding.observables.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

import javax.annotation.*;
import java.util.*;

abstract public class TableModelTableBase<T> extends Div implements ITableModelListener<T>, IListChangeListener<T> {
	@Nullable
	private ITableModel<T> m_model;

	@Nonnull
	private List<IDataTableChangeListener> m_listeners = Collections.EMPTY_LIST;

	private boolean m_disableClipboardSelection;

	protected TableModelTableBase(@Nonnull ITableModel<T> model) {
		m_model = model;
		model.addChangeListener(this);
	}

	public TableModelTableBase() {}

	/*--------------------------------------------------------------*/
	/*	CODING:	Model/page changed listener code..					*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a change listener to this model. Don't forget to remove it at destruction time.
	 */
	public void addChangeListener(@Nonnull IDataTableChangeListener l) {
		synchronized(this) {
			if(m_listeners.contains(l))
				return;
			m_listeners = new ArrayList<>(m_listeners);
			m_listeners.add(l);
		}
	}

	/**
	 * Remove a change listener from the model.
	 * @see to.etc.domui.component.tbl.ITableModel#removeChangeListener(to.etc.domui.component.tbl.ITableModelListener)
	 */
	public void removeChangeListener(@Nonnull IDataTableChangeListener l) {
		synchronized(this) {
			m_listeners = new ArrayList<>();
			m_listeners.remove(l);
		}
	}

	private synchronized List<IDataTableChangeListener> getListeners() {
		return m_listeners;
	}

	protected void fireModelChanged(@Nullable ITableModel<T> old, @Nullable ITableModel<T> nw) {
		for(IDataTableChangeListener l : getListeners()) {
			try {
				l.modelChanged(this, old, nw);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	protected void firePageChanged() {
		for(IDataTableChangeListener l : getListeners()) {
			try {
				l.pageChanged(this);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	protected void fireSelectionUIChanged() {
		for(IDataTableChangeListener l : getListeners()) {
			try {
				l.selectionUIChanged(this);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Model updates.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Return the current model being used.
	 */
	@Nonnull
	public ITableModel<T> getModel() {
		if(null != m_model)
			return m_model;
		throw new IllegalStateException("The table model has not been set.");
	}

	/**
	 * Set a new model for this table. This discards the entire presentation
	 * and causes a full build at render time.
	 */
	public void setModel(@Nonnull ITableModel<T> model) {
		if(model == null)
			throw new IllegalArgumentException("Cannot set a table model to null");

		ITableModel<T> itm = model; 				// Stupid Java Generics need cast here
		if(m_model == itm) 							// If the model did not change at all begone
			return;
		ITableModel<T> old = m_model;
		if(m_model != null)
			m_model.removeChangeListener(this); 	// Remove myself from listening to my old model
		m_model = itm;
		if(itm != null)
			itm.addChangeListener(this); 			// Listen for changes on the new model
		forceRebuild();
		resetState();
		fireModelChanged(old, model);
	}

	/**
	 * This should be overridden when setting a model requires state to be reset, like
	 * the current page number or selected cell (x, y)
	 */
	@OverridingMethodsMustInvokeSuper
	protected void resetState() {
	}

	@Nonnull
	protected T getModelItem(int index) throws Exception {
		List<T> res = getModel().getItems(index, index + 1);
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

	/*--------------------------------------------------------------*/
	/*	CODING:	Experimental: using ObservableList as a value.		*/
	/*--------------------------------------------------------------*/

	public void setList(@Nullable IObservableList<T> list) {
		ITableModel<T> om = m_model;
		if(om instanceof ObservableListModelAdapter< ? >) {
			//-- Check if this is the same list, wrapped already.
			ObservableListModelAdapter<T> oa = (ObservableListModelAdapter<T>) om;
			if(oa.getSource() == list)							// Same list?
				return;

			//-- We're going to replace this, so remove me as a list change listener.
			oa.getSource().removeChangeListener(this);
		}
		if(null != list) {
			ObservableListModelAdapter<T> ma = new ObservableListModelAdapter<T>(list);
			setModel(ma);
			list.addChangeListener(this);
		}
	}

	@Nullable
	public IObservableList<T> getList() {
		ITableModel<T> om = getModel();
		if(om instanceof ObservableListModelAdapter< ? >) {
			ObservableListModelAdapter<T> oa = (ObservableListModelAdapter<T>) om;
			return oa.getSource();
		}
		return null;
	}

	@Override
	public void onRemoveFromPage(Page p) {
		IObservableList<T> list = getList();
		if(null != list)
			list.removeChangeListener(this);
	}

	public boolean isDisableClipboardSelection() {
		return m_disableClipboardSelection;
	}

	public void setDisableClipboardSelection(boolean disableClipboardSelection) {
		if(m_disableClipboardSelection == disableClipboardSelection)
			return;
		m_disableClipboardSelection = disableClipboardSelection;
		if(isBuilt()) {
			if(disableClipboardSelection)
				appendJavascript(JavascriptUtil.disableSelection(this)); // Needed to prevent ctrl+click in IE doing clipboard-select, because preventDefault does not work there of course.
			else
				appendJavascript(JavascriptUtil.enableSelection(this)); // Needed to prevent ctrl+click in IE doing clipboard-select, because preventDefault does not work there of course.
		}

		// Does not work on IE11, as usual: it does not disable selection using shift + click 8-(
		//if(disableClipboardSelection)
		//	addCssClass("ui-dt-disable-selection");
		//else
		//	removeCssClass("ui-dt-disable-selection");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	ObservableList event handling						*/
	/*--------------------------------------------------------------*/

	/**
	 * IListChangeListener implementation: this handles ObservableList updates and updates the UI according to them.
	 *
	 * @param event
	 * @throws Exception
	 */
	@Override
	public void handleChange(@Nonnull ListChangeEvent<T> event) throws Exception {
		if(event.getChanges().size() == 1) {
			event.visit(new IListChangeVisitor<T>() {
				@Override
				public void visitAdd(@Nonnull ListChangeAdd<T> l) throws Exception {
					rowAdded(getModel(), l.getIndex(), l.getValue());
				}

				@Override
				public void visitDelete(@Nonnull ListChangeDelete<T> l) throws Exception {
					rowDeleted(getModel(), l.getIndex(), l.getValue());
				}

				@Override
				public void visitModify(@Nonnull ListChangeModify<T> l) throws Exception {
					rowModified(getModel(), l.getIndex(), l.getNewValue());
				}

				@Override
				public void visitAssign(@Nonnull ListChangeAssign<T> assign) throws Exception {
					forceRebuild();
					fireModelChanged(null, getModel());
				}
			});
		} else {
			forceRebuild();
		}
	}
}

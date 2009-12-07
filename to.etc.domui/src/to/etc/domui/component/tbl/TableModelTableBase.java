package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

abstract public class TableModelTableBase<T> extends Div implements ITableModelListener<T> {
	private Class<T> m_actualClass;

	private ITableModel<T> m_model;

	protected TableModelTableBase(@Nonnull Class<T> actualClass) {
		m_actualClass = actualClass;
	}

	protected TableModelTableBase(@Nonnull Class<T> actualClass, ITableModel<T> model) {
		m_actualClass = actualClass;
		m_model = model;
		model.addChangeListener(this);
	}

	@Nonnull
	final public Class<T> getActualClass() {
		return m_actualClass;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Model updates.										*/
	/*--------------------------------------------------------------*/

	/**
	 * Return the current model being used.
	 */
	public ITableModel<T> getModel() {
		return m_model;
	}


	/**
	 * Set a new model for this table. This discards the entire presentation
	 * and causes a full build at render time.
	 */
	public void setModel(ITableModel<T> model) {
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

	protected Object getModelItem(int index) throws Exception {
		List<T> res = m_model.getItems(index, index + 1);
		if(res.size() == 0)
			return null;
		return res.get(0);
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

	protected void firePageChanged() {}

	protected void fireModelChanged(ITableModel<T> old, ITableModel<T> model) {}


}

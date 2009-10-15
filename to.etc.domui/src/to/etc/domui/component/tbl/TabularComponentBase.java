package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

abstract public class TabularComponentBase extends Div implements ITableModelListener<Object> {
	private ITableModel<Object> m_model;

	/** The current page #, starting at 0 */
	private int m_currentPage;

	protected int m_six, m_eix;

	private List<IDataTableChangeListener> m_listeners = Collections.EMPTY_LIST;

	abstract int getPageSize();

	public TabularComponentBase() {}

	public TabularComponentBase(ITableModel< ? > model) {
		m_model = (ITableModel<Object>) model;
		m_model.addChangeListener(this);
	}

	/**
	 * Add a change listener to this model. Don't forget to remove it at destruction time.
	 */
	public void addChangeListener(IDataTableChangeListener l) {
		synchronized(this) {
			if(m_listeners.contains(l))
				return;
			m_listeners = new ArrayList<IDataTableChangeListener>(m_listeners);
			m_listeners.add(l);
		}
	}

	/**
	 * Remove a change listener from the model.
	 * @see to.etc.domui.component.tbl.ITableModel#removeChangeListener(to.etc.domui.component.tbl.ITableModelListener)
	 */
	public void removeChangeListener(IDataTableChangeListener l) {
		synchronized(this) {
			m_listeners = new ArrayList<IDataTableChangeListener>();
			m_listeners.remove(l);
		}
	}

	private synchronized List<IDataTableChangeListener> getListeners() {
		return m_listeners;
	}

	protected void fireModelChanged(ITableModel< ? > old, ITableModel< ? > nw) {
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

	protected void calcIndices() throws Exception {
		int size = m_model.getRows();
		int pageSize = getPageSize();
		if(pageSize <= 0) {
			m_six = 0;
			m_eix = size;
		} else {
			m_six = m_currentPage * pageSize; // Start index,
			if(m_six >= size) {
				//-- Move to the last page instead
				int lp = (size / pageSize); // Page# of last element,
				m_six = lp * pageSize; // Start of that page
			}
			m_eix = m_six + pageSize;
			if(m_eix > size)
				m_eix = size;
		}
	}


	protected List< ? > getPageItems() throws Exception {
		return m_model.getItems(m_six, m_eix); // Data to show
	}

	public ITableModel< ? > getModel() {
		return m_model;
	}

	public int getCurrentPage() {
		return m_currentPage;
	}

	public void setCurrentPage(int currentPage) {
		if(m_currentPage < 0 || (currentPage != 0 && getPageSize() <= 0))
			throw new IllegalStateException("Cannot set current page to " + currentPage);
		if(m_currentPage == currentPage)
			return;
		m_currentPage = currentPage;
		forceRebuild();
		firePageChanged();
	}

	public int getPageCount() throws Exception {
		int pageSize = getPageSize();
		if(pageSize <= 0)
			return 1;
		return (m_model.getRows() + pageSize - 1) / pageSize;
	}

	public int getTruncatedCount() {
		if(m_model == null || !(m_model instanceof ITruncateableDataModel))
			return 0;
		ITruncateableDataModel t = (ITruncateableDataModel) m_model;
		return t.getTruncatedCount();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Model updates.										*/
	/*--------------------------------------------------------------*/

	/**
	 * Set a new model for this table. This discards the entire presentation
	 * and causes a full build at render time.
	 */
	public void setModel(ITableModel< ? > model) {
		ITableModel<Object> itm = (ITableModel<Object>) model; // Stupid Java Generics need cast here
		if(m_model == itm) // If the model did not change at all begone
			return;
		ITableModel< ? > old = m_model;
		if(m_model != null)
			m_model.removeChangeListener(this); // Remove myself from listening to my old model
		m_model = itm;
		if(itm != null)
			itm.addChangeListener(this); // Listen for changes on the new model
		m_currentPage = 0;
		forceRebuild(); // Force a rebuild of all my nodes
		fireModelChanged(old, model);
	}

	protected Object getModelItem(int index) throws Exception {
		List<Object> res = m_model.getItems(index, index + 1);
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
}

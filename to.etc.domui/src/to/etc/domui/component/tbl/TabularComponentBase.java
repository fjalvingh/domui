package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

abstract public class TabularComponentBase<T> extends TableModelTableBase<T> implements ITableModelListener<T> {
	/** The current page #, starting at 0 */
	private int m_currentPage;

	protected int m_six, m_eix;

	@Nonnull
	private List<IDataTableChangeListener> m_listeners = Collections.EMPTY_LIST;

	abstract int getPageSize();

	public TabularComponentBase(ITableModel<T> model) {
		super(model);
	}

	//	public TabularComponentBase(Class<T> actualClass, ITableModel<T> model) {
	//		super(actualClass, model);
	//	}

	//	public TabularComponentBase(Class<T> actualClass) {
	//		super(actualClass);
	//	}

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
			m_listeners = new ArrayList<IDataTableChangeListener>(m_listeners);
			m_listeners.add(l);
		}
	}

	/**
	 * Remove a change listener from the model.
	 * @see to.etc.domui.component.tbl.ITableModel#removeChangeListener(to.etc.domui.component.tbl.ITableModelListener)
	 */
	public void removeChangeListener(@Nonnull IDataTableChangeListener l) {
		synchronized(this) {
			m_listeners = new ArrayList<IDataTableChangeListener>();
			m_listeners.remove(l);
		}
	}

	private synchronized List<IDataTableChangeListener> getListeners() {
		return m_listeners;
	}

	@Override
	protected void fireModelChanged(@Nullable ITableModel<T> old, @Nonnull ITableModel<T> nw) {
		for(IDataTableChangeListener l : getListeners()) {
			try {
				l.modelChanged(this, old, nw);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	@Override
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
		int size = getModel().getRows();
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

	@Nonnull
	protected List<T> getPageItems() throws Exception {
		return getModel().getItems(m_six, m_eix); // Data to show
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
		return (getModel().getRows() + pageSize - 1) / pageSize;
	}

	public int getTruncatedCount() {
		ITableModel<T> tm = getModel();
		if(tm == null || !(tm instanceof ITruncateableDataModel))
			return 0;
		ITruncateableDataModel t = (ITruncateableDataModel) tm;
		return t.getTruncatedCount();
	}
}

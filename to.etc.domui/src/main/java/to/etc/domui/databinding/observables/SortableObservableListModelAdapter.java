package to.etc.domui.databinding.observables;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.tbl.ISortableTableModel;
import to.etc.domui.component.tbl.ITableModel;
import to.etc.domui.component.tbl.ITableModelListener;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.databinding.list.ListChangeAdd;
import to.etc.domui.databinding.list.ListChangeAssign;
import to.etc.domui.databinding.list.ListChangeDelete;
import to.etc.domui.databinding.list.ListChangeModify;
import to.etc.domui.databinding.list2.IListChangeListener;
import to.etc.domui.databinding.list2.IListChangeVisitor;
import to.etc.domui.databinding.list2.ListChangeEvent;
import to.etc.util.StringTool;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This adapter creates a {@link ITableModel} from an {@link IObservableList} instance. This does <b>not</b> handle
 * events from the list: these are different in nature from the "real" model so they must be handled explicitly. Any
 * event listener added to this model is just ignored.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 4, 2013
 */
final public class SortableObservableListModelAdapter<T> implements ITableModel<T>, ISortableTableModel {
	@NonNull
	final private ObservableList<T> m_list;

	final private List<ITableModelListener<T>> m_listeners = new ArrayList<ITableModelListener<T>>();

	/** The current sort key (a property in the data class), or null if currently unsorted. */
	private String m_sortKey;

	/** T if the sort order is currently descending. */
	private boolean m_descending;

	public SortableObservableListModelAdapter(@NonNull ObservableList<T> list) {
		m_list = list;
	}

	@NonNull
	public ObservableList<T> getSource() {
		return m_list;
	}

	@NonNull
	@Override
	public List<T> getItems(int start, int end) throws Exception {
		return m_list.subList(start, end);
	}

	@Override
	public int getRows() throws Exception {
		return m_list.size();
	}

	/**
	 * Add a change listener to this model. Don't forget to remove it at destruction time.
	 */
	@Override
	public void addChangeListener(@NonNull ITableModelListener<T> l) {
		m_listeners.add(l);
	}

	/**
	 * Remove a change listener from the model.
	 * @see to.etc.domui.component.tbl.ITableModel#removeChangeListener(to.etc.domui.component.tbl.ITableModelListener)
	 */
	@Override
	public void removeChangeListener(@NonNull ITableModelListener<T> l) {
		m_listeners.remove(l);
	}

	protected List<ITableModelListener<T>> getListeners() {
		return m_listeners;
	}

	public void fireModelChanged() throws Exception {
		for(ITableModelListener<T> l : getListeners())
			l.modelChanged(this);
	}

	public void fireModelSorted() throws Exception {
		for(ITableModelListener<T> l : getListeners())
			l.rowsSorted(this);
	}

	@Override
	public void refresh() {}

	@Override
	public String getSortKey() {
		return m_sortKey;
	}

	@Override
	public boolean isSortDescending() {
		return m_descending;
	}

	@Override
	public void sortOn(String key, boolean descending) throws Exception {
		if(StringTool.isEqual(key, m_sortKey) && m_descending == descending)
			return;

		if(key == null) {
			if(m_list.getComparator() != null) {
				m_list.setComparator(null);
				fireModelChanged();
			}
		} else {
			//-- We need the property meta model for the specified property.
			ClassMetaModel cmm = MetaManager.findClassMeta(getDataClass());
			Comparator<T> comp = ConverterRegistry.getComparator(cmm, key, descending);
			m_list.setComparator(comp);
			fireModelSorted();
		}
		m_sortKey = key;
		m_descending = descending;
	}

	private Class<?> getDataClass() {
		return m_list.size() == 0 ? Object.class : m_list.get(0).getClass();
	}

	private class EvListener implements IListChangeListener<T> {
		final private ITableModelListener<T> m_modelListener;

		public EvListener(ITableModelListener<T> l) {
			m_modelListener = l;
		}

		/**
		 * Propagate {@link ListChangeEvent} events to a {@link ITableModelListener}.
		 * @see to.etc.domui.databinding.IChangeListener#handleChange(to.etc.domui.databinding.IChangeEvent)
		 */
		@Override
		public void handleChange(@NonNull ListChangeEvent<T> event) throws Exception {
			event.visit(new IListChangeVisitor<T>() {
				@Override
				public void visitAdd(@NonNull ListChangeAdd<T> l) throws Exception {
					m_modelListener.rowAdded(SortableObservableListModelAdapter.this, l.getIndex(), l.getValue());
				}

				@Override
				public void visitDelete(@NonNull ListChangeDelete<T> l) throws Exception {
					m_modelListener.rowDeleted(SortableObservableListModelAdapter.this, l.getIndex(), l.getValue());
				}

				@Override
				public void visitModify(@NonNull ListChangeModify<T> l) throws Exception {
					m_modelListener.rowModified(SortableObservableListModelAdapter.this, l.getIndex(), l.getNewValue());
				}

				@Override
				public void visitAssign(@NonNull ListChangeAssign<T> assign) throws Exception {
					m_modelListener.modelChanged(SortableObservableListModelAdapter.this);
				}
			});
		}
	}
}

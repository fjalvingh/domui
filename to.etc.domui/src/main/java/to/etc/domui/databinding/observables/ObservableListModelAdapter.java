package to.etc.domui.databinding.observables;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.tbl.ITableModel;
import to.etc.domui.component.tbl.ITableModelListener;
import to.etc.domui.databinding.list.ListChangeAdd;
import to.etc.domui.databinding.list.ListChangeAssign;
import to.etc.domui.databinding.list.ListChangeDelete;
import to.etc.domui.databinding.list.ListChangeModify;
import to.etc.domui.databinding.list2.IListChangeListener;
import to.etc.domui.databinding.list2.IListChangeVisitor;
import to.etc.domui.databinding.list2.ListChangeEvent;

import java.util.List;

/**
 * This adapter creates a {@link ITableModel} from an {@link IObservableList} instance. This does <b>not</b> handle
 * events from the list: these are different in nature from the "real" model so they must be handled explicitly. Any
 * event listener added to this model is just ignored.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 4, 2013
 */
final public class ObservableListModelAdapter<T> implements ITableModel<T> {
	@NonNull
	final private IObservableList<T> m_list;

	public ObservableListModelAdapter(@NonNull IObservableList<T> list) {
		m_list = list;
	}

	@NonNull
	public IObservableList<T> getSource() {
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

	@Override
	public void addChangeListener(@NonNull ITableModelListener<T> l) {
	}

	@Override
	public void removeChangeListener(@NonNull ITableModelListener<T> l) {
	}

	@Override
	public void refresh() {}

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
					m_modelListener.rowAdded(ObservableListModelAdapter.this, l.getIndex(), l.getValue());
				}

				@Override
				public void visitDelete(@NonNull ListChangeDelete<T> l) throws Exception {
					m_modelListener.rowDeleted(ObservableListModelAdapter.this, l.getIndex(), l.getValue());
				}

				@Override
				public void visitModify(@NonNull ListChangeModify<T> l) throws Exception {
					m_modelListener.rowModified(ObservableListModelAdapter.this, l.getIndex(), l.getNewValue());
				}

				@Override
				public void visitAssign(@NonNull ListChangeAssign<T> assign) throws Exception {
					m_modelListener.modelChanged(ObservableListModelAdapter.this);
				}
			});
		}
	}
}

package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

/**
 * This is a selection model that selects simple instances. It allows both single and multiple
 * selections. It collects the selected instances in a set, so the instances should have
 * well-defined equals and hashcode meaning either the Object versions or properly implemented
 * ones.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 17, 2011
 */
public class InstanceSelectionModel<T> extends AbstractSelectionModel<T> implements Iterable<T> {
	final private Set<T> m_selectedSet = new HashSet<T>();

	final private boolean m_multiSelect;

	public InstanceSelectionModel(boolean multiSelect) {
		m_multiSelect = multiSelect;
	}

	@Override
	final public boolean isMultiSelect() {
		return m_multiSelect;
	}

	@Override
	public int getSelectionCount() {
		return m_selectedSet.size();
	}

	@Override
	public boolean isSelected(@Nonnull T rowinstance) {
		if(null == rowinstance) // Should not happen.
			throw new IllegalArgumentException("null row");
		return m_selectedSet.contains(rowinstance);
	}

	@Override
	public void setInstanceSelected(@Nonnull T rowinstance, boolean on) throws Exception {
		if(null == rowinstance) // Should not happen.
			throw new IllegalArgumentException("null row");
		if(on) {
			if(!m_multiSelect && m_selectedSet.size() > 0) {
				//-- We need to remove an earlier selected item.
				T old = m_selectedSet.iterator().next();
				if(rowinstance.equals(old))
					return;

				m_selectedSet.clear();
				callChanged(old, false);
			}

			if(!m_selectedSet.add(rowinstance))
				return; // Was already selected.
			callChanged(rowinstance, true);
		} else {
			if(!m_selectedSet.remove(rowinstance))
				return; // Was not selected.
			callChanged(rowinstance, false);
		}
	}

	@Override
	public void clearSelection() throws Exception {
		if(m_selectedSet.size() == 0)
			return;
		m_selectedSet.clear();
		callSelectionAllChanged();
	}

	@Override
	public void selectAll(ITableModel<T> in) throws Exception {
		int index = 0;
		int rows = in.getRows();
		while(index < rows) {
			int eix = index + 50;
			if(eix > rows)
				eix = rows;
			List<T> itemlist = in.getItems(index, eix);
			m_selectedSet.addAll(itemlist);
			index = eix;
		}
		callSelectionAllChanged();
	}

	@Override
	public Iterator<T> iterator() {
		return m_selectedSet.iterator();
	}

	public Set<T> getSelectedSet() {
		return new HashSet<T>(m_selectedSet);
	}
}

package to.etc.domui.component.tree;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;

public class TreeSelect<T> extends Tree<T> {
	private T m_value;

	public TreeSelect() {}

	public TreeSelect(ITreeModel<T> model) {
		super(model);
	}

	public T getValue() {
		return m_value;
	}

	public void setValue(T value) {
		if(MetaManager.areObjectsEqual(value, m_value))
			return;
		if(value != null && getNodeSelectablePredicate() != null) {
			try {
				if(!getNodeSelectablePredicate().predicate(value))
					throw new IllegalStateException("You cannot the value to a node that is marked as NOT SELECTABLE by the nodeSelectablePredicate");
			} catch(Exception x) {
				;
			}
		}

		if(m_value != null)
			markAsSelected(m_value, false);
		m_value = value;
		if(value != null)
			markAsSelected(value, true);
	}

	@Override
	protected boolean isSelectable(T node) throws Exception {
		if(getNodeSelectablePredicate() == null)
			return true;
		return getNodeSelectablePredicate().predicate(node);
	}

	@Override
	protected void cellClicked(TD cell, T value) throws Exception {
		setValue(value);
		super.cellClicked(cell, value);
	}
}

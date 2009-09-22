package to.etc.domui.component.input;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

public class ComboFixed<T> extends Select implements IInputNode<T> {
	static public final class Pair<T> {
		private T m_value;

		private String m_label;

		public Pair(T value, String label) {
			m_value = value;
			m_label = label;
		}

		public T getValue() {
			return m_value;
		}

		public String getLabel() {
			return m_label;
		}
	}

	private T m_currentValue;

	private String m_emptyText;

	private List<Pair<T>> m_choiceList = new ArrayList<Pair<T>>();

	public ComboFixed(List<Pair<T>> choiceList) {
		m_choiceList = choiceList;
	}

	public ComboFixed() {}

	@Override
	public void createContent() throws Exception {
		if(!isMandatory()) {
			//-- Add 1st "empty" thingy representing the unchosen.
			SelectOption o = new SelectOption();
			if(getEmptyText() != null)
				o.setText(getEmptyText());
			add(o);
			o.setSelected(m_currentValue == null);
		}

		ClassMetaModel cmm = null;
		for(Pair<T> val : m_choiceList) {
			SelectOption o = new SelectOption();
			add(o);
			o.add(val.getLabel());
			if(cmm == null)
				cmm = MetaManager.findClassMeta(val.getValue().getClass());
			boolean eq = MetaManager.areObjectsEqual(val.getValue(), m_currentValue, cmm);
			o.setSelected(eq);
		}
	}

	@Override
	public void forceRebuild() {
		super.forceRebuild();
	}

	public T getValue() {
		if(isMandatory() && m_currentValue == null) {
			setMessage(MsgType.ERROR, Msgs.BUNDLE, Msgs.MANDATORY);
			throw new ValidationException(Msgs.NOT_VALID, "null");
		}
		return m_currentValue;
	}

	/**
	 * FIXME Changing the selected option has O(2n) runtime. Use a map on large input sets?
	 *
	 * @see to.etc.domui.dom.html.IInputNode#setValue(java.lang.Object)
	 */
	public void setValue(T v) {
		if(MetaManager.areObjectsEqual(v, m_currentValue, null)) // FIXME Needs metamodel for better impl
			return;
		m_currentValue = v;
		if(!isBuilt())
			return;

		//-- We must set the index of one of the rendered thingies.
		for(NodeBase nb : this) {
			if(!(nb instanceof SelectOption))
				continue;
			SelectOption o = (SelectOption) nb;
			o.setSelected(false);
		}
		if(v == null) { // Set to null-> unselected.
			if(!isMandatory())
				((SelectOption) getChild(0)).setSelected(true);
			return;
		}

		//-- Locate the selected thingerydoo's index.
		int ix = 0;
		for(Pair<T> p : getData()) {
			if(p.getValue().equals(v)) {
				//-- Gotcha.
				if(!isMandatory())
					ix++;
				((SelectOption) getChild(ix)).setSelected(true);
				return;
			}
			ix++;
		}
	}

	public int findListIndexFor(T value) {
		int ix = 0;
		for(Pair<T> p : getData()) {
			if(p.getValue().equals(value))
				return ix;
			ix++;
		}
		return -1;
	}

	public String getEmptyText() {
		return m_emptyText;
	}

	public void setEmptyText(String emptyText) {
		m_emptyText = emptyText;
	}

	@Override
	public void acceptRequestParameter(String[] values) throws Exception {
		String in = values[0]; // Must be the ID of the selected Option thingy.
		SelectOption selo = (SelectOption) getPage().findNodeByID(in);
		//		T	oldvalue = m_currentValue;
		if(selo == null) {
			m_currentValue = null; // Nuttin' selected @ all.
		} else {
			int index = findChildIndex(selo); // Must be found
			if(index == -1)
				throw new IllegalStateException("Where has my child " + in + " gone to??");
			setSelectedIndex(index); // Set that selected thingerydoo
			if(!isMandatory()) {
				//-- If the index is 0 we have the "unselected" thingy; if not we need to decrement by 1 to skip that entry.
				if(index == 0)
					m_currentValue = null; // "Unselected"
				index--; // IMPORTANT Index becomes -ve if value lookup may not be done!
			}

			if(index >= 0) {
				if(index >= m_choiceList.size()) {
					m_currentValue = null; // Unexpected: value has gone.
				} else
					m_currentValue = m_choiceList.get(index).getValue(); // Retrieve actual value.
			}
		}
	}

	public void setData(List<Pair<T>> set) {
		m_choiceList = set;
		forceRebuild();
	}

	public List<Pair<T>> getData() {
		return m_choiceList;
	}
}

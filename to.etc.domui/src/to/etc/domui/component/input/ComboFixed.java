package to.etc.domui.component.input;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

public class ComboFixed<T> extends Select implements IInputNode<T>, IHasModifiedIndication {
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
			boolean eq = false;
			if(m_currentValue == null && val.getValue() == null && isMandatory()) // null is part of value domain, and in pair list?
				eq = true;
			else if(m_currentValue != null && val.getValue() != null) {
				if(cmm == null) {
					cmm = MetaManager.findClassMeta(val.getValue().getClass());
				}
				eq = MetaManager.areObjectsEqual(val.getValue(), m_currentValue, cmm);
			}
			o.setSelected(eq);
		}
	}

	@Override
	public void forceRebuild() {
		super.forceRebuild();
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#getValue()
	 */
	public T getValue() {
		if(isMandatory() && m_currentValue == null) {
			setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.MANDATORY));
			throw new ValidationException(Msgs.NOT_VALID, "null");
		}
		return m_currentValue;
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#getValueSafe()
	 */
	@Override
	public T getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#hasError()
	 */
	@Override
	public boolean hasError() {
		getValueSafe();
		return super.hasError();
	}

	/**
	 * FIXME Changing the selected option has O(2n) runtime. Use a map on large input sets?
	 *
	 * @see to.etc.domui.dom.html.IInputNode#setValue(java.lang.Object)
	 */
	public void setValue(T v) {
		ClassMetaModel cmm = v != null ? MetaManager.findClassMeta(v.getClass()) : null;
		if(MetaManager.areObjectsEqual(v, m_currentValue, cmm))
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
		int ix = findListIndexFor(v);
		if(ix < 0)
			return;
		if(!isMandatory())
			ix++;
		((SelectOption) getChild(ix)).setSelected(true);
	}

	public int findListIndexFor(T value) {
		int ix = 0;
		ClassMetaModel cmm = null;
		for(Pair<T> p : getData()) {
			if(value == null && p.getValue() == null && isMandatory()) // null is part of value domain, and in pair list?
				return ix;
			else if(value != null && p.getValue() != null) {
				if(cmm == null)
					cmm = MetaManager.findClassMeta(p.getValue().getClass());
				if(MetaManager.areObjectsEqual(p.getValue(), value, cmm))
					return ix;
			}
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
	public boolean acceptRequestParameter(String[] values) throws Exception {
		String in = values[0]; // Must be the ID of the selected Option thingy.
		SelectOption selo = (SelectOption) getPage().findNodeByID(in);
		T oldvalue = m_currentValue;
		if(selo == null) {
			updateCurrent(null); // Nuttin' selected @ all.
		} else {
			int index = findChildIndex(selo); // Must be found
			if(index == -1)
				throw new IllegalStateException("Where has my child " + in + " gone to??");
			setSelectedIndex(index); // Set that selected thingerydoo
			if(!isMandatory()) {
				//-- If the index is 0 we have the "unselected" thingy; if not we need to decrement by 1 to skip that entry.
				if(index == 0)
					updateCurrent(null); // "Unselected"
				index--; // IMPORTANT Index becomes -ve if value lookup may not be done!
			}

			if(index >= 0) {
				if(index >= m_choiceList.size()) {
					updateCurrent(null); // Unexpected: value has gone.
				} else
					updateCurrent(m_choiceList.get(index).getValue()); // Retrieve actual value.
			}
		}

		//-- Has anything changed?
		ClassMetaModel cmm = (oldvalue != null ? MetaManager.findClassMeta(oldvalue.getClass()) : null);
		if(MetaManager.areObjectsEqual(oldvalue, m_currentValue, cmm))
			return false;
		DomUtil.setModifiedFlag(this);
		return true;
	}

	private void updateCurrent(T newval) {
		m_currentValue = newval;
	}

	public void setData(List<Pair<T>> set) {
		m_choiceList = set;
		forceRebuild();
	}

	public List<Pair<T>> getData() {
		return m_choiceList;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IBindable interface (EXPERIMENTAL)					*/
	/*--------------------------------------------------------------*/

	/** When this is bound this contains the binder instance handling the binding. */
	private SimpleBinder m_binder;

	/**
	 * Return the binder for this control.
	 * @see to.etc.domui.component.input.IBindable#bind()
	 */
	public IBinder bind() {
		if(m_binder == null)
			m_binder = new SimpleBinder(this);
		return m_binder;
	}

	/**
	 * Returns T if this control is bound to some data value.
	 *
	 * @see to.etc.domui.component.input.IBindable#isBound()
	 */
	public boolean isBound() {
		return m_binder != null && m_binder.isBound();
	}
}

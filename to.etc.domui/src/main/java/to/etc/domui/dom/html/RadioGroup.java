package to.etc.domui.dom.html;

import java.util.*;

import to.etc.domui.component.meta.*;

/**
 * This is a simple marker which groups radiobuttons together. It can be used as a component too.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 4, 2011
 */
public class RadioGroup<T> extends Div implements IHasChangeListener {
	static private int m_gidCounter;

	private String m_groupName;

	private List<RadioButton<T>> m_buttonList = new ArrayList<RadioButton<T>>();

	private T m_value;

	private IValueChanged< ? > m_onValueChanged;

	private boolean m_immediate;

	public RadioGroup() {
		m_groupName = "g" + nextID();
	}

	static private synchronized int nextID() {
		return ++m_gidCounter;
	}

	void addButton(RadioButton<T> b) {
		m_buttonList.add(b);
		b.setChecked(MetaManager.areObjectsEqual(m_value, b.getButtonValue()));
	}

	void removeButton(RadioButton<T> b) {
		m_buttonList.remove(b);
	}

	public String getName() {
		return m_groupName;
	}

	public T getValue() {
		return m_value;
	}

	public void setValue(T value) {
		if(MetaManager.areObjectsEqual(value, m_value))
			return;
		m_value = value;
		for(RadioButton<T> rb : getButtonList()) {
			rb.setChecked(MetaManager.areObjectsEqual(value, rb.getButtonValue()));
		}
	}

	void internalSetValue(T newval) {
//		if(m_value != newval)
//			System.out.println("Changed from " + m_value + " to " + newval);
		m_value = newval;
	}

	public List<RadioButton<T>> getButtonList() {
		return Collections.unmodifiableList(m_buttonList);
	}

	@Override
	public IValueChanged< ? > getOnValueChanged() {
		IValueChanged< ? > vc = m_onValueChanged;
		if(null == vc && isImmediate()) {
			return IValueChanged.DUMMY;
		}
		return vc;
	}

	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	public boolean isImmediate() {
		return m_immediate;
	}

	public void immediate(boolean immediate) {
		m_immediate = immediate;
	}

	public void immediate() {
		m_immediate = true;
	}
}

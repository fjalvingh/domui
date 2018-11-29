package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a simple marker which groups radiobuttons together. It can be used as a component too.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 4, 2011
 */
public class RadioGroup<T> extends Div implements IHasChangeListener, IControl<T> {
	static private int m_gidCounter;

	private String m_groupName;

	private List<RadioButton<T>> m_buttonList = new ArrayList<RadioButton<T>>();

	private T m_value;

	private IValueChanged< ? > m_onValueChanged;

	private boolean m_readOnly;

	private boolean m_disabled;

	private boolean m_immediate;

	private boolean m_mandatory;

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

	@Override
	public T getValue() {
		try {
			validateBindValue();
			setMessage(null);
			return m_value;
		} catch(ValidationException vx) {
			setMessage(UIMessage.error(vx));
			throw vx;
		}
	}

	@Override
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

	final public T getBindValue() {
		validateBindValue();
		return m_value;
	}

	final public void setBindValue(T value) {
		if(MetaManager.areObjectsEqual(m_value, value)) {
			return;
		}
		setValue(value);
	}

	private void validateBindValue() {
		if(isMandatory() && m_value == null) {
			throw new ValidationException(Msgs.MANDATORY);
		}
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

	public RadioButton<T> addButton(String text, T value) {
		Div d = new Div();
		add(d);
		RadioButton<T> rb = new RadioButton<>(value);
		d.add(rb);
		d.add(new Label(rb, text));
		m_buttonList.add(rb);
		rb.setDisabled(m_disabled);
		rb.setReadOnly(m_readOnly);
		return rb;
	}

	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	public boolean isImmediate() {
		return m_immediate;
	}

	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	@Override
	public void setMandatory(boolean mandatory) {
		m_mandatory = mandatory;
	}

	public void immediate(boolean immediate) {
		m_immediate = immediate;
	}

	public void immediate() {
		m_immediate = true;
	}


	@Nullable @Override public NodeBase getForTarget() {
		return null;
	}

	@Override public T getValueSafe() {
		try {
			return getValue();
		} catch(Exception x) {
			return null;
		}
	}

	@Override public boolean isReadOnly() {
		return m_readOnly;
	}

	@Override public void setReadOnly(boolean ro) {
		if(m_readOnly == ro)
			return;
		for(RadioButton<T> rb : m_buttonList) {
			rb.setReadOnly(ro);
		}
		m_readOnly = ro;
	}

	@Override public boolean isDisabled() {
		return m_disabled;
	}

	@Override public void setDisabled(boolean d) {
		if(m_disabled == d)
			return;
		for(RadioButton<T> rb : m_buttonList) {
			rb.setDisabled(d);
		}
		m_disabled = d;
	}

}

package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.input.ValueLabelPair;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.misc.UIControlUtil;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.Msgs;
import to.etc.webapp.nls.NlsContext;

import java.util.ArrayList;
import java.util.Arrays;
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

	private boolean m_valueIsSet;

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

	public RadioGroup<T> asButtons() {
		addCssClass("ui-rbb-buttons");
		return this;
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
		if(MetaManager.areObjectsEqual(value, m_value) && m_valueIsSet)
			return;
		m_valueIsSet = true;
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

	@Nullable
	protected T internalGetValue() {
		return m_value;
	}

	final public T getBindValue() {
		validateBindValue();
		return m_value;
	}

	final public void setBindValue(T value) {
		if(MetaManager.areObjectsEqual(m_value, value) && m_valueIsSet) {
			return;
		}
		setValue(value);
	}

	private void validateBindValue() {
		if(isMandatory() && m_value == null) {
			throw new ValidationException(Msgs.mandatory);
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

	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
		if(null != onValueChanged) {
			List<RadioButton<?>> deepChildren =(List<RadioButton<?>>) (Object)getDeepChildren(RadioButton.class);		// What a trainwreck.
			for(RadioButton<?> deepChild : deepChildren) {
				deepChild.setClicked(clickednode -> {});
			}
		}
	}


	public RadioButton<T> addButton(String text, T value) {
		return addButton(text, value, null);
	}

	public RadioButton<T> addButton(String text, T value, @Nullable String title) {
		Div d = new Div("ui-rbb-item");
		add(d);
		RadioButton<T> rb = new RadioButton<>(value);
		d.add(rb);
		Label label = new Label(rb, text);
		d.add(label);
		label.setTitle(title);
		m_buttonList.add(rb);
		rb.setDisabled(m_disabled);
		rb.setReadOnly(m_readOnly);
		IValueChanged<?> ovc = m_onValueChanged;
		if(null != ovc)
			rb.setClicked(clickednode -> {});				// Force an event

		if(isBuilt())
			forceRebuild();
		return rb;
	}

	static public <T extends Enum<T>> RadioGroup<T> createEnumRadioGroup(Class<T> clz, T... exceptions) {
		ClassMetaModel cmm = MetaManager.findClassMeta(clz);
		List<ValueLabelPair<T>> l = new ArrayList<ValueLabelPair<T>>();
		T[] ar = clz.getEnumConstants();
		for(T v : ar) {
			if(!DomUtil.contains(exceptions, v)) {
				String label = cmm.getDomainLabel(NlsContext.getLocale(), v);
				if(label == null)
					label = v.name();
				l.add(new ValueLabelPair<T>(v, label));
			}
		}
		Collections.sort(l, (a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));
		var rg = new RadioGroup<T>();
		for(ValueLabelPair<T> tValueLabelPair : l) {
			rg.addButton(tValueLabelPair.getLabel(), tValueLabelPair.getValue());
		}
		return rg;
	}

	static public <T extends Enum<T>> RadioGroup<T> createEnumRadioGroup(T... enums) {
		ClassMetaModel metaModel = null;
		List<ValueLabelPair<T>> l = new ArrayList<>();
		for(T anEnum : enums) {
			if(metaModel == null) {
				metaModel = MetaManager.findClassMeta(anEnum.getClass());
			}
			String label = metaModel.getDomainLabel(NlsContext.getLocale(), anEnum);
			if(label == null)
				label = anEnum.name();
			l.add(new ValueLabelPair<T>(anEnum, label));
		}
		Collections.sort(l, (a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));
		var rg = new RadioGroup<T>();
		for(ValueLabelPair<T> tValueLabelPair : l) {
			rg.addButton(tValueLabelPair.getLabel(), tValueLabelPair.getValue());
		}
		return rg;
	}

	public void clearButtons() {
		m_buttonList.clear();
		forceRebuild();
	}

	public void removeButton(T value) {
		for(RadioButton<T> rb : m_buttonList) {
			if(rb.getButtonValue() == value) {
				m_buttonList.remove(rb);
				forceRebuild();
				if(m_value == value)
					m_value = null;
				return;
			}
		}
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
		if(ro) {
			addCssClass("ui-ro");
		} else {
			removeCssClass("ui-ro");
		}
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
		if(d) {
			addCssClass("ui-disabled");
		} else {
			removeCssClass("ui-disabled");
		}
	}

	public static <T extends Enum<T>> RadioGroup<T> createFromEnum(Class<T> enumClass, T... ignored) {
		List<T> list = new ArrayList<>(Arrays.asList(enumClass.getEnumConstants()));
		for(T t : ignored) {
			list.remove(t);
		}

		ClassMetaModel cmm = MetaManager.findClassMeta(enumClass);
		RadioGroup<T> rg = new RadioGroup<>();
		for(T t : list) {
			String label = cmm.getDomainLabel(NlsContext.getLocale(), t);
			if(null == label)
				label = t.name();
			rg.addButton(label, t);
		}

		return rg;
	}


	@Override public void setHint(String hintText) {
		setTitle(hintText);
	}



	@NonNull
	static public <T> RadioGroup<T> createGroupFor(PropertyMetaModel<T> pmm, boolean editable, boolean asButtons) {
		if(pmm == null)
			throw new IllegalArgumentException("propertyMeta cannot be null");
		Object[] vals = pmm.getDomainValues();
		if(vals == null || vals.length == 0)
			throw new IllegalArgumentException("The type of property " + pmm + " (" + pmm.getActualType() + ") is not known as a fixed-size domain type");

		RadioGroup<T> rg = new RadioGroup<>();
		ClassMetaModel ecmm = null;
		for(Object o : vals) {
			String label = pmm.getDomainValueLabel(NlsContext.getLocale(), o); // Label known to property?
			if(label == null) {
				if(ecmm == null)
					ecmm = MetaManager.findClassMeta(pmm.getActualType()); // Try to get the property's type.
				label = ecmm.getDomainLabel(NlsContext.getLocale(), o);
				if(label == null)
					label = o == null ? "" : o.toString();
			}
			rg.addButton(label, (T) o);
		}

		UIControlUtil.configure(rg, pmm, editable);
		if(asButtons)
			rg.asButtons();
		return rg;
	}
}

package to.etc.domui.component.buttons;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.input.AbstractDivControl;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.Msgs;

/**
 * This is a button that acts like a checkbox. See <a href="https://1stwebdesigner.com/css-snippets-radio-toggles-switches/">here</a> for
 * details.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-12-18.
 */
public class CheckboxButton extends AbstractDivControl<Boolean> {
	private Checkbox m_cb = new Checkbox();

	@Nullable
	private String m_onLabel;

	@Nullable
	private String m_offLabel;

	@Override public void createContent() throws Exception {
		addCssClass("ui-chkbb");
		if(isMandatory())
			addCssClass("ui-mandatory");
		if(isReadOnly())
			addCssClass("ui-ro");
		if(isDisabled())
			addCssClass("ui-disabled");
		add(m_cb);
		Label l = new Label();
		add(l);
		l.setForTarget(m_cb);
		Div cont = new Div("ui-chkbb-sw");
		l.add(cont);

		//-- Calculate on and off texts
		String on = getOnLabel();
		if(null == on)
			on = Msgs.uiChkbbOn.getString();
		String off = getOffLabel();
		if(null == off)
			off = Msgs.uiChkbbOff.getString();

		cont.setSpecialAttribute("data-checked", on);
		cont.setSpecialAttribute("data-unchecked", off);
	}

	@NonNull @Override public CheckboxButton css(@NonNull String... classNames) {
		super.css(classNames);
		return this;
	}

	@Nullable @Override public NodeBase getForTarget() {
		return m_cb;
	}

	@Nullable public String getOnLabel() {
		return m_onLabel;
	}

	public CheckboxButton setOnLabel(@Nullable String onLabel) {
		m_onLabel = onLabel;
		return this;
	}

	@Nullable public String getOffLabel() {
		return m_offLabel;
	}

	public CheckboxButton setOffLabel(@Nullable String offLabel) {
		m_offLabel = offLabel;
		return this;
	}

	@Deprecated
	@Override
	public IValueChanged< ? > getOnValueChanged() {
		IValueChanged< ? > vc = m_cb.getOnValueChanged();
		return vc;
	}

	@Override public void setOnValueChanged(IValueChanged<?> onValueChanged) {
		m_cb.setOnValueChanged(onValueChanged);
	}

	@Override protected void internalSetValue(@Nullable Boolean value) {
		m_cb.setValue(value);
	}

	@Nullable @Override protected Boolean internalGetValue() {
		return m_cb.getValue();
	}

	public CheckboxButton setChecked(boolean on) {
		setValue(on);
		return this;
	}

	public boolean isChecked() {
		return Boolean.TRUE.equals(getBindValue());
	}

	public boolean isImmediate() {
		return m_cb.isImmediate();
	}


	public void immediate(boolean immediate) {
		m_cb.immediate(immediate);
	}

	public void immediate() {
		m_cb.immediate();
	}

	@Override
	public void setDisabled(boolean d) {
		super.setDisabled(d);
		m_cb.setDisabled(d);
	}

	@Override
	public void setReadOnly(boolean ro) {
		super.setReadOnly(ro);
		m_cb.setReadOnly(ro);
	}
}

package to.etc.domui.component.buttons;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.input.AbstractDivControl;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;

/**
 * This is a simple on/off switch button, like the CheckBoxButton but
 * without text.
 */
public class SwitchButton extends AbstractDivControl<Boolean> {
	private Checkbox m_cb = new Checkbox();

	public enum DisplayMode {
		Square,
		Rounded,
	}

	private DisplayMode m_displayMode = DisplayMode.Rounded;

	public SwitchButton() {
		internalSetTag("label");
	}

	@Override
	public void createContent() throws Exception {
		addCssClass("ui-swtch");
		add(m_cb);
		add(new Span("ui-swtch-sl ui-swtch-" + m_displayMode.name().toLowerCase(), null));

	}

	public void setDisplayMode(DisplayMode displayMode) {
		if(m_displayMode == displayMode)
			return;
		m_displayMode = displayMode;
		forceRebuild();
	}

	@Nullable
	@Override
	public NodeBase getForTarget() {
		return m_cb;
	}

	@Deprecated
	@Override
	public IValueChanged<?> getOnValueChanged() {
		IValueChanged<?> vc = m_cb.getOnValueChanged();
		return vc;
	}

	@Override
	public void setOnValueChanged(IValueChanged<?> onValueChanged) {
		m_cb.setOnValueChanged(onValueChanged);
	}

	public boolean isChecked() {
		return m_cb.isChecked();
	}

	public void setChecked(boolean checked) {
		m_cb.setChecked(checked);
	}
}

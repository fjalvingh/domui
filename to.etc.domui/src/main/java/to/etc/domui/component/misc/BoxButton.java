package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.Button;

final class BoxButton {
	private final Button m_button;

	@Nullable
	private final MsgBoxButton m_type;

	private MsgBoxButtonPrio m_prio;

	public BoxButton(Button button, @Nullable MsgBoxButton type, MsgBoxButtonPrio prio) {
		m_button = button;
		m_type = type;
		m_prio = prio;
	}

	public Button getButton() {
		return m_button;
	}

	public MsgBoxButtonPrio getPrio() {
		return m_prio;
	}

	public int getOrder() {
		return m_prio.ordinal();
	}

	@Nullable
	public MsgBoxButton getType() {
		return m_type;
	}

	public void setPrio(MsgBoxButtonPrio prio) {
		m_prio = prio;
	}
}

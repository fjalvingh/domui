package to.etc.domui.component.misc;

import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;

import java.util.Set;

/**
 * This is an empty div which reacts to a given keycode.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 05-01-22.
 */
public class KeyCodeDiv extends Div {
	private final String m_keyCode;

	private final Set<KeyModifier> m_modifierSet;

	public KeyCodeDiv(String keyCode, Set<KeyModifier> modifierSet, IClicked<KeyCodeDiv> clicked) {
		m_keyCode = keyCode;
		m_modifierSet = modifierSet;
		setClicked(clicked);
	}

	@Override
	public void createContent() throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("$(document).keypress(function(e) {\n");
		sb.append("if(e.key=='").append(m_keyCode).append("'");
		if(m_modifierSet.contains(KeyModifier.Shift)) {
			sb.append("&& e.shiftKey");
		} else {
			sb.append("&& !e.shiftKey");
		}
		if(m_modifierSet.contains(KeyModifier.Alt)) {
			sb.append("&& e.altKey");
		} else {
			sb.append("&& !e.altKey");
		}
		if(m_modifierSet.contains(KeyModifier.Ctrl)) {
			sb.append("&& e.ctrlKey");
		} else {
			sb.append("&& !e.ctrlKey");
		}
		sb.append(") {\n");
		sb.append("WebUI.clicked(this, '").append(getActualID()).append("');\n");
		sb.append("}\n");
		sb.append("});\n");
		appendCreateJS(sb);;
	}
}

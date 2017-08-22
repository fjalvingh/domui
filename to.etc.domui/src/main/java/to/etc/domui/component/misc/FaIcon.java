package to.etc.domui.component.misc;

import to.etc.domui.dom.html.Span;

import java.util.Objects;

/**
 * FontAwesome icon.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-8-17.
 */
final public class FaIcon extends Span {
	private String m_iconName;

	public FaIcon(String name) {
		m_iconName = name;
	}

	public FaIcon cssClass(String s) {
		addCssClass(s);
		return this;
	}

	@Override public void createContent() throws Exception {
		//removeFaClasses();
		addCssClass("fa");
		addCssClass(m_iconName);
	}

	private void removeFaClasses() {
		String cssClass = getCssClass();
		if(null == cssClass)
			return;

		String[] split = cssClass.split("\\s+");
		for(String s : split) {
			if(s.equals("fa") || s.startsWith("fa-")) {
				removeCssClass(s);
			}
		}
	}

	public void setIconName(String iconName) {
		if(Objects.equals(iconName, m_iconName))
			return;
		removeCssClass(m_iconName);
		addCssClass(iconName);
		m_iconName = iconName;
	}
}

package to.etc.domui.fontawesome;

import to.etc.domui.component.misc.FontIcon;
import to.etc.domui.component.misc.IFontIcon;
import to.etc.domui.dom.html.NodeBase;

/**
 * All of the definitions in the FontAwesome 4.7.0 font distribution.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-10-18.
 */
public enum FaIcon implements IFontIcon {
	///--- BEGIN ICONS
	///--- END ICONS
	;

	///--- BEGIN MAP
	///--- END MAP
	;

	private final String m_css;
	private final String m_prefix;

	FaIcon(String css, String prefix) {
		m_css = css;
		m_prefix = prefix;
	}

	public String getCssClassName() {
		return m_prefix + " " + m_css;
	}

	public NodeBase createNode() {
		return new FontIcon(this);
	}
}

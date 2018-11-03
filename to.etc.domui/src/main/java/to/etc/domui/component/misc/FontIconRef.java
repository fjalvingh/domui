package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.dom.html.NodeBase;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-11-18.
 */
@NonNullByDefault
final public class FontIconRef implements IFontIconRef {
	private final String m_fontIcon;

	public FontIconRef(String fontIcon, String[] cssClasses) {
		StringBuilder sb = new StringBuilder();
		sb.append(fontIcon);
		for(String cssClass : cssClasses) {
			sb.append(" ").append(cssClass);
		}
		m_fontIcon = sb.toString();
	}

	@Override public String getCssClassName() {
		return m_fontIcon;
	}

	@Override public NodeBase createNode() {
		return new FontIcon(this);
	}

	@Override public IIconRef css(String... classes) {
		return new FontIconRef(m_fontIcon, classes);
	}
}

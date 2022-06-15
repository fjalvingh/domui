package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.NodeBase;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-11-18.
 */
final public class WrappedIconRef implements IFontIconRef {
	private final IIconRef m_wrapped;

	private final String m_cssClasses;

	public WrappedIconRef(IIconRef wrapped, String[] cssClasses) {
		m_wrapped = wrapped;
		StringBuilder sb = new StringBuilder(m_wrapped.getClasses());
		for(String cssClass : cssClasses) {
			sb.append(" ").append(cssClass);
		}
		m_cssClasses = sb.toString();
	}

	@Override public NodeBase createNode(String cssClasses) {
		return m_wrapped.createNode(getClasses());
	}

	@Override public String getClasses() {
		return m_cssClasses;
	}

	@NonNull
	@Override
	public String getCssClassName() {
		return m_cssClasses;
	}
}

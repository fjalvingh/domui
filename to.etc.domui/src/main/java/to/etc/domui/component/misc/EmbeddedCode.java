package to.etc.domui.component.misc;

import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Span;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-11-18.
 */
final public class EmbeddedCode extends Div {
	final private String m_code;

	public EmbeddedCode(String code) {
		m_code = code;
	}

	@Override public void createContent() throws Exception {
		addCssClass("ui-embcd");
		add(new Span(m_code));
	}
}

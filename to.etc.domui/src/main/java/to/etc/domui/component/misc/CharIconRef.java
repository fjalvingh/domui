package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 01-07-2022.
 */
final public class CharIconRef implements IIconRef {
	private final char m_char;

	@NonNull
	private final String m_cssClasses;

	public CharIconRef(char character) {
		m_char = character;
		m_cssClasses = "";
	}

	public CharIconRef(char character, String[] cssClasses) {
		m_char = character;
		StringBuilder sb = new StringBuilder();
		for(String cssClass : cssClasses) {
			sb.append(" ").append(cssClass);
		}
		m_cssClasses = sb.toString();
	}

	@Override
	public String getClasses() {
		return m_cssClasses;
	}

	public char getCharacter() {
		return m_char;
	}

	@Override
	public NodeBase createNode(String cssClasses) {
		char ch = m_char;
		Span sp = new Span("ui-icon-char", null);
		sp.add(String.valueOf(ch));
		return sp;
	}

	@Override
	public IIconRef css(@NonNull String... classes) {
		return new CharIconRef(m_char, classes);
	}
}

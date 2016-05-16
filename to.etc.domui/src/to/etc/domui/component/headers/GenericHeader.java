package to.etc.domui.component.headers;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9/21/15.
 */
@DefaultNonNull
public class GenericHeader extends Div {
	@Nullable
	private String m_text;

	public enum Type {
		/** A simple header with just a bigger font, in black */
		SIMPLE

		/** A simple header with a bigger font, in blue */
		, BLUE

		/* Generic styles */
		, HEADER_1
		, HEADER_2
		, HEADER_3
		, HEADER_4
	}

	final private Type m_type;

	public GenericHeader(Type type, String text) {
		m_type = type;
		m_text = text;
	}

	public GenericHeader(String text) {
		this(Type.SIMPLE, text);
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-generichd ui-generichd-" + m_type.name().toLowerCase().replace('_', '-'));
		add(m_text);
	}
}

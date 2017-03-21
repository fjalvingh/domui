package to.etc.domui.component.layout;

import javax.annotation.*;

import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Forms a simple error message line with a small icon and a text (which may contain basic html).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 3, 2012
 */
public class MessageLine extends Div {
	@Nullable
	final private MsgType m_type;

	@Nonnull
	final private String m_text;

	@Nullable
	final private String m_icon;

	public MessageLine(@Nonnull MsgType type, @Nonnull String text) {
		m_type = type;
		m_text = text;
		m_icon = null;
	}

	public MessageLine(@Nonnull String icon, @Nonnull String text) {
		m_icon = icon;
		m_text = text;
		m_type = null;
	}

	@Override
	public void createContent() throws Exception {
		addCssClass("ui-msgln");
		Img img = new Img();
		if(m_type != null) {
			img.setSrc("THEME/mini-" + m_type.name().toLowerCase() + ".png");
		} else if(m_icon != null) {
			img.setSrc(m_icon);
		} else
			throw new IllegalArgumentException("No icon nor type.");
		add(img);
		Span sp = new Span();
		add(sp);
		sp.addCssClass("ui-msgln-txt");
		DomUtil.renderHtmlString(sp, m_text);
	}
}

package to.etc.domui.component.layout;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;
import to.etc.domui.util.DomUtil;
import to.etc.function.ConsumerEx;
import to.etc.webapp.nls.IBundleCode;

/**
 * Forms a simple error message line with a small icon and a text (which may contain basic html).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 3, 2012
 */
public class MessageLine extends Div {
	@Nullable
	final private MsgType m_type;

	@Nullable
	final private String m_text;

	@Nullable
	final private String m_icon;

	@Nullable
	final private ConsumerEx<NodeContainer> m_renderer;

	public MessageLine(@NonNull MsgType type, @NonNull IBundleCode code) {
		this(type, code.getString());
	}

	public MessageLine(@NonNull MsgType type, @NonNull String text) {
		m_type = type;
		m_text = text;
		m_icon = null;
		m_renderer = null;
	}

	public MessageLine(@NonNull String icon, @NonNull String text) {
		m_icon = icon;
		m_text = text;
		m_type = null;
		m_renderer = null;
	}

	public MessageLine(@Nullable MsgType type, @Nullable ConsumerEx<NodeContainer> renderer) {
		m_type = type;
		m_renderer = renderer;
		m_icon = null;
		m_text = null;
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
		String text = m_text;
		ConsumerEx<NodeContainer> r = m_renderer;
		if(null != text) {
			DomUtil.renderHtmlString(sp, text);
		} else if(r != null) {
			r.accept(sp);
		}
	}
}

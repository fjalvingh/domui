package to.etc.domuidemo;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.misc.ALink;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-3-19.
 */
public class ListFragment extends Div {
	final private String m_caption;

	final private Div m_content = new Div("lpf-content-links");

	public ListFragment(String caption) {
		m_caption = caption;
		setCssClass("lpf-panel");
	}

	@Override public void createContent() throws Exception {
		add(new HTag(1, m_caption));
		add(m_content);
	}

	final protected void addLink(@NonNull Class< ? extends UrlPage> clz, @NonNull String text) throws Exception {
		addLink(clz, text, false);
	}

	final protected void addLink(@NonNull Class< ? extends UrlPage> clz, @NonNull String text, boolean nw) throws Exception {
		build();
		Div d = new Div();
		m_content.add(d);
		ALink link = new ALink(clz);
		d.add(link);
		link.setText(text);
	}
}

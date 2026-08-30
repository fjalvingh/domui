package to.etc.domuidemo.pages.tutorial.navigation;

import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.UIGoto;

/**
 * Tutorial, "page navigation", step 1: a page keeps its state in the server, and
 * what the different UIGoto moves do with that state.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class NavStatePage extends UrlPage {
	/** The state of this screen: two fields, and nothing else. */
	private int m_clicks;

	private String m_note;

	@Override
	public void createContent() throws Exception {
		setPageTitle("Page state and navigation");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Page state and navigation"));

		Text2<String> note = new Text2<>(String.class);
		note.setValue(m_note);
		note.setOnValueChanged(c -> {
			m_note = note.getValueSafe();
			forceRebuild();
		});

		FormBuilder fb = new FormBuilder(cp);
		fb.label("A note").control(note);

		Div state = new Div("dm-tut");
		cp.add(state);
		state.add("Clicks: " + m_clicks + ", note: " + (m_note == null ? "(empty)" : m_note));

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addButton("Count a click", a -> {
			m_clicks++;
			forceRebuild();
		});

		bb.addButton("Detail (moveSub)", a -> UIGoto.moveSub(NavDetailPage.class));
		bb.addButton("Detail with a message", a -> {
			UIGoto.addActionMessage(MsgType.INFO, "Sent along by the page you came from");
			UIGoto.moveSub(NavDetailPage.class);
		});
		bb.addButton("Detail (replace)", a -> UIGoto.replace(NavDetailPage.class));
		bb.addButton("Reload this page", a -> UIGoto.reload());
	}
}

package to.etc.domuidemo.pages.tutorial.tables;

import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.derbydata.db.Track_;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

/**
 * Tutorial, "showing rows": the page a row click opens, so that the list page
 * gets shelved and re-queries when you come back to it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TableDetailPage extends UrlPage {
	private Track m_track;

	@Override
	public String getPageTitle() {
		return m_track.getName();
	}

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, m_track.getName()));

		FormBuilder fb = new FormBuilder(cp);
		fb.property(m_track, Track_.name()).readOnly().control();
		fb.property(m_track, Track_.composer()).readOnly().control();
		fb.property(m_track, Track_.unitPrice()).readOnly().control();

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addBackButton();
	}

	@UIUrlParameter(name = "id")
	public Track getTrack() {
		return m_track;
	}

	public void setTrack(Track track) {
		m_track = track;
	}
}

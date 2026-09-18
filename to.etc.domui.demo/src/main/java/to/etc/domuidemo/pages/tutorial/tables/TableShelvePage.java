package to.etc.domuidemo.pages.tutorial.tables;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.derbydata.db.Track_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.UIGoto;
import to.etc.webapp.query.QCriteria;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Tutorial, "showing rows", step 4: shelving the page throws the model's result
 * away, so returning to it re-runs the query. The counter below is incremented
 * inside the query itself.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TableShelvePage extends UrlPage {
	/** The state of this page: how often its query has run. */
	private int m_queryCount;

	@Override
	public void createContent() throws Exception {
		setPageTitle("Re-querying after a visit");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Re-querying after a visit"));

		Div info = new Div("dm-tut");
		cp.add(info);
		info.add("The query has not run yet.");

		SimpleSearchModel<Track> model = new SimpleSearchModel<>(this, (dc, sortOn, maxRows) -> {
			m_queryCount++;
			info.removeAllChildren();
			info.add("The query has run " + m_queryCount + " time(s), the last one at "
				+ LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ".");

			QCriteria<Track> q = QCriteria.create(Track.class);
			q.ascending(sortOn == null ? Track_.name().getName() : sortOn);
			return dc.query(q.limit(maxRows));
		});

		RowRenderer<Track> rr = new RowRenderer<>(Track.class);
		rr.column(Track_.name()).label("Track").ascending().sortdefault();
		rr.column(Track_.album().title()).label("Album");
		rr.setRowClicked(t -> UIGoto.moveSub(TableDetailPage.class, "id", t.getId()));

		DataTable<Track> dt = new DataTable<>(model, rr);
		cp.add(dt);
		dt.setPageSize(10);
		cp.add(new DataPager(dt));
	}
}

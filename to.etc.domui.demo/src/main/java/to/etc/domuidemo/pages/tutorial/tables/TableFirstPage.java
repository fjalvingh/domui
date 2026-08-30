package to.etc.domuidemo.pages.tutorial.tables;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.derbydata.db.Track_;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

/**
 * Tutorial, "showing rows", step 1: the three parts of every table - the model
 * that has the rows, the RowRenderer that says what a row looks like, and the
 * DataTable that shows them.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TableFirstPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Your first table");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Your first table"));

		//-- 1. The model: the question, and the thing that will run it.
		QCriteria<Track> q = QCriteria.create(Track.class);
		SimpleSearchModel<Track> model = new SimpleSearchModel<>(this, q);

		//-- 2. The renderer: which columns, in which order.
		RowRenderer<Track> rr = new RowRenderer<>(Track.class);
		rr.column(Track_.name()).label("Track").ascending().sortdefault();
		rr.column(Track_.album().title()).label("Album");
		rr.column(Track_.unitPrice()).label("Price");

		//-- 3. The table itself, plus a pager to walk through the result.
		DataTable<Track> dt = new DataTable<>(model, rr);
		cp.add(dt);
		dt.setPageSize(10);
		cp.add(new DataPager(dt));

		cp.add(new HTag(2, "The same table without columns"));

		SimpleSearchModel<Track> model2 = new SimpleSearchModel<>(this, QCriteria.create(Track.class));
		DataTable<Track> dt2 = new DataTable<>(model2, new RowRenderer<>(Track.class));
		cp.add(dt2);
		dt2.setPageSize(5);
		cp.add(new DataPager(dt2));
	}
}

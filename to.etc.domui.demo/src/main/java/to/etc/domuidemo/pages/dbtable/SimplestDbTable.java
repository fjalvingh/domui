package to.etc.domuidemo.pages.dbtable;

import to.etc.domui.component.layout.CaptionedHeader;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

/**
 * Simplest example of showing data from the database. Shows:
 * <ul>
 * 	<li>Using QCriteria to define a selection</li>
 *	<li>Using
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 25, 2010
 */
public class SimplestDbTable extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new CaptionedHeader("Artists in the database"));

		// Just select all artists. This does not query but /specifies/ the query to do
		QCriteria<Artist> q = QCriteria.create(Artist.class);

		SimpleSearchModel<Artist> model = new SimpleSearchModel<>(this, q);
		model.sortOn("name", false);

		RowRenderer<Artist> rr = new RowRenderer<>(Artist.class);
		DataTable<Artist> dt = new DataTable<>(model, rr);
		add(dt);
		dt.setPageSize(25);

		DataPager dp = new DataPager(dt);
		add(dp);
	}
}

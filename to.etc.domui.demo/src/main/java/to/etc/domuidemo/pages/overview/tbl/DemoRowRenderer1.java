package to.etc.domuidemo.pages.overview.tbl;

import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

public class DemoRowRenderer1 extends UrlPage {
	@Override
	public void createContent() throws Exception {
		// Set up a data table
		QCriteria<Album> q = QCriteria.create(Album.class);
		SimpleSearchModel<Album> ssm = new SimpleSearchModel<>(this, q);

		RowRenderer<Album> brr = new RowRenderer<>(Album.class);
		brr.column(Album_.artist().name()).label("The Artist").width(30);
		brr.column(Album_.title()).label("Album Name").ascending().width(40);

		DataTable<Album> dt = new DataTable<>(ssm, brr);
		add(dt);
		dt.setPageSize(25);

		//-- This is the actual demo ;-)
		add(new DataPager(dt));
	}
}

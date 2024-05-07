package to.etc.domuidemo.pages.overview.tbl;

import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.IRenderInto;
import to.etc.webapp.query.QCriteria;

public class DemoRowRenderer2 extends UrlPage {
	@Override
	public void createContent() throws Exception {
		// Set up a data table
		QCriteria<Album> q = QCriteria.create(Album.class);
		SimpleSearchModel<Album> ssm = new SimpleSearchModel<>(this, q);

		IRenderInto<String>	ncr = (node, value) -> {
			String inside = value.toLowerCase();
			if(inside.contains("import")) {
				node.add(new Img("img/import.png"));
			} else if(inside.contains("live")) {
				node.add(new Img("img/live.png"));
			}
			node.add("\u00a0" + value);
		};

		RowRenderer<Album> brr = new RowRenderer<>(Album.class);
		brr.column(Album_.artist().name()).label("The Artist").width(30);
		brr.column(Album_.title()).label("Album Name").ascending().width(40).renderer(ncr);
		DataTable<Album> dt = new DataTable<Album>(ssm, brr);
		add(dt);
		dt.setPageSize(25);

		//-- This is the actual demo ;-)
		add(new DataPager(dt));
	}
}

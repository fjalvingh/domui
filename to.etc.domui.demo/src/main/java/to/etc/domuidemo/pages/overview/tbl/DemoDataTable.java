package to.etc.domuidemo.pages.overview.tbl;

import to.etc.domui.component.tbl.*;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

public class DemoDataTable extends UrlPage {
	@Override
	public void createContent() throws Exception {
		QCriteria<Album> q = QCriteria.create(Album.class);
		SimpleSearchModel<Album> ssm = new SimpleSearchModel<Album>(this, q);

		BasicRowRenderer<Album> brr = new BasicRowRenderer<Album>(Album.class, "artist.name", "title");
		DataTable<Album> dt = new DataTable<Album>(ssm, brr);
		add(dt);
		dt.setPageSize(25);
	}
}

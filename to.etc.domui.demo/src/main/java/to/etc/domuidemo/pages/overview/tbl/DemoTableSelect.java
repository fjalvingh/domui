package to.etc.domuidemo.pages.overview.tbl;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

public class DemoTableSelect extends UrlPage {
	@Override
	public void createContent() throws Exception {
		// Set up a data table
		QCriteria<Album> q = QCriteria.create(Album.class);
		SimpleSearchModel<Album> ssm = new SimpleSearchModel<Album>(this, q);

		BasicRowRenderer<Album> brr = new BasicRowRenderer<Album>(Album.class, "artist.name", "^The Artist", "%50", "title", "^Album title", "%50", SortableType.SORTABLE_ASC);
		DataTable<Album> dt = new DataTable<Album>(ssm, brr);
		add(dt);
		dt.setPageSize(25);

		//-- Set up a selection model.
		InstanceSelectionModel<Album> sm = new InstanceSelectionModel<Album>(true);

		dt.setSelectionModel(sm);

		//-- This is the actual demo ;-)
		add(new DataPager(dt));
	}
}

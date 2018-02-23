package to.etc.domuidemo.pages.test;

import to.etc.domui.component.input.Text2;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleListModel;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

public class RowRendererFactoryTest extends UrlPage {

	@Override public void createContent() throws Exception {
		SimpleListModel<Artist> slm = new SimpleListModel<>(getSharedContext().query(QCriteria.create(Artist.class).eq("name", "Black Sabbath")));
		RowRenderer<Artist> rr = new RowRenderer<>(Artist.class);
		rr.column(String.class, "name").label("Read-only").factory(row -> {
				Text2<String> ctrl = new Text2<>(String.class);
				ctrl.bind().to(row, "name");
				ctrl.setReadOnly(true);
				return ctrl;
			}
		).editable();

		rr.column(String.class, "name").label("Editable").factory(row -> {
				Text2<String> ctrl = new Text2<>(String.class);
				ctrl.bind().to(row, "name");
				return ctrl;
			}
		);

		DataTable<Artist> dt = new DataTable<>(slm, rr);
		add(dt);
	}
}

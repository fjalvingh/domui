package to.etc.domuidemo.pages.binding.tbl;

import javax.annotation.*;

import to.etc.domui.component.tbl.*;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.dom.html.*;
import to.etc.domuidemo.pages.*;
import to.etc.webapp.query.*;

public class DemoTableBinding1 extends WikiExplanationPage {
	@Override
	public void createContent() throws Exception {
		QDataContext dc = getSharedContext();
		QCriteria<Artist> q = QCriteria.create(Artist.class);

		SimpleSearchModel<Artist> sm = new SimpleSearchModel<Artist>(this, q);
		RowRenderer<Artist> rr = new RowRenderer<Artist>(Artist.class);
		rr.column("name").label("Name");

		DataTable<Artist> dt = new DataTable<Artist>(sm, rr);
		add(dt);
		dt.setPageSize(10);
		add(new DataPager(dt));

		rr.setRowClicked(new ICellClicked<Artist>() {
			@Override
			public void cellClicked(Artist rowval) throws Exception {
				clickedOne(rowval);
			}
		});
	}

	private void clickedOne(@Nonnull final Artist a) {
		//-- Change the artist's name field.
		String name = a.getName();
		name = name.substring(1) + name.substring(0, 1);
		a.setName(name);
	}

}

package to.etc.domuidemo.pages.binding;

import java.util.*;

import to.etc.domui.component.tbl.*;
import to.etc.domui.databinding.*;
import to.etc.domui.dom.html.*;
import to.etc.domuidemo.db.*;
import to.etc.webapp.query.*;

/**
 * Demo/test for {@link IObservableList} support in Hibernate relations.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 4, 2013
 */
public class DatabaseRelationPage extends UrlPage {

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
			public void cellClicked(NodeBase tr, Artist rowval) throws Exception {
				clickedOne(rowval);
			}
		});

	}

	private void clickedOne(Artist rowval) {
		List<Album> res = rowval.getAlbumList();
		System.out.println("Type is: " + res.getClass());

	}

}

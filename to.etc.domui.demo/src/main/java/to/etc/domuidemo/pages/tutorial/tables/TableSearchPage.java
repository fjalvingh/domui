package to.etc.domuidemo.pages.tutorial.tables;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.derbydata.db.Track_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

/**
 * Tutorial, "showing rows", step 3: a SearchPanel makes the QCriteria, and the
 * table shows what that query returns.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TableSearchPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("A search screen");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "A search screen"));

		SearchPanel<Track> sp = new SearchPanel<>(Track.class, "name", "album.title", "album.artist.name");
		cp.add(sp);

		//-- One more field, added by hand.
		sp.add().property(Track_.unitPrice()).label("Price").control();

		Div results = new Div();
		cp.add(results);

		sp.setClicked(a -> {
			QCriteria<Track> criteria = sp.getCriteria();
			if(null == criteria)                            // Bad input: the errors are on the screen already.
				return;
			results.removeAllChildren();

			SimpleSearchModel<Track> model = new SimpleSearchModel<>(this, criteria);
			RowRenderer<Track> rr = new RowRenderer<>(Track.class);
			rr.column(Track_.name()).label("Track").ascending().sortdefault();
			rr.column(Track_.milliseconds()).label("Duration");
			rr.column(Track_.album().title()).label("Album");
			rr.column(Track_.album().artist().name()).label("Artist");

			DataTable<Track> dt = new DataTable<>(model, rr);
			results.add(dt);
			dt.setPageSize(10);
			results.add(new DataPager(dt));
		});
	}
}

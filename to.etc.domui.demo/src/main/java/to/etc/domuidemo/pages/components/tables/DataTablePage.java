package to.etc.domuidemo.pages.components.tables;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

/**
 * DataTable: a model, a row renderer and a pager - the three parts every table
 * screen is made of.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class DataTablePage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("DataTable: rows on the screen");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "DataTable: rows on the screen"));

		//-- 1. The model: what to show.
		QCriteria<Album> q = QCriteria.create(Album.class).ascending(Album_.title());
		SimpleSearchModel<Album> model = new SimpleSearchModel<>(this, q);

		//-- 2. The renderer: how one row looks.
		RowRenderer<Album> rr = new RowRenderer<>(Album.class);
		rr.column(Album_.title()).label("Album").width(40).ascending().sortdefault();
		rr.column(Album_.artist().name()).label("Artist").width(30).ascending();

		//-- 3. The table, and the pager that walks it.
		DataTable<Album> table = new DataTable<>(model, rr);
		cp.add(table);
		table.setPageSize(10);
		cp.add(new DataPager(table));

		//-- A table whose query finds nothing says so.
		cp.add(new HTag(2, "A table with nothing in it"));
		SimpleSearchModel<Album> none = new SimpleSearchModel<>(this,
			QCriteria.create(Album.class).eq(Album_.title(), "no album is called this"));
		DataTable<Album> emptyTable = new DataTable<>(none, new RowRenderer<>(Album.class));
		emptyTable.setEmptyMessage("No album matches that");
		emptyTable.setShowHeaderAlways(true);
		cp.add(emptyTable);

		Div notes = new Div("dm-tut");
		cp.add(notes);
		notes.add("The first table asks its model for one page of rows at a time: paging does "
			+ "not re-run the query, it asks the model for the next slice.");

		cp.add(new Para().add("The three parts are separate on purpose. The model knows what "
			+ "the rows are and nothing about the screen; the row renderer knows what a row "
			+ "looks like and nothing about where the rows came from; the table puts them "
			+ "together and handles paging, selection and clicks."));
		cp.add(new Para().add("The second table has a model that finds nothing. Its message is "
			+ "the empty message, and it keeps its header because it was told to - by default "
			+ "an empty table shows only the message."));
	}
}

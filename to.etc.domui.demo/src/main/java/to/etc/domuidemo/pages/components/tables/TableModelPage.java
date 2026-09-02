package to.etc.domuidemo.pages.components.tables;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.component.tbl.SortableListModel;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Artist_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.ArrayList;
import java.util.List;

/**
 * The models a table can be given: one that queries, and one over a list you
 * hold yourself - and what changing a row through the model does.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TableModelPage extends UrlPage {
	/** How many times the query has run; a field, because the model must be able to increment it. */
	private int m_queryCount;

	@Override
	public void createContent() throws Exception {
		setPageTitle("Table models");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Table models"));

		//-- 1. A model that runs a query, and counts how often it does.
		cp.add(new HTag(2, "SimpleSearchModel: a query"));
		Div counter = new Div("dm-tut-q");

		//-- The query itself counts how often it runs.
		SimpleSearchModel<Artist> queried = new SimpleSearchModel<>(this, (dc, sortOn, maxRows) -> {
			m_queryCount++;
			QCriteria<Artist> q = QCriteria.create(Artist.class);
			q.ascending(sortOn == null ? Artist_.name().getName() : sortOn);
			return dc.query(q.limit(maxRows));
		});
		counter.add("Press the button to see how often the query has run.");

		RowRenderer<Artist> arr = new RowRenderer<>(Artist.class);
		arr.column(Artist_.name()).label("Artist").width(40).ascending().sortdefault();

		DataTable<Artist> queriedTable = new DataTable<>(queried, arr);
		cp.add(queriedTable);
		queriedTable.setPageSize(8);
		cp.add(new DataPager(queriedTable));
		cp.add(counter);
		cp.add(new DefaultButton("Count the queries again", a -> {
			counter.removeAllChildren();
			counter.add("The query has run " + m_queryCount + " time(s) so far.");
		}));

		cp.add(new Para().add("Press the button, page through the table, and press it again: "
			+ "the count stays at one. The model runs its query once and hands out slices of "
			+ "the result; only sorting, or being shelved and returned to, makes it query "
			+ "again."));

		//-- 2. A model over a list of your own.
		cp.add(new HTag(2, "SortableListModel: a list you hold"));
		Div box = new Div("dm-tut-q");
		box.add("Add and remove lines: the table follows without a single forceRebuild().");

		List<BasketLine> lines = new ArrayList<>();
		lines.add(new BasketLine("Rubber Soul", 2));
		lines.add(new BasketLine("Revolver", 1));
		lines.add(new BasketLine("Abbey Road", 3));
		SortableListModel<BasketLine> listModel = new SortableListModel<>(BasketLine.class, lines);

		RowRenderer<BasketLine> lrr = new RowRenderer<>(BasketLine.class);
		lrr.column("album").label("Album").width(30).ascending().sortdefault();
		lrr.column("copies").label("Copies").width(10).ascending();

		DataTable<BasketLine> listTable = new DataTable<>(listModel, lrr);
		cp.add(listTable);

		Div buttons = new Div("dm-tut");
		cp.add(buttons);
		buttons.add(new DefaultButton("Add a line", a -> listModel.add(new BasketLine("Let It Be", 1))));
		buttons.add(new DefaultButton("One more copy of the first", a -> {
			BasketLine line = listModel.getItem(0);
			line.setCopies(line.getCopies() + 1);
			listModel.modified(0);                        // Tell the model: it tells the table
		}));
		buttons.add(new DefaultButton("Delete the first", a -> {
			if(listModel.getRows() > 0)
				listModel.delete(0);
		}));
		cp.add(box);

		cp.add(new Para().add("Every change goes through the model: add(), delete() and "
			+ "modified() are what tell the table which rows changed, and the table updates "
			+ "exactly those. Changing the list behind the model's back leaves the screen "
			+ "showing the old rows."));
		cp.add(new Para().add("This model sorts in memory, so clicking a header sorts the list "
			+ "itself. The query model sorts in the database instead - which is what keeps "
			+ "sorting correct when the result is bigger than one page."));
	}

	/**
	 * A line in a basket - a plain object, not an entity, to show that a table model
	 * does not need a database.
	 */
	public static class BasketLine {
		private String m_album;

		private int m_copies;

		public BasketLine(String album, int copies) {
			m_album = album;
			m_copies = copies;
		}

		public String getAlbum() {
			return m_album;
		}

		public void setAlbum(String album) {
			m_album = album;
		}

		public int getCopies() {
			return m_copies;
		}

		public void setCopies(int copies) {
			m_copies = copies;
		}
	}
}

package to.etc.domuidemo.pages.tutorial.tables;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.derbydata.db.Track_;
import to.etc.domui.dom.css.TextAlign;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.math.BigDecimal;

/**
 * Tutorial, "showing rows", step 2: what a column can be told - a label, a
 * width, an alignment, a converter, how it sorts, a cell click handler, and a
 * renderer that fills the cell itself.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TableColumnsPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Defining the columns");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Defining the columns"));

		Div clicked = new Div("dm-tut");
		cp.add(clicked);
		clicked.add("Click a track name, or any other place in a row.");

		QCriteria<Track> q = QCriteria.create(Track.class);
		SimpleSearchModel<Track> model = new SimpleSearchModel<>(this, q);

		RowRenderer<Track> rr = new RowRenderer<>(Track.class);

		//-- A label of your own, a width in characters, and the column sorted on initially.
		rr.column(Track_.name()).label("Track").width(30).ascending().sortdefault()
			.cellClicked(t -> {
				clicked.removeAllChildren();
				clicked.add("Cell clicked: " + t.getName());
			});

		//-- Nothing is said about the format: the property's metadata has the converter.
		rr.column(Track_.milliseconds()).label("Duration").align(TextAlign.RIGHT);

		//-- A property of a property: the column follows the relation.
		rr.column(Track_.album().title()).label("Album").width(25);

		//-- A renderer gets the column's value and fills the cell itself. It replaces
		//-- the money format the metadata would have given this column.
		rr.column(Track_.unitPrice()).label("Price").align(TextAlign.RIGHT)
			.renderer((node, price) -> {
				Span s = new Span(price.toString());
				node.add(s);
				if(price.compareTo(BigDecimal.ONE) >= 0)
					s.setCssClass("dm-tut-hi");
			});

		//-- A column that is the whole row. It has no property, so say what it sorts on.
		rr.column().label("Where it is from").maxWidth(40).sort(Track_.album().artist().name())
			.renderer((node, track) -> node.add(track.getAlbum().getTitle() + " by " + track.getAlbum().getArtist().getName()));

		rr.setRowClicked(t -> {
			clicked.removeAllChildren();
			clicked.add("Row clicked: " + t.getName());
		});

		DataTable<Track> dt = new DataTable<>(model, rr);
		cp.add(dt);
		dt.setPageSize(10);
		cp.add(new DataPager(dt));
	}
}

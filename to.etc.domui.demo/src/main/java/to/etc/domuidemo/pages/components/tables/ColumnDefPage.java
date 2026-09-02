package to.etc.domuidemo.pages.components.tables;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.converter.MsDurationConverter;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.derbydata.db.Track_;
import to.etc.domui.dom.css.TextAlign;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.math.BigDecimal;

/**
 * ColumnDef: everything a column can be told, and what a cell click does.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ColumnDefPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Columns: what a column can be told");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Columns: what a column can be told"));

		Div shown = new Div("dm-tut-q");
		shown.add("Click a row, or the price of a row.");

		SimpleSearchModel<Track> model = new SimpleSearchModel<>(this,
			QCriteria.create(Track.class).ascending(Track_.name()).limit(60));

		RowRenderer<Track> rr = new RowRenderer<>(Track.class);

		//-- Label, width in characters, and the default sort of the table.
		rr.column(Track_.name()).label("Title").width(30).ascending().sortdefault();

		//-- A converter decides how the value reads; alignment and a css class how it looks.
		rr.column(Track_.milliseconds()).label("Duration").width(10)
			.converter(new MsDurationConverter())
			.align(TextAlign.RIGHT);

		//-- maxWidth truncates with the whole value as the hover text.
		rr.column(Track_.composer()).label("Composer").maxWidth(20).nowrap()
			.hint("Who wrote it");

		//-- A renderer builds the cell content itself.
		rr.column(Track_.unitPrice()).label("Price").width(8).align(TextAlign.RIGHT)
			.renderer((node, value) -> {
				Span span = new Span();
				node.add(span);
				span.add("$ " + value);
				if(value != null && value.compareTo(new BigDecimal("0.99")) > 0)
					span.addCssClass("dm-tut-hi");
			})
			.cellClicked(track -> {
				shown.removeAllChildren();
				shown.add("The price cell of " + track.getName() + " was clicked");
			});

		//-- A column with no property of its own: it renders the row, and sorts on a property.
		rr.column().label("Album / artist").width(40)
			.renderer((node, track) -> node.add(track.getAlbum().getTitle()
				+ " - " + track.getAlbum().getArtist().getName()))
			.sort(Track_.album().artist().name())
			.ascending();

		//-- What a click anywhere else on the row does.
		rr.setRowClicked(track -> MsgBox2.on(this).info("Row clicked: " + track.getName()));

		DataTable<Track> table = new DataTable<>(model, rr);
		cp.add(table);
		table.setPageSize(10);
		cp.add(new DataPager(table));
		cp.add(shown);

		cp.add(new Para().add("The price column has a cell click handler of its own, and it "
			+ "wins over the row handler: clicking a price reports the cell, clicking anything "
			+ "else on the row reports the row."));
		cp.add(new Para().add("The last column has no property at all. It renders whatever it "
			+ "likes from the row, and because a renderer gives the table nothing to sort on, "
			+ "it is told which property to sort by."));
		cp.add(new Para().add("Hover over a truncated composer: maxWidth cuts the text off and "
			+ "puts the whole of it in the cell's title."));
	}
}

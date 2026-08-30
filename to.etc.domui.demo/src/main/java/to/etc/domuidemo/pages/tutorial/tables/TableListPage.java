package to.etc.domuidemo.pages.tutorial.tables;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SortableListModel;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.converter.MoneyBigDecimalNoSign;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Tutorial, "showing rows", step 5: a table over a list that is not a query
 * result. Every change goes through the model, and the table follows it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TableListPage extends UrlPage {
	/** The state of this page: the basket itself. */
	private final List<BasketLine> m_basket = new ArrayList<>(List.of(
		new BasketLine("Kind of Blue", 1, new BigDecimal("12.50"))
		, new BasketLine("Abbey Road", 2, new BigDecimal("14.95"))
		, new BasketLine("The Wall", 1, new BigDecimal("19.95"))
	));

	private int m_added;

	@Override
	public void createContent() throws Exception {
		setPageTitle("A table of your own data");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "A table of your own data"));

		SortableListModel<BasketLine> model = new SortableListModel<>(BasketLine.class, m_basket);

		RowRenderer<BasketLine> rr = new RowRenderer<>(BasketLine.class);
		rr.column(BasketLine_.title()).label("Album").width(30).ascending().sortdefault();
		rr.column(BasketLine_.copies()).label("Copies");
		rr.column(BasketLine_.price()).label("Price each").converter(new MoneyBigDecimalNoSign());

		DataTable<BasketLine> dt = new DataTable<>(model, rr);
		cp.add(dt);

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addButton("Add a line", a -> model.add(new BasketLine("New album " + (++m_added), 1, new BigDecimal("9.95"))));
		bb.addButton("One more copy of the first line", a -> {
			BasketLine line = model.getItem(0);
			line.setCopies(line.getCopies() + 1);
			model.modified(0);                            // Tell the model, or the screen keeps the old number.
		});
		bb.addButton("Delete the first line", a -> {
			if(model.getRows() > 0)
				model.delete(0);
		});

		Div note = new Div("dm-tut");
		cp.add(note);
		note.add("Nothing on this page calls forceRebuild(): the table follows the model.");
	}
}

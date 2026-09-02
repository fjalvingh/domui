package to.etc.domuidemo.pages.components.tables;

import to.etc.domui.component.binding.StyleBinder;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleListModel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.DomUtil;
import to.etc.domuidemo.pages.components.tables.TableModelPage.BasketLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Data binding inside a table: the cells are bound to the row objects, so
 * changing an object changes the screen - and a total in the footer can be
 * bound to something else entirely.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TableBindingPage extends UrlPage {
	private final List<BasketLine> m_lines = new ArrayList<>();

	private SimpleListModel<BasketLine> m_model;

	@Override
	public void createContent() throws Exception {
		setPageTitle("Data binding in a table");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Data binding in a table"));

		if(m_lines.isEmpty()) {
			m_lines.add(new BasketLine("Rubber Soul", 2));
			m_lines.add(new BasketLine("Revolver", 1));
			m_lines.add(new BasketLine("Abbey Road", 12));
		}
		SimpleListModel<BasketLine> model = m_model = new SimpleListModel<>(m_lines);

		//-- A cell showing a value is bound to that property of the row object.
		RowRenderer<BasketLine> rr = new RowRenderer<>(BasketLine.class);
		rr.column("album").label("Album").width(30);
		rr.column("copies").label("Copies").width(10)

			//-- The cell goes red when the row holds more than ten copies.
			.styleBinding(new StyleBinder()
				.define(Boolean.TRUE, "dm-tut-hi")
				.define(Boolean.FALSE, ""))
			.to("bulk");

		//-- A cell built by a renderer has nothing to bind: it must be told to redraw.
		rr.column().label("Line total").width(12)
			.renderer((node, line) -> node.add(line.getCopies() + " x 14.95 = " + (line.getCopies() * 1495 / 100.0)))
			.rerenderOnBind();

		//-- ...where this one is not told, so it keeps whatever it first rendered.
		rr.column().label("Total, unbound").width(12)
			.renderer((node, line) -> node.add(String.valueOf(line.getCopies() * 1495 / 100.0)));

		DataTable<BasketLine> table = new DataTable<>(model, rr);
		cp.add(table);

		//-- The footer is an ordinary TBody: a control in it binds like any other.
		Text2<Integer> total = new Text2<>(Integer.class);
		total.setReadOnly(true);
		total.bind().to(this, "totalCopies");

		TBody footer = table.getFooterBody();
		TR tr = footer.addRow();
		TD label = tr.addCell();
		label.setColspan(3);
		label.add("Copies in total");
		tr.addCell().add(total);

		Div buttons = new Div("dm-tut");
		cp.add(buttons);
		buttons.add(new DefaultButton("One more of the first", a -> {
			BasketLine line = m_lines.get(0);
			line.setCopies(line.getCopies() + 1);
		}));
		buttons.add(new DefaultButton("Eleven more of the first", a -> {
			BasketLine line = m_lines.get(0);
			line.setCopies(line.getCopies() + 11);
		}));
		buttons.add(new DefaultButton("Add a line", a -> {
			BasketLine line = new BasketLine("Let It Be", 1);
			m_lines.add(line);
			DomUtil.nullChecked(m_model).add(line);            // The model, not the list, tells the table
		}));

		cp.add(new Para().add("Press 'one more of the first': the Copies cell and the bound "
			+ "line total follow, without the page being rebuilt and without the table being "
			+ "told anything. Each cell is a control bound to a property of the row object, so "
			+ "changing the object is enough."));
		cp.add(new Para().add("The last column is the same computation without "
			+ "rerenderOnBind(), and it does not follow: a cell built by a renderer has no "
			+ "value of its own to compare, so it is only redrawn when it is asked to be."));
		cp.add(new Para().add("Press it eleven more times' worth: the cell turns yellow at "
			+ "more than ten copies. That is a style binding on the column, bound to a "
			+ "property of the row."));
		cp.add(new Para().add("The total under the table is bound to a property of the *page*, "
			+ "not of a row. The footer of a DataTable is an ordinary TBody, so anything may "
			+ "go in it."));
	}

	/**
	 * The bound total under the table: it is read every request, so it follows whatever
	 * the rows hold.
	 */
	public int getTotalCopies() {
		int total = 0;
		for(BasketLine line : m_lines) {
			total += line.getCopies();
		}
		return total;
	}
}

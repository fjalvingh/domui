package to.etc.domuidemo.pages.components.tables;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.ntbl.ExpandingEditTable;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleListModel;
import to.etc.domui.component2.combo.ComboFixed2;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domuidemo.pages.components.tables.TableModelPage.BasketLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Tables whose cells can be typed in: an editable column, a control factory per
 * row, and the ExpandingEditTable.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TableEditPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Editing in a table");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Editing in a table"));

		Div shown = new Div("dm-tut-q");
		shown.add("Change something and press the button.");

		List<BasketLine> lines = new ArrayList<>();
		lines.add(new BasketLine("Rubber Soul", 2));
		lines.add(new BasketLine("Revolver", 1));
		lines.add(new BasketLine("Abbey Road", 3));

		//-- 1. Editable columns: the table puts a control in every cell.
		cp.add(new HTag(2, "Editable columns"));
		SimpleListModel<BasketLine> model = new SimpleListModel<>(lines);

		RowRenderer<BasketLine> rr = new RowRenderer<>(BasketLine.class);
		rr.column("album").label("Album").width(30).editable();
		rr.column("copies").label("Copies").width(10).editable();

		DataTable<BasketLine> table = new DataTable<>(model, rr);
		cp.add(table);

		cp.add(new DefaultButton("What is in the basket?", a -> {
			StringBuilder sb = new StringBuilder();
			for(BasketLine line : lines) {
				sb.append(line.getAlbum()).append(" x ").append(line.getCopies()).append("\n");
			}
			shown.removeAllChildren();
			shown.add(sb.toString());
		}));
		cp.add(shown);

		cp.add(new Para().add("An editable column makes a control per cell and binds it to the "
			+ "row's property, so what the user types is in the object by the time the button "
			+ "handler runs. Which control it is comes from the metadata of the property."));

		//-- 2. A control factory decides the control per row.
		cp.add(new HTag(2, "A control of your own, per row"));
		List<BasketLine> more = new ArrayList<>();
		more.add(new BasketLine("Rubber Soul", 2));
		more.add(new BasketLine("Revolver", 1));
		SimpleListModel<BasketLine> model2 = new SimpleListModel<>(more);

		RowRenderer<BasketLine> rr2 = new RowRenderer<>(BasketLine.class);
		rr2.column("album").label("Album").width(30);
		rr2.column("copies").label("Copies").width(14)
			.factory(row -> ComboFixed2.createCombo(1, 2, 3, 4, 5));

		DataTable<BasketLine> table2 = new DataTable<>(model2, rr2);
		cp.add(table2);

		cp.add(new Para().add("A factory is asked once per row and may look at that row, so a "
			+ "cell can get a different control - or a differently configured one - depending "
			+ "on what is in the row."));

		//-- 3. The table that edits a row at a time.
		cp.add(new HTag(2, "ExpandingEditTable"));
		List<BasketLine> expanding = new ArrayList<>();
		expanding.add(new BasketLine("Let It Be", 1));
		expanding.add(new BasketLine("Help!", 4));
		SimpleListModel<BasketLine> model3 = new SimpleListModel<>(expanding);

		RowRenderer<BasketLine> rr3 = new RowRenderer<>(BasketLine.class);
		rr3.column("album").label("Album").width(30);
		rr3.column("copies").label("Copies").width(10);

		ExpandingEditTable<BasketLine> eet = new ExpandingEditTable<>(model3, rr3);
		cp.add(eet);
		eet.setEnableDeleteButton(true);
		eet.setEnableExpandItems(true);
		eet.setNewAtStart(true);

		cp.add(new Para().add("The ExpandingEditTable shows the rows read-only and opens one "
			+ "of them at a time for editing, in place. It is for a small list that is edited "
			+ "as a whole - the lines of an order - rather than for a page of search results."));
	}
}

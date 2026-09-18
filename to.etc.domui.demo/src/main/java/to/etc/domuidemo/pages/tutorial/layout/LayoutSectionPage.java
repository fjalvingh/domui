package to.etc.domuidemo.pages.tutorial.layout;

import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

/**
 * Tutorial, "layout", step 6: the collapsible section, three times on one screen, with
 * different things inside it. The page only says what goes in each one.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class LayoutSectionPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("A section that folds shut");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "A section that folds shut"));

		Div log = new Div("dm-tut");

		//-- A form inside a section.
		CollapsibleSection customer = new CollapsibleSection("Customer");
		cp.add(customer);
		Text2<String> name = new Text2<>(String.class);
		Text2<String> city = new Text2<>(String.class);
		FormBuilder fb = new FormBuilder(customer.getContent());
		fb.label("Name").control(name);
		fb.label("City").control(city);

		//-- A table inside a section, closed to start with.
		CollapsibleSection albums = new CollapsibleSection("Albums", false);
		cp.add(albums);
		DataTable<Album> dt = new DataTable<>(new SimpleSearchModel<>(this, QCriteria.create(Album.class)), new RowRenderer<>(Album.class));
		albums.getContent().add(dt);
		dt.setPageSize(5);

		//-- And one that tells the page when it is opened or closed.
		CollapsibleSection notes = new CollapsibleSection("Notes", false);
		cp.add(notes);
		notes.getContent().add("Nothing to say about this order.");
		notes.setOnToggle(section -> {
			log.removeAllChildren();
			log.add("Notes are now " + (section.isExpanded() ? "open" : "closed"));
		});

		cp.add(log);
		log.add("Open and close the Notes section to see this line change.");
	}
}

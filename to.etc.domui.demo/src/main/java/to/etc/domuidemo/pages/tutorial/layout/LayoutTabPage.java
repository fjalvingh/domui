package to.etc.domuidemo.pages.tutorial.layout;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.ITabHandle;
import to.etc.domui.component.layout.TabPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

/**
 * Tutorial, "layout", step 2: a TabPanel puts several pieces of a screen in the same
 * place, and the tab decides which one you see.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class LayoutTabPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Tabs");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Tabs"));

		TabPanel tp = new TabPanel();
		cp.add(tp);

		//-- The simplest tab: content plus a label.
		Div details = new Div();
		details.add("The artist's details would be here.");
		tp.tab()
			.label("Details")
			.content(details)
			.build();

		//-- A tab with an icon, whose content is only built when it is first shown.
		Div albums = new Div();
		ITabHandle albumTab = tp.tab()
			.label("Albums")
			.image(Icon.faMusic)
			.content(albums)
			.lazy()
			.build();
		albums.add(new HTag(2, "All albums"));
		DataTable<Album> dt = new DataTable<>(new SimpleSearchModel<>(this, QCriteria.create(Album.class)), new RowRenderer<>(Album.class));
		albums.add(dt);
		dt.setPageSize(10);
		albums.add(new DataPager(dt));

		//-- A tab the user can throw away.
		Div history = new Div();
		history.add("Nothing was sold yet.");
		tp.tab()
			.label("Sales history")
			.content(history)
			.closable()
			.build();

		//-- A tab can also be selected from the outside.
		cp.add(new DefaultButton("Show the albums", a -> albumTab.select()));
	}
}

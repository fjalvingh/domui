package to.etc.domuidemo.pages.components.tables;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.DefaultSelectAllHandler;
import to.etc.domui.component.tbl.InstanceSelectionModel;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.ArrayList;
import java.util.List;

/**
 * Selecting rows: a selection model turns the checkbox column on, and is what
 * holds - and reports - what is selected.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TableSelectionPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Selecting rows");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Selecting rows"));

		Div shown = new Div("dm-tut-q");
		shown.add("Tick some rows: the selection is reported as it changes.");

		SimpleSearchModel<Album> model = new SimpleSearchModel<>(this,
			QCriteria.create(Album.class).ascending(Album_.title()));

		RowRenderer<Album> rr = new RowRenderer<>(Album.class);
		rr.column(Album_.title()).label("Album").width(40).ascending();
		rr.column(Album_.artist().name()).label("Artist").width(30).ascending().sortdefault();

		DataTable<Album> table = new DataTable<>(model, rr);
		cp.add(table);
		table.setPageSize(10);
		cp.add(new DataPager(table));

		//-- The selection model decides what may be selected, and holds what is.
		//-- Only AC/DC albums may be picked, to show what an acceptor does to the others.
		InstanceSelectionModel<Album> selection = new InstanceSelectionModel<>(true,
			album -> album.getArtist().getName().equals("AC/DC"));
		table.setSelectionModel(selection);
		table.setSelectionAllHandler(new DefaultSelectAllHandler());
		table.setShowSelection(true);

		selection.addListener(new to.etc.domui.component.tbl.ISelectionListener<Album>() {
			@Override
			public void selectionChanged(Album row, boolean on) {
				report(shown, selection);
			}

			@Override
			public void selectionAllChanged() {
				report(shown, selection);
			}
		});

		cp.add(new DefaultButton("What is selected?", a -> report(shown, selection)));
		cp.add(new DefaultButton("Clear the selection", a -> {
			selection.clearSelection();
			report(shown, selection);
		}));
		cp.add(shown);

		cp.add(new Para().add("The checkbox column appears because the table has a selection "
			+ "model, not because a column was defined for it. The model is also what says "
			+ "whether more than one row may be selected."));
		cp.add(new Para().add("This model only accepts AC/DC albums - the first two rows. The "
			+ "other rows keep their checkbox, but it arrives dead - a checkbox has no "
			+ "read-only state, so DomUI disables it. That is an IAcceptable handed to the "
			+ "selection model; the table "
			+ "itself knows nothing about the rule."));
		cp.add(new Para().add("The tick in the header selects everything the *model* holds, not "
			+ "just the page on screen, because the table was given a select-all handler - and "
			+ "it still respects the acceptor, so it selects two albums out of 347."));
	}

	private static void report(Div into, InstanceSelectionModel<Album> selection) {
		List<String> titles = new ArrayList<>();
		for(Album album : selection) {
			titles.add(album.getTitle());
		}
		into.removeAllChildren();
		into.add(selection.getSelectionCount() + " selected"
			+ (titles.isEmpty() ? "" : ": " + String.join(", ", titles)));
	}
}

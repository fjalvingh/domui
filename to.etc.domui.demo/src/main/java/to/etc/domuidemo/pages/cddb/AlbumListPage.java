package to.etc.domuidemo.pages.cddb;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.UIGoto;
import to.etc.webapp.query.QCriteria;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-10-18.
 */
@NonNullByDefault
final public class AlbumListPage extends UrlPage {
	@Nullable
	private DataTable<Album> m_table;

	@Override @Nullable public String getPageTitle() {
		return "Album List";
	}

	@Override public void createContent() throws Exception {
		add(new HTag(1, "Find an album"));

		ContentPanel cp = new ContentPanel();
		add(cp);

		SearchPanel<Album> lookup = new SearchPanel<>(Album.class);
		cp.add(lookup);

		lookup.add().property(Album_.artist()).control();
		lookup.add().property(Album_.title()).control();
		lookup.setClicked(a -> search(lookup.getCriteria()));
	}

	private void search(@Nullable QCriteria<Album> criteria) {
		if(criteria == null)
			return;
		DataTable<Album> table = m_table;
		SimpleSearchModel<Album> model = new SimpleSearchModel<Album>(this, criteria);
		if(null == table) {
			RowRenderer<Album> rr = getRowRenderer();
			rr.setRowClicked(a -> {
				UIGoto.moveSub(AlbumEditPage.class, "id", a.getId());
			});

			table = m_table = new DataTable<>(model, rr);

			add(new DataPager(table));
			add(table);
			add(new DataPager(table));
		} else {
			table.setModel(model);
		}
	}

	@NotNull private RowRenderer<Album> getRowRenderer() {
		RowRenderer<Album> rr = new RowRenderer<>(Album.class);
		rr.column(Album_.title()).width(20).ascending();
		rr.column(Album_.artist().name()).width(20).sortdefault().ascending();
		return rr;
	}
}

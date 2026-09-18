package to.etc.domuidemo.pages.cddb;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.state.UIGoto;

/**
 * Find an album in the catalogue; clicking one opens it with its tracks.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-10-18.
 */
final public class AlbumListPage extends AbstractCdShopListPage<Album> {
	public AlbumListPage() {
		super(Album.class, "Albums");
	}

	@Override
	protected void configureSearch(@NonNull SearchPanel<Album> sp) throws Exception {
		sp.add().property(Album_.artist()).control();
		sp.add().property(Album_.title()).control();
	}

	@Override
	protected void configureColumns(@NonNull RowRenderer<Album> rr) throws Exception {
		rr.column(Album_.title()).width(20).ascending();
		rr.column(Album_.artist().name()).width(20).sortdefault().ascending();
	}

	@Override
	protected void onRowSelected(@NonNull Album instance) throws Exception {
		UIGoto.moveSub(AlbumEditPage.class, "id", instance.getId());
	}
}

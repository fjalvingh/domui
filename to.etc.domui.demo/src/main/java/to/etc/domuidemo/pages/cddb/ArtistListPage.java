package to.etc.domuidemo.pages.cddb;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Artist_;
import to.etc.domui.state.UIGoto;

/**
 * Find an artist in the catalogue; clicking one opens the artist with its albums.
 */
public class ArtistListPage extends AbstractCdShopListPage<Artist> {
	public ArtistListPage() {
		super(Artist.class, "Artists");
	}

	@Override
	protected void configureColumns(@NonNull RowRenderer<Artist> rr) throws Exception {
		rr.column(Artist_.name()).ascending().sortdefault();
	}

	@Override
	protected void onRowSelected(@NonNull Artist instance) throws Exception {
		UIGoto.moveSub(ArtistDetailPage.class, "id", instance.getId());
	}
}

package to.etc.domuidemo.pages.cddb;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.state.UIGoto;

/**
 * Find a track in the catalogue; clicking one opens what is known about it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-7-17.
 */
public class CdCollection extends AbstractCdShopListPage<Track> {
	public CdCollection() {
		super(Track.class, "Tracks for sale");
	}

	@Override
	protected void onRowSelected(@NonNull Track instance) throws Exception {
		UIGoto.moveSub(TrackDetails.class, "id", instance.getId());
	}
}

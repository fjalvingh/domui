package to.etc.domuidemo.pages.cddb;

import to.etc.domui.component.layout.Caption;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.lookup.LookupForm;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.UIGoto;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-7-17.
 */
public class CdCollection extends UrlPage {
	@Override public void createContent() throws Exception {
		add(new Caption("Find your favorite track"));

		ContentPanel cp = new ContentPanel();
		add(cp);

		LookupForm<Track> lookup = new LookupForm<>(Track.class);
		cp.add(lookup);

		ResultFragment<Track> resultFragment = new ResultFragment<>(lookup);
		cp.add(resultFragment);

		resultFragment.setOnClick(row -> UIGoto.moveSub(TrackDetails.class, row));
	}
}

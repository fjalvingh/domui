package to.etc.domuidemo.pages.tutorial.layout;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Artist_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.List;

/**
 * Tutorial, "layout", step 4: the same fragment three times, each with its own data and
 * its own state. The page says what it wants, not how it is drawn.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class LayoutFragmentPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("A fragment of your own");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "A fragment of your own"));

		//-- The first three artists that actually have something in the shop.
		QCriteria<Artist> q = QCriteria.create(Artist.class)
			.ascending(Artist_.name())
			.limit(3);
		q.exists(Album.class, Artist_.albumList().getName());
		List<Artist> artistList = getSharedContext().query(q);

		Div row = new Div("dm-cardrow");
		cp.add(row);
		for(Artist artist : artistList) {
			row.add(new ArtistCardFragment(artist));       // Three instances, three states
		}
	}
}

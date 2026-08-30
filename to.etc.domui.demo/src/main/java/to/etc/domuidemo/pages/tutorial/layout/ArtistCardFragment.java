package to.etc.domuidemo.pages.tutorial.layout;

import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Li;
import to.etc.domui.dom.html.Ul;

/**
 * Tutorial, "layout", step 3: a fragment. It is an ordinary Div that fills itself in
 * createContent(), which makes it a piece of screen with a name - one that several
 * pages can use, and that redraws itself without the page around it knowing.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ArtistCardFragment extends Div {
	/** What this fragment shows: passed in when it is made. */
	private final Artist m_artist;

	/** What it remembers: its own state, and the only reason it rebuilds. */
	private boolean m_showAlbums;

	public ArtistCardFragment(Artist artist) {
		m_artist = artist;
		setCssClass("dm-card");
	}

	@Override
	public void createContent() throws Exception {
		add(new HTag(2, m_artist.getName()));

		Div count = new Div();
		add(count);
		int albums = m_artist.getAlbumList().size();
		count.add(albums == 1 ? "1 album in the shop" : albums + " albums in the shop");

		add(new LinkButton(m_showAlbums ? "hide the albums" : "show the albums",
			m_showAlbums ? Icon.faAngleDown : Icon.faAngleRight,
			a -> {
				m_showAlbums = !m_showAlbums;              // Change the state...
				forceRebuild();                            // ...and build this fragment again
			}));

		if(m_showAlbums) {
			Ul ul = new Ul();
			add(ul);
			for(Album album : m_artist.getAlbumList()) {
				ul.add(new Li().add(album.getTitle()));
			}
		}
	}
}

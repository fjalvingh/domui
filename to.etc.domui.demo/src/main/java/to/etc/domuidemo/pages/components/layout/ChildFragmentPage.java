package to.etc.domuidemo.pages.components.layout;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.masterchild.ChildFragment;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Artist_;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.List;

/**
 * ChildFragment: the children of a record, in a table that follows the record.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ChildFragmentPage extends UrlPage {
	/** The artist being looked at; the fragment below follows this. */
	private Artist m_artist;

	@Override
	public void createContent() throws Exception {
		setPageTitle("ChildFragment");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "ChildFragment: the children of a record"));

		//-- Only artists that actually have albums, so every choice shows something.
		QCriteria<Artist> q = QCriteria.create(Artist.class);
		q.exists(Album.class, Artist_.albumList());
		q.ascending(Artist_.name()).limit(25);
		List<Artist> artists = getSharedContext().query(q);
		if(null == m_artist) {
			m_artist = artists.get(0);
		}

		//-- Pick the master record.
		ComboLookup2<Artist> artist = new ComboLookup2<>(artists);
		artist.setValue(m_artist);
		artist.setMandatory(true);
		artist.setOnValueChanged(a -> {
			m_artist = artist.getValue();
			forceRebuild();                                // The fragment is built from the artist
		});

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Artist").control(artist);

		//-- ...and the fragment showing its children.
		cp.add(new HTag(2, "Albums"));
		ChildFragment<Artist, Album> albums = new ChildFragment<>(m_artist, Artist_.albumList());
		cp.add(albums);
		albums.column(Album_.title()).label("Album").width(50);
		albums.onClick(album -> MsgBox2.on(this).info("You picked " + album.getTitle()));

		cp.add(new Para().add("The fragment was given the artist and the property holding its "
			+ "albums, and that is all: it works out the child type, binds itself to the "
			+ "relation, and shows the children in a table."));
		cp.add(new Para().add("Because a Hibernate relation list is observable, adding an "
			+ "album to the artist's list would add a row here without the fragment being "
			+ "told anything."));
	}
}

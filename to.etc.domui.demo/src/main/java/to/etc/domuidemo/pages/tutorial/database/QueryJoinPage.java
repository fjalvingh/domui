package to.etc.domuidemo.pages.tutorial.database;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QRestrictor.ExistsRestrictor;

import java.util.List;

/**
 * Tutorial, "using databases", step 3: querying over a relation - a parent
 * property with a dotted path, and a child collection with exists().
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class QueryJoinPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Querying over a relation");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Querying over a relation"));

		//-- Part 1: albums, restricted on a property of their parent artist.
		cp.add(new HTag(2, "Albums of an artist"));
		Text2<String> artistPart = new Text2<>(String.class);
		artistPart.setValue("Black");
		Div artistQuery = new Div("dm-tut-q");
		Div artistResult = new Div("dm-tut");

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Artist name contains").control(artistPart);
		cp.add(new DefaultButton("Search albums", a -> searchAlbums(artistPart, artistQuery, artistResult)));
		cp.add(artistQuery);
		cp.add(artistResult);

		//-- Part 2: artists, restricted on their child albums.
		cp.add(new HTag(2, "Artists having an album"));
		Text2<String> titlePart = new Text2<>(String.class);
		titlePart.setValue("Live");
		Div titleQuery = new Div("dm-tut-q");
		Div titleResult = new Div("dm-tut");

		FormBuilder fb2 = new FormBuilder(cp);
		fb2.label("Album title contains").control(titlePart);
		cp.add(new DefaultButton("Search artists", a -> searchArtists(titlePart, titleQuery, titleResult)));
		cp.add(titleQuery);
		cp.add(titleResult);

		searchAlbums(artistPart, artistQuery, artistResult);
		searchArtists(titlePart, titleQuery, titleResult);
	}

	private void searchAlbums(Text2<String> artistPart, Div query, Div result) throws Exception {
		QCriteria<Album> q = QCriteria.create(Album.class);
		String part = artistPart.getValueSafe();
		if(part != null) {
			//-- A dotted path walks to the parent record: this joins Artist in.
			q.ilike("artist.name", "%" + part + "%");
		}
		q.ascending("artist.name").ascending("title");
		q.limit(20);

		query.removeAllChildren();
		query.add(q.toString());

		List<Album> albumList = getSharedContext().query(q);
		result.removeAllChildren();
		result.add(new HTag(3, albumList.size() == 1 ? "1 album" : albumList.size() + " albums"));
		for(Album album : albumList) {
			Div line = new Div();
			result.add(line);
			line.add(album.getArtist().getName() + " - " + album.getTitle());
		}
	}

	private void searchArtists(Text2<String> titlePart, Div query, Div result) throws Exception {
		QCriteria<Artist> q = QCriteria.create(Artist.class);
		String part = titlePart.getValueSafe();
		if(part != null) {
			//-- "exists": every artist that has at least one such album, once.
			ExistsRestrictor<Album> albums = q.exists(Album.class, "albumList");
			albums.ilike("title", "%" + part + "%");
		}
		q.ascending("name");
		q.limit(20);

		query.removeAllChildren();
		query.add(q.toString());

		List<Artist> artistList = getSharedContext().query(q);
		result.removeAllChildren();
		result.add(new HTag(3, artistList.size() == 1 ? "1 artist" : artistList.size() + " artists"));
		for(Artist artist : artistList) {
			Div line = new Div();
			result.add(line);
			line.add(artist.getName());
		}
	}
}

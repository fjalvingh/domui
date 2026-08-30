package to.etc.domuidemo.pages.tutorial.typed;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Artist_;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.derbydata.db.Track_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QRestrictor.ExistsRestrictor;

import java.util.List;

/**
 * Tutorial, "typed properties", step 2: a property path of any depth, and
 * descending into a child collection with a typed property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TypedPathPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Typed property paths");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Typed property paths"));

		//-- Part 1: a path two parents deep.
		cp.add(new HTag(2, "Tracks of an artist"));
		Text2<String> artistPart = new Text2<>(String.class);
		artistPart.setValue("Miles Davis");
		Div trackQuery = new Div("dm-tut-q");
		Div trackResult = new Div("dm-tut");

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Artist name contains").control(artistPart);
		cp.add(new DefaultButton("Search tracks", a -> searchTracks(artistPart, trackQuery, trackResult)));
		cp.add(trackQuery);
		cp.add(trackResult);

		//-- Part 2: descending into a child collection.
		cp.add(new HTag(2, "Artists having an album"));
		Text2<String> titlePart = new Text2<>(String.class);
		titlePart.setValue("Live");
		Div artistQuery = new Div("dm-tut-q");
		Div artistResult = new Div("dm-tut");

		FormBuilder fb2 = new FormBuilder(cp);
		fb2.label("Album title contains").control(titlePart);
		cp.add(new DefaultButton("Search artists", a -> searchArtists(titlePart, artistQuery, artistResult)));
		cp.add(artistQuery);
		cp.add(artistResult);

		searchTracks(artistPart, trackQuery, trackResult);
		searchArtists(titlePart, artistQuery, artistResult);
	}

	private void searchTracks(Text2<String> artistPart, Div query, Div result) throws Exception {
		QCriteria<Track> q = QCriteria.create(Track.class);
		String part = artistPart.getValueSafe();
		if(part != null) {
			//-- Track -> album -> artist -> name, every step checked by the compiler.
			q.ilike(Track_.album().artist().name(), "%" + part + "%");
		}
		q.ascending(Track_.album().title()).ascending(Track_.name());
		q.limit(20);

		query.removeAllChildren();
		query.add(q.toString());

		List<Track> trackList = getSharedContext().query(q);
		result.removeAllChildren();
		result.add(new HTag(3, trackList.size() == 1 ? "1 track" : trackList.size() + " tracks"));
		for(Track track : trackList) {
			Div line = new Div();
			result.add(line);
			line.add(track.getAlbum().getTitle() + " - " + track.getName());
		}
	}

	private void searchArtists(Text2<String> titlePart, Div query, Div result) throws Exception {
		QCriteria<Artist> q = QCriteria.create(Artist.class);
		String part = titlePart.getValueSafe();
		if(part != null) {
			ExistsRestrictor<Album> albums = q.exists(Album.class, Artist_.albumList());
			albums.ilike(Album_.title(), "%" + part + "%");
		}
		q.ascending(Artist_.name());
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

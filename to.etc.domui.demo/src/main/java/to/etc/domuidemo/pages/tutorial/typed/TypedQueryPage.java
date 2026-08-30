package to.etc.domuidemo.pages.tutorial.typed;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.List;

/**
 * Tutorial, "typed properties", step 1: the same query as in "using databases",
 * with every property name replaced by a generated typed property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TypedQueryPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("The same query, typed");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "The same query, typed"));

		Text2<String> titlePart = new Text2<>(String.class);
		titlePart.setValue("rock");
		Text2<String> artistPart = new Text2<>(String.class);

		Div query = new Div("dm-tut-q");
		Div result = new Div("dm-tut");

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Album title contains").control(titlePart);
		fb.label("Artist name contains").control(artistPart);

		cp.add(new DefaultButton("Search", a -> search(titlePart, artistPart, query, result)));
		cp.add(query);
		cp.add(result);
		search(titlePart, artistPart, query, result);
	}

	private void search(Text2<String> titlePart, Text2<String> artistPart, Div query, Div result) throws Exception {
		QCriteria<Album> q = QCriteria.create(Album.class);

		String title = titlePart.getValueSafe();
		if(title != null) {
			q.ilike(Album_.title(), "%" + title + "%");
		}
		String artist = artistPart.getValueSafe();
		if(artist != null) {
			//-- artist() walks to the parent, name() is its property: both are checked.
			q.ilike(Album_.artist().name(), "%" + artist + "%");
		}
		q.ascending(Album_.title());
		q.limit(20);

		query.removeAllChildren();
		query.add(q.toString());

		List<Album> albumList = getSharedContext().query(q);
		result.removeAllChildren();
		result.add(new HTag(2, albumList.size() == 1 ? "1 album" : albumList.size() + " albums"));
		for(Album album : albumList) {
			Div line = new Div();
			result.add(line);
			line.add(album.getTitle() + " - " + album.getArtist().getName());
		}
	}
}

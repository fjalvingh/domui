package to.etc.domuidemo.pages.tutorial.database;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.List;

/**
 * Tutorial, "using databases", step 1: a QCriteria query, run on the page's
 * shared QDataContext.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class QueryFirstPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Your first query");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Your first query"));

		Text2<String> titlePart = new Text2<>(String.class);
		titlePart.setValue("rock");
		Div result = new Div("dm-tut");

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Album title contains").control(titlePart);

		cp.add(new DefaultButton("Search", a -> search(titlePart, result)));
		cp.add(result);
		search(titlePart, result);
	}

	private void search(Text2<String> titlePart, Div result) throws Exception {
		QCriteria<Album> q = QCriteria.create(Album.class);
		String part = titlePart.getValueSafe();
		if(part != null) {
			q.ilike("title", "%" + part + "%");
		}
		q.ascending("title");
		q.limit(20);

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

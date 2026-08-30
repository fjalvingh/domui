package to.etc.domuidemo.pages.tutorial.typed;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
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
import to.etc.webapp.query.QField;

import java.util.List;

/**
 * Tutorial, "typed properties", step 3: a typed property is a value carrying
 * both the class it belongs to and the type of the property, so a helper
 * method can take one and stay typed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TypedGenericPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("A property as a value");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "A property as a value"));

		QCriteria<Artist> aq = QCriteria.create(Artist.class);
		aq.ascending(Artist_.name()).limit(5);
		cp.add(new HTag(2, "Artists, by name"));
		cp.add(listOf(getSharedContext().query(aq), Artist_.name()));

		QCriteria<Album> bq = QCriteria.create(Album.class);
		bq.ascending(Album_.title()).limit(5);
		cp.add(new HTag(2, "Albums, by title"));
		cp.add(listOf(getSharedContext().query(bq), Album_.title()));

		QCriteria<Track> tq = QCriteria.create(Track.class);
		tq.ascending(Track_.name()).limit(5);
		cp.add(new HTag(2, "Tracks, by the name of the artist that made them"));
		cp.add(listOf(getSharedContext().query(tq), Track_.album().artist().name()));
	}

	/**
	 * Render a list of anything, labelled by whatever String property of it you pass in.
	 * The QField says both what T is and that the property is a String, so nothing
	 * here needs a cast or a class parameter.
	 */
	private <T> Div listOf(List<T> list, QField<T, String> labelProperty) throws Exception {
		PropertyMetaModel<String> pmm = MetaManager.getPropertyMeta(labelProperty.getRootClass(), labelProperty);

		Div box = new Div("dm-tut");
		for(T item : list) {
			Div line = new Div();
			box.add(line);
			line.add(pmm.getValue(item));
		}
		return box;
	}
}

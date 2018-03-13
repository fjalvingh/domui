package to.etc.domuidemo.pages.searchpanel;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component2.enumsetinput.EnumSetInput;
import to.etc.domui.component2.enumsetinput.EnumSetQueryBuilder;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.derbydata.db.Track;
import to.etc.webapp.query.QCriteria;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Example on how to use/create a custom control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-1-18.
 */
public class SearchPanelCustomControl4 extends AbstractSearchPage<Track> {
	public SearchPanelCustomControl4() {
		super(Track.class);
	}

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		SearchPanel<Track> lf = new SearchPanel<>(Track.class);
		cp.add(lf);
		lf.setClicked(a -> search(lf.getCriteria()));

		//-- For Genre we will use a new control
		EnumSetInput<Genre> genreC = new EnumSetInput<>(Genre.class, "name");
		List<Genre> genreList = getSharedContext().query(QCriteria.create(Genre.class));
		genreC.setData(genreList);

		Set<Genre> def = new HashSet<>();
		def.add(genreList.get(0));
		def.add(genreList.get(1));

		lf.add().property("genre").defaultValue(def).control(genreC, new EnumSetQueryBuilder<>("genre"));

		lf.add().property("name").control();
		lf.add().property("album").control();				// Allow searching for a total
	}
}

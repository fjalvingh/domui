package to.etc.domuidemo.pages.test.componenterrors;

import to.etc.domui.component.layout.Caption;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.searchpanel.lookupcontrols.LookupQueryBuilderResult;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.derbydata.db.Track_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Pre;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.List;

/**
 * Test github issue #6: clearing a mandatory property must throw an exception.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 27-9-17.
 */
public class LookupForm1TestPage extends UrlPage {
	private Div m_res = new Div();

	@Override
	public void createContent() throws Exception {
		//-- Segment modeled to reproduce github issue #6
		SearchPanel<Track> lf1 = new SearchPanel<>(Track.class);
		add(lf1);

		List<Album> list = getSharedContext().query(QCriteria.create(Album.class)); //.like("name", "a%"));
		ComboLookup2<Album> trackL1 = new ComboLookup2<>(list);
		trackL1.setValue(list.get(0));
		//trackL1.setMandatory(true);

		List<Genre> glist = getSharedContext().query(QCriteria.create(Genre.class));
		ComboLookup2<Genre> genreL1 = new ComboLookup2<>(glist);
		genreL1.setValue(glist.get(0));
		//genreL1.setMandatory(true);

		lf1.add().property(Track_.album()).control(trackL1, (criteria, lookupValue) -> {
			Album track = trackL1.getValue();
			if(null == track) {
				return LookupQueryBuilderResult.EMPTY;
			}
			criteria.eq(Track_.album(), track);
			return LookupQueryBuilderResult.VALID;

		});

		lf1.add().property(Track_.genre()).control(genreL1, (criteria, lookupValue) -> {
			Genre genre = genreL1.getValue();
			if(null == genre) {
				return LookupQueryBuilderResult.EMPTY;
			}
			criteria.eq(Track_.genre(), genre);
			return LookupQueryBuilderResult.VALID;
		});

		lf1.setClicked(f -> renderCriteria(lf1.getCriteria()));

		add(new VerticalSpacer(10));
		add(new Caption("Criteria"));
		add(m_res);

	}

	private void renderCriteria(QCriteria<Track> enteredCriteria) {
		m_res.removeAllChildren();

		Pre pre = new Pre();
		m_res.add(pre);
		pre.add(String.valueOf(enteredCriteria));
	}
}

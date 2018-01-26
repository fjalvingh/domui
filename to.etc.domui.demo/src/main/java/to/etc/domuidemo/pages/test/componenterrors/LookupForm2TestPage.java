package to.etc.domuidemo.pages.test.componenterrors;

import to.etc.domui.component.layout.Caption;
import to.etc.domui.component.lookup.AbstractLookupControlImpl;
import to.etc.domui.component.lookup.ILookupControlInstance;
import to.etc.domui.component.lookup.LookupForm;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Pre;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Test github issue #6: this should throw an error when clear is pressed because
 * {@link ILookupControlInstance#clearInput()} is missing on a mandatory control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 27-9-17.
 */
public class LookupForm2TestPage extends UrlPage {
	private Div m_res = new Div();


	@Override public void createContent() throws Exception {
		//-- Segment modeled to reproduce github issue #6
		LookupForm<Track> lf1		= new LookupForm<>(Track.class);
		add(lf1);

		List<Album> list = getSharedContext().query(QCriteria.create(Album.class)); //.like("name", "a%"));
		ComboLookup2<Album> trackL1 = new ComboLookup2<>(list);
		trackL1.setValue(list.get(0));
		trackL1.setMandatory(true);

		List<Genre> glist = getSharedContext().query(QCriteria.create(Genre.class));
		ComboLookup2<Genre> genreL1 = new ComboLookup2<>(glist);
		genreL1.setValue(glist.get(0));
		genreL1.setMandatory(true);

		lf1.addManualPropertyLabel("album", new AbstractLookupControlImpl(trackL1) {
			@Nonnull
			@Override
			public AppendCriteriaResult appendCriteria(@Nonnull QCriteria<?> crit) throws Exception {
				Album track = trackL1.getValue();
				if(null == track) {
					return AppendCriteriaResult.EMPTY;
				}
				crit.eq("album", track);
				return AppendCriteriaResult.VALID;
			}
		});

		lf1.addManualPropertyLabel("genre", new AbstractLookupControlImpl(genreL1) {
			@Nonnull
			@Override
			public AppendCriteriaResult appendCriteria(@Nonnull QCriteria<?> crit) throws Exception {
				Genre genre = genreL1.getValue();
				if(null == genre) {
					return AppendCriteriaResult.EMPTY;
				}
				crit.eq("genre", genre);
				return AppendCriteriaResult.VALID;
			}
		});

		lf1.setClicked(f -> {
			renderCriteria(lf1.getEnteredCriteria());
		});

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

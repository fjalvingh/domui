package to.etc.domuidemo.pages.components.lookup;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.enumsetinput.EnumSetInput;
import to.etc.domui.component2.enumsetinput.EnumSetQueryBuilder;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.derbydata.db.Track_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.webapp.query.QCriteria;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SearchPanel: putting a control of your own on a search line, with and
 * without a query builder to go with it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class SearchPanelControlPage extends AbstractSearchPage<Track> {
	public SearchPanelControlPage() {
		super(Track.class);
	}

	@Override
	public void createContent() throws Exception {
		setPageTitle("SearchPanel: controls of your own");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "SearchPanel: controls of your own"));

		SearchPanel<Track> sp = new SearchPanel<>(Track.class);
		cp.add(sp);

		List<Genre> genres = getSharedContext().query(QCriteria.create(Genre.class));

		//-- A control whose value is a Genre: the panel knows how to search on that itself.
		ComboLookup2<Genre> oneGenre = new ComboLookup2<>(genres);
		sp.add().property(Track_.genre()).label("Genre (one)").control(oneGenre);

		//-- A control whose value is a Set<Genre>: nothing can guess that, so it
		//-- comes with the query builder that knows what to do with it.
		EnumSetInput<Genre> someGenres = new EnumSetInput<>(Genre.class, genres, "name");
		Set<Genre> initial = new HashSet<>();
		initial.add(genres.get(0));
		initial.add(genres.get(1));
		sp.add().property(Track_.genre())
			.label("Genre (any of)")
			.defaultValue(initial)
			.control(someGenres, new EnumSetQueryBuilder<>("genre"));

		sp.add().property(Track_.name()).control();
		sp.add().property(Track_.album()).control();

		cp.add(new Para().add("The first control hands the panel a Genre, and a value that is "
			+ "just a value needs no explanation: the panel compares the property with it. "
			+ "That is the default query builder, and it is why handing in a control is "
			+ "usually all there is to it."));
		cp.add(new Para().add("The second hands over a Set<Genre>, which no default could "
			+ "make sense of - so the control arrives together with an EnumSetQueryBuilder, "
			+ "which turns the set into an 'or' over the genre property. Take all the genres "
			+ "off and that line simply adds nothing to the query."));
		cp.add(new Para().add("Both lines search the same property. The panel does not mind: "
			+ "a search line is a control plus a way to turn its value into a restriction, "
			+ "not a property."));

		//-- The result of a search appears here.
		Div results = new Div();
		cp.add(results);
		sp.setClicked(a -> showResult(results, sp.getCriteria()));
	}
}

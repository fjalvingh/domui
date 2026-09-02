package to.etc.domuidemo.pages.components.lookup;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.SearchAsYouType;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * SearchAsYouType: a box where the value is typed rather than picked, over a
 * list held in memory.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class SearchAsYouTypePage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("SearchAsYouType: typing the value");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "SearchAsYouType: typing the value"));

		Div shown = new Div("dm-tut");
		shown.add("Type 'ro' in the first box: two genres match, and the marker stays red "
			+ "until one of them is the whole value.");

		List<Genre> genres = getSharedContext().query(QCriteria.create(Genre.class));

		//-- A record, labelled and searched by one of its properties.
		SearchAsYouType<Genre> genre = new SearchAsYouType<>(Genre.class, "name");
		genre.setData(genres);
		genre.setMandatory(true);
		genre.setOnValueChanged(a -> {
			shown.removeAllChildren();
			Genre value = genre.getValue();
			shown.add("Genre is now " + (value == null ? "empty" : value.getName()));
		});

		//-- The same list, matched only at the start of the name.
		SearchAsYouType<Genre> starts = new SearchAsYouType<>(Genre.class, "name");
		starts.setData(genres);
		starts.setMode(SearchAsYouType.MatchMode.STARTS_CI);

		//-- A value with no property to search on: hand it a converter.
		List<Date> months = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		for(int i = 0; i < 24; i++) {
			cal.add(Calendar.MONTH, -1);
			months.add(cal.getTime());
		}
		SearchAsYouType<Date> month = new SearchAsYouType<>(Date.class);
		month.setData(months);
		month.setConverter((loc, value) -> new SimpleDateFormat("MM-yyyy").format(value));

		//-- Rendering the drop-down differently from the text that is typed.
		SearchAsYouType<Genre> rendered = new SearchAsYouType<>(Genre.class, "name");
		rendered.setData(genres);
		rendered.setRenderer((node, value) -> node.add(value.getName() + " (#" + value.getId() + ")"));

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Genre (mandatory)").control(genre);
		fb.label("Matching only the start").control(starts);
		fb.label("A month, through a converter").control(month);
		fb.label("Own drop-down renderer").control(rendered);

		cp.add(new DefaultButton("Read the values", a -> {
			Genre value = genre.getValue();
			Genre startsValue = starts.getValue();
			Date monthValue = month.getValue();
			shown.removeAllChildren();
			shown.add("genre=" + (value == null ? null : value.getName())
				+ ", starts=" + (startsValue == null ? null : startsValue.getName())
				+ ", month=" + (monthValue == null ? null : new SimpleDateFormat("MM-yyyy").format(monthValue)));
		}));
		cp.add(shown);

		cp.add(new Para().add("The marker at the end of the box is the control's state: red "
			+ "while what is typed is not (yet) a value, green when it is. That is the whole "
			+ "point of the control - the user can see whether what they typed counts."));
		cp.add(new Para().add("Type 'rock' in the first box: it goes green at once, but the "
			+ "list stays open because 'Rock And Roll' also matches. Press enter to settle "
			+ "on Rock."));
	}
}

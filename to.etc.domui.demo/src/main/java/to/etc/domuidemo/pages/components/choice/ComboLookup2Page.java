package to.etc.domuidemo.pages.components.choice;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Artist_;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.derbydata.db.MediaType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.List;

/**
 * ComboLookup2: a drop-down over records, with the list coming from the database.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ComboLookup2Page extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("ComboLookup2: choosing a record");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "ComboLookup2: choosing a record"));

		Div shown = new Div("dm-tut");
		shown.add("Pick something and press the button.");

		//-- The query runs when the combo is built; the rendering comes from metadata.
		QCriteria<Artist> aq = QCriteria.create(Artist.class).ascending(Artist_.name()).limit(20);
		ComboLookup2<Artist> byQuery = new ComboLookup2<>(aq);

		//-- A list you already have, rendered by naming the properties to show.
		List<Artist> artists = getSharedContext().query(aq);
		ComboLookup2<Artist> byProperty = new ComboLookup2<>(artists, Artist_.name());

		//-- Genre and MediaType carry @MetaCombo, so they need nothing at all.
		ComboLookup2<Genre> genre = new ComboLookup2<>(
			getSharedContext().query(QCriteria.create(Genre.class)));
		ComboLookup2<MediaType> mediaType = new ComboLookup2<>(
			getSharedContext().query(QCriteria.create(MediaType.class)));

		//-- A renderer of your own decides what each option looks like.
		ComboLookup2<Artist> rendered = new ComboLookup2<>(artists);
		rendered.setRenderer((node, artist) -> {
			node.add(new Span("dm-tut-hi", artist.getName()));
			node.add(" (#" + artist.getId() + ")");
		});
		rendered.addExtraButton(Icon.faInfoCircle, "What did I pick?", a -> {
			Artist artist = rendered.getValue();
			MsgBox2.on(this).info(artist == null ? "Nothing picked yet" : artist.getName());
		});

		FormBuilder fb = new FormBuilder(cp);
		fb.label("From a QCriteria").control(byQuery);
		fb.label("From a list, by property").control(byProperty);
		fb.label("Genre (from @MetaCombo)").control(genre);
		fb.label("Media type (from @MetaCombo)").control(mediaType);
		fb.label("With a renderer and a button").control(rendered);

		cp.add(new DefaultButton("Read the values", a -> {
			Artist artist = byQuery.getValue();
			shown.removeAllChildren();
			shown.add("artist=" + (artist == null ? null : artist.getName())
				+ ", genre=" + genre.getValue()
				+ ", media type=" + mediaType.getValue());
		}));
		cp.add(shown);

		cp.add(new Para().add("The value of this combo is the record itself - an Artist, a "
			+ "Genre - not a code. Two records with the same primary key count as the same "
			+ "value, which is what makes a combo find the value it was given back in its "
			+ "own list."));
		cp.add(new Para().add("The query is only run when the combo is built, so a list of "
			+ "twenty is fine and a list of twenty thousand is a LookupInput2."));
	}
}

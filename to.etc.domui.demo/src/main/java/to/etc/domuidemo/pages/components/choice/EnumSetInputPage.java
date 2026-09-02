package to.etc.domuidemo.pages.components.choice;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.enumsetinput.EnumSetInput;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * EnumSetInput: more than one value at a time, each chosen by typing and shown
 * as a label that can be taken off again.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class EnumSetInputPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("EnumSetInput: choosing several");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "EnumSetInput: choosing several"));

		List<Genre> genres = getSharedContext().query(QCriteria.create(Genre.class));

		Div shown = new Div("dm-tut");
		shown.add("Type a letter in the box: what matches drops down. Pick one and it "
			+ "becomes a label; press its cross to take it off again.");

		EnumSetInput<Genre> chosen = new EnumSetInput<>(Genre.class, genres, "name");
		chosen.setValue(Set.of(genres.get(0)));
		chosen.setOnValueChanged(a -> {
			shown.removeAllChildren();
			shown.add("Now searching in: " + names(chosen.getValue()));
		});

		//-- The set of values a control holds is a Set, so the same value cannot be in twice.
		EnumSetInput<Medium> media = new EnumSetInput<>(Medium.class, List.of(Medium.values()), null);

		EnumSetInput<Genre> readOnly = new EnumSetInput<>(Genre.class, genres, "name");
		readOnly.setValue(Set.of(genres.get(1), genres.get(2)));
		readOnly.setReadOnly(true);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Genres to search in").control(chosen);
		fb.label("Media (from an enum)").control(media);
		fb.label("setReadOnly(true)").control(readOnly);

		cp.add(new DefaultButton("Read the sets", a -> {
			shown.removeAllChildren();
			shown.add("genres=" + names(chosen.getValue())
				+ ", media=" + (media.getValue() == null ? "none" : media.getValue()));
		}));
		cp.add(shown);

		cp.add(new Para().add("The value of this control is a Set: the labels on screen are "
			+ "what is in it. A value already chosen is dropped from the list the box "
			+ "searches, so it cannot be picked twice."));
		cp.add(new Para().add("A read-only one shows its labels without their crosses; the "
			+ "search box is still there but cannot be typed in."));
	}

	private static String names(Set<Genre> set) {
		if(set == null || set.isEmpty())
			return "nothing";
		return set.stream().map(Genre::getName).sorted().collect(Collectors.joining(", "));
	}
}

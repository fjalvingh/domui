package to.etc.domuidemo.pages.components.lookup;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.component2.lookupinput.LookupInput2;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * LookupInput2: the three states it can be in, and where its quick search
 * comes from.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class LookupInput2Page extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("LookupInput2: finding a record");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "LookupInput2: finding a record"));

		Div shown = new Div("dm-tut");
		shown.add("Type two letters of a customer's name in the first box.");

		//-- Customer has firstName and lastName as BOTH search properties, so
		//-- the control gets its quick search from metadata.
		LookupInput2<Customer> customer = new LookupInput2<>(Customer.class);
		customer.setMandatory(true);
		customer.setOnValueChanged(a -> {
			shown.removeAllChildren();
			Customer value = customer.getValue();
			shown.add("Customer is now " + (value == null ? "empty" : value.getFirstName() + " " + value.getLastName()));
		});

		//-- Album's own keyword metadata is the title; here we search the artist name instead.
		LookupInput2<Album> album = new LookupInput2<>(Album.class);
		album.addKeywordProperty("artist.name", 2);

		//-- No keyword search at all: only the lookup button opens the dialog.
		LookupInput2<Artist> noQuick = new LookupInput2<>(Artist.class);
		noQuick.setAllowKeyWordSearch(false);

		//-- Tracks: thousands of them, so a vague search hits the "too many" branch.
		LookupInput2<Track> track = new LookupInput2<>(Track.class);
		track.addKeywordProperty("name", 1);

		LookupInput2<Customer> readOnly = new LookupInput2<>(Customer.class);
		readOnly.setValue(getSharedContext().get(Customer.class, Long.valueOf(1)));
		readOnly.setReadOnly(true);

		LookupInput2<Customer> disabled = new LookupInput2<>(Customer.class);
		disabled.setDisabledBecause("The invoice has already been sent");

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Customer (mandatory)").control(customer);
		fb.label("Album, searched by artist name").control(album);
		fb.label("Artist, button only").control(noQuick);
		fb.label("Track, thousands of rows").control(track);
		fb.label("setReadOnly(true)").control(readOnly);
		fb.label("setDisabledBecause()").control(disabled);

		cp.add(new DefaultButton("Read the values", a -> {
			Customer customerValue = customer.getValue();
			Album albumValue = album.getValue();
			Artist artistValue = noQuick.getValue();
			shown.removeAllChildren();
			shown.add("customer=" + (customerValue == null ? null : customerValue.getLastName())
				+ ", album=" + (albumValue == null ? null : albumValue.getTitle())
				+ ", artist=" + (artistValue == null ? null : artistValue.getName()));
		}));
		cp.add(shown);

		cp.add(new Para().add("A control with keyword search is an input box: typing at least "
			+ "one character searches, and the matches drop down under it. Pressing the "
			+ "lookup button instead opens the full search dialog, with the text already "
			+ "typed used as its first search."));
		cp.add(new Para().add("What the drop-down does depends on how many records were "
			+ "found: nothing at all says so, exactly one is selected straight away, more "
			+ "than a hundred says how many there are instead of listing them, and anything "
			+ "in between is the list. Try 'a', 'love' and 'zzz' in the Track box."));
		cp.add(new Para().add("Type $$1 in a box to look a record up by its primary key, or "
			+ "$$city=Oslo to search one property exactly."));
	}
}

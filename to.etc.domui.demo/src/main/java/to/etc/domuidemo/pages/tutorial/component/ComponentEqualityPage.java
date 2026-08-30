package to.etc.domuidemo.pages.tutorial.component;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

/**
 * Tutorial, "writing a component", step 3: what "the value changed" means when the
 * value is an object. Everything here works on detached objects made by hand, so
 * nothing touches the database.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ComponentEqualityPage extends UrlPage {
	private final Album m_album = album(1L, "Big Ones");

	private final Review m_review = new Review();

	@Override
	public void createContent() throws Exception {
		setPageTitle("When has a value changed?");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "When has a value changed?"));

		Div result = new Div("dm-tut");

		//-- A control that is set by hand.
		cp.add(new HTag(2, "setValue() on the control"));
		AlbumBadge badge = new AlbumBadge();
		badge.setValue(m_album);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Album").control(badge);

		Div buttons = new Div();
		cp.add(buttons);
		buttons.add(new DefaultButton("Change the title in place", a -> {
			m_album.setTitle(m_album.getTitle() + "!");
			badge.setValue(m_album);                       // The same instance: setValue does nothing
			say(result, "The object now says \"" + m_album.getTitle() + "\", but the badge still shows the old text:"
				+ " setValue() got the very object it already held.");
		}));
		buttons.add(new DefaultButton("...and force a redraw", a -> {
			m_album.setTitle(m_album.getTitle() + "!");
			badge.forceRebuild();                          // Say it yourself
			say(result, "forceRebuild() redraws the control whether or not it thinks its value changed.");
		}));
		buttons.add(new DefaultButton("Another object, same id", a -> {
			badge.setValue(album(m_album.getId(), "A different object with id " + m_album.getId()));
			say(result, "Nothing happened: two entities with the same primary key count as the same value.");
		}));
		buttons.add(new DefaultButton("A really different album", a -> {
			badge.setValue(album(2L, "Nevermind"));
			say(result, "That one is a different value, so the control redrew.");
		}));

		//-- And the same question on the binding side.
		cp.add(new HTag(2, "The same thing, through a binding"));
		m_review.setAlbum(album(3L, "Let There Be Rock"));

		AlbumBadge bound = new AlbumBadge();
		bound.bind().to(m_review, Review_.album());

		FormBuilder fb2 = new FormBuilder(cp);
		fb2.label("Album (bound)").control(bound);

		Div boundButtons = new Div();
		cp.add(boundButtons);
		boundButtons.add(new DefaultButton("Change the model's album in place", a -> {
			m_review.getAlbum().setTitle(m_review.getAlbum().getTitle() + "!");
			say(result, "The model's object changed, but the binding compares it with the object it delivered last time"
				+ " - the same one - so nothing moved.");
		}));
		boundButtons.add(new DefaultButton("Put a new object in the model", a -> {
			m_review.setAlbum(album(4L, "A new object at " + System.currentTimeMillis() % 100000));
			say(result, "A different object: the binding moved it into the control at the end of the request.");
		}));

		cp.add(result);
		result.add("Press the buttons in order and watch what the badges do.");
	}

	/**
	 * A detached Album, made by hand: this page never reads or writes the database.
	 */
	private static Album album(Long id, String title) {
		Album album = new Album();
		album.setId(id);
		album.setTitle(title);
		return album;
	}

	private static void say(Div result, String what) {
		result.removeAllChildren();
		result.add(what);
	}
}

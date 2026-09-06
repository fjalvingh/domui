package to.etc.domuidemo.pages.components.form;

import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.derbydata.db.Track_;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * FormBuilder: what property() adds over label()/control() - the control is
 * created from the property's metadata and bound to it, and readOnly and
 * disabled can be bound to the model too.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class FormPropertyPage extends UrlPage {
	/** The state the readOnly bindings on this page follow. */
	private boolean m_locked;

	@Override
	public void createContent() throws Exception {
		setPageTitle("FormBuilder: a form from properties");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "A form from properties"));

		Track track = getSharedContext().get(Track.class, 1L);

		FormBuilder fb = new FormBuilder(cp);
		fb.property(track, Track_.name()).control();
		fb.property(track, Track_.composer()).control();
		fb.property(track, Track_.unitPrice()).control();
		fb.property(track, Track_.genre()).control();

		cp.add(new Para().add("Not one of these four rows says what its label is, which "
			+ "control to make, or where the value comes from: property() asks the "
			+ "metadata of Track for all three, and binds the control it made to the "
			+ "property. Type into a box and the Track instance changes with it."));

		//-- Overriding what the metadata says
		cp.add(new HTag(2, "Overriding what the metadata says"));

		Track second = getSharedContext().get(Track.class, 2L);

		Text2<String> ownControl = new Text2<>(String.class);
		ownControl.setSize(60);

		FormBuilder fb2 = new FormBuilder(cp);
		fb2.property(second, Track_.composer()).label("A label of my own").control();
		fb2.property(second, Track_.unitPrice()).mandatory().hint("What the shop charges").control();
		fb2.property(second, Track_.genre()).control(ComboLookup2.class);
		fb2.property(second, Track_.name()).label("A control of my own").control(ownControl);

		cp.add(new Para().add("Every step of the chain overrules the metadata for that one "
			+ "row. control(Class) asks for a particular kind of control: genre is a "
			+ "relation, which by default becomes a LookupInput2, and here it is a "
			+ "ComboLookup2 instead. control(control) hands the builder a control you made "
			+ "yourself, which is bound and laid out just the same."));

		//-- readOnly and disabled, bound
		cp.add(new HTag(2, "readOnly follows the model"));

		Track third = getSharedContext().get(Track.class, 3L);

		Checkbox lock = new Checkbox();
		lock.immediate();
		lock.bind().to(this, "locked");

		FormBuilder fb3 = new FormBuilder(cp);
		fb3.label("Lock this form").control(lock);

		FormBuilder fb4 = new FormBuilder(cp);
		fb4.readOnlyAll(this, "locked");
		fb4.property(third, Track_.name()).control();
		fb4.property(third, Track_.composer()).control();
		fb4.readOnlyAllClear();
		fb4.property(third, Track_.unitPrice()).control();

		cp.add(new Para().add("Tick the box: the first two controls become read only, the "
			+ "third does not. readOnlyAll() binds the readOnly property of every control "
			+ "after it to a boolean property of the page, until readOnlyAllClear() ends "
			+ "it. readOnly(instance, property) does the same for one row only, and "
			+ "readOnly() without arguments simply sets it."));

		cp.add(new HTag(2, "Disabled, and why"));

		FormBuilder fb5 = new FormBuilder(cp);
		fb5.property(third, Track_.milliseconds()).readOnly().control();
		fb5.property(third, Track_.bytes()).disabled().control();
		fb5.property(third, Track_.mediaType())
			.disabledBecause("The media type is set when the track is imported").control();

		cp.add(new Para().add("A read-only control shows its value but cannot be typed "
			+ "into; a disabled one is greyed out as well. disabledBecause() does the "
			+ "same and says why - hover the last control to see the reason. Both have "
			+ "an ...All() form and a bound form, exactly like readOnly."));
	}

	public boolean isLocked() {
		return m_locked;
	}

	public void setLocked(boolean locked) {
		m_locked = locked;
	}
}

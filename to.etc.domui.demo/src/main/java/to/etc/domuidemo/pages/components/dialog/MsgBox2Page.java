package to.etc.domuidemo.pages.components.dialog;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component.misc.MsgBoxButton;
import to.etc.domui.component.misc.MsgBoxButtonPrio;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Li;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.Ul;
import to.etc.domui.dom.html.UrlPage;

/**
 * MsgBox2: the message box builder - the types, the buttons and the ways an
 * answer comes back.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class MsgBox2Page extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("MsgBox2");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "MsgBox2"));

		Div result = new Div("dm-tut-q");
		result.add("The answer of the last box appears here.");

		cp.add(new HTag(2, "The four types"));
		Div types = new Div("dm-tut");
		cp.add(types);
		types.add(new DefaultButton("info", a -> MsgBox2.on(this).info("The album has been saved.")));
		types.add(new DefaultButton("warning", a -> MsgBox2.on(this).warning("This album has no tracks yet.")));
		types.add(new DefaultButton("error", a -> MsgBox2.on(this).error("The album could not be saved.")));
		types.add(new DefaultButton("question", a -> MsgBox2.on(this)
			.question()
			.text("Delete the album?")
			.yesNo()
			.onAnswer(button -> answered(result, button.name()))));

		cp.add(new HTag(2, "Something other than a sentence"));
		Div content = new Div("dm-tut");
		cp.add(content);
		content.add(new DefaultButton("A piece of DOM", a -> {
			Div box = new Div();
			box.add("The import finished with these results:");
			Ul ul = new Ul();
			box.add(ul);
			ul.add(new Li().add("212 albums read"));
			ul.add(new Li().add("3 albums skipped: no artist"));

			MsgBox2.on(this)
				.title("Import finished")                     // Overrides the title the type gives
				.warning()
				.content(box)                                 // Instead of text(...)
				.icon(Icon.faUpload)                          // Overrides the icon the type gives
				.size(500, -1)                                // -1: as high as its content needs
				.button(MsgBoxButton.OK)
			;
		}));

		cp.add(new HTag(2, "Buttons, and how the answer comes back"));
		Div answers = new Div("dm-tut");
		cp.add(answers);

		//-- Standard buttons: the answer is the button that was pressed.
		answers.add(new DefaultButton("Three standard buttons", a -> MsgBox2.on(this)
			.question()
			.text("The album has changed. Save it before leaving?")
			.button(MsgBoxButton.YES)
			.button(MsgBoxButton.NO)
			.button(MsgBoxButton.CANCEL)
			.onAnswer(button -> answered(result, "the button " + button.name()))));

		//-- Buttons of your own, each carrying the value it answers with.
		answers.add(new DefaultButton("Buttons with values", a -> MsgBox2.on(this)
			.question()
			.text("Which format?")
			.button("Compact disc", "CD")
			.button("Vinyl", "LP")
			.button("Download", "MP3")
			.button(MsgBoxButton.CANCEL)
			.onAnswer2(value -> answered(result, "the value " + value))));

		//-- A button that does its own thing: it does not answer the box.
		answers.add(new DefaultButton("A button with a handler", a -> MsgBox2.on(this)
			.warning()
			.text("This album is sold out.")
			.button("Order more", MsgBoxButtonPrio.Secondary, b -> answered(result, "'order more'"))
			.buttonDefault(MsgBoxButton.OK, MsgBoxButtonPrio.Primary)
			.onAnswer(button -> answered(result, "the button " + button.name()))));

		cp.add(new HTag(2, "Asking for a value"));
		Div input = new Div("dm-tut");
		cp.add(input);
		input.add(new DefaultButton("Ask for a number", a -> {
			Text2<Integer> copies = new Text2<>(Integer.class);
			copies.setMandatory(true);

			MsgBox2.on(this)
				.title("Order")
				.input("Copies", copies, value -> answered(result, value + " copies"))
				.onValidate(button -> {
					if(button != MsgBoxButton.CONTINUE) {
						return true;                          // Cancelling is always allowed
					}
					Integer value = copies.getValueSafe();    // Empty: reports "mandatory" itself
					if(null == value) {
						return false;                         // ...and the box stays open
					}
					if(value.intValue() > 10) {
						MsgBox2.on(this).error("At most 10 copies can be ordered at once.");
						return false;
					}
					return true;
				});
		}));

		cp.add(result);

		cp.add(new Para().add("A message box does not block the code that made it: the call returns "
			+ "at once and the box appears on the next screen the user sees. Everything that has to "
			+ "happen after the answer belongs in the answer handler."));
	}

	private void answered(Div result, String what) {
		result.removeAllChildren();
		result.add("Answered with " + what);
	}
}

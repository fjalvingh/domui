package to.etc.domuidemo.pages.tutorial.messages;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component.misc.MsgBoxButton;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Li;
import to.etc.domui.dom.html.Ul;
import to.etc.domui.dom.html.UrlPage;

/**
 * Tutorial, "telling something to a user", step 1: MsgBox2 shows a message on top
 * of the page the user is looking at.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class MsgBoxPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Telling the user something");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Telling the user something"));

		Div buttons = new Div("dm-tut");
		cp.add(buttons);

		buttons.add(new DefaultButton("Info", a -> MsgBox2.on(this)
			.info("The album has been saved.")
		));

		buttons.add(new DefaultButton("Warning", a -> MsgBox2.on(this)
			.warning("This album has no tracks yet, so nobody can buy it.")
		));

		buttons.add(new DefaultButton("Error", a -> MsgBox2.on(this)
			.error("The album could not be saved: the shop is closed.")
		));

		buttons.add(new DefaultButton("Translated text", a -> MsgBox2.on(this)
			.info()
			.text(TutorialMsg.orderStockLow, "Big Ones", 3)
		));

		buttons.add(new DefaultButton("A title and a content of your own", a -> {
			//-- Anything that is a NodeContainer can be the body of the box.
			Div content = new Div();
			content.add("The import finished with these results:");
			Ul ul = new Ul();
			content.add(ul);
			ul.add(new Li().add("212 albums read"));
			ul.add(new Li().add("3 albums skipped: no artist"));
			ul.add(new Li().add("1 album skipped: duplicate title"));

			MsgBox2.on(this)
				.title("Import finished")
				.warning()
				.content(content)
				.button(MsgBoxButton.OK)
				.size(500, -1)
			;
		}));
	}
}

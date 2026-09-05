package to.etc.domuidemo.pages.components.dialog;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.ErrorMessageDiv;
import to.etc.domui.component.layout.ErrorPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * The two components that show the messages a fence catches: ErrorPanel and
 * ErrorMessageDiv, each on a panel that is a fence of its own.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ErrorDisplayPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Showing messages");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Showing messages"));

		//-- A panel that catches its own messages, and shows them in an ErrorPanel.
		Div first = new Div("dm-tut");
		cp.add(first);
		first.add(new HTag(2, "ErrorPanel"));
		first.setErrorFence();                            // Messages from below stop here...
		ErrorPanel panel = new ErrorPanel();
		first.add(panel);                                 // ...and this shows what it caught
		addForm(first, "Big Ones");

		//-- The same, with the smaller display component.
		Div second = new Div("dm-tut");
		cp.add(second);
		second.add(new HTag(2, "ErrorMessageDiv"));
		ErrorMessageDiv emd = new ErrorMessageDiv(second);   // Makes the panel a fence and listens to it
		second.add(emd);
		addForm(second, "Nevermind");

		//-- ...and one that passes everything it catches on to the page as well.
		Div third = new Div("dm-tut");
		cp.add(third);
		third.add(new HTag(2, "ErrorMessageDiv, propagating"));
		ErrorMessageDiv propagating = new ErrorMessageDiv(third, true);
		third.add(propagating);
		addForm(third, "Nevermind");

		cp.add(new Para().add("Leave the field empty and press Order: the message is shown by the "
			+ "panel it was raised in, because that panel is an error fence. The third panel's "
			+ "fence also hands what it catches to the page, so its messages appear twice - once "
			+ "in the panel and once where the application shows page messages."));
		cp.add(new Para().add("An ErrorPanel writes a titled block whose title follows the severest "
			+ "message in it; an ErrorMessageDiv writes the bare lines. Both are hidden while there "
			+ "is nothing to show."));
	}

	private void addForm(Div panel, String album) throws Exception {
		Text2<Integer> copies = new Text2<>(Integer.class);
		copies.setMandatory(true);

		FormBuilder fb = new FormBuilder(panel);
		fb.label("Copies of " + album).control(copies);

		panel.add(new DefaultButton("Order", a -> {
			Integer value = copies.getValue();            // Empty: the control reports it into this fence
			panel.addGlobalMessage(UIMessage.info(DialogMsg.albumSaved, album + " x " + value));
		}));
		panel.add(new DefaultButton("A message about the panel", a ->
			panel.addGlobalMessage(UIMessage.warning(DialogMsg.albumStockLow, album, Integer.valueOf(3)))));
		panel.add(new DefaultButton("Clear", a -> {
			copies.setMessage(null);
			panel.clearGlobalMessage();
		}));
	}
}

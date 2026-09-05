package to.etc.domuidemo.pages.components.dialog;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.Dialog;
import to.etc.domui.component.layout.InputDialog;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * InputDialog: a Dialog that asks for one value, using one control - plus the
 * two confirmation dialogs the class ships as static methods.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class InputDialogPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("InputDialog");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "InputDialog"));

		Div result = new Div("dm-tut-q");
		result.add("What was answered appears here.");

		Div buttons = new Div("dm-tut");
		cp.add(buttons);

		//-- One value, one control, save and cancel.
		buttons.add(new DefaultButton("Rename the album", a -> {
			Text2<String> title = new Text2<>(String.class);
			title.setMandatory(true);
			title.setValue("Big Ones");

			InputDialog<String, Text2<String>> dlg = new InputDialog<>(title, "Rename the album", "New title") {
				@Override
				protected boolean onValidateData(String value) throws Exception {
					return value.length() >= 2;               // Refuse: the dialog stays open
				}

				@Override
				protected boolean onSaveData(String value) throws Exception {
					result.removeAllChildren();
					result.add("The album is now called \"" + value + "\"");
					return true;
				}
			};
			add(dlg);
		}));

		//-- "Type the name to confirm": the answer must match the value passed in.
		buttons.add(new DefaultButton("Delete the album", Icon.faTrash, a -> {
			Dialog dlg = InputDialog.confirmDeleteInBlood("Delete \"Big Ones\"?", "Big Ones",
				"Type the album title to confirm", value -> {
					result.removeAllChildren();
					result.add("The album was deleted");
					return true;
				});
			add(dlg);
		}));

		//-- The same shape, but the answer is a reason and only has to be filled in.
		buttons.add(new DefaultButton("Cancel the order", Icon.faTimes, a -> {
			Dialog dlg = InputDialog.confirmWithReason("Cancel the order", 80, 40,
				"Cancel the order", Icon.faTimes, reason -> {
					result.removeAllChildren();
					result.add("The order was cancelled: " + reason);
					return true;
				});
			add(dlg);
		}));

		cp.add(result);

		cp.add(new Para().add("An InputDialog is a Dialog whose content is one label and one "
			+ "control. The value of that control arrives in onSaveData(), after onValidateData() "
			+ "has agreed to it - both of them see the value, not the control."));
	}
}

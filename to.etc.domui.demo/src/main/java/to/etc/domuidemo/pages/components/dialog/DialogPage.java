package to.etc.domuidemo.pages.components.dialog;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.Dialog;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * Dialog: a Window with a button bar, and the save/cancel handling that goes
 * with it - validate, save, then close with the "save" reason.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class DialogPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Dialog");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Dialog"));

		Div result = new Div("dm-tut-q");
		result.add("What the dialog did appears here.");

		Div buttons = new Div("dm-tut");
		cp.add(buttons);

		//-- The usual shape: a form, a save button that validates, and a cancel button.
		buttons.add(new DefaultButton("Order copies", a -> {
			Text2<Integer> copies = new Text2<>(Integer.class);
			copies.setMandatory(true);
			copies.setValue(Integer.valueOf(1));

			Dialog dlg = new Dialog(true, false, 460, -1, "Order \"Big Ones\"") {
				@Override
				protected void createFrame() throws Exception {
					super.createFrame();
					createButtons();                          // A Dialog does not make its buttons by itself
				}

				@Override
				public void createContent() throws Exception {
					FormBuilder fb = new FormBuilder(this);
					fb.label("Copies").control(copies);
				}

				@Override
				protected boolean onValidate() throws Exception {
					Integer value = copies.getValueSafe();    // Empty: the control reports it itself
					if(null == value) {
						return false;                         // ...and the dialog stays open
					}
					if(value.intValue() > 10) {
						MsgBox2.on(this).error("At most 10 copies can be ordered at once.");
						return false;
					}
					return true;
				}

				@Override
				protected boolean onSave() throws Exception {
					result.removeAllChildren();
					result.add("Ordered " + copies.getValue() + " copies");
					return true;
				}
			};
			dlg.setButtonsOnBottom(true);                     // Before the bar exists, or it throws
			dlg.setOnClose(reason -> {
				if(!Dialog.RSN_SAVE.equals(reason)) {
					result.removeAllChildren();
					result.add("The dialog was closed without saving, reason: " + reason);
				}
			});
			add(dlg);
		}));

		//-- A dialog with buttons of its own instead of save and cancel.
		buttons.add(new DefaultButton("Buttons of my own", a -> {
			Dialog dlg = new Dialog(true, false, 460, -1, "What shall it be?");
			add(dlg);
			dlg.add(new Para().add("This dialog never called createButtons(), so it has no save "
				+ "and no cancel - only what was added to its button bar."));

			dlg.getButtonBar().addButton("Buy it", Icon.faShoppingCart, b -> {
				result.removeAllChildren();
				result.add("Bought");
				dlg.close();
			});
			dlg.getButtonBar().addButton("Put it back", Icon.faUndo, b -> dlg.close());
		}));

		cp.add(result);

		cp.add(new Para().add("The save button runs bindErrors(), onSaveBind(), onValidate() and "
			+ "onSave() in that order and stops at the first one that says no, which is how a "
			+ "dialog refuses to close. Only when all four pass does it close with the reason "
			+ "\"save\"; the cancel button and the cross close with \"closed\"."));
	}
}

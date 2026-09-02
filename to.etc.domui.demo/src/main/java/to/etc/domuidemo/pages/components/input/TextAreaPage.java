package to.etc.domuidemo.pages.components.input;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.TextArea;
import to.etc.domui.dom.html.UrlPage;

/**
 * TextArea: multi-line text, with the two length limits it can enforce.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TextAreaPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("TextArea: more than one line");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "TextArea: more than one line"));

		TextArea review = new TextArea(60, 6);
		review.setValue("The best album they made.\nSide two especially.");
		review.setMandatory(true);

		TextArea limited = new TextArea(60, 3);
		limited.setMaxLength(40);

		TextArea bytes = new TextArea(60, 3);
		bytes.setMaxByteLength(20);

		TextArea readOnly = new TextArea(60, 3);
		readOnly.setValue("Written by the shop owner.\nCannot be changed here.");
		readOnly.setReadOnly(true);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Review (mandatory)").control(review);
		fb.label("setMaxLength(40)").control(limited);
		fb.label("setMaxByteLength(20)").control(bytes);
		fb.label("setReadOnly(true)").control(readOnly);

		Div result = new Div("dm-tut-q");
		result.add("Press Save to see the text exactly as the control hands it over.");

		cp.add(new DefaultButton("Save", a -> {
			String text = review.getValue();
			result.removeAllChildren();
			result.add(text);
		}));
		cp.add(result);

		cp.add(new Para().add("Type accented characters in the byte-limited box: it stops "
			+ "earlier than the one limited to characters, because one character can cost "
			+ "more than one UTF-8 byte."));
	}
}

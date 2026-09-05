package to.etc.domuidemo.pages.components.editors;

import to.etc.domui.component.htmleditor.HtmlEditor;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * HtmlEditor: the small wysiwyg editor - a textarea with a toolbar over it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class HtmlEditorPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("HtmlEditor");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "HtmlEditor"));

		HtmlEditor editor = new HtmlEditor();
		editor.setWidth("600px");
		editor.setValue("<p>The <b>small</b> editor: bold, italic, lists, colours and a few more.</p>");
		cp.add(editor);

		Div result = new Div("dm-tut-q");
		result.add("Press \"Show the html\" to see what the editor produced.");

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addButton("Show the html", a -> {
			result.removeAllChildren();
			String html = editor.getValue();
			result.add(html == null ? "(empty)" : html);  // Added as text, so the markup is readable
		});
		bb.addButton("Set new text", a -> editor.setValue("<p>Text set <i>from the server</i>.</p>"));
		bb.addButton("Clear", a -> editor.setValue(null));

		cp.add(result);

		cp.add(new Para().add("The value is html, and getValue() hands back what the user made of it. "
			+ "The editor is a TextArea underneath - the wysiwyg is a jQuery plugin drawn over it - "
			+ "so setValue(), setReadOnly() and data binding are the ones every control has."));

		cp.add(new Para().add("It shows itself immediately, which is the reason it exists: the "
			+ "CKEditor next door can do far more, and takes noticeably longer to appear. For a "
			+ "remark, a description or a note this one is enough."));
	}
}

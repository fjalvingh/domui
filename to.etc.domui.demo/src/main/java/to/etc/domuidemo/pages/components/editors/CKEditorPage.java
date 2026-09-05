package to.etc.domuidemo.pages.components.editors;

import to.etc.domui.component.ckeditor.CKEditor;
import to.etc.domui.component.ckeditor.CKToolbarSet;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * CKEditor: the full wysiwyg editor, and what its toolbar sets do.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class CKEditorPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("CKEditor");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "CKEditor"));

		//-- Without this the editor's javascript is not on the page and nothing appears at all.
		CKEditor.initialize(this);

		//-- The default toolbar set: DOMUI, which adds the framework's own image and character buttons.
		CKEditor editor = new CKEditor();
		editor.setWidth("100%");
		editor.setHeight("300px");
		editor.setValue("<p>The <b>full</b> editor: styles, tables, colours, images and the rest.</p>");
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
		bb.addButton("Read only", a -> editor.setReadOnly(!editor.isReadOnly()));

		cp.add(result);

		cp.add(new Para().add("The value is html, exactly as with the small editor, and the control "
			+ "methods are the same. What differs is how much the toolbar offers, and how long the "
			+ "editor takes to appear - it is a large third-party editor being started up."));

		//-- The same editor with the smallest toolbar there is.
		cp.add(new HTag(2, "The toolbar sets"));
		CKEditor plain = new CKEditor();
		plain.setWidth("100%");
		plain.setHeight("150px");
		plain.setToolbarSet(CKToolbarSet.TXTONLY);
		plain.setValue("<p>The same editor, with the TXTONLY toolbar.</p>");
		cp.add(plain);

		cp.add(new Para().add("Four sets are on offer. TXTONLY is the one above: bold, italic, "
			+ "underline, strike, cut, copy, paste, undo and redo. BASIC adds a second row with "
			+ "Styles, Format, Font and Size. DOMUI - the default - and FULL are the same toolbar, "
			+ "the wide one at the top of this page, and they are the only two that load the extra "
			+ "plugins: the green image picker, the special-characters button, the colours and the "
			+ "smileys."));

		cp.add(new Para().add("Pick the set when the editor is made. Changing it on an editor that "
			+ "is already on the screen does not redraw its toolbar."));
	}
}

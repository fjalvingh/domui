package to.etc.domuidemo.pages.special.ace;

import to.etc.domui.component.ace.AceEditor;
import to.etc.domui.component.layout.ButtonBar;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.FileTool;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 23-12-17.
 */
public class AcePage extends UrlPage {
	@Override public void createContent() throws Exception {
		AceEditor.initialize(this);

		String text = FileTool.readResourceAsString(getClass(), "demojs.js", "utf-8");

		AceEditor editor = new AceEditor();
		add(editor);
		editor.setWidth("auto");
		editor.setHeight("500px");
		editor.setValue(text);

		ButtonBar bb = new ButtonBar();
		add(bb);
		bb.addButton("SetText", a -> {
			String t2 = FileTool.readResourceAsString(getClass(), "demo2js.js", "utf-8");
			editor.setValue(t2);
		});

		bb.addButton("Clear", a -> editor.setValue(null));

		bb.addButton("Toggle RO", a -> editor.setReadOnly(! editor.isReadOnly()));

		bb.addButton("Show Val", a -> MsgBox.info(this, editor.getValue()));
	}
}

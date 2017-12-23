package to.etc.domuidemo.pages.special.ace;

import to.etc.domui.component.ace.AceEditor;
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
		editor.setWidth("500px");
		editor.setHeight("500px");
		editor.setValue(text);


	}
}

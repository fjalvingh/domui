package to.etc.domuidemo.pages.overview.htmleditor;

import to.etc.domui.component.ckeditor.CKEditor;
import to.etc.domui.component.htmleditor.FileBasedEditorFileSystem;
import to.etc.domui.dom.html.BR;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;

import java.io.File;

public class DemoCKEditor extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);
		d.add(new BR());

		//Div cont = new Div();
		//cont.setDisplay(DisplayType.BLOCK);
		//cont.setWidth("800px");
		//cont.setHeight("400px");
		//d.add(cont);

		CKEditor cke = new CKEditor();
		cke.setWidth("800px");
		cke.setText("Some sample text");
		//cke.setHeight("200px");
		d.add(cke);

		//-- Optional
		File f = new File("/");
		cke.setFileSystem(new FileBasedEditorFileSystem(f));

	}
}

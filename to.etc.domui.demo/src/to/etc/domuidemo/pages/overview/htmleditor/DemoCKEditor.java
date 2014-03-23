package to.etc.domuidemo.pages.overview.htmleditor;

import java.io.*;

import to.etc.domui.component.ckeditor.*;
import to.etc.domui.component.htmleditor.*;
import to.etc.domui.dom.html.*;

public class DemoCKEditor extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);
		d.add(new BR());

		CKEditor cke = new CKEditor();
		cke.setWidth("800px");
		cke.setText("Some sample text");
		d.add(cke);

		//-- Optional
		File f = new File("/");
		cke.setFileSystem(new FileBasedEditorFileSystem(f));

	}
}

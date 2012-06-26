package to.etc.domuidemo.pages.overview.htmleditor;

import java.io.*;

import to.etc.domui.component.htmleditor.*;
import to.etc.domui.dom.html.*;

public class DemoFCKEditor extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);
		d.add(new BR());

		FCKEditor fcke = new FCKEditor();
		fcke.setWidth("800px");
		fcke.setText("Some sample text");
		d.add(fcke);

		//-- Optional
		File f = new File("/");
		fcke.setFileSystem(new FileBasedEditorFileSystem(f));

	}
}

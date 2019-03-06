package to.etc.domuidemo.pages.overview.htmleditor;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.ckeditor.CKEditor;
import to.etc.domui.component.htmleditor.FileBasedEditorFileSystem;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

import java.io.File;

public class DemoCKEditor extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new HTag(1, "Fixed size CKEditor"));
		ContentPanel d = new ContentPanel();
		add(d);

		//Div cont = new Div();
		//cont.setDisplay(DisplayType.BLOCK);
		//cont.setWidth("800px");
		//cont.setHeight("400px");
		//d.add(cont);

		CKEditor cke = new CKEditor();
		cke.setWidth("800px");
		cke.setValue("Some sample text");
		cke.setHeight("100%");
		d.add(cke);

		add(new VerticalSpacer(10));
		Div res = new Div();
		d.add(res);

		//-- Optional
		File f = new File("/");
		cke.setFileSystem(new FileBasedEditorFileSystem(f));

		d.add(new DefaultButton("Show HTML", a -> show(res, cke)));
		d.add("\u00a0\u00a0");
		d.add(new DefaultButton("Set text", a -> cke.setValue("<p>This is <b>new</b> text</p>")));
	}

	private void show(Div res, CKEditor cke) {
		res.removeAllChildren();
		res.add(cke.getValue());
	}
}

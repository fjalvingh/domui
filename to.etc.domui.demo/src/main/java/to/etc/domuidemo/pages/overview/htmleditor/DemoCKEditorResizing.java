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

public class DemoCKEditorResizing extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		setHeight("98%");
		cp.add(new HTag(1, "Resizing CKEditor"));

		//Div cont = new Div();
		//cont.setDisplay(DisplayType.BLOCK);
		//cont.setWidth("800px");
		//cont.setHeight("400px");
		//d.add(cont);

		CKEditor cke = new CKEditor();
		cke.setValue("Some sample text");
		cke.setWidth("80%");
		cke.setHeight("50%");
		cp.add(cke);

		cp.add(new VerticalSpacer(10));
		Div res = new Div();
		cp.add(res);

		//-- Optional
		File f = new File("/");
		cke.setFileSystem(new FileBasedEditorFileSystem(f));

		cp.add(new DefaultButton("Show HTML", a -> show(res, cke)));
		cp.add("\u00a0\u00a0");
		cp.add(new DefaultButton("Set text", a -> cke.setValue("<p>This is <b>new</b> text</p>")));
	}

	private void show(Div res, CKEditor cke) {
		res.removeAllChildren();
		res.add(cke.getValue());
	}
}

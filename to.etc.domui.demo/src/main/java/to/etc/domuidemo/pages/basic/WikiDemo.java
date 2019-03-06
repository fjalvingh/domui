package to.etc.domuidemo.pages.basic;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.ckeditor.CKEditor;
import to.etc.domui.component.htmleditor.FileBasedEditorFileSystem;
import to.etc.domui.dom.html.BR;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.UrlPage;

import java.io.File;

/**
 * Demo page containing an CKEditor.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 30, 2008
 */
public class WikiDemo extends UrlPage {
	private CKEditor m_editor;

	public WikiDemo() {
	}

	@Override
	public void createContent() throws Exception {
		setTitle("Wiki Editor example");

		add(m_editor = new CKEditor());
		m_editor.setWidth("600px");
		m_editor.setHeight("300px");

		add(new BR());
		DefaultButton	b = new DefaultButton("Opslaan", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton xxb) throws Exception {
				klikked();
			}
		});
		add(b);

		File	f	= new File("/home/jal/Pictures");
		m_editor.setFileSystem(new FileBasedEditorFileSystem(f));
	}

	void klikked() {
		System.out.println("Submitted is: "+m_editor.getValue());
	}

}

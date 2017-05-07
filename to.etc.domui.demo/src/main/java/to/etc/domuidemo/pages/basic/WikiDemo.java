package to.etc.domuidemo.pages.basic;

import java.io.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.ckeditor.*;
import to.etc.domui.component.htmleditor.*;
import to.etc.domui.dom.html.*;

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
		m_editor.setRows(24);
		m_editor.setCols(80);

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

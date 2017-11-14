package to.etc.domuidemo.pages.overview.allcomponents;

import to.etc.domui.component.upload.FileUpload2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-11-17.
 */
public class FileUploadFragment extends Div {
	@Override public void createContent() throws Exception {
		add(new HTag(2, "File upload component").css("ui-header"));

		FormBuilder fb = new FormBuilder(this);
		FileUpload2 u1 = new FileUpload2("png", "jpg", "gif", "jpeg");
		fb.label("Select an image").item(u1);


		//FileUpload u2 = new FileUpload("png", "jpg", "gif", "jpeg");
		//fb.label("OLD").item(u2);

	}
}

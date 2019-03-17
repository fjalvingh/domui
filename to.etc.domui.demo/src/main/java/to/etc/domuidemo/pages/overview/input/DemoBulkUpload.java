package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.layout.Caption;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component.upload.FileUploadMultiple;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;

public class DemoBulkUpload extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new Caption("Bulk upload"));

		add(new VerticalSpacer(40));

		Div t = new Div();
		add(t);
		t.setMargin("40px");

		FileUploadMultiple bu = new FileUploadMultiple();
		t.add(bu);
	}

}

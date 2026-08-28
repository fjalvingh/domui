package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component.upload.FileUploadMultiple;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

public class DemoBulkUpload extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		cp.add(new HTag(1, "Bulk upload"));

		cp.add(new VerticalSpacer(40));

		Div t = new Div();
		cp.add(t);
		t.setMargin("40px");

		FileUploadMultiple bu = new FileUploadMultiple();
		t.add(bu);
	}

}

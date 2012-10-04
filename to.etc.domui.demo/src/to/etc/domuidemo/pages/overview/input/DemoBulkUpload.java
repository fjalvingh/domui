package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.component.upload.*;
import to.etc.domui.component.upload.BulkUpload.IUpload;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.upload.*;

public class DemoBulkUpload extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new Caption("Bulk upload"));

		add(new VerticalSpacer(40));

		Div t = new Div();
		add(t);
		t.setMargin("40px");

		BulkUpload bu = new BulkUpload();
		t.add(bu);
		bu.setOnUpload(new IUpload() {
			@Override
			public boolean fileUploaded(UploadItem item) throws Exception {
				Div d = new Div();
				d.add("Got file " + item.getRemoteFileName() + ", " + item.getFile().length() + " bytes");
				DemoBulkUpload.this.add(d);
				return true;						// Delete it.
			}
		});

		bu.setOnUploadsComplete(new IClicked<BulkUpload>() {

			@Override
			public void clicked(BulkUpload clickednode) throws Exception {
				MsgBox.info(DemoBulkUpload.this, "All uploads have completed.");
			}
		});
	}

}

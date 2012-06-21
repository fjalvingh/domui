package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.upload.*;
import to.etc.domui.dom.html.*;

public class DemoBulkUpload extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new Caption("Bulk upload"));

		BulkUpload bu = new BulkUpload();
		add(bu);

	}

}

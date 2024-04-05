package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.upload.FileUploadMultiple;
import to.etc.domui.dom.html.BR;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.upload.UploadItem;
import to.etc.util.FileTool;
import to.etc.util.StringTool;

import java.io.IOException;
import java.util.List;

public class DemoFileUpload extends UrlPage {

	@Override
	public void createContent() {
		Div d = new Div();
		add(d);
		add(new BR());

		Label lab = new Label("Select a file to upload : ");
		d.add(lab);
		d.add(new BR());

		final FileUploadMultiple upload = new FileUploadMultiple("jpg", "gif", "png", "log");
		d.add(upload);

		DefaultButton db = new DefaultButton("display hashes");
		d.add(db);
		db.setClicked(new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				showHashes(upload);
			}

		});
	}

	void showHashes(FileUploadMultiple upload) throws IOException {
		List<UploadItem> uil = upload.getValue();
		if(null == uil || uil.isEmpty()) {
			add(new BR());
			add(new Label("No files uploaded !"));
			return;
		}

		for(UploadItem ui : uil) {
			add(new BR());
			String calcHash = StringTool.toHex(FileTool.hashFile(ui.getFile()));
			Label lab = new Label("Hash of file " + ui.getRemoteFileName() + " is " + calcHash);
			add(lab);
		}
	}
}

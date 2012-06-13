package to.etc.domuidemo.pages.overview.input;

import java.io.*;
import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.upload.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.upload.*;
import to.etc.util.*;

public class DemoFileUpload extends UrlPage {

	@Override
	public void createContent() {
		Div d = new Div();
		add(d);
		add(new BR());

		Label lab = new Label("Select a file to upload : ");
		d.add(lab);
		d.add(new BR());

		final FileUpload upload = new FileUpload();
		d.add(upload);

		// Set the extensions allowed uploading
		upload.setAllowedExtensions("jpg,gif,png,log");

		// Set the max number of files allowed to upload
		upload.setMaxFiles(3);

		DefaultButton db = new DefaultButton("display hashes");
		d.add(db);
		db.setClicked(new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				showHashes(upload);
			}

		});
	}

	void showHashes(FileUpload upload) throws IOException {
		List<UploadItem> uil = upload.getFiles();
		if(uil.isEmpty()) {
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

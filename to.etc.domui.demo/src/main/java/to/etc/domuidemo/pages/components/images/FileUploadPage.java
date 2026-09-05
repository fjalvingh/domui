package to.etc.domuidemo.pages.components.images;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.upload.FileUpload2;
import to.etc.domui.component.upload.FileUploadMultiple;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.upload.UploadItem;

import java.util.List;

/**
 * The two upload controls: one file, and any number of them.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class FileUploadPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("File upload");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "File upload"));

		Div result = new Div("dm-tut-q");
		result.add("Choose a file; what arrived on the server appears here.");

		//-- One file, restricted to a few extensions, and told when it arrives.
		FileUpload2 single = new FileUpload2("png", "jpg", "gif");
		single.setMaxSize(4 * 1024 * 1024);               // Refused by the browser above this size
		single.setOnValueChanged(a -> describe(result, single.getValue()));

		//-- ...and any number of them, of any type.
		FileUploadMultiple several = new FileUploadMultiple();

		FormBuilder fb = new FormBuilder(cp);
		fb.label("An image, at most 4MB").control(single);
		fb.label("Any number of files").control(several);

		cp.add(new DefaultButton("What did I get?", a -> describeAll(result, several.getValue())));
		cp.add(result);

		cp.add(new Para().add("The upload starts the moment a file is chosen: the control posts it "
			+ "to the server in the background and rebuilds itself to show the name and a button to "
			+ "clear it. Nothing has to be submitted, and there is no progress bar - the screen "
			+ "blocks while the file is on its way."));

		cp.add(new Para().add("What you get is an UploadItem: the name the file had on the client, "
			+ "its content type, its size, and the File it was written to on the server. That file "
			+ "belongs to the conversation and is deleted when the page is left, so a page that "
			+ "wants to keep the contents has to copy them somewhere while it can."));

		cp.add(new Para().add("The extensions given to the constructor are passed to the browser, "
			+ "which uses them both to filter its file chooser and to refuse anything else, and the "
			+ "same for setMaxSize(). It is the browser doing the refusing, so a server that cares "
			+ "checks the UploadItem itself as well."));
	}

	private static void describe(Div where, UploadItem item) {
		where.removeAllChildren();
		if(null == item) {
			where.add("Nothing uploaded.");
			return;
		}
		where.add(one(item));
	}

	private static void describeAll(Div where, List<UploadItem> items) {
		where.removeAllChildren();
		if(null == items || items.isEmpty()) {
			where.add("Nothing uploaded.");
			return;
		}
		for(UploadItem item : items) {
			where.add(new Div().add(one(item)));
		}
	}

	private static String one(UploadItem item) {
		return item.getRemoteFileName() + " - " + item.getContentType() + ", " + item.getSize() + " bytes, "
			+ "written to " + item.getFile();
	}
}

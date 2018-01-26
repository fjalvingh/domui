package to.etc.domui.util.importers;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ButtonBar;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.upload.FileUpload2;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.asyncdialog.AsyncDialog;
import to.etc.domui.util.upload.UploadItem;
import to.etc.function.ConsumerEx;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 27-10-17.
 */
public class AbstractImportPage extends UrlPage {
	private FileUpload2 m_upload;

	private ButtonBar m_buttonBar;

	@Override public void createContent() throws Exception {
		add(createUpload());
		//
		//addUploadButton("Inlezen", );
		//addCancelButton(Msgs.BUNDLE.getString("ui.buttonbar.back"));
	}

	protected void addCancelButton(String string) {
		getButtonBar().addBackButton(string, FaIcon.faCircle);
	}

	protected <T extends AbstractImportTask> void addUploadButton(String text, T task, ConsumerEx<T> onComplete) throws Exception {
		DefaultButton b = getButtonBar().addButton(text, FaIcon.faUpload, v -> startUpload(task, onComplete));
		b.bind("disabled").to(this, "uploadDisabled");
	}

	private <T extends AbstractImportTask> void startUpload(T task, ConsumerEx<T> onComplete) {
		UploadItem value = m_upload.getValue();
		if(null == value)
			return;
		task.setInputFile(value.getFile());
		AsyncDialog.runInDialog(this, task, "Import", true, onComplete);
	}

	public boolean isUploadDisabled() {
		return m_upload == null || m_upload.getValue() == null;
	}

	protected ButtonBar getButtonBar() {
		ButtonBar buttonBar = m_buttonBar;
		if(null == buttonBar) {
			buttonBar = m_buttonBar = new ButtonBar();
			add(buttonBar);
		}
		return buttonBar;
	}

	protected FileUpload2 createUpload() {
		FileUpload2 fu = m_upload = new FileUpload2("xls", "xlsx");
		return fu;
	}
}

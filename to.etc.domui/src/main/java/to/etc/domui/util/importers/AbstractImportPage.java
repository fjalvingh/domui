package to.etc.domui.util.importers;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.delayed.IProgress;
import to.etc.domui.component.layout.ButtonBar;
import to.etc.domui.component.layout.Dialog;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.upload.FileUpload;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.AsyncDialogTask;
import to.etc.domui.util.upload.UploadItem;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 27-10-17.
 */
public class AbstractImportPage<R> extends UrlPage {
	private FileUpload m_upload;

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

	protected void addUploadButton(String text, IImportTask<R> task) throws Exception {
		DefaultButton b = getButtonBar().addButton(text, FaIcon.faUpload, v -> startUpload(task));
		b.bind("disabled").to(this, "uploadDisabled");
	}

	private void startUpload(IImportTask<R> handler) {
		UploadItem value = m_upload.getValue();
		if(null == value)
			return;
		ImporterTask<R> task = new ImporterTask<>(handler, value.getFile());
		AsyncDialogTask.runInDialog(this, task, "Import", true, true);
	}

	public boolean isUploadDisabled() {
		return m_upload == null || m_upload.getFiles().size() == 0;
	}

	protected ButtonBar getButtonBar() {
		ButtonBar buttonBar = m_buttonBar;
		if(null == buttonBar) {
			buttonBar = m_buttonBar = new ButtonBar();
			add(buttonBar);
		}
		return buttonBar;
	}

	protected FileUpload createUpload() {
		FileUpload fu = m_upload = new FileUpload("xls", "xlsx");
		return fu;
	}

	private class ImporterTask<R> extends AsyncDialogTask {
		private final IImportTask<R> m_handler;

		private File m_input;

		private R m_result;

		public ImporterTask(IImportTask<R> handler, File input) {
			m_handler = handler;
			m_input = input;
		}

		@Override protected void execute(@Nonnull IProgress progress) throws Exception {
			m_result = m_handler.execute(m_input, progress);
		}

		@Override public void onCompleted(NodeContainer node) {
			m_handler.onComplete(node, m_result);
		}

		@Override protected void onError(Dialog dlg, boolean cancelled, @Nonnull Exception errorException) {
			errorException.printStackTrace();
			super.onError(dlg, cancelled, errorException);
		}
	}
}

package to.etc.domui.util.importers;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ButtonBar;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.upload.FileUpload;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.Msgs;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 27-10-17.
 */
public class AbstractImportPage extends UrlPage {
	private FileUpload m_upload;

	private ButtonBar m_buttonBar;

	@Override public void createContent() throws Exception {
		add(createUpload());

		addUploadButton("Inlezen");
		addCancelButton(Msgs.BUNDLE.getString(Msgs.BTN_CANCEL));
	}

	private void addCancelButton(String string) {
		getButtonBar().addBackButton(string, FaIcon.faCircle);
	}

	private void addUploadButton(String text) throws Exception {
		DefaultButton b = getButtonBar().addButton(text, FaIcon.faUpload, v -> startUpload());
		b.bind("disabled").to(this, "uploadDisabled");
	}

	private void startUpload() {



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
}

package to.etc.domui.log;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.log.*;
import to.etc.webapp.nls.*;

public class LoggerConfigPage extends UrlPage implements IUserInputModifiedFence {
	static final BundleRef BUNDLE = Msgs.BUNDLE;

	private ButtonBar m_buttonBar;

	private DefaultButton m_saveButton;

	private DefaultButton m_cancelButton;

	private LoggerConfigPartBase< ? >[] m_tabs;

	private TabPanel m_tabPnl;

	private boolean m_modified;

	private Label m_notSavedInfo;

	@Override
	public void createContent() throws Exception {
		super.createContent();
		createButtonBar();
		createButtons();

		m_tabPnl = new TabPanel();
		add(m_tabPnl);
		m_tabs = new LoggerConfigPartBase< ? >[5];
		m_tabs[0] = new LoggerOutputsConfigPart();
		m_tabPnl.add(m_tabs[0], "Outputs");
		m_tabPnl.add(createDisabledLogPnl(), "Disabled/Enabled");
		m_tabPnl.add(createLevelsPnl(), "Levels");
		m_tabPnl.add(createMarkersPnl(), "Markers");
		m_tabPnl.add(createSessionFilterPnl(), "Session");
	}

	protected void createButtonBar() {
		add(getButtonBar());
	}

	public ButtonBar getButtonBar() {
		if(m_buttonBar == null) {
			m_buttonBar = new ButtonBar();
		}
		return m_buttonBar;
	}

	protected void createButtons() throws Exception {
		createCommitButton();
		createCancelButton();
	}

	protected void createCommitButton() {
		m_saveButton = getButtonBar().addButton(BUNDLE.getString(Msgs.EDLG_OKAY), Msgs.BTN_SAVE, new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton b) throws Exception {
				save();
			}
		});
		m_saveButton.setTestID("saveButton");
		//hide by default, it would become visible if modifications on page are detected
		m_saveButton.setDisplay(DisplayType.NONE);
	}

	protected void createCancelButton() {
		m_cancelButton = getButtonBar().addButton(BUNDLE.getString(Msgs.EDLG_CANCEL), Msgs.BTN_CANCEL, new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton b) throws Exception {
				reloadPageData();
			}
		});
		//hide by default, it would become visible if modifications on page are detected
		m_cancelButton.setDisplay(DisplayType.NONE);
	}

	protected void onAfterSave() throws Exception {
		reloadPageData();
		MessageFlare.display(this, MsgType.INFO, $("data.saved"));
	}

	protected void save() throws Exception {
		if(validateData()) {
			onSave();
			onAfterSave();
		}
	}

	private boolean validateData() throws Exception {
		int firstError = -1;
		for(int i = 0; i < m_tabs.length; i++) {
			if(m_tabs[i] != null) {
				if(!m_tabs[i].validateChanges()) {
					if(firstError == -1) {
						firstError = i;
					}
				}
			}
		}
		if(firstError > -1) {
			m_tabPnl.setCurrentTab(firstError);
			return false;
		}
		return true;
	}

	private boolean onSave() throws Exception {
		int firstError = -1;
		for(int i = 0; i < m_tabs.length; i++) {
			if(m_tabs[i] != null) {
				if(!m_tabs[i].saveChanges()) {
					if(firstError == -1) {
						firstError = i;
					}
				}
			}
		}
		if(firstError > -1) {
			m_tabPnl.setCurrentTab(firstError);
			return false;
		} else {
			MyLoggerFactory.save();
		}
		return true;
	}

	protected void reloadPageData() throws Exception {
		UIGoto.reload();
	}

	private NodeBase createDisabledLogPnl() {
		Div pnl = new Div();
		return pnl;
	}

	private NodeBase createLevelsPnl() {
		Div pnl = new Div();
		return pnl;
	}

	private NodeBase createMarkersPnl() {
		Div pnl = new Div();
		return pnl;
	}

	private NodeBase createSessionFilterPnl() {
		Div pnl = new Div();
		return pnl;
	}

	@Override
	public boolean isModified() {
		return m_modified;
	}

	@Override
	public void setModified(boolean modified) {
		m_modified = modified;
	}

	@Override
	public boolean isFinalUserInputModifiedFence() {
		return true;
	}

	@Override
	public void onModifyFlagRaised() {
		if(m_saveButton != null) {
			m_saveButton.setDisplay(DisplayType.INLINE);
		}
		if(m_cancelButton != null) {
			m_cancelButton.setDisplay(DisplayType.INLINE);
		}
		if(m_notSavedInfo == null) {
			addNotSavedWarning();
		}
	}

	private void addNotSavedWarning() {
		if(m_buttonBar != null && m_notSavedInfo == null) {
			m_notSavedInfo = new Label($("data.modified"));
			m_notSavedInfo.setFontStyle(FontStyle.ITALIC);
			m_notSavedInfo.setColor(UIControlUtil.getRgbHex(java.awt.Color.RED, true));
			m_buttonBar.appendAfterMe(m_notSavedInfo);
		}
	}
}

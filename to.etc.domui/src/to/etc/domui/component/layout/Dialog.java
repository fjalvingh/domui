package to.etc.domui.component.layout;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * A base class for retrieving any kind of input from a user.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 18, 2011
 */
public class Dialog extends Window {
	/** The button bar for the dialog. */
	private ButtonBar m_buttonBar;

	public Dialog(boolean modal, boolean resizable, int width, int height, String title) {
		super(modal, resizable, width, height, title);
	}

	public Dialog(boolean modal, boolean resizable, String title) {
		super(modal, resizable, title);
	}

	public Dialog(boolean resizable, String title) {
		super(resizable, title);
	}

	public Dialog(String title) {
		super(title);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Services.											*/
	/*--------------------------------------------------------------*/
	/**
	 * Define the button bar to be on the bottom. Must be called before the button bar
	 * is created or used.
	 * @param ontop
	 */
	public void setButtonsOnBottom(boolean onbottom) {
		if(m_buttonBar != null)
			throw new IllegalStateException("The button bar has already been set.");
		createButtonBar(onbottom);
	}

	/**
	 * Create the button bar if it does not already exists.
	 * @param ontop
	 */
	private void createButtonBar(boolean onbottom) {
		if(m_buttonBar != null)
			return;
		m_buttonBar = new ButtonBar();
		Div area = onbottom ? getBottomContent() : getTopContent();
		if(area.getHeight() == null)
			area.setHeight("30px");
		area.add(m_buttonBar);
	}

	/**
	 * Get the control's button bar. If it does not already exists it will be created and
	 * added to the top content area.
	 * @return
	 */
	public ButtonBar getButtonBar() {
		if(m_buttonBar == null)
			createButtonBar(false);
		return m_buttonBar;
	}

	protected void createCancelButton() {
		DefaultButton b;
		b = getButtonBar().addButton(Msgs.EDLG_CANCEL, Msgs.BTN_CANCEL, new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				buttonCancel();
			}
		});
		b.setTestID("cancelButton");
	}

	protected void createSaveButton() {
		DefaultButton b = getButtonBar().addButton(Msgs.EDLG_OKAY, Msgs.BTN_SAVE, new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				buttonSave();
			}
		});
		b.setTestID("saveButton");
	}

	protected void buttonCancel() throws Exception {
		closePressed();
	}

	protected void buttonSave() throws Exception {}


}

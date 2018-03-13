/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.layout;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.themes.*;
import to.etc.domui.util.*;

import javax.annotation.*;

/**
 * A base class for retrieving any kind of input from a user.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 18, 2011
 */
public class Dialog extends Window {
	/** Close reason {@link IWindowClosed#closed(String)}: the save button was pressed and onSave() did not die. */
	static public final String RSN_SAVE = "save";

	/** The button bar for the dialog. */
	private ButtonBar m_buttonBar;

	@Nullable
	private IExecute m_onSave;

	public Dialog() {}

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

	public Dialog(int width, int height, String title) {
		super(width, height, title);
	}

	@Nonnull
	@Override
	public Dialog size(int width, int height) {
		super.size(width, height);
		return this;
	}

	@Nonnull
	@Override
	public Dialog resizable() {
		super.resizable();
		return this;
	}

	@Nonnull
	@Override
	public Dialog modal(boolean yes) {
		super.modal(yes);
		return this;
	}

	@Nonnull
	@Override
	public Dialog modal() {
		super.modal();
		return this;
	}

	@Nonnull
	@Override
	public Dialog title(@Nonnull String set) {
		super.title(set);
		return this;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Services.											*/
	/*--------------------------------------------------------------*/
	/**
	 * Define the button bar to be on the bottom. Must be called before the button bar
	 * is created or used.
	 * @param onbottom
	 */
	public void setButtonsOnBottom(boolean onbottom) {
		if(m_buttonBar != null)
			throw new IllegalStateException("The button bar has already been set.");
		createButtonBar(onbottom);
	}

	/**
	 * Create the button bar if it does not already exists.
	 * @param onbottom
	 */
	private void createButtonBar(boolean onbottom) {
		if(m_buttonBar != null)
			return;
		m_buttonBar = new ButtonBar();
		Div area = onbottom ? getBottomContent() : getTopContent();
//		if(area.getHeight() == null)
//			area.setHeight("34px");
		area.add(m_buttonBar);
	}

	/**
	 * Can be overridden to add extra buttons to the button bar where needed - this default
	 * implementation adds the save and cancel buttons. If you override you should decide on
	 * their fate yourself!
	 *
	 * @throws Exception
	 */
	protected void createButtons() throws Exception {
		createSaveButton();
		createCancelButton();
	}


	/**
	 * Get the control's button bar. If it does not already exists it will be created and
	 * added to the top content area.
	 * @return
	 */
	public IButtonBar getButtonBar() {
		if(m_buttonBar == null)
			createButtonBar(true);
		return m_buttonBar;
	}

	protected void createCancelButton() {
		createCancelButton(Msgs.BUNDLE.getString(Msgs.EDLG_CANCEL));
	}

	protected void createCancelButton(@Nonnull String text) {
		createCancelButton(text, Theme.BTN_CANCEL);
	}

	protected void createCancelButton(@Nonnull String text, @Nonnull String image) {
		DefaultButton b;
		b = getButtonBar().addButton(text, image, new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				buttonCancel();
			}
		});
		b.setTestID("cancelButton");
	}

	@Nonnull
	protected DefaultButton createSaveButton() {
		return createSaveButton(Msgs.BUNDLE.getString(Msgs.EDLG_OKAY), Msgs.BTN_SAVE);
	}

	@Nonnull
	protected DefaultButton createSaveButton(String caption, String iconUrl) {
		DefaultButton b = getButtonBar().addButton(caption, iconUrl, new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				buttonSave();
			}
		});
		b.setTestID("saveButton");
		return b;
	}

	/**
	 * Default handler for the cancel button: this will send the "CLOSE pressed" event ({@link FloatingDiv#RSN_CLOSE}).
	 * @throws Exception
	 */
	protected void buttonCancel() throws Exception {
		closePressed();
	}

	/**
	 * The default save() implementation will call onValidate(), onSave(), then it will
	 * send a {@link #RSN_SAVE} close event. If the close event itself fails with exception
	 * the code will ask onCloseException() to see if we need to throw the exception or if
	 * it gets handled and shown as an error message or something like that.
	 * @throws Exception
	 */
	protected void buttonSave() throws Exception {
		clearGlobalMessage();
		if(bindErrors())
			return;
		if(!onSaveBind())
			return;
		if(!onValidate())
			return;
		if(!onSave())
			return;

		/*
		 * onSave() was successful. We will send the close reason SAVE, but if it fails with exception we'll
		 * remain in this dialog.
		 */
		try {
			IExecute onSave = getOnSave();
			if(null != onSave) {
				onSave.execute();
			}
			callCloseHandler(RSN_SAVE);
		} catch(Exception x) {
			if(!onCloseException(x))
				throw x;
		}
		close();
	}

	/**
	 * First part of save button handling: this should be overridden to move the data
	 * in the dialog's controls into their model.
	 *
	 * @return
	 * @throws Exception
	 */
	protected boolean onSaveBind() throws Exception {
		return true;
	}

	/**
	 * Second part of the save button handling: this should validate all input present
	 * in the model and any non-model controls.
	 *
	 * @return
	 * @throws Exception
	 */
	protected boolean onValidate() throws Exception {
		return true;
	}

	/**
	 * If sending the SAVE message fails with exception it can be handled here. If the
	 * exception is handled here it must return true, else it should return false in
	 * which case the exception will pass through to toplevel. The default implementation
	 * returns false and does nothing.
	 * @param x
	 * @return
	 */
	protected boolean onCloseException(Exception x) throws Exception {
		return false;
	}

	/**
	 * Override to validate data before the close event is sent and the window is closed.
	 */
	protected boolean onSave() throws Exception {
		return true;
	}

	@Nullable
	public IExecute getOnSave() {
		return m_onSave;
	}

	public void setOnSave(@Nullable IExecute onSave) {
		m_onSave = onSave;
	}
}

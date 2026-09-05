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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.dom.html.Div;
import to.etc.domui.themes.Theme;
import to.etc.function.IExecute;
import to.etc.domui.util.Msgs;

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
	@Nullable
	private ButtonBar2 m_buttonBar;

	@Nullable
	private IExecute m_onSave;

	/** Do not show default icons on buttons, sigh */
	private boolean m_noIcons;

	/** Global setting to disable all icons */
	private volatile static boolean m_allNoIcons;

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

	public static void setAllNoIcons(boolean allNoIcons) {
		m_allNoIcons = allNoIcons;
	}

	public Dialog noIcons(boolean noIcons) {
		m_noIcons = noIcons;
		return this;
	}

	public Dialog noIcons() {
		m_noIcons = true;
		return this;
	}

	@NonNull
	@Override
	public Dialog size(int width, int height) {
		super.size(width, height);
		return this;
	}

	@NonNull
	@Override
	public Dialog resizable() {
		super.resizable();
		return this;
	}

	@NonNull
	@Override
	public Dialog modal(boolean yes) {
		super.modal(yes);
		return this;
	}

	@NonNull
	@Override
	public Dialog modal() {
		super.modal();
		return this;
	}

	@NonNull
	@Override
	public Dialog title(@NonNull String set) {
		super.title(set);
		return this;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Services.											*/
	/*--------------------------------------------------------------*/
	/**
	 * Define the button bar to be on the bottom. Must be called before the button bar
	 * is created or used.
	 */
	public void setButtonsOnBottom(boolean onbottom) {
		if(m_buttonBar != null)
			throw new IllegalStateException("The button bar has already been set.");
		createButtonBar(onbottom);
	}

	/**
	 * Create the button bar if it does not already exists.
	 */
	private void createButtonBar(boolean onbottom) {
		if(m_buttonBar != null)
			return;
		ButtonBar2 bb = m_buttonBar = new ButtonBar2();
		Div area = onbottom ? getBottomContent() : getTopContent();
		area.add(bb);
	}

	/**
	 * Can be overridden to add extra buttons to the button bar where needed - this default
	 * implementation adds the save and cancel buttons. If you override you should decide on
	 * their fate yourself!
	 */
	protected void createButtons() throws Exception {
		createSaveButton();
		createCancelButton();
	}


	/**
	 * Get the control's button bar. If it does not already exists it will be created and
	 * added to the top content area.
	 */
	public IButtonBar getButtonBar() {
		ButtonBar2 bb = m_buttonBar;
		if(bb == null) {
			createButtonBar(true);
			bb = m_buttonBar;
			if(null == bb)
				throw new IllegalStateException("The button bar was not created");
		}
		return bb;
	}

	protected void createCancelButton() {
		createCancelButton(Msgs.BUNDLE.getString(Msgs.EDLG_CANCEL));
	}

	protected void createCancelButton(@NonNull String text) {
		createCancelButton(text, Theme.BTN_CANCEL);
	}

	protected void createCancelButton(@NonNull String text, @Nullable IIconRef image) {
		if(isNoIcons())
			image = null;
		DefaultButton b = getButtonBar().addButton(text, image, clickednode -> buttonCancel());
		b.setTestID("cancelButton");
		b.addCssClass("is-primary is-outlined");
	}

	private boolean isNoIcons() {
		return m_noIcons || m_allNoIcons;
	}

	@NonNull
	protected DefaultButton createSaveButton() {
		return createSaveButton(Msgs.BUNDLE.getString(Msgs.EDLG_OKAY), Theme.BTN_SAVE);
	}

	@NonNull
	protected DefaultButton createSaveButton(String caption, IIconRef iconUrl) {
		if(isNoIcons())
			iconUrl = null;
		DefaultButton b = getButtonBar().addButton(caption, iconUrl, clickednode -> buttonSave());
		b.setTestID("saveButton");
		b.addCssClass("is-primary");
		return b;
	}

	/**
	 * Default handler for the cancel button: this will send the "CLOSE pressed" event ({@link FloatingDiv#RSN_CLOSE}).
	 */
	protected void buttonCancel() throws Exception {
		closePressed();
	}

	/**
	 * The default save() implementation will call onValidate(), onSave(), then it will
	 * send a {@link #RSN_SAVE} close event. If the close event itself fails with exception
	 * the code will ask onCloseException() to see if we need to throw the exception or if
	 * it gets handled and shown as an error message or something like that.
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

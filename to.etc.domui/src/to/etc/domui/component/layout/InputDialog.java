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

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * A Dialog that is used to inout some data of type &lt;T&gt;, using input control of type &lt;C&gt;.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 30 Aug 2011
 */
public class InputDialog<T, C extends NodeBase & IControl<T>> extends Dialog {
	private C m_inputControl;

	private T m_instance;

	public InputDialog(C inputControl, int width, int height, String title) {
		super(width, height, title);
		m_inputControl = inputControl;
	}

	public InputDialog(C inputControl, boolean modal, boolean resizable, int width, int height, String title) {
		super(modal, resizable, width, height, title);
		m_inputControl = inputControl;
	}

	public InputDialog(C inputControl, boolean modal, boolean resizable, String title) {
		super(modal, resizable, title);
		m_inputControl = inputControl;
	}

	public InputDialog(C inputControl, boolean resizable, String title) {
		super(resizable, title);
		m_inputControl = inputControl;
	}

	public InputDialog(C inputControl, String title) {
		super(title);
		m_inputControl = inputControl;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Creating the UI core.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Overridden to create the initial button bar with the "save" button and the
	 * "cancel" buttons in place.
	 *
	 * @see to.etc.domui.component.layout.Window#createFrame()
	 */
	@OverridingMethodsMustInvokeSuper
	@Override
	protected void createFrame() throws Exception {
		super.createFrame();
		createButtons();
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
	 * This uses the bindings created by createContent to move data in controls back
	 * to the instance (the model). If more than just those bindings are needed you
	 * need to override this <b>and call super too</b>.
	 *
	 * @see to.etc.domui.component.layout.Dialog#onSaveBind()
	 */
	@OverridingMethodsMustInvokeSuper
	@Override
	protected boolean onSaveBind() throws Exception {
		//-- Move all bound data to the actual instance
		m_instance = m_inputControl.getValue();
		return true;
	}

	/**
	 * Can be overridden to add extra validation logic - instance is provided using getInstance method.
	 *
	 * @see to.etc.domui.component.layout.Dialog#onSaveBind()
	 */
	@OverridingMethodsMustInvokeSuper
	@Override
	protected boolean onValidate() throws Exception {
		getInstance();
		return true;
	}

	/**
	 * The data instance being edited.
	 * @return
	 */
	protected T getInstance() {
		return m_instance;
	}

	public C getInputControl() {
		return m_inputControl;
	}
}

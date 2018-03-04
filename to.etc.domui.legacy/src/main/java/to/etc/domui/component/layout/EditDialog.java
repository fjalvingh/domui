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

import to.etc.domui.component.controlfactory.ModelBindings;
import to.etc.domui.component.form.HorizontalFormBuilder;
import to.etc.domui.component.form.TabularFormBuilder;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * A Dialog that is used to edit some instance of data &lt;T&gt;, containing methods to handle all
 * parts of the editing process.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 19, 2011
 */
public class EditDialog<T> extends Dialog {
	private T m_instance;

	/** If bindings are used this is the global instance used by the common code. */
	private ModelBindings m_bindings = new ModelBindings();

	private TabularFormBuilder m_tfb;

	private HorizontalFormBuilder m_hfb;

	public EditDialog(boolean modal, boolean resizable, int width, int height, String title) {
		super(modal, resizable, width, height, title);
	}

	public EditDialog(boolean modal, boolean resizable, String title) {
		super(modal, resizable, title);
	}

	public EditDialog(boolean resizable, String title) {
		super(resizable, title);
	}

	public EditDialog(String title) {
		super(title);
	}

	public EditDialog(int width, int height, String title) {
		super(width, height, title);
	}

	public EditDialog(T instance, int width, int height, String title) {
		super(width, height, title);
		m_instance = instance;
	}

	public EditDialog(T instance, boolean modal, boolean resizable, int width, int height, String title) {
		super(modal, resizable, width, height, title);
		m_instance = instance;
	}

	public EditDialog(T instance, boolean modal, boolean resizable, String title) {
		super(modal, resizable, title);
		m_instance = instance;
	}

	public EditDialog(T instance, boolean resizable, String title) {
		super(resizable, title);
		m_instance = instance;
	}

	public EditDialog(T instance, String title) {
		super(title);
		m_instance = instance;
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
	@Override
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
		getBindings().moveControlToModel();
		return true;
	}

	/**
	 * Get a tabular form builder using the shared bindings.
	 * @return
	 * @throws Exception
	 */
	public TabularFormBuilder getTabularFormBuilder() throws Exception {
		if(m_tfb == null) {
			m_tfb = new TabularFormBuilder(getInstance());
			m_tfb.setBindings(getBindings()); // Use the dialog's shared bindings.
		}
		return m_tfb;
	}

	/**
	 * Get a horizontal form builder using the shared bindings.
	 * @return
	 */
	public HorizontalFormBuilder getHorizontalFormBuilder() {
		if(m_hfb == null) {
			m_hfb = new HorizontalFormBuilder(getInstance());
			m_hfb.setBindings(getBindings()); // Use the dialog's shared bindings.
		}
		return m_hfb;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Info properties.									*/
	/*--------------------------------------------------------------*/
	/**
	 * The data instance being edited.
	 * @return
	 */
	public T getInstance() {
		if(null == m_instance)
			throw new IllegalStateException("The instance-to-edit has not been set.");
		return m_instance;
	}

	/**
	 * Set the data to change.
	 * @param instance
	 */
	public void setInstance(@Nonnull T instance) {
		if(null == instance)
			throw new IllegalArgumentException("Instance cannot be null!!");
		m_instance = instance;
	}

	/**
	 * The bindings as returned by the createEditable call.
	 * @return
	 */
	@Nonnull
	public ModelBindings getBindings() {
		return m_bindings;
	}
}

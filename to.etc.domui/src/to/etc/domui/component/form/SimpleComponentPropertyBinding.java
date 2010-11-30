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
package to.etc.domui.component.form;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class SimpleComponentPropertyBinding<T> implements IModelBinding, IControl<T> {
	final IControl<T> m_control;

	private PropertyMetaModel m_propertyMeta;

	private IReadOnlyModel< ? > m_model;

	public SimpleComponentPropertyBinding(IReadOnlyModel< ? > model, PropertyMetaModel propertyMeta, IInputNode<T> control) {
		m_model = model;
		m_propertyMeta = propertyMeta;
		m_control = control;
	}

	@Override
	public void moveControlToModel() throws Exception {
		Object val = m_control.getValue();
		Object base = m_model.getValue();
		IValueAccessor<Object> a = (IValueAccessor<Object>) m_propertyMeta.getAccessor();
		a.setValue(base, val);
	}

	@Override
	public void moveModelToControl() throws Exception {
		Object base = m_model.getValue();
		IValueAccessor< ? > vac = m_propertyMeta.getAccessor();
		if(vac == null)
			throw new IllegalStateException("Null IValueAccessor<T> returned by PropertyMeta " + m_propertyMeta);
		T pval = (T) m_propertyMeta.getAccessor().getValue(base);
		m_control.setValue(pval);
	}

	@Override
	public void setControlsEnabled(boolean on) {
		m_control.setReadOnly(!on);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IControl interface									*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getValue() {
		return m_control.getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(T value) {
		m_control.setValue(value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IControl#getValueSafe()
	 */
	@Override
	public T getValueSafe() {
		return m_control.getValueSafe();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IHasChangeListener#getOnValueChanged()
	 */
	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return m_control.getOnValueChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOnValueChanged(IValueChanged< ? > listener) {
		m_control.setOnValueChanged(listener);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IControl#hasError()
	 */
	@Override
	public boolean hasError() {
		return m_control.hasError();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IControl#isDisabled()
	 */
	@Override
	public boolean isDisabled() {
		return m_control.isDisabled();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IActionControl#setDisabled(boolean)
	 */
	@Override
	public void setDisabled(boolean d) {
		m_control.setDisabled(d);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IControl#isMandatory()
	 */
	@Override
	public boolean isMandatory() {
		return m_control.isMandatory();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IControl#setMandatory(boolean)
	 */
	@Override
	public void setMandatory(boolean ro) {
		m_control.setMandatory(ro);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IControl#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return m_control.isReadOnly();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IControl#setReadOnly(boolean)
	 */
	@Override
	public void setReadOnly(boolean ro) {
		m_control.setReadOnly(ro);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTestID(String testID) {
		m_control.setTestID(testID);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	INodeErrorDelegate interface.						*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.dom.errors.INodeErrorDelegate#clearMessage()
	 */
	@Override
	public void clearMessage() {
		m_control.clearMessage();
	}

	@Override
	public UIMessage getMessage() {
		return m_control.getMessage();
	}

	@Override
	public UIMessage setMessage(UIMessage m) {
		return m_control.setMessage(m);
	}

	@Override
	public String getErrorLocation() {
		return m_control.getErrorLocation();
	}

	@Override
	public void setErrorLocation(String errorLocation) {
		m_control.setErrorLocation(errorLocation);
	}
}

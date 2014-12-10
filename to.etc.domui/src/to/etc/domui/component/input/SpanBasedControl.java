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
package to.etc.domui.component.input;

import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

import javax.annotation.*;
import java.util.*;

/**
 * DEPRECATED - one big bag full of problems.
 * Base class to implement an input control using a span as the baae. This implements
 * all basic code for an input control like the IControl interface.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 13, 2008
 */
@Deprecated
abstract public class SpanBasedControl<T> extends Span implements IControl<T> {
	private boolean m_mandatory;

	private boolean m_readOnly;

	private boolean m_disabled;

	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	@Override
	public void setMandatory(boolean mandatory) {
		m_mandatory = mandatory;
	}

	@Override
	public boolean isReadOnly() {
		return m_readOnly;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		if(m_readOnly == readOnly)
			return;
		m_readOnly = readOnly;
		forceRebuild();
	}

	@Override
	final public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	final public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		m_disabled = disabled;
		forceRebuild();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	IControl implementation.							*/
	/*--------------------------------------------------------------*/
	private T m_value;

	private IValueChanged< ? > m_onValueChanged;

	protected T getRawValue() {
		return m_value;
	}

	protected void setRawValue(T v) {
		m_value = v;
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#getValue()
	 */
	@Override
	public T getValue() {
		if(m_value == null && isMandatory()) {
			setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.MANDATORY));
			throw new ValidationException(Msgs.MANDATORY);
		}
		return m_value;
	}

	/**
	 * Sets a new value. This re-renders the entire control's contents always.
	 *
	 * @see to.etc.domui.dom.html.IControl#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(@Nullable T v) {
		if(DomUtil.isEqual(v, m_value))
			return;
		m_value = v;
		forceRebuild();
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#getValueSafe()
	 */
	@Override
	public T getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#hasError()
	 */
	@Override
	public boolean hasError() {
		getValueSafe();
		return super.hasError();
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#getOnValueChanged()
	 */
	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return m_onValueChanged;
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#setOnValueChanged(to.etc.domui.dom.html.IValueChanged)
	 */
	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IBindable interface.								*/
	/*--------------------------------------------------------------*/

	@Nullable
	private List<SimpleBinder> m_bindingList;

	@Override
	public @Nonnull IBinder bind() {
		return bind("value");
	}

	@Override
	@Nonnull
	public IBinder bind(@Nonnull String componentProperty) {
		List<SimpleBinder> list = m_bindingList;
		if(list == null)
			list = m_bindingList = new ArrayList<SimpleBinder>(1);
		SimpleBinder binder = new SimpleBinder(this, componentProperty);
		list.add(binder);
		return binder;
	}
}

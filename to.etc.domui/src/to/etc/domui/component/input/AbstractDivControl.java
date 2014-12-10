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

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;

import javax.annotation.*;
import java.util.*;

/**
 * Abstract base class for a control that is implemented on top of a DIV. This handles most basic actions required of
 * all controls.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 19, 2010
 */
abstract public class AbstractDivControl<T> extends Div implements IControl<T> {
	private boolean m_readOnly;

	private boolean m_disabled;

	private boolean m_mandatory;

	private IValueChanged< ? > m_valueChanged;

	private T m_value;

	@Override
	abstract public void createContent() throws Exception;

	@Override
	public T getValueSafe() {
		try {
			return getValue();
		} catch(Exception x) {
			return null;
		}
	}

	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	@Override
	public void setMandatory(boolean m) {
		if(m_mandatory == m)
			return;
		m_mandatory = m;
		mandatoryChanged();
	}

	protected void mandatoryChanged() {
		forceRebuild();
	}

	@Override
	public boolean isReadOnly() {
		return m_readOnly;
	}

	@Override
	public void setReadOnly(boolean ro) {
		if(ro == m_readOnly)
			return;
		m_readOnly = ro;
		readOnlyChanged();
	}

	protected void readOnlyChanged() {
		forceRebuild();
	}

	@Override
	public T getValue() {
		validate();
		return m_value;
	}

	@Nullable
	protected T internalGetValue() {
		return m_value;
	}

	protected void validate() {
	}

	@Override
	public void setValue(@Nullable T v) {
		if(MetaManager.areObjectsEqual(v, m_value))
			return;
		m_value = v;
		forceRebuild();
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean d) {
		if(m_disabled == d)
			return;
		m_disabled = d;
		disabledChanged();
	}

	protected void disabledChanged() {
		forceRebuild();
	}

	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return m_valueChanged;
	}

	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_valueChanged = onValueChanged;
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

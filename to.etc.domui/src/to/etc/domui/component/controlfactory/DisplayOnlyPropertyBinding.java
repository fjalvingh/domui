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
package to.etc.domui.component.controlfactory;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * A property binding specific for display-only controls. This binding will
 * abort any attempt to put the associated displayonly field into a state
 * that is not allowed (like setting it to editable, enabled or not-readonly).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 15, 2010
 */
public class DisplayOnlyPropertyBinding<T> implements IModelBinding {
	final IDisplayControl<T> m_control;

	public IDisplayControl<T> getControl() {
		return m_control;
	}

	private PropertyMetaModel<T> m_propertyMeta;

	public PropertyMetaModel<T> getPropertyMeta() {
		return m_propertyMeta;
	}

	private IReadOnlyModel< ? > m_model;

	public IReadOnlyModel< ? > getModel() {
		return m_model;
	}

	public DisplayOnlyPropertyBinding(IReadOnlyModel< ? > model, PropertyMetaModel<T> propertyMeta, IDisplayControl<T> control) {
		m_model = model;
		m_propertyMeta = propertyMeta;
		m_control = control;
	}

	@Override
	public void moveControlToModel() throws Exception {
		//		if(m_propertyMeta.getReadOnly() == YesNoType.YES)
		//			return;

		//		T val = m_control.getValue();
		//		Object base = m_model.getValue();
		//		m_propertyMeta.setValue(base, val);
	}

	@Override
	public void moveModelToControl() throws Exception {
		Object base = m_model.getValue();
		IValueAccessor< ? > vac = m_propertyMeta;
		if(vac == null)
			throw new IllegalStateException("Null IValueAccessor<T> returned by PropertyMeta " + m_propertyMeta);
		T pval = m_propertyMeta.getValue(base);
		m_control.setValue(pval);
	}

	@Override
	public void setControlsEnabled(boolean on) {
	}
}

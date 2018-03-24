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
package to.etc.domui.component.lookup;

import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.NodeBase;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;

/**
 * ILookupControlInstance which uses a generic input control to create an equals criterion
 * on the input value, provided it is not null.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 19, 2009
 */
@Deprecated
final public class EqLookupControlImpl<T> extends BaseAbstractLookupControlImpl<T> {
	final private IControl<T> m_control;

	final private String m_property;

	public EqLookupControlImpl(String property, IControl<T> n) {
		super((NodeBase) n);
		m_control = n;
		m_property = property;
	}

	@Override
	@Nonnull
	public AppendCriteriaResult appendCriteria(@Nonnull QCriteria<?> crit) throws Exception {
		Object value = m_control.getValue();
		if(value != null) {
			crit.eq(m_property, value);
			return AppendCriteriaResult.VALID;
		}
		return AppendCriteriaResult.EMPTY; // Okay but no data
	}

	@Override
	public T getValue() {
		return m_control.getValue();
	}

	@Override
	public void setValue(T value) throws Exception {
		m_control.setValue(value);
	}
}

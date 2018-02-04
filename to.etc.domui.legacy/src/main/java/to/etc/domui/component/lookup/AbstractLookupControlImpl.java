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

import to.etc.domui.dom.html.*;

/**
 * Abstract default implementation of a ILookupControlInstance. This merely keeps the
 * list of Nodes representing the visual representation; concrete implementations must
 * handle the other parts.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 31, 2009
 */
@Deprecated
abstract public class AbstractLookupControlImpl extends BaseAbstractLookupControlImpl<Object> implements ILookupControlInstance<Object> {

	public AbstractLookupControlImpl(NodeBase... nodes) {
		super(nodes);
	}

	@Override
	public Object getValue() {
		throw new IllegalStateException("This is not implemented for this control. You must do this before turning on the save filter possibility.");
	}

	@Override
	public void setValue(Object value) {
		throw new IllegalStateException("This is not implemented for this control. You must do this before turning on the save filter possibility.");
	}
}

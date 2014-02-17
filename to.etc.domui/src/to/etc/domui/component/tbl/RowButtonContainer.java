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
package to.etc.domui.component.tbl;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;

public class RowButtonContainer extends ButtonMakerBase {
	private TD m_into;

	private int m_count;

	public RowButtonContainer() {}

	public RowButtonContainer(TD into) {
		m_into = into;
	}

	public TD getInto() {
		return m_into;
	}

	public void setContainer(TD nc) {
		m_into = nc;
		m_count = 0;
	}

	@Override
	protected void addButton(NodeBase b) {
		if(m_count++ > 0)
			m_into.add(" ");
		m_into.add(b);
	}

	public void add(NodeBase other) {
		m_into.add(other);
	}
}

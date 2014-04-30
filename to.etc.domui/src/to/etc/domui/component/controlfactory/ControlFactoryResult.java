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

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * Represents the result of a call to createControl.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
public class ControlFactoryResult {
	/** The list of nodes forming the control */
	@Nonnull
	private final NodeBase[] m_nodeList;

	/** The node to be used as the target for a "label" */
	private final NodeBase m_labelNode;

	/** The FormControl handle for the created control */
	@Nonnull
	private IControl< ? > m_handle;

	public <T extends NodeBase & IControl< ? >> ControlFactoryResult(@Nonnull final T fc) {
		m_labelNode = fc;
		m_handle = fc;
		m_nodeList = new NodeBase[]{fc};
	}

	public <T extends NodeBase & IControl< ? >> ControlFactoryResult(@Nonnull T fc, @Nullable final NodeBase labelNode) {
		m_labelNode = labelNode;
		m_handle = fc;
		m_nodeList = new NodeBase[]{labelNode};
	}

	@Nonnull
	public NodeBase[] getNodeList() {
		return m_nodeList;
	}

	@Nullable
	public NodeBase getLabelNode() {
		return m_labelNode;
	}

	@Nonnull
	public <T extends NodeBase & IControl< ? >> T getFormControl() {
		return (T) m_handle;
	}
}

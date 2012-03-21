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

import javax.annotation.*;
import javax.annotation.concurrent.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Represents the result of a call to createControl.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
@Immutable
final public class ControlFactoryResult {
	/** The list of nodes forming the control */
	private final NodeBase[] m_nodeList;

	/** The binding of the control to it's model and property */
	@Nonnull
	private final IModelBinding m_binding;

	/** The node to be used as the target for a "label" */
	private final NodeBase m_labelNode;

	/** The FormControl handle for the created control */
	@Nonnull
	private IControl< ? > m_handle;

	// jal 20091206 tentative removal of unused/unusable constructors because they do not expose the IFormControl interface
	//		public ControlFactoryResult(final IModelBinding binding, final NodeBase labelNode, final NodeBase[] nodeList) {
	//			m_binding = binding;
	//			m_labelNode = labelNode;
	//			m_nodeList = nodeList;
	//		}

	//		public ControlFactoryResult(final IModelBinding binding, final NodeBase control) {
	//			m_binding = binding;
	//			m_labelNode = control;
	//			m_nodeList = new NodeBase[]{control};
	//		}
	public ControlFactoryResult(@Nonnull final IModelBinding binding, @Nonnull IControl< ? > fc, @Nonnull final NodeBase control) {
		m_binding = binding;
		m_labelNode = control;
		m_handle = fc;
		m_nodeList = new NodeBase[]{control};
	}

	public ControlFactoryResult(@Nonnull IControl< ? > handle, @Nonnull NodeBase[] nodeList, @Nonnull NodeBase labelNode, @Nonnull IModelBinding binding) {
		m_handle = handle;
		m_nodeList = nodeList;
		m_labelNode = labelNode;
		m_binding = binding;
	}

	public <M, C> ControlFactoryResult(@Nonnull final IInputNode<C> control, @Nonnull final IReadOnlyModel<M> model, @Nonnull final PropertyMetaModel<C> pmm) {
		m_labelNode = (NodeBase) control;
		m_nodeList = new NodeBase[]{(NodeBase) control};
		SimpleComponentPropertyBinding<C> b = new SimpleComponentPropertyBinding<C>(model, pmm, control);
		m_binding = b;
		m_handle = b;

		//-- 20091208 jal Experimental: also bind to treemodel ModelBinding
		control.bind().to(model, pmm);
	}

	public <A, B> ControlFactoryResult(@Nonnull final IDisplayControl<A> control, @Nonnull final IReadOnlyModel<B> model, @Nonnull final PropertyMetaModel<A> pmm) {
		m_labelNode = (NodeBase) control;
		m_nodeList = new NodeBase[]{(NodeBase) control};
		DisplayOnlyPropertyBinding<A> b = new DisplayOnlyPropertyBinding<A>(model, pmm, control);
		m_binding = b;
		m_handle = b;

		//-- 20091208 jal Experimental: also bind to treemodel ModelBinding
		if(control instanceof IBindable)
			((IBindable) control).bind().to(model, pmm);
	}

	@Nonnull
	public NodeBase[] getNodeList() {
		return m_nodeList;
	}

	@Nonnull
	public IModelBinding getBinding() {
		return m_binding;
	}

	@Nonnull
	public NodeBase getLabelNode() {
		return m_labelNode;
	}

	@Nonnull
	public IControl< ? > getFormControl() {
		return m_handle;
	}
}
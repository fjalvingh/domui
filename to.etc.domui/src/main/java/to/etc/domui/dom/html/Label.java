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
package to.etc.domui.dom.html;

import to.etc.domui.component.binding.ComponentPropertyBinding;
import to.etc.util.WrappedException;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;
import java.util.Objects;

@DefaultNonNull
public class Label extends NodeContainer {
	@Nullable
	private NodeBase m_forTarget;

	@Nullable
	private NodeBase m_forNode;

	@Nullable
	private ComponentPropertyBinding m_binding;

	public Label() {
		super("label");
	}

	public Label(@Nullable String text) {
		super("label");
		setText(text);
	}

	public Label(@Nullable NodeBase fr, String text) {
		super("label");
		setText(text);
		setForTarget(fr);
	}

	public Label(String text, String cssClass) {
		this();
		setText(text);
		setCssClass(cssClass);
	}

	public Label(NodeBase fr, String text, String cssClass) {
		super("label");
		setText(text);
		setForTarget(fr);
		setCssClass(cssClass);
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitLabel(this);
	}

	@Nullable public NodeBase getForTarget() {
		return m_forTarget;
	}

	/**
	 * This private property is the one actually containing the link to the "for". It
	 * is bound to the controls's "getForNode" method as exposed by that control's
	 * IForTarget interface.
	 *
	 * @return
	 */
	@Nullable
	public NodeBase getForNode() {
		return m_forNode;
	}

	/**
	 * As this method is not meant to be used from code: hide it.
	 * @param forNode
	 */
	private void setForNode(@Nullable NodeBase forNode) {
		if(Objects.equals(m_forNode, forNode))
			return;

		m_forNode = forNode;
		changed();
	}

	public void setForTarget(@Nullable NodeBase forTarget) {
		if(Objects.equals(m_forTarget, forTarget))
			return;
		NodeBase old = m_forTarget;
		if(null != old) {
			// Unbind from previous
			ComponentPropertyBinding binding = m_binding;
			if(null != binding)
				old.removeBinding(binding);
			m_binding = null;
		}
		m_forTarget = forTarget;
		if(forTarget instanceof IForTarget) {
			try {
				ComponentPropertyBinding binding = m_binding = bind("forNode");
				binding.to(forTarget, "forTarget");
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
		} else {
			m_binding = null;
		}
		changed();
	}
}

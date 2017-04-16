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
package to.etc.domui.component.layout;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * A panel with a beveled caption above it. The caption defaults to some text but can be
 * anything actually. Both the title and the content of this panel can be changed and can
 * contain any other node structure. Simple constructors exist to quickly render a panel
 * around a structure. The parameterless constructor expects the title of the panel to
 * be set separately; the content is initially created as a Div, and can either be replaced
 * or it can be added to.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class CaptionedPanel extends Div {
	private NodeContainer m_contentContainer;

	private NodeContainer m_titleContainer;

	/**
	 * Create a panel with the specified String title and a content node.
	 * @param title
	 * @param content
	 */
	public CaptionedPanel(String title, NodeContainer content) {
		this(new TextNode(title), content);
	}

	public CaptionedPanel(NodeContainer title) {
		this(title, new Div());
	}

	public CaptionedPanel(NodeContainer title, NodeContainer content) {
		setCssClass("ui-pnl-outer");
		m_titleContainer = title;
		m_titleContainer.addCssClass("ui-pnl-caption");
		m_contentContainer = content;
		m_contentContainer.addCssClass("ui-pnl-cont");
	}

	/**
	 * Create a panel with both the title and the container as a Node structure.
	 * @param title
	 * @param content
	 */
	public CaptionedPanel(NodeBase title, NodeContainer content) {
		this(new Div(), content);
		m_titleContainer.add(title);
	}

	/**
	 * Create a panel with a title and an empty Div as the container.
	 * @param title
	 */
	public CaptionedPanel(String title) {
		this(new TextNode(title), new Div());
	}

	/**
	 * Create an empty panel without a title and with an empty Div as the content node.
	 */
	public CaptionedPanel() {
		this(new Div(), new Div());
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void createContent() throws Exception {
		super.createContent();
		add(m_titleContainer);
		add(m_contentContainer);
	}

	/**
	 * Return the current content container; it can be added to.
	 * @return
	 */
	public NodeContainer getContent() {
		return m_contentContainer;
	}

	/**
	 * Get the current title container.
	 * @return
	 */
	public NodeContainer getTitleContainer() {
		return m_titleContainer;
	}

	/**
	 * Set the title for this panel as a String. This replaces the current node
	 * with a Div(TextNode) node.
	 * @see to.etc.domui.dom.html.NodeBase#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String txt) {
		getTitleContainer().setText(txt);
	}

	/**
	 * Replaces the current title container with a different one.
	 * @param c
	 */
	public void setTitleContainer(NodeContainer c) {
		m_titleContainer.remove();
		m_titleContainer = c;
		add(0, c);
		m_titleContainer.addCssClass("ui-pnl-caption");
	}

	/**
	 * Replaces the current content container with a different one.
	 * @param c
	 */
	public void setContentContainer(NodeContainer c) {
		m_contentContainer.remove();
		m_contentContainer = c;
		add(1, c);
		m_contentContainer.addCssClass("ui-pnl-cont");
	}
}

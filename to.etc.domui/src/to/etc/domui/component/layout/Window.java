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

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * This is a basic floating window, with a title area, optional fixed content area's
 * at the top and the bottom, and a scrollable content area in between. It has only
 * presentational characteristics, no logic.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 19, 2011
 */
public class Window extends FloatingDiv {
	/** The container holding this dialog's title bar. This is also the drag source. */
	private NodeContainer m_titleBar;

	/** The container holding the content area for this dialog. */
	private Div m_content;

	/** The title in the title bar. */
	@Nullable
	private String m_windowTitle;

	/** When T the window has a close button in it's title bar. */
	private boolean m_closable = true;

	/** The close button in the title bar. */
	private Img m_closeButton;

	/** If present, an image to use as the icon inside the title bar. */
	private Img m_titleIcon;

	/** The optional area just above the content area which remains fixed when the content area scrolls. */
	private Div m_topContent;

	/** The optional area just below the content area which remains fixed when the content area scrolls. */
	private Div m_bottomContent;


	public Window() {
		init();
	}

	/**
	 * Full constructor: create a window and be able to set all options at once.
	 * @param modal			T for a modal window.
	 * @param resizable		T for a window that can be resized by the user.
	 * @param width			The window width in pixels.
	 * @param height		The window height in pixels.
	 * @param title			The window title (or null if no title is required)
	 */
	public Window(boolean modal, boolean resizable, int width, int height, @Nullable String title) {
		super(modal, resizable, width, height);
		if(null != title)
			setWindowTitle(title);
		init();
	}

	/**
	 * Create a window of default size, with a specified title, modality and resizability.
	 * @param modal
	 * @param resizable
	 * @param title
	 */
	public Window(boolean modal, boolean resizable, String title) {
		super(modal, resizable);
		if(null != title)
			setWindowTitle(title);
		init();
	}

	/**
	 * Create a modal window with the specified title and resizable option.
	 * @param resizable
	 * @param title
	 */
	public Window(boolean resizable, String title) {
		this(true, resizable, title);
	}

	/**
	 * Create a modal, non-resizable window with the specified title.
	 * @param title
	 */
	public Window(String title) {
		this(true, false, title);
	}

	/**
	 * Create a modal, resizable window of the given size and with the specified title.
	 * @param width
	 * @param height
	 * @param title
	 */
	public Window(int width, int height, String title) {
		this(true, true, width, height, title);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Builder modifiers.									*/
	/*--------------------------------------------------------------*/

	@Override
	@Nonnull
	public Window size(int width, int height) {
		super.size(width, height);
		return this;
	}

	@Nonnull
	@Override
	public Window resizable() {
		super.resizable();
		return this;
	}

	@Nonnull
	@Override
	public Window modal(boolean yes) {
		super.modal(yes);
		return this;
	}

	@Nonnull
	@Override
	public Window modal() {
		super.modal();
		return this;
	}

	@Nonnull
	public Window title(@Nonnull String set) {
		setWindowTitle(set);
		return this;
	}

	@Override
	@Nonnull
	public Window width(int pxsl) {
		super.width(pxsl);
		return this;
	}

	private void init() {
		m_content = new Div();
		m_content.addCssClass("ui-flw-c ui-fixovfl");
		//		m_content.setStretchHeight(true);
		m_topContent = new Div();
		m_topContent.addCssClass("ui-flw-tc");
		m_bottomContent = new Div();
		m_bottomContent.addCssClass("ui-flw-bc");
		setErrorFence();
		delegateTo(m_content);
		m_content.setErrorFence(); // jal experimental
	}

	/**
	 * This creates the title bar frame.
	 * @see to.etc.domui.dom.html.NodeContainer#createFrame()
	 */
	@Override
	protected void createFrame() throws Exception {
		getPage().calculateDefaultFocus(this);
		m_titleBar = new Div();
		add(m_titleBar);
		createTitleBar();
		add(m_topContent);
		add(m_content);
		add(m_bottomContent);
		setErrorFence();

		//-- jal 20121105 If an explicit height is set then we stretch the content to max, else the content itself decides on the height of the window.
		if(getHeight() != null) {
			m_content.setStretchHeight(true);
		}

		//vmijic 20091125 - since z-index is dynamic value, correct value has to be used also in js.
		appendCreateJS("$('#" + getActualID() + "').draggable({" + "ghosting: false, zIndex:" + getZIndex() + ", handle: '#" + m_titleBar.getActualID() + "', stop: WebUI.notifySizePositionChanged});");
		delegateTo(m_content);
	}

	@Override
	public void setDimensions(int width, int height) {
		super.setDimensions(width, height);
		if(null != m_content && getHeight() != null)
			m_content.setStretchHeight(true);
	}

	/**
	 * Create the title bar for the floater.
	 * Also replaces existing title bar in case that new is set.
	 * @return
	 */
	protected void createTitleBar() {
		if(m_titleBar == null)
			return;

		//-- The titlebar div must not change after creation because it is the drag handle.
		m_titleBar.removeAllChildren();
		m_titleBar.setCssClass("ui-flw-ttl");
		if(m_closable) {
			m_closeButton = new Img();
			m_closeButton.setSrc("THEME/close.png");
			m_closeButton.setFloat(FloatType.RIGHT);

			//some margin fixes have to be applied with css
			m_closeButton.setCssClass("ui-flw-btn-close");
			m_titleBar.add(m_closeButton);
			m_closeButton.setClicked(new IClicked<NodeBase>() {
				@Override
				public void clicked(@Nonnull NodeBase b) throws Exception {
					closePressed();
				}
			});
		}
		if(m_titleIcon != null)
			m_titleBar.add(m_titleIcon);
		m_titleBar.add(getWindowTitle());
	}

	private Img createIcon() {
		if(m_titleIcon == null) {
			m_titleIcon = new Img();
			m_titleIcon.setBorder(0);
			m_titleIcon.setCssClass("ui-flw-ttl-icon");
			if(m_titleBar != null) {
				//Since IE has bug that floater object is rendered under previous sibling, close button must be rendered before any other element in title bar.
				if(m_closeButton != null && m_titleBar.getChildCount() > 0 && m_titleBar.getChild(0) == m_closeButton) {
					m_titleBar.add(1, m_titleIcon);
				} else {
					m_titleBar.add(0, m_titleIcon);
				}
			}
		}
		return m_titleIcon;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Properties.											*/
	/*--------------------------------------------------------------*/
	/**
	 * When set to TRUE, the floater will display a close button on it's title bar, and will close
	 * if that thingy is pressed.
	 * @return
	 */
	public boolean isClosable() {
		return m_closable;
	}

	/**
	 * When set to TRUE, the floater will display a close button on it's title bar, and will close
	 * if that thingy is pressed.
	 * @param closable
	 */
	public void setClosable(boolean closable) {
		if(m_closable == closable)
			return;
		m_closable = closable;
	}

	/**
	 * Return the floater's title bar title string.
	 * @return
	 */
	public String getWindowTitle() {
		return m_windowTitle;
	}

	/**
	 * Set the floater's title bar string.
	 * @param windowTitle
	 */
	public void setWindowTitle(String windowTitle) {
		if(DomUtil.isEqual(windowTitle, m_windowTitle))
			return;
		m_windowTitle = windowTitle;
		if(m_titleBar != null)
			createTitleBar();
	}

	/**
	 * Set an icon for the title bar, using the absolute path to a web resource. If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param ico
	 */
	public void setIcon(String ico) {
		createIcon().setSrc(ico);
	}

	/**
	 * Return the div that is the bottom content area. Before it can be used it's heigth <b>must</b> be set
	 * manually to a size in pixels. This allows the Javascript layout calculator to calculate the size of
	 * the content area. After setting the height any content can be added here.
	 * @return
	 */
	public Div getBottomContent() {
		return m_bottomContent;
	}

	/**
	 * Return the div that is the top content area. Before it can be used it's heigth <b>must</b> be set
	 * manually to a size in pixels. This allows the Javascript layout calculator to calculate the size of
	 * the content area. After setting the height any content can be added here.
	 * @return
	 */
	public Div getTopContent() {
		return m_topContent;
	}
}

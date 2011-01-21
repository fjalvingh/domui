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

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * A simple floating window, non-blocking, with a title bar which can be dragged. This also
 * acts as an error fence, limiting all errors generated within this control to be displayed
 * within this window.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 30, 2008
 */
public class FloatingWindow extends Div {
	private boolean m_modal;

	private NodeContainer m_titleBar;

	private NodeContainer m_content;

	private String m_windowTitle;

	private boolean m_closable = true;

	private Img m_closeButton;

	//	private boolean			m_constructed;
	private Img m_titleIcon;

	IClicked<FloatingWindow> m_onClose;

	private Div m_hider;

	private static final int WIDTH = 640;

	private static final int HEIGHT = 400;

	protected FloatingWindow() {
		init();
	}

	/**
	 * Create a floating window with the specified title in the title bar.
	 * @param txt
	 */
	protected FloatingWindow(boolean modal, String txt) {
		m_modal = modal;
		if(txt != null)
			setWindowTitle(txt);
		init();
	}

	/**
	 * Create and link a modal floating window.
	 * @return
	 */
	static public FloatingWindow create(NodeBase parent) {
		return create(parent, null, true);
	}

	static public FloatingWindow create(NodeBase parent, String ttl) {
		return create(parent, ttl, true);
	}

	static public FloatingWindow create(NodeBase parent, String ttl, boolean modal) {
		UrlPage body = parent.getPage().getBody();
		FloatingWindow w = new FloatingWindow(modal, ttl); // Create instance
		//vmijic 20091125 - in case of cascading floating windows, z-index higher than one from parent floating window must be set.
		FloatingWindow parentFloatingWindow = parent.getParent(FloatingWindow.class);
		if(parentFloatingWindow != null) {
			w.setZIndex(parentFloatingWindow.getZIndex() + 100);
		}
		body.add(w);
		return w;
	}

	@Override
	public void onAddedToPage(Page p) {
		super.onAddedToPage(p);
		if(!m_modal)
			return;

		//-- 1. Add a DIV obscuring all other input.
		m_hider = new Div();
		m_hider.setCssClass("ui-fw-hider");
		p.getBody().add(m_hider);
	}

	@Override
	public void onRemoveFromPage(Page p) {
		super.onRemoveFromPage(p);
		if(m_hider != null) {
			m_hider.remove();
			m_hider = null;
		}
	}

	/**
	 * Create the floater.
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		delegateTo(null);
		setCssClass("ui-fw");

		if(getWidth() == null)
			setWidth(WIDTH + "px");
		if(getHeight() == null)
			setHeight(HEIGHT + "px");
		if(getZIndex() <= 0) {
			setZIndex(100);
		}
		if(getTestID() == null) {
			setTestID("popup_" + getZIndex());
		}

		//vmijic 20091125 - hider z-index has to be set in order to hide other floating windows with lower z-index, if any exists in same time.
		if(null != m_hider) { // Initial createContent will have null hider usually.
			m_hider.setZIndex(getZIndex() - 1);
			m_hider.setClicked(new IClicked<NodeBase>() {
				@Override
				public void clicked(NodeBase clickednode) throws Exception {
					closePressed();
				}
			});
		}

		setPosition(PositionType.FIXED);

		int width = DomUtil.pixelSize(getWidth());
		if(width == -1)
			width = WIDTH;

		// center floating window horizontally on screen
		setMarginLeft("-" + width / 2 + "px");

		//-- Construct the title bar
		createTitleBar();
		super.add(1, m_content);
		delegateTo(m_content);

		//-- Test jq drag
		//		appendCreateJS("$('#"+getActualID()+"').draggable({" +
		//			"ghosting: false, zIndex:100, opacity: 0.7, handle: '#"+m_titleBar.getActualID()+"'});"
		//		);
		//vmijic 20091125 - since z-index is dinamic value, correct value has to be used also in js.
		appendCreateJS("$('#" + getActualID() + "').draggable({" + "ghosting: false, zIndex:" + getZIndex() + ", handle: '#" + m_titleBar.getActualID() + "'});");
		//		m_constructed = true;
	}

	private void init() {
		m_content = new Div();
		m_content.setCssClass("ui-fw-c");
		setErrorFence();
		delegateTo(m_content);
	}

	//	/**
	//	 * Set an icon from the current theme for the title bar.
	//	 * @param ico
	//	 */
	//	public void	setThemeIcon(String ico) {
	//		createIcon().setThemeSrc(ico);
	//	}
	/**
	 * Set an icon for the title bar, using the absolute path to a web resource. If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param ico
	 */
	public void setIcon(String ico) {
		createIcon().setSrc(ico);
	}

	private Img createIcon() {
		if(m_titleIcon == null) {
			m_titleIcon = new Img();
			m_titleIcon.setBorder(0);
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

	//	/**
	//	 * This is an overridden method which causes content added to the FloatingWindow to be added
	//	 * to it's content area instead.
	//	 *
	//	 * @see to.etc.domui.dom.html.NodeContainer#add(int, to.etc.domui.dom.html.NodeBase)
	//	 */
	//	@Override
	//	public void add(int index, NodeBase nd) {
	//		m_content.add(index, nd);
	//	}
	//
	//	/**
	//	 * This is an overridden method which causes content added to the FloatingWindow to be added
	//	 * to it's content area instead.
	//	 *
	//	 * @see to.etc.domui.dom.html.NodeContainer#add(to.etc.domui.dom.html.NodeBase)
	//	 */
	//	@Override
	//	public void add(NodeBase nd) {
	//		m_content.add(nd);
	//	}

	/**
	 * Create the title bar for the floater.
	 * Also replaces existing title bar in case that new is set.
	 * @return
	 */
	protected NodeContainer createTitleBar() {
		Div ttl = new Div();
		if(m_titleBar != null) {
			//if old title bar exists
			NodeContainer parent = m_titleBar.getParent();
			if(parent != null) {
				//replace old title bar if already added to page
				parent.replaceChild(m_titleBar, ttl);
			} else {
				//just remove old title bar if it is still not attached
				m_titleBar.remove();
			}
		}
		m_titleBar = ttl;
		if(null == ttl.getParent()) {
			//in case that title bar is still not added to page, add it as first item
			super.add(0, ttl);
		}
		ttl.setCssClass("ui-fw-ttl");
		if(m_closable) {
			m_closeButton = new Img();
			m_closeButton.setSrc("THEME/close.png");
			m_closeButton.setFloat(FloatType.RIGHT);
			//some margin fixes have to be applied with css
			m_closeButton.setCssClass("ui-fw-btn-close");
			ttl.add(m_closeButton);
			m_closeButton.setClicked(new IClicked<NodeBase>() {
				@Override
				public void clicked(NodeBase b) throws Exception {
					closePressed();
				}
			});
		}
		if(m_titleIcon != null)
			ttl.add(m_titleIcon);
		ttl.add(getWindowTitle());
		return ttl;
	}

	/**
	 * Close the window !AND CALL THE CLOSE HANDLER!.
	 *
	 * @throws Exception
	 */
	public void closePressed() throws Exception {
		close();
		if(m_onClose != null)
			m_onClose.clicked(FloatingWindow.this);
	}

	/**
	 * Returns T if the window can be closed using a close button on the title bar.
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
	 * Close this floater and cause it to be destroyed from the UI.
	 */
	public void close() {
		remove();
	}

	/**
	 * Get the current "onClose" handler.
	 * @return
	 */
	public IClicked<FloatingWindow> getOnClose() {
		return m_onClose;
	}

	/**
	 * Set a Clicked handler to be called when this floater is closed by it's close button. This does <i>not</i> get
	 * called when the floater is closed programmatically (i.e. when close() is called).
	 *
	 * @param onClose
	 */
	public void setOnClose(IClicked<FloatingWindow> onClose) {
		m_onClose = onClose;
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
	 * This links this floater as a "modal" window to the page specified by the base node.
	 * @param parent
	 */
	public void linkToPageModally(NodeBase parent) {
		//-- 1. Add a DIV obscuring all other input.
		Div hider = new Div();
		hider.setCssClass("ui-fw-hider");
		UrlPage body = parent.getPage().getBody();
		body.add(hider);

		//-- 2. Add the floater @zIndex=100
		body.add(this);
	}


}

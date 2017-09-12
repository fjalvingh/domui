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

import to.etc.domui.component.event.*;
import to.etc.domui.component.image.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.util.*;

import javax.annotation.*;

/**
 * The base class for all floating thingeries (new style).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 18, 2011
 */
public class FloatingDiv extends Div {
	/** Close reason {@link IWindowClosed#closed(String)}: the dialog was closed by the close button or by pressing the hider. */
	static public final String RSN_CLOSE = "closed";

	static protected final int DEFWIDTH = 640;

	static protected final int DEFHEIGHT = 400;

	static protected final int MINWIDTH = 256;

	static protected final int MINHEIGHT = 200;

	private boolean m_modal;

	private boolean m_resizable;

	/** When set, clicking outside the dialog will close it. */
	private boolean m_autoClose = true;

	/** A handler to call when the floating (window) is closed. This is only called if the window is closed by a user action, not when the window is closed by code (by calling {@link #close()}). */
	@Nullable
	private IWindowClosed m_onClose;

	/** If this is a modal window it will have a "hider" div to make it modal, and that div will be placed in here by the Page when the div is shown. */
	@Nullable
	private Div m_hider;

	public FloatingDiv() {}

	public FloatingDiv(boolean modal) {
		this(modal, false, -1, -1);
	}

	public FloatingDiv(boolean modal, boolean resizable) {
		this(modal, resizable, -1, -1);
	}

	public FloatingDiv(boolean modal, boolean resizable, int widthinpx, int heightinpx) {
		m_modal = modal;
		m_resizable = resizable;
		setDimensions(widthinpx, heightinpx);
	}

	@Nonnull
	public FloatingDiv size(int width, int height) {
		setDimensions(width, height);
		return this;
	}

	@Nonnull
	public FloatingDiv resizable() {
		m_resizable = true;
		return this;
	}

	@Nonnull
	public FloatingDiv modal(boolean yes) {
		m_modal = yes;
		return this;
	}

	@Nonnull
	public FloatingDiv modal() {
		return modal(true);
	}

	@Nonnull
	public FloatingDiv width(int pxsl) {
		setWidth(pxsl + "px");
		return this;
	}

	/**
	 * Change the width and height for the dialog - only valid before it has been
	 * built!! The minimum size is 250x200 pixels.
	 *
	 * @param width
	 * @param height
	 */
	public void setDimensions(int width, int height) {
		if(width > 0) {
			if(width < 250)
				throw new IllegalArgumentException("The width=" + width + " is invalid: it cannot be smaller than 250.");
			setWidth(width + "px");
			setMinWidth(width + "px");
		}
		if(height > 0) {
			if(height < 100)
				throw new IllegalArgumentException("The height=" + height + " is invalid: it cannot be smaller than 100.");
			setHeight(height + "px");
			setMinHeight(height + "px");
		}
	}

	/**
	 * Returns T if this is a MODAL window, obscuring windows it is on top of.
	 */
	public boolean isModal() {
		return m_modal;
	}

	public boolean isResizable() {
		return m_resizable;
	}

	@Nullable
	public Div internalGetHider() {
		return m_hider;
	}

	public void internalSetHider(@Nullable Div hider) {
		m_hider = hider;
	}

	/**
	 * Overridden to tell the floating thing handler to remove this floater from
	 * the stack.
	 * @see to.etc.domui.dom.html.NodeBase#onRemoveFromPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onRemoveFromPage(Page p) {
		super.onRemoveFromPage(p);
		p.internalRemoveFloater(this);
	}

	@Override
	protected void beforeCreateContent() {
		super.beforeCreateContent();
		addCssClass("ui-flw");
		deactivateHiddenAccessKeys();

		// jal 20121105 Removed: interferes with auto-resizing floating window.
		//		if(getWidth() == null) // Should not be possible.
		//			setWidth("640px");
		//		if(getHeight() == null) // Should not be possible
		//			setHeight("400px");

		if(getZIndex() <= 0) { // Should not be possible.
			FloatingDiv parentFloatingDiv = findParent(FloatingDiv.class);
			if(parentFloatingDiv != null) {
				setZIndex(parentFloatingDiv.getZIndex() + 100);
			} else {
				setZIndex(100);
			}
		}
		if(getTestID() == null) {
			setTestID("popup_" + getZIndex());
		}
		setPosition(PositionType.FIXED); //floaters always use FIXED position, this should not be changed

        if(getWidth() != null && getWidth().endsWith("%")) {
			//when relative size is in use we don't center window horizontally, otherwise we need to center it
			int widthPerc = DomUtil.percentSize(getWidth());
			if(widthPerc != -1) {
				// center floating window horizontally on screen
				setMarginLeft("-" + widthPerc / 2 + "%");
				setLeft("50%");
			}
		} else {
			int width = DomUtil.pixelSize(getWidth());
			if(-1 == width) {
				StringBuilder sb = new StringBuilder();
				appendJQuerySelector(sb);
				sb.append(".center();");
				appendCreateJS(sb.toString());
			} else {
				int left = DomUtil.pixelSize(getLeft());
				if(-1 == left) {
					// center floating window horizontally on screen by default
					setMarginLeft("-" + width / 2 + "px");
					setLeft("50%");
				}
				int top = DomUtil.pixelSize(getTop());
				if(-1 == top) {
					// place floating window near to top of screen by default
					setMarginTop("0px");
					setTop("5%");
				}
			}
		}

		//-- If this is resizable add the resizable() thing to the create javascript. This relies on jquery built in support for ui-resizeable.
		if(isResizable())
			appendCreateJS("$('#" + getActualID() + "').resizable({minHeight: " + MINHEIGHT + ", minWidth: " + MINWIDTH
				 + ", resize: WebUI.floatingDivResize, stop: WebUI.notifySizePositionChanged});");
	}

	public boolean isAutoClose() {
		return m_autoClose;
	}

	/**
	 * When set (which is the default), clicking outside the dialog will automatically close (cancel) it.
	 * @param autoClose
	 */
	public void setAutoClose(boolean autoClose) {
		m_autoClose = autoClose;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Close control and floater close event handling.		*/
	/*--------------------------------------------------------------*/
	/**
	 * Get the current "onClose" handler: a handler to call when the window is closed. This is
	 * only called if the window is closed by a user action, not when the window is closed by
	 * code (by calling {@link #close()}).
	 * @return
	 */
	@Nullable
	final public IWindowClosed getOnClose() {
		return m_onClose;
	}

	/**
	 * Set the current "onClose" handler: a handler to call when the window is closed. This is
	 * only called if the window is closed by a user action, not when the window is closed by
	 * code (by calling {@link #close()}).
	 *
	 * @param onClose
	 */
	final public void setOnClose(@Nullable IWindowClosed onClose) {
		m_onClose = onClose;
	}

	/**
	 * Internal use: call the registered "close" handler with the close reason. For this base class the only
	 * reason passed will be RSN_CLOSED. Derived classes can add other string constants to use.
	 * @param reasonCode
	 * @throws Exception
	 */
	final protected void callCloseHandler(@Nonnull String closeReason) throws Exception {
		if(null == closeReason)
			throw new IllegalArgumentException("Close reason cannot be null");
		onClosed(closeReason);
		if(null != m_onClose)
			m_onClose.closed(closeReason);
	}

	/**
	 * Can be overridden to handle close events inside a subclass. This gets called when the
	 * close event fires, before the onClose property handler is called.
	 * @param closeReason
	 * @throws Exception
	 */
	protected void onClosed(@Nonnull String closeReason) throws Exception {}

	/**
	 * Close the window !AND CALL THE CLOSE HANDLER!. To close the window without calling
	 * the close handler use {@link #close()}. This code represents the "cancel" action
	 * for dialogs.
	 *
	 * @throws Exception
	 */
	public void closePressed() throws Exception {
		close();
		callCloseHandler(RSN_CLOSE);
	}

	/**
	 * Close this floater and cause it to be destroyed from the UI without calling the
	 * close handler. To call the close handler use {@link #closePressed()}.
	 */
	@OverridingMethodsMustInvokeSuper
	public void close() {
		reactivateHiddenAccessKeys();
		remove();
	}

	/**
	 * Can be called from panels that know they are inside a FloatingDiv/Window/Dialog to close
	 * the surrounding dialog.
	 * @param node
	 */
	public static void close(@Nonnull NodeBase node) {
		while(! (node instanceof FloatingDiv)) {
			if(node == null)
				return;
			node = node.getParent();
		}
		((FloatingDiv) node).close();
	}

	/**
	 * Position floater into center of screen vertically.
	 */
	public void verticallyAlignToCenter() {
		setTop("50%");
		String height = getHeight();
		if(!StringTool.isBlank(height) && height.endsWith("px")) {
			// center floating window vertically on screen
			setMarginTop("-" + Integer.parseInt(height.replace("px", "")) / 2 + "px");
		} else {
			throw new IllegalStateException("Unable to vertically align floater if height is not specified in px!");
		}
	}

	/**
	 * Disables all button access keys which are not part of active floating window.
	 * They will be enabled again on window close
	 * @see FloatingDiv#reactivateHiddenAccessKeys()
	 */
	private void deactivateHiddenAccessKeys() {
		appendCreateJS("WebUI.deactivateHiddenAccessKeys(" + getActualID() + ");");
	}

	/**
	 * Enables all button access keys, disabled during the floating window presence.
	 * @see FloatingDiv#deactivateHiddenAccessKeys()
	 */
	private void reactivateHiddenAccessKeys() {
		appendJavascript("WebUI.reactivateHiddenAccessKeys('" + getActualID() + "');");
	}

	/**
	 * Exposed client bounds. This gets set internally in case that floating div is dragged or resized in client (browser).
	 * @see to.etc.domui.dom.html.NodeContainer#getClientBounds()
	 */
	@Override
	public Rect getClientBounds() {
		return super.getClientBounds();
	}

	/**
	 * Exposed call to get browser window size. This gets set internally in case that floating div is dragged or resized in client (browser).
	 * @see to.etc.domui.dom.html.NodeContainer#getClientBounds()
	 */
	@Override
	public Dimension getBrowserWindowSize() {
		return super.getBrowserWindowSize();
	}
	/**
	 * Exposed listener setter for notification on size and/or position change. This gets changed in case that floating div is dragged or resized in client (browser).
	 * Call {@link FloatingDiv#getClientBounds()} in order to read size and position after change.
	 * @see to.etc.domui.dom.html.NodeBase#setOnSizeAndPositionChange()
	 */
	@Override
	public final void setOnSizeAndPositionChange(@Nonnull INotify<NodeBase> onSizeAndPositionChange) {
		super.setOnSizeAndPositionChange(onSizeAndPositionChange);
	}
}

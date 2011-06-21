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

import to.etc.domui.dom.css.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

public class Div extends NodeContainer implements IDropTargetable, IDraggable {
	private IReturnPressed m_returnPressed;

	private MiniTableBuilder m_miniTableBuilder;

	private IDropHandler m_dropHandler;

	private IDragHandler m_dragHandler;

	public Div() {
		super("div");
	}

	public Div(String txt) {
		this();
		setText(txt);
	}

	public Div(NodeBase... children) {
		this();
		for(NodeBase b : children)
			add(b);
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitDiv(this);
	}

	public IReturnPressed getReturnPressed() {
		return m_returnPressed;
	}

	public void setReturnPressed(IReturnPressed returnPressed) {
		m_returnPressed = returnPressed;
	}

	@Override
	protected void afterCreateContent() throws Exception {
		m_miniTableBuilder = null;
	}

	/**
	 * Handle the action sent by the return pressed Javascript thingerydoo.
	 *
	 * @see to.etc.domui.dom.html.NodeBase#componentHandleWebAction(to.etc.domui.server.RequestContextImpl, java.lang.String)
	 */
	@Override
	public void componentHandleWebAction(RequestContextImpl ctx, String action) throws Exception {
		if(!"returnpressed".equals(action)) {
			super.componentHandleWebAction(ctx, action);
			return;
		}

		//-- Return is pressed- call it's handler.
		if(m_returnPressed != null)
			m_returnPressed.returnPressed(this);
	}

	public MiniTableBuilder tb() {
		if(m_miniTableBuilder == null)
			m_miniTableBuilder = new MiniTableBuilder();
		return m_miniTableBuilder;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Drag and drop support.								*/
	/*--------------------------------------------------------------*/

	/** When in table-drop mode this defines whether cells or rows are added to the table. */
	private DropMode m_dropMode;

	/** When in table-drop mode this defines the TBody where the drop has to take place. */
	private TBody m_dropBody;

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.util.IDraggable#setDragHandler(to.etc.domui.util.IDragHandler)
	 */
	@Override
	public void setDragHandler(IDragHandler dragHandler) {
		m_dragHandler = dragHandler;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.util.IDraggable#getDragHandler()
	 */
	@Override
	public IDragHandler getDragHandler() {
		return m_dragHandler;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.util.IDropTargetable#getDropHandler()
	 */
	@Override
	public IDropHandler getDropHandler() {
		return m_dropHandler;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.util.IDropTargetable#setDropHandler(to.etc.domui.util.IDropHandler)
	 */
	@Override
	public void setDropHandler(IDropHandler dropHandler) {
		m_dropHandler = dropHandler;
		if(m_dropMode == null)
			m_dropMode = DropMode.DIV;
	}

	/**
	 * Sets this DIV in table-drop mode. This assigns the TBody where droppings are to be done and
	 * the mode to use when dropping.
	 * @param body
	 * @param dropMode
	 */
	public void setDropBody(TBody body, DropMode dropMode) {
		switch(dropMode){
			default:
				throw new IllegalStateException("Unsupported DROP mode for TABLE container: " + dropMode);
			case ROW:
				break;
		}

		//-- I must be the parent for the table passed
		NodeBase b = body;
		while(b != this) {
			if(b.getParent() == null)
				throw new IllegalStateException("Programmer error: the TBody passed MUST be a child of the DIV node if you want to use the DIV as a DROP container for that TBody.");
			b = b.getParent();
		}
		m_dropMode = dropMode;
		m_dropBody = body;
	}

	public TBody getDropBody() {
		return m_dropBody;
	}

	public DropMode getDropMode() {
		return m_dropMode;
	}

	/**
	 * Effect: hide this div by adjusting it's height, ending as a display: none.
	 */
	public void slideUp() {
		if(internalSetDisplay(DisplayType.NONE))
			appendJavascript("$('#" + getActualID() + "').slideUp();");
	}

	/**
	 * Effect: hide this div by adjusting it's height, ending as a display: none.
	 * @param javascriptCallback specify callback
	 */
	public void slideUp(String javascriptCallback) {
		if(javascriptCallback == null) {
			slideUp();
		}
		if(internalSetDisplay(DisplayType.NONE)) {
			appendJavascript("$('#" + getActualID() + "').slideUp({complete: function() {" + javascriptCallback + "}});");
		}
	}

	/**
	 * Redisplay a display: none thing slowly.
	 */
	public void slideDown() {
		if(internalSetDisplay(DisplayType.BLOCK))
			appendJavascript("$('#" + getActualID() + "').slideDown();");
	}

	/**
	 * Effect: hide this div by fading out.
	 */
	public void fadeOut() {
		if(internalSetDisplay(DisplayType.NONE))
			appendJavascript("$('#" + getActualID() + "').fadeOut();");
	}

	/**
	 * Redisplay a display: none thing slowly.
	 */
	public void fadeIn() {
		if(internalSetDisplay(DisplayType.BLOCK))
			appendJavascript("$('#" + getActualID() + "').fadeIn();");
	}

	/**
	 * Redisplay a display: none thing slowly.
	 */
	public void fadeIn(int tm) {
		if(internalSetDisplay(DisplayType.BLOCK))
			appendJavascript("$('#" + getActualID() + "').fadeIn(" + tm + ");");
	}


}

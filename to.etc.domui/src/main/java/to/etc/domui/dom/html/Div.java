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

import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.util.IDragHandler;
import to.etc.domui.util.IDraggable;
import to.etc.domui.util.IDropBody;
import to.etc.domui.util.IDropHandler;
import to.etc.domui.util.IDropTargetable;
import to.etc.domui.util.MiniTableBuilder;
import to.etc.domui.util.UIDragDropUtil;

import javax.annotation.Nonnull;

public class Div extends NodeContainer implements IDropTargetable, IDraggable, IDropBody {
	private IReturnPressed< ? extends NodeBase> m_returnPressed;

	private MiniTableBuilder m_miniTableBuilder;

	private IDropHandler m_dropHandler;

	private IDragHandler m_dragHandler;

	public Div() {
		super("div");
	}

	public Div(String css) {
		this();
		setCssClass(css);
	}

	public Div(NodeBase... children) {
		this();
		for(NodeBase b : children)
			add(b);
	}

	public Div(String css, String text) {
		this(css);
		setText(text);
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitDiv(this);
	}

	public IReturnPressed< ? extends NodeBase> getReturnPressed() {
		return m_returnPressed;
	}

	public void setReturnPressed(IReturnPressed< ? extends NodeBase> returnPressed) {
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
	public void componentHandleWebAction(@Nonnull RequestContextImpl ctx, @Nonnull String action) throws Exception {
		if(!"returnpressed".equals(action)) {
			super.componentHandleWebAction(ctx, action);
			return;
		}

		//-- Return is pressed- call it's handler.
		if(m_returnPressed != null)
			((IReturnPressed<NodeBase>) m_returnPressed).returnPressed(this);
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

	/** When in table-drop mode this defines the TBody where the drop has to take place. When in div-drop mode this defines the Div where the drop has to take place */
	private IDropBody m_dropBody;

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
	public void setDropBody(@Nonnull IDropBody body, DropMode dropMode) {
		switch(dropMode){
			default:
				throw new IllegalStateException("Unsupported DROP mode for TABLE or DIV container: " + dropMode);
			case ROW:
			case DIV:
				break;
		}

		//-- I must be the parent for the body passed
		NodeBase b = (NodeBase) body;
		while(b != this) {
			if(!b.hasParent())
				throw new IllegalStateException("Programmer error: the TBody or DIV passed MUST be a child of the DIV node if you want to use the DIV as a DROP container for that TBody or DIV.");
			b = b.getParent();
		}
		m_dropMode = dropMode;
		m_dropBody = body;
		setSpecialAttribute(UIDragDropUtil.DROP_MODE_ATTRIBUTE, dropMode.name());
	}

	public IDropBody getDropBody() {
		return m_dropBody;
	}

	public DropMode getDropMode() {
		return m_dropMode;
	}

	///**
	// * Effect: hide this div by adjusting it's height, ending as a display: none.
	// * Additional callback javascript is executed after animation is done. @See {@link Div#getCustomUpdatesCallJS()} callback.
	// */
	//public void slideUp() {
	//	if(internalSetDisplay(DisplayType.NONE))
	//		appendJavascript("$('#" + getActualID() + "').slideUp({complete: function() {" + getCustomUpdatesCallJS() + "}});");
	//}
	//
	///**
	// * Redisplay a display: slideDown thing slowly.
	// * Additional callback javascript is executed after animation is done. @See {@link Div#getCustomUpdatesCallJS()} callback.
	// */
	//public void slideDown() {
	//	if(internalSetDisplay(DisplayType.BLOCK))
	//		appendJavascript("$('#" + getActualID() + "').slideDown({complete: function() {" + getCustomUpdatesCallJS() + "}});");
	//}
	//
	///**
	// * Effect: hide this div by fading out.
	// * Additional callback javascript is executed after animation is done. @See {@link Div#getCustomUpdatesCallJS()} callback.
	// */
	//public void fadeOut() {
	//	if(internalSetDisplay(DisplayType.NONE))
	//		appendJavascript("$('#" + getActualID() + "').fadeOut({complete: function() {" + getCustomUpdatesCallJS() + "}});");
	//}
	//
	///**
	// * Redisplay a display: fadeIn thing slowly.
	// * Additional callback javascript is executed after animation is done. @See {@link Div#getCustomUpdatesCallJS()} callback.
	// */
	//public void fadeIn() {
	//	if(internalSetDisplay(DisplayType.BLOCK))
	//		appendJavascript("$('#" + getActualID() + "').fadeIn({complete: function() {" + getCustomUpdatesCallJS() + "}});");
	//}
	//
	/**
	 * Returns Domui internal javascript call: <I>WebUI.doCustomUpdates();</I>
	 * @return
	 */
	protected static String getCustomUpdatesCallJS() {
		return "WebUI.doCustomUpdates();";
	}
}

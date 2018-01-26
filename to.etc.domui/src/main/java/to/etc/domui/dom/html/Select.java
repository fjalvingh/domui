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

import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.util.DomUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * INCOMPLETE A full-coded select box: this is unsuitable for large amount of options.
 *
 * Handling the selected item is incomplete.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 11, 2008
 */
public class Select extends InputNodeContainer implements INativeChangeListener, IHasModifiedIndication, IHtmlInput, IForTarget {
	private boolean m_multiple;

	private boolean m_disabled;

	private int m_size;

	private int m_selectedIndex;

	private IReturnPressed<Select> m_returnPressed;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	private boolean m_modifiedByUser;

	private List<SmallImgButton> m_buttonList = Collections.EMPTY_LIST;

	public Select() {
		super("select");
	}

	public Select(String... txt) {
		this();
		for(String s : txt) {
			add(new SelectOption(s));
		}
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitSelect(this);
	}

	/**
	 * Only allow SelectOption as child.
	 * @see to.etc.domui.dom.html.NodeContainer#canContain(to.etc.domui.dom.html.NodeBase)
	 */
	@Override
	protected void canContain(@Nonnull NodeBase node) {
		if(!(node instanceof SelectOption))
			throw new IllegalStateException(getClass().getName() + " cannot contain a " + node + " type, only a SelectOption node type.");
	}

	public boolean isMultiple() {
		return m_multiple;
	}

	public void setMultiple(boolean multiple) {
		if(m_multiple == multiple)
			return;
		m_multiple = multiple;
		changed();
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		m_disabled = disabled;
		if(disabled) {
			addCssClass("ui-ro");
		} else {
			removeCssClass("ui-ro");
		}
		changed();
	}

	/**
	 * Util for updating select enabled / disabled state depending on existence of error (reason for disabling).
	 *
	 * @param rsn reason to disable select. If null, select gets enabled, otherwise it gets disabled with rsn.getMessage() as title (hint)
	 */
	public void setDisabled(@Nullable UIMessage rsn) {
		if(null != rsn) {
			setDisabled(true);
			setTitle(rsn.getMessage());
		} else {
			setDisabled(false);
			setTitle(null);
		}
	}

	public int getSize() {
		return m_size;
	}

	public void setSize(int size) {
		if(m_size == size)
			return;
		m_size = size;
		changed();
	}

	/**
	 * WARNING: The "select" node HAS NO READONLY!!!
	 * @see to.etc.domui.dom.html.InputNodeContainer#setReadOnly(boolean)
	 */
	@Override
	public void setReadOnly(boolean readOnly) {
		setDisabled(readOnly);
		super.setReadOnly(readOnly);
	}

	public SelectOption getOption(int ix) {
		if(ix < 0 || ix >= getChildCount())
			throw new ArrayIndexOutOfBoundsException("The option index " + ix + " is invalid, the #options is " + getChildCount());
		return (SelectOption) getChild(ix);
	}

	@Override
	final public boolean acceptRequestParameter(@Nonnull String[] values) throws Exception {
		if(isDisabled()) {                                // Never accept data from request in disabled control.
			return false;
		}
		String in = values[0];
		SelectOption selo = (SelectOption) getPage().findNodeByID(in);
		int nindex = selo == null ? -1 : findChildIndex(selo);
		int oldindex = m_selectedIndex;
		setSelectedIndex(nindex);
		if(!internalOnUserInput(oldindex, nindex))
			return false;
		DomUtil.setModifiedFlag(this);
		return true;
	}

	/**
	 * Called when user input has changed the selected index.
	 * @param oldindex
	 * @param nindex
	 */
	protected boolean internalOnUserInput(int oldindex, int nindex) {
		return oldindex != nindex; // Index has changed so this is a change
	}

	/**
	 * Dangerous interface for derived classes.
	 */
	@Deprecated
	public void clearSelected() {
		m_selectedIndex = -1;
		for(int i = getChildCount(); --i >= 0;) {
			getOption(i).setSelected(false);
		}
	}

	public int getSelectedIndex() {
		return m_selectedIndex;
	}

	/**
	 * Fast way to set index without walking the option tree, to use if the subclass knows
	 * a faster way to set all option selected values.
	 * @param ix
	 */
	public void internalSetSelectedIndex(int ix) {
		m_selectedIndex = ix;
	}

	/**
	 * Set the selected index - expensive because it has to walk all Option children and reset their
	 * selected attribute - O(n) runtime.
	 * @param ix
	 */
	public void setSelectedIndex(int ix) {
		m_selectedIndex = ix;
		for(int i = getChildCount(); --i >= 0;) {
			getOption(i).setSelected(i == m_selectedIndex);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Code to add extra stuff after this combo.			*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a small image button after the combo.
	 * @param img
	 * @param title
	 * @param click
	 */
	public void addExtraButton(String img, String title, final IClicked<NodeBase> click) {
		if(m_buttonList == Collections.EMPTY_LIST)
			m_buttonList = new ArrayList<SmallImgButton>();
		SmallImgButton si = new SmallImgButton(img);
		if(click != null) {
			si.setClicked(new IClicked<SmallImgButton>() {
				@Override
				public void clicked(@Nonnull SmallImgButton b) throws Exception {
					click.clicked(Select.this);
				}
			});
		}
		if(title != null)
			si.setTitle(title);
		si.addCssClass("ui-cl2-btn");
		m_buttonList.add(si);

		if(isBuilt())
			forceRebuild();
	}

	@Override
	public void onAddedToPage(Page p) {
		NodeContainer curr = this;
		for(SmallImgButton sib : m_buttonList) {
			curr.appendAfterMe(sib);
			curr = sib;
		}
	}

	@Override
	public void onRemoveFromPage(Page p) {
		for(SmallImgButton sib : m_buttonList) {
			sib.remove();
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	IHasModifiedIndication impl							*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the modified-by-user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#isModified()
	 */
	@Override
	final public boolean isModified() {
		return m_modifiedByUser;
	}

	/**
	 * Set or clear the modified by user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#setModified(boolean)
	 */
	@Override
	final public void setModified(boolean as) {
		m_modifiedByUser = as;
	}

	/**
	 * Set or clear return press handler. Dropdown list that has size > 1 behaves different then ordinary drop down list, since value changed event gets triggered while list is browsed.
	 * In order to prevent server roundtrip for each selection change while moving trought list wth arrow keys, implement setClicked and setReturnPressed events instead.
	 * @return
	 */
	public IReturnPressed<Select> getReturnPressed() {
		return m_returnPressed;
	}

	public void setReturnPressed(IReturnPressed<Select> returnPressed) {
		m_returnPressed = returnPressed;
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
			m_returnPressed.returnPressed(this);
	}

	@Nullable @Override public NodeBase getForTarget() {
		return this;
	}
}

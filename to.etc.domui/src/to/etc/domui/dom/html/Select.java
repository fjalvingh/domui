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

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.util.*;

/**
 * INCOMPLETE A full-coded select box: this is unsuitable for large amount of options.
 *
 * Handling the selected item is incomplete.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 11, 2008
 */
public class Select extends InputNodeContainer implements IHasModifiedIndication {
	private boolean m_multiple;

	private boolean m_disabled;

	private int m_size;

	private int m_selectedIndex;

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
	protected void canContain(NodeBase node) {
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

	public boolean isDisabled() {
		return m_disabled;
	}

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
	final public boolean acceptRequestParameter(String[] values) throws Exception {
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
	protected void internalSetSelectedIndex(int ix) {
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
	 * @param clicked
	 */
	public void addExtraButton(String img, String title, final IClicked<NodeBase> click) {
		if(m_buttonList == Collections.EMPTY_LIST)
			m_buttonList = new ArrayList<SmallImgButton>();
		SmallImgButton si = new SmallImgButton(img);
		if(click != null) {
			si.setClicked(new IClicked<SmallImgButton>() {
				@Override
				public void clicked(SmallImgButton b) throws Exception {
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

}

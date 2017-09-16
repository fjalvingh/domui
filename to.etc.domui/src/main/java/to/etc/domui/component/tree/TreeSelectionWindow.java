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
package to.etc.domui.component.tree;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ButtonBar;
import to.etc.domui.component.layout.FloatingWindow;
import to.etc.domui.component.layout.IWindowClosed;
import to.etc.domui.component.tbl.ICellClicked;
import to.etc.domui.dom.css.Overflow;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClickBase;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IClicked2;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.Msgs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A popup window that shows a tree and lets the user select one entry from it. It shows a tree with
 * the specified model, user controllable node content renderer. It adds a button bar with select and
 * cancel buttons, and has setClicked() and setCancelClicked() handlers to handle selection events.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 10, 2010
 */
public class TreeSelectionWindow<T> extends FloatingWindow implements ICellClicked<T> {
	private TreeSelect<T> m_tree;

	private T	m_selected;

	@Nonnull
	private ITreeModel<T>	m_model;

//	private NodeBase		m_selectedNode;

	private long m_lastClickTS;

	private IClickBase< ? > m_clicked;

	private IClickBase< ? > m_cancelClicked;

	private IRenderInto<T> m_contentRenderer;

	public TreeSelectionWindow(boolean modal, String txt, @Nonnull ITreeModel<T> model) {
		super(modal, txt);
		m_model = model;
		setOnClose(new IWindowClosed() {
			@Override
			public void closed(@Nonnull String closeReason) throws Exception {
				cancel();
			}
		});
	}

	@Override
	public void createContent() throws Exception {
		setWidth("400px");
		super.createContent();
		ButtonBar	bb = new ButtonBar();
		add(bb);
		bb.addButton(Msgs.BUNDLE.getString("ui.tsw.select"), new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				select();
			}
		});
		bb.addButton(Msgs.BUNDLE.getString("ui.tsw.cancel"), new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				cancel();
			}
		});
		Div	fixed = new Div();
		add(fixed);
		fixed.setHeight("300px");
		fixed.setOverflow(Overflow.AUTO);
		m_tree = new TreeSelect<>();
		fixed.add(m_tree);
		m_tree.setModel(m_model);
		m_tree.setContentRenderer(m_contentRenderer);
		m_tree.setCellClicked(this);
	}

	protected void cancel() throws Exception {
		close();
		m_selected = null;

		if(getCancelClicked() != null) {
			((IClicked<TreeSelectionWindow<T>>) getCancelClicked()).clicked(this);
			return;
		}
		IClickBase< ? > clicked = getClicked();
		if(clicked != null) {
			((IClicked<TreeSelectionWindow<T>>) clicked).clicked(this);
		}
	}

	protected void select() throws Exception {
		if(m_selected == null)
			return;
		close();
		IClickBase< ? > clicked = getClicked();
		if(clicked != null) {
			((IClicked<TreeSelectionWindow<T>>) clicked).clicked(this);
		}
	}

	/**
	 * Internally called when tree node is clicked. Handles selection events.
	 * @see ICellClicked#cellClicked(Object)
	 */
	@Override
	final public void cellClicked(@Nonnull T rowval) throws Exception {
		long ts = System.currentTimeMillis();
		if(m_selected == rowval) {
			//-- Reselect...
			long dt = ts - m_lastClickTS;
			if(dt < 500) {
				//-- Treat double click as select
				select();
				return;
			}
		}

		m_lastClickTS = ts;
		m_selected = rowval;
	}

	/**
	 * Inhibit a Javascript clicked handler so we can override the clicked handler for selection.
	 * @return
	 */
	@Override
	public boolean internalNeedClickHandler() {
		return false;
	}

	public T getSelected() {
		return m_selected;
	}

	public void setSelected(T selected) {
		m_selected = selected;
	}

	@Override
	public IClickBase< ? > getClicked() {
		return m_clicked;
	}

	@Override
	public void setClicked(@Nullable IClicked< ? > clicked) {
		m_clicked = clicked;
	}

	@Override public void setClicked2(IClicked2<?> clicked) {
		m_clicked = clicked;
	}

	public IClickBase< ? > getCancelClicked() {
		return m_cancelClicked;
	}

	public void setCancelClicked(IClickBase< ? > cancelClicked) {
		m_cancelClicked = cancelClicked;
	}

	public IRenderInto<T> getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(IRenderInto<T> contentRenderer) {
		m_contentRenderer = contentRenderer;
	}
}

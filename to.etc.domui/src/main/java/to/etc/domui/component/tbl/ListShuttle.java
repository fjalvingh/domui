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
package to.etc.domui.component.tbl;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.dom.css.Overflow;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.domui.dom.html.TableVAlign;
import to.etc.domui.server.DomApplication;
import to.etc.domui.util.IRenderInto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * The ListShuttle component contains a SOURCE list and a TARGET list, and allows the user to
 * "shuttle" elements from the SOURCE list to the TARGET list, and v.v. In addition the target
 * list can be ordered, if necessary.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 5, 2008
 */
public class ListShuttle extends Div implements ITableModelListener<Object> {
	private TBody m_body;

	private boolean m_orderable;

	private TBody m_sourceBody;

	private TBody m_targetBody;

	private Div m_sourceDiv, m_targetDiv;

	private IShuttleModel<Object, Object> m_model;

	private ITableModel<Object> m_sourceModel;

	private ITableModel<Object> m_targetModel;

	//	private ITableModelListener<Object>		m_targetChangeListener;

	private Class< ? extends IRenderInto< ? >> m_sourceRendererClass;

	private Class< ? extends IRenderInto< ? >> m_targetRendererClass;

	private IRenderInto<Object> m_sourceRenderer;

	private IRenderInto<Object> m_targetRenderer;

	static private final int INNERHEIGHT = 400;

	static private IClicked<TD> C_CLICK = b -> {
		if(b.hasCssClass("selected"))
			b.removeCssClass("selected");
		else
			b.addCssClass("selected");
	};

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-sh");
		Table t = new Table(); // Main 3 or 4 column layout.
		add(t);
		m_body = new TBody();
		t.add(m_body);
		m_body.addRow(); // Zhe one row
		TD sourceCell = m_body.addCell();
		TD midcell = m_body.addCell(); // Cell for the buttons in zhe middle
		TD targetCell = m_body.addCell();

		sourceCell.setCssClass("ui-sh-sc");
		targetCell.setCssClass("ui-sh-tc");

		sourceCell.add(m_sourceDiv = new Div());
		targetCell.add(m_targetDiv = new Div());
		m_sourceDiv.setCssClass("ui-sh-scd");
		m_targetDiv.setCssClass("ui-sh-tcd");
		m_sourceDiv.setHeight(INNERHEIGHT + "px");
		m_targetDiv.setHeight(INNERHEIGHT + "px");
		m_sourceDiv.setOverflow(Overflow.AUTO);
		m_targetDiv.setOverflow(Overflow.AUTO);
		sourceCell.setValign(TableVAlign.TOP);
		targetCell.setValign(TableVAlign.TOP);

		createMiddle(midcell);

		createOrderPane();

		renderSource();
		renderTarget();
	}

	/**
	 * Create the last panel containing the UP and DOWN order buttons.
	 */
	private void createOrderPane() {
		TD orderCell = m_body.addCell(); // Order buttons cell

		orderCell.setCssClass("ui-sh-oc");
		Table t = new Table();
		if(needOrderButtons()) {
			orderCell.add(t);
		}
		TBody b = new TBody();
		t.add(b);

		DefaultButton ib = new DefaultButton("", "THEME/sh-up.png", xb -> moveUp());
		b.addRowAndCell().add(ib);

		ib = new DefaultButton("", "THEME/sh-down.png", xb -> moveDown());
		b.addRowAndCell().add(ib);
	}

	/**
	 * Create the middle pane containing the move buttons.
	 */
	private void createMiddle(final TD mid) {
		mid.setCssClass("ui-sh-bc");
		Table t = new Table();
		mid.add(t);
		TBody b = new TBody();
		t.add(b);

		DefaultButton ib = new DefaultButton("", "THEME/sh-2ar-right.png", clickednode -> moveRight());
		b.addRowAndCell().add(ib);

		ib = new DefaultButton("", "THEME/sh-1ar-right.png", xb -> moveRight());
		b.addRowAndCell().add(ib);

		ib = new DefaultButton("", "THEME/sh-1ar-left.png", xb -> moveLeft());
		b.addRowAndCell().add(ib);

		ib = new DefaultButton("", "THEME/sh-2ar-left.png", clickednode -> moveLeft());
		b.addRowAndCell().add(ib);

	}

	/**
	 * Render the contents of the source part of the shuttle.
	 */
	private void renderSource() throws Exception {
		//		if(m_sourceBody == null) {
			Table t = new Table();
			m_sourceDiv.add(t);
			t.add(m_sourceBody = new TBody());
		//		} else
		//			m_sourceBody.removeAllChildren();
		if(m_sourceModel == null)
			return;
		int count = m_sourceModel.getRows();
		if(count == 0)
			return;

		List<Object> list = m_sourceModel.getItems(0, count);

		IRenderInto<Object> r = null;
		for(int i = 0; i < count; i++) {
			TD td = m_sourceBody.addRowAndCell();
			Object value = list.get(i);
			if(r == null)
				r = calculateSourceRenderer(value);
			r.render(td, value);

			td.setClicked(C_CLICK);
		}
	}

	/**
	 * Render the contents of the target part of the shuttle.
	 */
	private void renderTarget() throws Exception {
		//		if(m_targetBody == null) {
			Table t = new Table();
			m_targetDiv.add(t);
			t.add(m_targetBody = new TBody());
		//		} else
		//			m_targetBody.removeAllChildren();
		if(m_targetModel == null)
			return;
		int count = m_targetModel.getRows();
		if(count == 0)
			return;

		List<Object> list = m_targetModel.getItems(0, count);
		IRenderInto<Object> r = null;
		for(int i = 0; i < count; i++) {
			TD td = m_targetBody.addRowAndCell();
			Object value = list.get(i);
			if(r == null)
				r = calculateTargetRenderer(value);
			r.render(td, value);
			td.setClicked(C_CLICK);
		}
	}

	private boolean needOrderButtons() {
		return isOrderable() && getModel() instanceof IMovableShuttleModel< ? , ? >;
	}

	private IRenderInto<Object> calculateSourceRenderer(final Object val) {
		if(m_sourceRenderer != null)
			return m_sourceRenderer;
		if(m_sourceRendererClass != null)
			return (IRenderInto<Object>) DomApplication.get().createInstance(m_sourceRendererClass);

		if(val == null)
			throw new IllegalStateException("Cannot calculate content renderer for null value");
		ClassMetaModel cmm = MetaManager.findClassMeta(val.getClass());
		return (IRenderInto<Object>) MetaManager.createDefaultComboRenderer(null /* m_propertyMetaModel */, cmm);
	}

	private IRenderInto<Object> calculateTargetRenderer(final Object val) {
		if(m_targetRenderer != null)
			return m_targetRenderer;
		if(m_targetRendererClass != null)
			return (IRenderInto<Object>) DomApplication.get().createInstance(m_targetRendererClass);

		if(val == null)
			throw new IllegalStateException("Cannot calculate content renderer for null value");
		ClassMetaModel cmm = MetaManager.findClassMeta(val.getClass());
		return (IRenderInto<Object>) MetaManager.createDefaultComboRenderer(null /* m_propertyMetaModel */, cmm);
	}

	/**
	 * Move all selected thingies to the right. This scans all TD's having a class of SELECTED, then moves
	 * the items found to the TARGET model.
	 */
	private void moveRight() throws Exception {
		for(int ix = m_sourceBody.getChildCount(); --ix >= 0;) {
			NodeBase tb = m_sourceBody.getChild(ix);
			if(!(tb instanceof TR))
				throw new IllegalStateException("?? Unexpected node: " + tb);
			TR tr = (TR) tb;
			TD td = (TD) tr.getChild(0); // The row containing the rendition for the value
			if(td.hasCssClass("selected")) {
				m_model.moveSourceToTarget(ix, 9999); // Since this changes both models
			}
		}
	}

	/**
	 * Move all selected thingies to the right. This scans all TD's having a class of SELECTED, then moves
	 * the items found to the TARGET model.
	 */
	private void moveLeft() throws Exception {
		for(int ix = m_targetBody.getChildCount(); --ix >= 0;) {
			NodeBase tb = m_targetBody.getChild(ix);
			if(!(tb instanceof TR))
				throw new IllegalStateException("?? Unexpected node: " + tb);
			TR tr = (TR) tb;
			TD td = (TD) tr.getChild(0); // The row containing the rendition for the value
			if(td.hasCssClass("selected")) {
				m_model.moveTargetToSource(ix); // Since this changes both models
			}
		}
	}

	/**
	 * Move all selected items in the TARGET table UP.
	 */
	private void moveUp() throws Exception {
		for(int ix = 0; ix < m_targetBody.getChildCount(); ix++) {
			NodeBase tb = m_targetBody.getChild(ix);
			if(!(tb instanceof TR))
				throw new IllegalStateException("?? Unexpected node: " + tb);
			TR tr = (TR) tb;
			TD td = (TD) tr.getChild(0); // The row containing the rendition for the value
			if(td.hasCssClass("selected")) {
				//-- This must move up.. If we're at index 0 we cannot move up!
				if(ix == 0)
					return;
				((IMovableShuttleModel<Object, Object>) m_model).moveTargetItem(ix, ix - 1);
				reselect(ix - 1);
			}
		}
	}

	/**
	 * Moves all selected items in the TARGET downwards.
	 */
	private void moveDown() throws Exception {
		for(int ix = m_targetBody.getChildCount(); --ix >= 0;) {
			NodeBase tb = m_targetBody.getChild(ix);
			if(!(tb instanceof TR))
				throw new IllegalStateException("?? Unexpected node: " + tb);
			TR tr = (TR) tb;
			TD td = (TD) tr.getChild(0); // The row containing the rendition for the value
			if(td.hasCssClass("selected")) {
				if(ix >= m_targetBody.getChildCount() - 1) // At the end already?
					return; // Cannot move down!
				((IMovableShuttleModel<Object, Object>) m_model).moveTargetItem(ix, ix + 1);
				reselect(ix + 1);
			}
		}
	}

	private void reselect(final int ix) {
		//		if(ix < 0 || ix >= m_targetBody.getChildCount())
		//			return;
		NodeBase tb = m_targetBody.getChild(ix);
		if(!(tb instanceof TR))
			throw new IllegalStateException("?? Unexpected node: " + tb);
		TR tr = (TR) tb;
		TD td = (TD) tr.getChild(0); // The row containing the rendition for the value
		td.setCssClass("selected");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Model based code.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Get the model for this thingy.
	 */
	public IShuttleModel< ? , ? > getModel() {
		return m_model;
	}

	/**
	 * Set the model to use for the shuttle. If the model changes or one of the component models has
	 * changed this will cause a redraw.
	 */
	public void setModel(final IShuttleModel< ? , ? > model) {
		if(m_model == model)
			return;
		m_model = (IShuttleModel<Object, Object>) model;

		//-- Replace source model.
		ITableModel<Object> tm = m_model.getSourceModel();
		if(m_sourceModel != tm) { // Source model has changed?
			if(m_sourceModel != null)
				m_sourceModel.removeChangeListener(this); // Remove myself from that model's listener chain
			m_sourceModel = tm;
			if(m_sourceModel != null)
				m_sourceModel.addChangeListener(this); // Add myself to the new model
			forceRebuild();
		}

		//-- Replace target model
		tm = m_model.getTargetModel();
		if(m_targetModel != tm) {
			if(m_targetModel != null)
				m_targetModel.removeChangeListener(this);
			m_targetModel = tm;
			if(m_targetModel != null)
				m_targetModel.addChangeListener(this);
			forceRebuild();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Source table ITableModelChanged implementation.		*/
	/*--------------------------------------------------------------*/
	/**
	 * The source model has changed. Rebuild the entire component.
	 * @see to.etc.domui.component.tbl.ITableModelListener#modelChanged(to.etc.domui.component.tbl.ITableModel)
	 */
	@Override
	public void modelChanged(@Nullable final ITableModel<Object> model) {
		forceRebuild();
	}

	/**
	 * A row was moved (back) to the source model: change it.
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowAdded(to.etc.domui.component.tbl.ITableModel, int, java.lang.Object)
	 */
	@Override
	public void rowAdded(final @Nonnull ITableModel<Object> model, final int index, @Nonnull final Object value) throws Exception {
		boolean issrc = model == m_sourceModel;
		TBody b = issrc ? m_sourceBody : m_targetBody;

		//-- Create a new node to insert here,
		TR tr = new TR();
		TD td = new TD();
		tr.add(td);
		td.setClicked(C_CLICK);
		IRenderInto<Object> r = issrc ? calculateSourceRenderer(value) : calculateTargetRenderer(value);
		r.render(td, value);
		b.add(index, tr);
	}

	@Override
	public void rowDeleted(final @Nonnull ITableModel<Object> model, final int index, @Nonnull final Object value) throws Exception {
		boolean issrc = model == m_sourceModel;
		TBody b = issrc ? m_sourceBody : m_targetBody;
		b.removeChild(index); // Discard this one;
	}

	@Override
	public void rowModified(final @Nonnull ITableModel<Object> model, final int index, @Nonnull final Object value) throws Exception {
		boolean issrc = model == m_sourceModel;
		TBody b = issrc ? m_sourceBody : m_targetBody;

		//-- Locate the TD containing the changed thingy.
		TR tr = (TR) b.getChild(index);
		TD td = (TD) tr.getChild(0);
		td.removeAllChildren(); // Clear it's contents,
		IRenderInto<Object> r = issrc ? calculateSourceRenderer(value) : calculateTargetRenderer(value);
		r.render(td, value);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple Setters and getters.							*/
	/*--------------------------------------------------------------*/

	public IRenderInto<Object> getSourceRenderer() {
		return m_sourceRenderer;
	}

	public void setSourceRenderer(final IRenderInto<Object> sourceRenderer) {
		m_sourceRenderer = sourceRenderer;
	}

	public IRenderInto<Object> getTargetRenderer() {
		return m_targetRenderer;
	}

	public void setTargetRenderer(final IRenderInto<Object> targetRenderer) {
		m_targetRenderer = targetRenderer;
	}

	/**
	 * Is this set to be orderable?
	 */
	public boolean isOrderable() {
		return m_orderable;
	}

	public Class< ? extends IRenderInto< ? >> getSourceRendererClass() {
		return m_sourceRendererClass;
	}

	public void setSourceRendererClass(final Class< ? extends IRenderInto< ? >> sourceRendererClass) {
		m_sourceRendererClass = sourceRendererClass;
	}

	public Class< ? extends IRenderInto< ? >> getTargetRendererClass() {
		return m_targetRendererClass;
	}

	public void setTargetRendererClass(final Class< ? extends IRenderInto< ? >> targetRendererClass) {
		m_targetRendererClass = targetRendererClass;
	}

	/**
	 * When set (and the model supports it) this shows ordering buttons at the right of the
	 * target shuttle, allowing the content to be moved up- and downwards.
	 *
	 * @param orderable
	 */
	public void setOrderable(final boolean orderable) {
		m_orderable = orderable;
	}

}

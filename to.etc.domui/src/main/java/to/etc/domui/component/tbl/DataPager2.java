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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.Button;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.Span;
import to.etc.domui.util.IExecute;
import to.etc.domui.util.Msgs;
import to.etc.webapp.nls.BundleRef;
import to.etc.webapp.nls.IBundleCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Datapager using buttons and a page number list.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 2, 2019
 */
final public class DataPager2 extends Div implements IDataTablePager {
	private Button m_prevBtn;

	private Button m_nextBtn;

	private PageableTabularComponentBase< ? > m_table;

	/** When set (default) this shows selection details when a table has a selectable model. */
	private boolean m_showSelection = true;

	private Div m_buttonContainer;

	private Div m_buttonDiv = new Div();

	@NonNull
	private List<SmallImgButton> m_extraButtonList = new ArrayList<>();

	public DataPager2(final PageableTabularComponentBase< ? > tbl) {
		m_table = tbl;
		tbl.addChangeListener(this);
	}

	@Override
	public void createContent() throws Exception {
		addCssClass("ui-dp2");
		m_buttonContainer = new Div("ui-dp2-bc");
		add(m_buttonContainer);
		Div bd = m_buttonDiv = new Div("ui-dp2-buttons");
		m_buttonContainer.add(bd);

		m_prevBtn = appendButton(bd, Msgs.uiPagerPrev, () -> {
			int cp = m_table.getCurrentPage();
			if(cp <= 0)
				return;
			m_table.setCurrentPage(cp - 1);
		});


		//-- Last part
		m_nextBtn = appendButton(bd, Msgs.uiPagerNext, () -> {
			int cp = m_table.getCurrentPage();
			int mx = m_table.getPageCount();
			cp++;
			if(cp >= mx)
				return;
			m_table.setCurrentPage(cp);
		});
		redraw();
	}

	private Button appendButton(Div bd, IBundleCode code, IExecute x) {
		Button b = new Button("ui-dp2-btn");
		b.setClicked(clickednode -> x.execute());
		bd.add(b);
		b.add(code.getString());
		return b;
	}

	@Nullable
	private ISelectableTableComponent< ? > getSelectableTable() {
		if(m_table instanceof ISelectableTableComponent< ? >)
			return m_table;
		return null;
	}

	@Nullable
	private ISelectionModel< ? > getSelectionModel() {
		ISelectableTableComponent< ? > stm = getSelectableTable();
		if(null == stm)
			return null;
		return stm.getSelectionModel();
	}

	/**
	 * Return T if the "show selection UI" button should be visible.
	 */
	private boolean isNeedSelectionButton() throws Exception {
		ISelectionModel< ? > sm = getSelectionModel();
		if(sm == null || !m_showSelection)
			return false;
		if(!sm.isMultiSelect())
			return false;
		ISelectableTableComponent< ? > tc = getSelectableTable();
		if(null == tc)
			throw new IllegalStateException("Null selectable table?");
		if(tc.isMultiSelectionVisible())
			return false;
		return tc.getModel() != null && tc.getModel().getRows() != 0;
	}

	@Override
	public void selectionUIChanged(@NonNull TableModelTableBase< ? > tbl) throws Exception {
		redraw();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handle changes to the table.						*/
	/*--------------------------------------------------------------*/

	private void redraw() throws Exception {
		if(! isBuilt())
			return;
		Div bd = m_buttonDiv;
		int np = m_table.getPageCount();
		if(np <= 1) {
			m_buttonContainer.setDisplay(DisplayType.NONE);
			return;
		}
		m_buttonContainer.setDisplay(DisplayType.BLOCK);

		int cp = m_table.getCurrentPage();
		if(np == 0) {
			setDisplay(DisplayType.NONE);
			return;
		}

		setDisplay(DisplayType.BLOCK);

		if(cp <= 0) {
			m_prevBtn.setDisabled(true);
		} else {
			m_prevBtn.setDisabled(false);
		}

		if(cp + 1 >= np) {
			m_nextBtn.setDisabled(true);
		} else {
			m_nextBtn.setDisabled(false);
		}

		bd.removeAllChildren();
		bd.add(m_prevBtn);

		/*
		 * render page numbers. The basic group is: 3 at the start, 3 at the end, 5 in the middle, unless we have <= 10 pages
		 * in which case we render all.
		 *
		 * 1 2 3 ... n-2 n-1 n n+1 n+2 ... np-2 np-1 np
		 */
		if(np <= 10) {
			renderButtons(0, 0, 10);
		} else {
			int ci = renderButtons(0, 0, 3);        // First 3 buttons

			if(ci < np) {
				//-- do we have a middle range?
				int ms = cp - 2;
				if(ms < ci)
					ms = ci;
				int me = cp + 3;            // exclusive bound
				if(me > np)
					me = np;

				if(ms < me) {
					if(ci < ms)
						bd.add(Icon.faEllipsisH.createNode().css("ui-dp2-ellipsis"));
					ci = ms;

					//bd.add(" ... ");

					ci = renderButtons(ci, ms, me);
				}

				//-- Now do the end range, if applicable
				if(ci < np) {
					ms = np - 2;
					if(ms < ci)
						ms = ci;

					if(ci < ms)
						bd.add(Icon.faEllipsisH.createNode().css("ui-dp2-ellipsis"));

					ci = ms;
					ci = renderButtons(ci, ms, np);
				}
			}
		}

		bd.add(m_nextBtn);

		for(@NonNull SmallImgButton sib : m_extraButtonList) {
			bd.add(sib);
		}

		Span reco = new Span();
		reco.addCssClass("ui-dp2-nurec");
		reco.add(Msgs.uiPagerRecordCount.format(m_table.getResultCount()));
		bd.add(reco);
		if(m_table.isTruncated()) {
			Div node = new Div("ui-dp2-trunc");
			bd.add(node);
			node.setTitle(Msgs.uiPagerOverflow2.getString());
		}

	}


	private int renderButtons(int ci, int from, int to) throws Exception {
		int np = m_table.getPageCount();
		for(int i = from; i < to; i++) {
			Button b;
			if(ci >= np)
				break;
			if(ci == m_table.getCurrentPage()) {
				b = new Button("ui-dp2-btn ui-dp2-pn ui-dp2-cp");
			} else {
				b = new Button("ui-dp2-btn ui-dp2-pn");
			}
			b.add(Integer.toString(ci + 1));
			final int morons = ci;
			b.setClicked(clickednode -> m_table.setCurrentPage(morons));
			m_buttonDiv.add(b);
			ci++;
		}
		return ci;
	}

//	private void redrawSelectionButtons() throws Exception {
//		//-- Show/hide the "show selection" button
//		final ISelectableTableComponent<Object> dt = (ISelectableTableComponent<Object>) getSelectableTable();
//		if(null == dt)
//			throw new IllegalStateException("Null selectable table?");
//
//		if(isNeedSelectionButton()) {
//			if(m_showSelectionBtn == null) {
//				m_showSelectionBtn = new SmallImgButton(Icon.of("THEME/dpr-select-on.png"));
//				m_buttonDiv.add(4, m_showSelectionBtn); // Always after last navigation button
//				m_showSelectionBtn.setClicked(new IClicked<NodeBase>() {
//					@Override
//					public void clicked(@NonNull NodeBase clickednode) throws Exception {
//						dt.setShowSelection(true);
//						clickednode.remove();
//						m_showSelectionBtn = null;
//					}
//				});
//				m_showSelectionBtn.setTitle(Msgs.BUNDLE.getString("ui.dpr.selections"));
//			}
//		} else {
//			if(m_showSelectionBtn != null) {
//				m_showSelectionBtn.remove();
//				m_showSelectionBtn = null;
//			}
//		}
//	}

	public Div getButtonDiv() {
		return m_buttonDiv;
	}

	public void addButton(IIconRef image, final IClicked<DataPager2> click, final BundleRef bundle, final String ttlkey) {
		SmallImgButton i = new SmallImgButton(image, (IClicked<SmallImgButton>) b -> click.clicked(DataPager2.this));
		if(bundle != null)
			i.setTitle(bundle.getString(ttlkey));
		else if(ttlkey != null)
			i.setTitle(ttlkey);
		getButtonDiv().add(i);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	DataTableChangeListener implementation.				*/
	/*--------------------------------------------------------------*/
	@Override
	public void modelChanged(final @NonNull TableModelTableBase< ? > tbl, final @Nullable ITableModel< ? > old, final @Nullable ITableModel< ? > nw) throws Exception {
		forceRebuild();										// jal See bugzilla 7383: table queries done twice
		m_buttonDiv = null;									// Odd thing indicating that control is unbuilt, apparently
		//redraw();
	}

	@Override
	public void pageChanged(final @NonNull TableModelTableBase< ? > tbl) throws Exception {
		redraw();
	}

	@Override
	public boolean isShowSelection() {
		return m_showSelection;
	}

	@Override
	public void setShowSelection(boolean showSelection) {
		if(m_showSelection == showSelection)
			return;
		m_showSelection = showSelection;
		forceRebuild();
	}

	@Override
	public void addButton(@NonNull SmallImgButton sib) {
		m_extraButtonList.add(sib);
		forceRebuild();
	}

	public void addButton(@NonNull IIconRef img, @NonNull IClicked<SmallImgButton> clicked) {
		addButton(new SmallImgButton(img, clicked));
	}
}

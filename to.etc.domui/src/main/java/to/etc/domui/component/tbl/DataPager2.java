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
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.IExecute;
import to.etc.domui.util.Msgs;
import to.etc.webapp.nls.BundleRef;
import to.etc.webapp.nls.IBundleCode;

import java.util.ArrayList;
import java.util.List;

/**
 * New datapager using the
 *
 *
 * A pager component for a DataTable-based table. This gets attached
 * to a table, and then controls the table's paging. This pager has
 * a fixed L&F.
 *
 * The pager looks something like:
 * <pre>
 * [<<] [<] [>] [>>]     Record 50-75
 * </pre>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 19, 2008
 */
public class DataPager2 extends Div implements IDataTableChangeListener {
	private Button m_firstBtn;

	private Button m_prevBtn;

	private Button m_nextBtn;

	private Button m_lastBtn;

	private PageableTabularComponentBase< ? > m_table;

	private List<Button> m_pageButtonList = new ArrayList<>();

	/** When set (default) this shows selection details when a table has a selectable model. */
	private boolean m_showSelection = true;

	private Div m_buttonDiv = new Div();

	@NonNull
	private List<SmallImgButton> m_extraButtonList = new ArrayList<SmallImgButton>();

	public DataPager2() {}

	public DataPager2(final PageableTabularComponentBase< ? > tbl) {
		m_table = tbl;
		tbl.addChangeListener(this);
	}

	@Override
	public void createContent() throws Exception {
		addCssClass("ui-dp2");
		Div bd = m_buttonDiv = new Div("ui-dp2-buttons");
		add(bd);

		m_firstBtn = appendButton(bd, Msgs.uiPagerFirst, () -> m_table.setCurrentPage(0));
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
		m_lastBtn = appendButton(bd, Msgs.uiPagerLast, () -> {
			int pg = m_table.getPageCount();
			if(pg == 0)
				return;
			m_table.setCurrentPage(pg - 1);
		});
		redraw();
	}

	private Button appendButton(Div bd, IBundleCode code, IExecute x) {
		Button b = new Button("ui-dp2-btn");
		b.setClicked(clickednode -> x.execute());
		bd.add(b);
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
		Div bd = m_buttonDiv;
		if(bd == null)
			return;

		int cp = m_table.getCurrentPage();
		int np = m_table.getPageCount();
		int rowsAsked = -1;
		if(np == 0) {
			setDisplay(DisplayType.NONE);
			return;
		}

		int rows = rowsAsked = m_table.getModel().getRows();
		setDisplay(DisplayType.BLOCK);

		if(cp <= 0) {
			m_firstBtn.setDisabled(true);
			m_prevBtn.setDisabled(true);
		} else {
			m_firstBtn.setDisabled(false);
			m_prevBtn.setDisabled(false);
		}

		if(cp + 1 >= np) {
			m_lastBtn.setDisabled(true);
			m_nextBtn.setDisabled(true);
		} else {
			m_lastBtn.setDisabled(false);
			m_nextBtn.setDisabled(false);
		}

		bd.removeAllChildren();
		bd.add(m_firstBtn);
		bd.add(m_prevBtn);



		bd.add(m_nextBtn);
		bd.add(m_lastBtn);

		bd.add("\u00a0\u00a0");
		for(@NonNull SmallImgButton sib : m_extraButtonList) {
			bd.add(sib);
		}
//
//		int tc = m_table.getTruncatedCount();						// FIXME jal 20160125 Should be an isTruncated property, as the count is just model.size.
//		if(tc > 0) {
//			if(m_truncated == null) {
//				m_truncated = new Img();
//				m_truncated.setSrc("THEME/nav-overflow.png");
//				m_truncated.setTitle(Msgs.uiPagerOverflow.format(Integer.valueOf(tc)));
//				m_truncated.setCssClass("ui-dp-nav-pgr-ovf");
//				m_textDiv.add(m_truncated);
//			}
//		} else {
//			if(m_truncated != null) {
//				m_truncated.remove();
//				m_truncated = null;
//			}
//		}
//		//System.err.println("DataPager: redraw() called, currentPage=" + cp + ", pageCount=" + np + ", rowsAsked=" + rowsAsked);
//		if(isShowSelection() && getSelectableTable() != null) {
//			redrawSelectionButtons();
//		}
	}

	private void redrawSelectionButtons() throws Exception {
		//-- Show/hide the "show selection" button
		final ISelectableTableComponent<Object> dt = (ISelectableTableComponent<Object>) getSelectableTable();
		if(null == dt)
			throw new IllegalStateException("Null selectable table?");

		if(isNeedSelectionButton()) {
			if(m_showSelectionBtn == null) {
				m_showSelectionBtn = new SmallImgButton(Icon.of("THEME/dpr-select-on.png"));
				m_buttonDiv.add(4, m_showSelectionBtn); // Always after last navigation button
				m_showSelectionBtn.setClicked(new IClicked<NodeBase>() {
					@Override
					public void clicked(@NonNull NodeBase clickednode) throws Exception {
						dt.setShowSelection(true);
						clickednode.remove();
						m_showSelectionBtn = null;
					}
				});
				m_showSelectionBtn.setTitle(Msgs.BUNDLE.getString("ui.dpr.selections"));
			}
		} else {
			if(m_showSelectionBtn != null) {
				m_showSelectionBtn.remove();
				m_showSelectionBtn = null;
			}
		}
	}

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

	public boolean isShowSelection() {
		return m_showSelection;
	}

	public void setShowSelection(boolean showSelection) {
		if(m_showSelection == showSelection)
			return;
		m_showSelection = showSelection;
		forceRebuild();
	}

	public void addButton(@NonNull SmallImgButton sib) {
		m_extraButtonList.add(sib);
		forceRebuild();
	}

	public void addButton(@NonNull IIconRef img, @NonNull IClicked<SmallImgButton> clicked) {
		m_extraButtonList.add(new SmallImgButton(img, clicked));
	}
}

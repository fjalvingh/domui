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

import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.TextNode;
import to.etc.domui.util.Msgs;
import to.etc.webapp.nls.BundleRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
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
public class DataPager extends Div implements IDataTableChangeListener {
	private ATag m_firstBtn;

	private ATag m_prevBtn;

	private ATag m_nextBtn;

	private ATag m_lastBtn;

	private SmallImgButton m_showSelectionBtn;

	private Img m_truncated;

	private PageableTabularComponentBase< ? > m_table;

	private TextNode m_txt;

	private NodeContainer m_textDiv;

	/** When set (default) this shows selection details when a table has a selectable model. */
	private boolean m_showSelection = true;

	private Div m_buttonDiv;

	@Nonnull
	private List<SmallImgButton> m_extraButtonList = new ArrayList<SmallImgButton>();

	public DataPager() {}

	public DataPager(final PageableTabularComponentBase< ? > tbl) {
		m_table = tbl;
		tbl.addChangeListener(this);
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-dp");

		//-- The text part: message
		Div textPartRight = new Div();
		m_textDiv = textPartRight;
		add(textPartRight);
		textPartRight.setCssClass("ui-dp-txt");

		Span txtPnl = new Span();
		txtPnl.setTestID("pager results label");
		txtPnl.setCssClass("ui-dp-nav-pgr");

		//textPartRight.add(new VerticalSpacer(10));
		m_txt = new TextNode();
		txtPnl.add(m_txt);
		textPartRight.add(txtPnl);

		m_buttonDiv = new Div();
		add(m_buttonDiv);

		m_buttonDiv.setCssClass("ui-dp-btns");
		m_firstBtn = new ATag();
		m_firstBtn.setTestID("firstBtn");
		m_buttonDiv.add(m_firstBtn);
		m_prevBtn = new ATag();
		m_prevBtn.setTestID("prevBtn");
		m_buttonDiv.add(m_prevBtn);
		m_nextBtn = new ATag();
		m_nextBtn.setTestID("nextBtn");
		m_buttonDiv.add(m_nextBtn);
		m_lastBtn = new ATag();
		m_lastBtn.setTestID("lastBtn");
		m_buttonDiv.add(m_lastBtn);

		m_buttonDiv.add("\u00a0\u00a0");
		for(@Nonnull SmallImgButton sib : m_extraButtonList) {
			m_buttonDiv.add(sib);
		}

		redraw();

		//-- Click handlers for paging.
		m_firstBtn.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(final @Nonnull NodeBase b) throws Exception {
				m_table.setCurrentPage(0);
			}
		});
		m_lastBtn.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(final @Nonnull NodeBase b) throws Exception {
				int pg = m_table.getPageCount();
				if(pg == 0)
					return;
				m_table.setCurrentPage(pg - 1);
			}
		});
		m_prevBtn.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(final @Nonnull NodeBase b) throws Exception {
				int cp = m_table.getCurrentPage();
				if(cp <= 0)
					return;
				m_table.setCurrentPage(cp - 1);
			}
		});
		m_nextBtn.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(final @Nonnull NodeBase b) throws Exception {
				int cp = m_table.getCurrentPage();
				int mx = m_table.getPageCount();
				cp++;
				if(cp >= mx)
					return;
				m_table.setCurrentPage(cp);
			}
		});
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
	 * @return
	 * @throws Exception
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
	public void selectionUIChanged(@Nonnull TableModelTableBase< ? > tbl) throws Exception {
		redraw();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handle changes to the table.						*/
	/*--------------------------------------------------------------*/

	private void redraw() throws Exception {
		if(m_buttonDiv == null)
			return;

		int cp = m_table.getCurrentPage();
		int np = m_table.getPageCount();
		int rowsAsked = -1;
		if(np == 0) {
			m_txt.setText("");
			setDisplay(DisplayType.NONE);
		} else {
			int rows = rowsAsked = m_table.getModel().getRows();
			m_txt.setText(Msgs.BUNDLE.formatMessage(Msgs.UI_PAGER_TEXT, Integer.valueOf(cp + 1), Integer.valueOf(np), Integer.valueOf(rows)));
			setDisplay(DisplayType.BLOCK);
		}

		if(cp <= 0) {
			m_firstBtn.setCssClass("ui-dp-nav-f-dis");
			m_prevBtn.setCssClass("ui-dp-nav-p-dis");
		} else {
			m_firstBtn.setCssClass("ui-dp-nav-f");
			m_prevBtn.setCssClass("ui-dp-nav-p");
		}

		if(cp + 1 >= np) {
			m_lastBtn.setCssClass("ui-dp-nav-l-dis");
			m_nextBtn.setCssClass("ui-dp-nav-n-dis");
		} else {
			m_lastBtn.setCssClass("ui-dp-nav-l");
			m_nextBtn.setCssClass("ui-dp-nav-n");
		}
		int tc = m_table.getTruncatedCount();						// FIXME jal 20160125 Should be an isTruncated property, as the count is just model.size.
		if(tc > 0) {
			if(m_truncated == null) {
				m_truncated = new Img();
				m_truncated.setSrc("THEME/nav-overflow.png");
				m_truncated.setTitle(Msgs.BUNDLE.formatMessage(Msgs.UI_PAGER_OVER, Integer.valueOf(tc)));
				m_truncated.setCssClass("ui-dp-nav-pgr-ovf");
				m_textDiv.add(m_truncated);
			}
		} else {
			if(m_truncated != null) {
				m_truncated.remove();
				m_truncated = null;
			}
		}
		//System.err.println("DataPager: redraw() called, currentPage=" + cp + ", pageCount=" + np + ", rowsAsked=" + rowsAsked);
		if(isShowSelection() && getSelectableTable() != null) {
			redrawSelectionButtons();
		}
	}

	private void redrawSelectionButtons() throws Exception {
		//-- Show/hide the "show selection" button
		final ISelectableTableComponent<Object> dt = (ISelectableTableComponent<Object>) getSelectableTable();
		if(null == dt)
			throw new IllegalStateException("Null selectable table?");

		if(isNeedSelectionButton()) {
			if(m_showSelectionBtn == null) {
				m_showSelectionBtn = new SmallImgButton("THEME/dpr-select-on.png");
				m_buttonDiv.add(4, m_showSelectionBtn); // Always after last navigation button
				m_showSelectionBtn.setClicked(new IClicked<NodeBase>() {
					@Override
					public void clicked(@Nonnull NodeBase clickednode) throws Exception {
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

	public void addButton(final String image, final IClicked<DataPager> click, final BundleRef bundle, final String ttlkey) {
		SmallImgButton i = new SmallImgButton(image, new IClicked<SmallImgButton>() {
			@Override
			public void clicked(final @Nonnull SmallImgButton b) throws Exception {
				click.clicked(DataPager.this);
			}
		});
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
	public void modelChanged(final @Nonnull TableModelTableBase< ? > tbl, final @Nullable ITableModel< ? > old, final @Nullable ITableModel< ? > nw) throws Exception {
		forceRebuild();										// jal See bugzilla 7383: table queries done twice
		m_buttonDiv = null;									// Odd thing indicating that control is unbuilt, apparently
		//redraw();
	}

	@Override
	public void pageChanged(final @Nonnull TableModelTableBase< ? > tbl) throws Exception {
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

	public void addButton(@Nonnull SmallImgButton sib) {
		m_extraButtonList.add(sib);
		forceRebuild();
	}

	public void addButton(@Nonnull String img, @Nonnull IClicked<SmallImgButton> clicked) {
		m_extraButtonList.add(new SmallImgButton(img, clicked));
	}
}

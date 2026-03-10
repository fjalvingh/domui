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
import to.etc.function.IExecute;
import to.etc.domui.util.Msgs;
import to.etc.webapp.nls.BundleRef;
import to.etc.webapp.nls.IBundleCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Datapager using buttons and a page number list.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 2, 2019
 */
final public class DataPager2 extends Div implements IDataTablePager {
	private Button m_prevBtn;

	private Button m_nextBtn;

	private IPageableComponent m_table;

	//private PageableTabularComponentBase< ? > m_table;

	/**
	 * When set (default) this shows selection details when a table has a selectable model.
	 */
	private boolean m_showSelection = true;

	private Div m_buttonContainer;

	private Div m_buttonDiv = new Div();

	@NonNull
	private List<SmallImgButton> m_extraButtonList = new ArrayList<>();

	private boolean m_showAlways;

	/**
	 * Sentinel value used in the slots array to represent an ellipsis.
	 * Example: [0, -1, 6, 7, 8, 9, 10, 11, 12, -1, 47] renders as:
	 * [ 1] [..] [ 7] [ 8] [ 9] [10] [11] [12] [13] [..] [48]
	 */
	private static final int ELLIPSIS = -1;

	/**
	 * Total number of fixed slots in the pager.
	 */
	private static final int TOTAL_SLOTS = 11;

	/**
	 * Number of pages to show on each side of the current page.
	 * For example, if HALF=3 and the current page is 10, then pages
	 * 7, 8, 9, 10, 11, 12, 13 must all be shown (3 left + current + 3 right).
	 */
	private static final int HALF = 3;

	public DataPager2(final IPageableComponent tbl) {
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
	private ISelectableTableComponent<?> getSelectableTable() {
		if(m_table instanceof ISelectableTableComponent<?>)
			return (ISelectableTableComponent<?>) m_table;
		return null;
	}

	@Nullable
	private ISelectionModel<?> getSelectionModel() {
		ISelectableTableComponent<?> stm = getSelectableTable();
		if(null == stm)
			return null;
		return stm.getSelectionModel();
	}

	/**
	 * Return T if the "show selection UI" button should be visible.
	 */
	private boolean isNeedSelectionButton() throws Exception {
		ISelectionModel<?> sm = getSelectionModel();
		if(sm == null || !m_showSelection)
			return false;
		if(!sm.isMultiSelect())
			return false;
		ISelectableTableComponent<?> tc = getSelectableTable();
		if(null == tc)
			throw new IllegalStateException("Null selectable table?");
		if(tc.isMultiSelectionVisible())
			return false;
		return tc.getModel() != null && tc.getModel().getRows() != 0;
	}

	@Override
	public void selectionUIChanged(@NonNull IPageableComponent tbl) throws Exception {
		redraw();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handle changes to the table.						*/
	/*--------------------------------------------------------------*/

	private void redraw() throws Exception {
		if(!isBuilt())
			return;
		Div bd = m_buttonDiv;
		int np = m_table.getPageCount();
		if(np <= 1 && !m_showAlways) {
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
		 * Render page numbers using a fixed-slot pager algorithm.
		 *
		 * We use TOTAL_SLOTS (11) fixed-size slots. Slot 1 always shows page 1,
		 * slot 11 always shows the last page. The current page is centered when
		 * possible with +/-HALF (3) pages visible on each side.
		 *
		 * An ellipsis is shown when there is a gap in page numbers.
		 *
		 * This keeps the Prev/Next buttons at a fixed position at all times.
		 */
		int[] slots = computePagerSlots(cp, np);
		renderButtons(bd, slots, cp);

		bd.add(m_nextBtn);

		for(@NonNull SmallImgButton sib : m_extraButtonList) {
			bd.add(sib);
			sib.addCssClass("ui-dp2-btn");
		}

		if(m_table instanceof PageableTabularComponentBase<?>) {
			PageableTabularComponentBase<?> tbl = (PageableTabularComponentBase<?>) m_table;
			Span reco = new Span();
			reco.addCssClass("ui-dp2-nurec");
			reco.add(Msgs.uiPagerRecordCount.format(tbl.getResultCount()));
			bd.add(reco);
			if(tbl.isTruncated()) {
				Div node = new Div("ui-dp2-trunc");
				bd.add(node);
				node.setTitle(Msgs.uiPagerOverflow2.getString());
			}
		}
	}


	/**
	 * Compute the pager slots for the given current page and total page count.
	 * All page indices are 0-based.
	 * Returns an array of length TOTAL_SLOTS (or less if totalPages &lt; TOTAL_SLOTS),
	 * where each element is either a 0-based page index or ELLIPSIS (-1).
	 * The algorithm uses three regimes:
	 * - Near start: consecutive pages from 0, ellipsis, then end pages.
	 * - Middle: [0] [..] [cur-3]..[cur+3] [..] [last].
	 * - Near end: start pages, ellipsis, consecutive pages to last (mirror of near start).
	 */
	private static int[] computePagerSlots(int currentPage, int totalPages) {
		if(totalPages <= TOTAL_SLOTS) {
			// Few enough pages: just show all of them, no ellipsis needed
			int[] slots = new int[totalPages];
			for(int i = 0; i < totalPages; i++) {
				slots[i] = i;
			}
			return slots;
		}

		int[] slots = new int[TOTAL_SLOTS];
		int last = totalPages - 1;

		int coreStart = currentPage - HALF;
		int coreEnd = currentPage + HALF;

		// How many consecutive pages from the start must we show?
		// At minimum current+HALF+1 (pages 0..current+HALF) to satisfy the +- 3 rule.
		// But also at least (TOTAL_SLOTS-1)/2 to keep the pager symmetric when near the start.
		int leftRun = Math.max(coreEnd + 1, (TOTAL_SLOTS - 1) / 2);

		// How many consecutive pages from the end must we show? (mirror)
		int rightRun = Math.max(last - coreStart + 1, (TOTAL_SLOTS - 1) / 2);

		if(leftRun <= TOTAL_SLOTS - 2) {
			// Near start: show pages 0..leftRun-1, ellipsis, then fill from end
			int rightCount = TOTAL_SLOTS - leftRun - 1; // -1 for ellipsis
			fillWithEllipsis(slots, leftRun, rightCount, last);
		} else if(rightRun <= TOTAL_SLOTS - 2) {
			// Near end: fill from start, ellipsis, then consecutive to last (mirror)
			int leftCount = TOTAL_SLOTS - rightRun - 1; // -1 for ellipsis
			fillWithEllipsis(slots, leftCount, rightRun, last);
		} else {
			// Middle: two ellipses, core centered on currentPage
			int idx = 0;
			slots[idx++] = 0;
			slots[idx++] = ELLIPSIS;
			for(int p = coreStart; p <= coreEnd; p++) {
				slots[idx++] = p;
			}
			slots[idx++] = ELLIPSIS;
			slots[idx] = last;
		}

		return slots;
	}

	/**
	 * Fill the slots array with leftCount consecutive pages from the start,
	 * an ellipsis, and rightCount consecutive pages ending at last.
	 */
	private static void fillWithEllipsis(int[] slots, int leftCount, int rightCount, int last) {
		int idx = 0;
		for(int i = 0; i < leftCount; i++) {
			slots[idx++] = i;
		}
		slots[idx++] = ELLIPSIS;
		for(int i = last - rightCount + 1; i <= last; i++) {
			slots[idx++] = i;
		}
	}


	/**
	 * Render the computed slots into the button div.
	 *
	 * @param bd the button div to add buttons/ellipsis to
	 * @param slots the computed slot array (0-based page indices or ELLIPSIS)
	 * @param currentPage the current page (0-based)
	 */
	private void renderButtons(Div bd, int[] slots, int currentPage) {
		for(int slot : slots) {
			if(slot == ELLIPSIS) {
				bd.add(Icon.faEllipsisH.createNode().css("ui-dp2-ellipsis"));
			} else {
				Button b;
				if(slot == currentPage) {
					b = new Button("ui-dp2-btn ui-dp2-pn ui-dp2-cp");
				} else {
					b = new Button("ui-dp2-btn ui-dp2-pn");
				}
				b.add(Integer.toString(slot + 1)); // display as 1-based
				final int pageIndex = slot;
				b.setClicked(clickednode -> m_table.setCurrentPage(pageIndex));
				bd.add(b);
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
	public void modelChanged(final @NonNull TableModelTableBase<?> tbl, final @Nullable ITableModel<?> old, final @Nullable ITableModel<?> nw) throws Exception {
		forceRebuild();                                        // jal See bugzilla 7383: table queries done twice
		m_buttonDiv = null;                                    // Odd thing indicating that control is unbuilt, apparently
		//redraw();
	}

	@Override
	public void pageChanged(final @NonNull IPageableComponent tbl) throws Exception {
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

	public boolean isShowAlways() {
		return m_showAlways;
	}

	@Override
	public void setShowAlways(boolean showAlways) {
		m_showAlways = showAlways;
	}

	/**
	 * Main method to print all pager states for verification.
	 * Usage: java DataPager2 [totalPages]
	 * Defaults to 48 pages if no argument given.
	 */
	public static void main(String[] args) {
		int totalPages = 48;
		if(args.length > 0) {
			totalPages = Integer.parseInt(args[0]);
		}

		System.out.println("DataPager2 - Pager states for " + totalPages + " pages, " + TOTAL_SLOTS + " slots, HALF=" + HALF);
		System.out.println();

		for(int page = 0; page < totalPages; page++) {
			int[] slots = computePagerSlots(page, totalPages);
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("Page %2d: ", page + 1));
			for(int i = 0; i < slots.length; i++) {
				if(i > 0)
					sb.append(' ');
				if(slots[i] == ELLIPSIS) {
					sb.append("[..]");
				} else if(slots[i] == page) {
					sb.append(String.format("[*%d*]", slots[i] + 1));
				} else {
					sb.append(String.format("[%2d]", slots[i] + 1));
				}
			}

			// Validate slot count
			if(slots.length != TOTAL_SLOTS && totalPages > TOTAL_SLOTS) {
				sb.append("  *** ERROR: ").append(slots.length).append(" slots ***");
			}

			// Validate +/-3 rule
			for(int delta = -HALF; delta <= HALF; delta++) {
				int target = page + delta;
				if(target >= 0 && target < totalPages) {
					boolean found = false;
					for(int s : slots) {
						if(s == target) {
							found = true;
							break;
						}
					}
					if(!found) {
						sb.append("  *** MISSING page ").append(target + 1).append(" ***");
					}
				}
			}

			System.out.println(sb);
		}
	}
}

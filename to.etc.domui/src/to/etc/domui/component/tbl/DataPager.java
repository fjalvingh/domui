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

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

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
	private SmallImgButton m_firstBtn;

	private SmallImgButton m_prevBtn;

	private SmallImgButton m_nextBtn;

	private SmallImgButton m_lastBtn;

	private SmallImgButton m_showSelectionBtn;

	private Img m_truncated;

	TabularComponentBase< ? > m_table;

	private TextNode m_txt;

	private Div m_textDiv;

	private Div m_buttonDiv;

	private String m_nextImg, m_nextDisImg;

	private String m_prevImg, m_prevDisImg;

	private String m_firstImg, m_firstDisImg;

	private String m_lastImg, m_lastDisImg;

	private String m_overflowImg;

	/** When set (default) this shows selection details when a table has a selectable model. */
	private boolean m_showSelection = true;

	public DataPager() {}

	public DataPager(final TabularComponentBase< ? > tbl) {
		m_table = tbl;
		tbl.addChangeListener(this);
	}

	@Override
	public void createContent() throws Exception {
		init();

		//-- The text part: message
		Div d = new Div();
		add(d);
		d.setFloat(FloatType.RIGHT);
		m_txt = new TextNode();
		d.add(m_txt);
		m_textDiv = d;

		Div btn = new Div();
		m_buttonDiv = btn;
		add(btn);
		btn.setCssClass("ui-szless");
		m_firstBtn = new SmallImgButton();
		btn.add(m_firstBtn);
		m_prevBtn = new SmallImgButton();
		btn.add(m_prevBtn);
		m_nextBtn = new SmallImgButton();
		btn.add(m_nextBtn);
		m_lastBtn = new SmallImgButton();
		btn.add(m_lastBtn);

		if(m_showSelection) {
			if(m_table instanceof DataTable) { // Fixme needs interface
				final DataTable< ? > dt = (DataTable< ? >) m_table;
				if(dt.getSelectionModel() != null && dt.getSelectionModel().isMultiSelect()) {
					m_showSelectionBtn = new SmallImgButton("THEME/dpr-select-on.png");
					btn.add(m_showSelectionBtn);
					m_showSelectionBtn.setClicked(new IClicked<NodeBase>() {
						@Override
						public void clicked(NodeBase clickednode) throws Exception {
							dt.setShowSelectionAlways(true);
							clickednode.remove();
						}
					});
					m_showSelectionBtn.setTitle(Msgs.BUNDLE.getString("ui.dpr.selections"));

					SmallImgButton sb = new SmallImgButton("THEME/dpr-select-all.png");
					btn.add(sb);
					sb.setTitle(Msgs.BUNDLE.getString("ui.dpr.all"));
					sb = new SmallImgButton("THEME/dpr-select-none.png");
					btn.add(sb);
					sb.setTitle(Msgs.BUNDLE.getString("ui.dpr.none"));
				}
			}
		}

		redraw();

		//-- Click handlers for paging.
		m_firstBtn.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(final NodeBase b) throws Exception {
				m_table.setCurrentPage(0);
			}
		});
		m_lastBtn.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(final NodeBase b) throws Exception {
				int pg = m_table.getPageCount();
				if(pg == 0)
					return;
				m_table.setCurrentPage(pg - 1);
			}
		});
		m_prevBtn.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(final NodeBase b) throws Exception {
				int cp = m_table.getCurrentPage();
				if(cp <= 0)
					return;
				m_table.setCurrentPage(cp - 1);
			}
		});
		m_nextBtn.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(final NodeBase b) throws Exception {
				int cp = m_table.getCurrentPage();
				int mx = m_table.getPageCount();
				cp++;
				if(cp >= mx)
					return;
				m_table.setCurrentPage(cp);
			}
		});
	}

	private boolean hasSelectionModel() {
		if(m_table == null)
			return false;
		if(m_table instanceof DataTable) { // Fixme needs interface
			DataTable< ? > dt = (DataTable< ? >) m_table;
			return dt.getSelectionModel() != null && dt.getSelectionModel().isMultiSelect();
		}
		return false;
	}

	private void init() throws Exception {
		if(m_nextImg != null)
			return;

		Map<String, Object> map = DomApplication.get().getThemeMap(null);
		m_nextImg = get(map, "dpr_next", "THEME/nav-next.png");
		m_prevImg = get(map, "dpr_prev", "THEME/nav-prev.png");
		m_firstImg = get(map, "dpr_first", "THEME/nav-first.png");
		m_lastImg = get(map, "dpr_last", "THEME/nav-last.png");

		m_nextDisImg = get(map, "dpr_dis_next", "THEME/nav-next-dis.png");
		m_prevDisImg = get(map, "dpr_dis_prev", "THEME/nav-prev-dis.png");
		m_firstDisImg = get(map, "dpr_dis_first", "THEME/nav-first-dis.png");
		m_lastDisImg = get(map, "dpr_dis_last", "THEME/nav-last-dis.png");

		m_overflowImg = get(map, "dpr_overflow", "THEME/nav-overflow.png");
	}

	private static String enc(String in) {
		return in;

		//		return in.replace("&", "&amp;");
	}

	private String get(Map<String, Object> map, String key, String def) {
		Object v = map.get(key);
		if(null == v)
			return enc(def);
		if(v instanceof String) {
			return enc((String) v);
		}
		throw new IllegalArgumentException("Bad key value for " + key + " in style.properties: expected string, got " + v);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handle changes to the table.						*/
	/*--------------------------------------------------------------*/

	private void redraw() throws Exception {
		int cp = m_table.getCurrentPage();
		int np = m_table.getPageCount();
		if(np == 0)
			// mtesic:there is already 'There are no results' message inside DataCellTable
			// m_txt.setText(NlsContext.getGlobalMessage(Msgs.UI_PAGER_EMPTY));
			m_txt.setText("");
		else
			m_txt.setText(Msgs.BUNDLE.formatMessage(Msgs.UI_PAGER_TEXT, Integer.valueOf(cp + 1), Integer.valueOf(np), Integer.valueOf(m_table.getModel().getRows())));

		if(cp <= 0) {
			m_firstBtn.setSrc(m_firstDisImg);
			m_prevBtn.setSrc(m_prevDisImg);
		} else {
			m_firstBtn.setSrc(m_firstImg);
			m_prevBtn.setSrc(m_prevImg);
		}

		if(cp + 1 >= np) {
			m_lastBtn.setSrc(m_lastDisImg);
			m_nextBtn.setSrc(m_nextDisImg);
		} else {
			m_lastBtn.setSrc(m_lastImg);
			m_nextBtn.setSrc(m_nextImg); // "THEME/go-next-view.png.svg?w=16&h=16");
		}
		int tc = m_table.getTruncatedCount();
		if(tc > 0) {
			if(m_truncated == null) {
				m_truncated = new Img();
				m_truncated.setSrc(m_overflowImg);
				m_truncated.setTitle(Msgs.BUNDLE.formatMessage(Msgs.UI_PAGER_OVER, Integer.valueOf(tc)));
				m_textDiv.add(m_truncated);
			}
		} else {
			if(m_truncated != null) {
				m_truncated.remove();
				m_truncated = null;
			}
		}
	}

	public Div getButtonDiv() {
		return m_buttonDiv;
	}

	public void addButton(final String image, final IClicked<DataPager> click, final BundleRef bundle, final String ttlkey) {
		SmallImgButton i = new SmallImgButton(image, new IClicked<SmallImgButton>() {
			@Override
			public void clicked(final SmallImgButton b) throws Exception {
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
	public void modelChanged(final TabularComponentBase< ? > tbl, final ITableModel< ? > old, final ITableModel< ? > nw) throws Exception {
		redraw();
	}

	@Override
	public void pageChanged(final TabularComponentBase< ? > tbl) throws Exception {
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
}

package to.etc.domui.component.tbl;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
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

	private Img m_truncated;

	TabularComponentBase m_table;

	private TextNode m_txt;

	private Div m_textDiv;

	private Div m_buttonDiv;

	public DataPager() {}

	public DataPager(final TabularComponentBase tbl) {
		m_table = tbl;
		tbl.addChangeListener(this);
	}

	@Override
	public void createContent() throws Exception {
		//-- The text part: message
		Div d = new Div();
		add(d);
		d.setFloat(FloatType.RIGHT);
		m_txt = new TextNode();
		d.add(m_txt);
		m_textDiv = d;
		//		if(m_table != null) {
		//			m_txt.setText("Pagina "+(m_table.getCurrentPage()+1)+" van "+m_table.getPageCount());
		//		}

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
		redraw();

		//-- Click handlers for paging.
		m_firstBtn.setClicked(new IClicked<NodeBase>() {
			public void clicked(final NodeBase b) throws Exception {
				m_table.setCurrentPage(0);
			}
		});
		m_lastBtn.setClicked(new IClicked<NodeBase>() {
			public void clicked(final NodeBase b) throws Exception {
				int pg = m_table.getPageCount();
				if(pg == 0)
					return;
				m_table.setCurrentPage(pg - 1);
			}
		});
		m_prevBtn.setClicked(new IClicked<NodeBase>() {
			public void clicked(final NodeBase b) throws Exception {
				int cp = m_table.getCurrentPage();
				if(cp <= 0)
					return;
				m_table.setCurrentPage(cp - 1);
			}
		});
		m_nextBtn.setClicked(new IClicked<NodeBase>() {
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
			m_txt.setText(NlsContext.getGlobalMessage(Msgs.UI_PAGER_TEXT, Integer.valueOf(cp + 1), Integer.valueOf(np), Integer.valueOf(m_table.getModel().getRows())));

		if(cp <= 0) {
			m_firstBtn.setSrc("THEME/nav-first-dis.png");
			m_prevBtn.setSrc("THEME/nav-prev-dis.png");
		} else {
			m_firstBtn.setSrc("THEME/nav-first.png");
			m_prevBtn.setSrc("THEME/nav-prev.png");
		}

		if(cp + 1 >= np) {
			m_lastBtn.setSrc("THEME/nav-last-dis.png");
			m_nextBtn.setSrc("THEME/nav-next-dis.png");
		} else {
			m_lastBtn.setSrc("THEME/nav-last.png");
			m_nextBtn.setSrc("THEME/nav-next.png");
		}
		int tc = m_table.getTruncatedCount();
		if(tc > 0) {
			if(m_truncated == null) {
				m_truncated = new Img();
				m_truncated.setSrc("THEME/nav-overflow.png");
				m_truncated.setTitle(NlsContext.getGlobalMessage(Msgs.UI_PAGER_OVER, Integer.valueOf(tc)));
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
	public void modelChanged(final TabularComponentBase tbl, final ITableModel< ? > old, final ITableModel< ? > nw) throws Exception {
		redraw();
	}

	public void pageChanged(final TabularComponentBase tbl) throws Exception {
		redraw();
	}
}

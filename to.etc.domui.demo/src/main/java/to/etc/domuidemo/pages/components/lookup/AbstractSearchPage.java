package to.etc.domuidemo.pages.components.lookup;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-2-18.
 */
public class AbstractSearchPage<T> extends UrlPage {
	final private Class<T> m_clazz;

	private DataTable<T> m_table;

	private ContentPanel m_cp;

	public AbstractSearchPage(Class<T> clazz) {
		m_clazz = clazz;
	}

	/**
	 * The panel holding this page's content; it provides the page padding.
	 */
	protected ContentPanel contentPanel() {
		ContentPanel cp = m_cp;
		if(null == cp) {
			cp = m_cp = new ContentPanel();
			add(cp);
		}
		return cp;
	}

	protected void search(SearchPanel<T> lf) throws Exception {
		QCriteria<T> criteria = lf.getCriteria();
		if(criteria == null) {					// Nothing entered or error
			return;
		}

		search(criteria);
	}

	protected void search(QCriteria<T> criteria) {
		if(null == criteria)
			return;
		SimpleSearchModel<T> model = new SimpleSearchModel<T>(this, criteria);

		DataTable<T> table = m_table;
		if(null == table) {
			RowRenderer<T> rr = createRowRenderer();
			table = m_table = new DataTable<>(model, rr);
			contentPanel().add(table);
			contentPanel().add(new DataPager(table));
			table.setPageSize(10);
		} else {
			table.setModel(model);
		}
	}

	private RowRenderer<T> createRowRenderer() {
		RowRenderer<T> rr = new RowRenderer<>(m_clazz);
		return rr;
	}


}

package to.etc.domuidemo.pages.searchpanel;

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

	public AbstractSearchPage(Class<T> clazz) {
		m_clazz = clazz;
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
			add(table);
			add(new DataPager(table));
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

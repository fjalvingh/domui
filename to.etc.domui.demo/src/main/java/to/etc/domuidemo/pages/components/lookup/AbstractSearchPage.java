package to.etc.domuidemo.pages.components.lookup;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

/**
 * Base for the SearchPanel demo pages: it only knows how to show the result of a
 * search. The page itself builds its search panel and decides where the result
 * goes, by handing the container to show it in to {@link #showResult}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-2-18.
 */
public class AbstractSearchPage<T> extends UrlPage {
	final private Class<T> m_clazz;

	public AbstractSearchPage(Class<T> clazz) {
		m_clazz = clazz;
	}

	/**
	 * Run the criteria the search panel produced, and show what it returns inside
	 * the container passed in - replacing the result of an earlier search.
	 */
	protected void showResult(@NonNull NodeContainer target, @Nullable QCriteria<T> criteria) throws Exception {
		if(null == criteria)					// Nothing entered, or an input error
			return;
		DataTable<T> table = new DataTable<>(new SimpleSearchModel<>(this, criteria), new RowRenderer<>(m_clazz));
		table.setPageSize(10);

		target.removeAllChildren();
		target.add(table);
		target.add(new DataPager(table));
	}
}

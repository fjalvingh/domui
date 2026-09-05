package to.etc.domuidemo.pages.cddb;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

/**
 * Base for the CD shop's "find a record" screens: a search panel on top, and the
 * result table below it which appears only after the first search. Clicking a
 * result row opens the detail screen for that record.
 *
 * The search fields and the result columns both come from the metadata on the
 * entity, so a subclass usually only needs to say where a row click goes to.
 */
abstract public class AbstractCdShopListPage<T> extends UrlPage {
	@NonNull
	private final Class<T> m_dataClass;

	@NonNull
	private final String m_title;

	protected AbstractCdShopListPage(@NonNull Class<T> dataClass, @NonNull String title) {
		m_dataClass = dataClass;
		m_title = title;
	}

	/**
	 * Called when a result row is clicked; open the detail screen from here.
	 */
	abstract protected void onRowSelected(@NonNull T instance) throws Exception;

	/**
	 * Override to define the result columns. The default uses the columns from
	 * the entity's metadata.
	 */
	protected void configureColumns(@NonNull RowRenderer<T> rr) throws Exception {
	}

	/**
	 * Override to allow creating a new record from the search screen.
	 */
	protected void onNew() throws Exception {
	}

	protected boolean isNewAllowed() {
		return false;
	}

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, m_title));

		SearchPanel<T> sp = new SearchPanel<>(m_dataClass);
		cp.add(sp);

		//-- The results appear here; both nodes are local, so a rebuild of the page
		//-- makes new ones and the handler below refers to those.
		Div results = new Div();
		cp.add(results);

		sp.setClicked(a -> showResult(results, sp.getCriteria()));
		if(isNewAllowed()) {
			sp.setOnNew(a -> onNew());
		}
	}

	private void showResult(@NonNull NodeContainer target, @Nullable QCriteria<T> criteria) throws Exception {
		if(null == criteria)								// Nothing entered, or an input error
			return;
		RowRenderer<T> rr = new RowRenderer<>(m_dataClass);
		configureColumns(rr);
		rr.setRowClicked(this::onRowSelected);

		DataTable<T> table = new DataTable<>(new SimpleSearchModel<>(this, criteria), rr);
		table.setPageSize(20);

		target.removeAllChildren();
		target.add(table);
		target.add(new DataPager(table));
	}

	@NonNull
	protected Class<T> getDataClass() {
		return m_dataClass;
	}
}

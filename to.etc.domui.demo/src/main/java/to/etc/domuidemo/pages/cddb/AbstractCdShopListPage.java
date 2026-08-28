package to.etc.domuidemo.pages.cddb;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.dom.html.HTag;
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

	@Nullable
	private ContentPanel m_cp;

	@Nullable
	private DataTable<T> m_table;

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
		ContentPanel cp = m_cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, m_title));

		SearchPanel<T> sp = new SearchPanel<>(m_dataClass);
		cp.add(sp);
		sp.setClicked(a -> search(sp.getCriteria()));
		if(isNewAllowed()) {
			sp.setOnNew(a -> onNew());
		}
	}

	private void search(@Nullable QCriteria<T> criteria) throws Exception {
		if(null == criteria)								// Nothing entered, or an input error
			return;
		SimpleSearchModel<T> model = new SimpleSearchModel<>(this, criteria);

		DataTable<T> table = m_table;
		if(null != table) {								// Result table already present -> just requery
			table.setModel(model);
			return;
		}

		RowRenderer<T> rr = new RowRenderer<>(m_dataClass);
		configureColumns(rr);
		rr.setRowClicked(this::onRowSelected);

		table = m_table = new DataTable<>(model, rr);
		table.setPageSize(20);
		contentPanel().add(table);
		contentPanel().add(new DataPager(table));
	}

	@NonNull
	protected ContentPanel contentPanel() {
		ContentPanel cp = m_cp;
		if(null == cp)
			throw new IllegalStateException("The content panel is not yet created");
		return cp;
	}

	@NonNull
	protected Class<T> getDataClass() {
		return m_dataClass;
	}
}

package my.domui.app.ui.pages.base;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.Panel;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.ITableModel;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.themes.Theme;
import to.etc.domui.util.DomUtil;
import to.etc.webapp.query.IIdentifyable;
import to.etc.webapp.query.QCriteria;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Very simple basic code to create a list page, with protected customize methods
 * to customize.
 */
@DefaultNonNull
abstract public class AbstractListPage<T extends IIdentifyable<?>> extends BasicPage {
	final private Class<T> m_typeClass;

	@Nullable
	private DataTable<T> m_table;

	private Panel m_panel = new Panel();

	private boolean m_optionSearchImmediately;

	private boolean m_optionEmptySearch;

	public AbstractListPage(Class<T> typeClass) {
		m_typeClass = typeClass;
	}

	@Override public void createContent() throws Exception {
		super.createContent();

		createPageHeader();
		m_panel = createRootPanel();
		add(m_panel);

		QCriteria<T> criteria = createBaseCriteria();
		SearchPanel<T> lf = createLookupForm(criteria);
		m_panel.add(lf);
		customize(lf);
		appendLookupFormButtons(lf);

		lf.setClicked(nodeBase -> search(lf));

		if(m_optionSearchImmediately) {
			search(criteria);
		}
	}

	private void appendLookupFormButtons(SearchPanel<T> lf) throws Exception {
		if(DomUtil.hasOverridden(getClass(), AbstractListPage.class, "onNew")) {
			DefaultButton newButton = lf.getButtonFactory().addButton($("newButton"), Theme.BTN_NEW, b -> onNew(), 600);
			newButton.bind("disabledBecause").to(this, "newDisabledBecause");
		}
	}

	@Nullable
	public String isNewDisabledBecause() {
		return null;
	}

	protected Panel createRootPanel() {
		return new Panel();
	}

	protected void search(SearchPanel<T> lf) throws Exception {
		QCriteria<T> criteria = lf.getCriteria();
		if(null == criteria) {
			return;
		}
		if(! lf.hasUserDefinedCriteria() && ! m_optionEmptySearch && ! m_optionSearchImmediately) {
			MsgBox.error(this, "Vul eerst een of meer zoekvelden in");
			return;
		}
		search(criteria);
	}

	protected void search(QCriteria<T> criteria) throws Exception {
		customize(criteria);
		SimpleSearchModel<T> ssm = createTableModel(criteria);
		search(ssm);
	}

	@Nonnull
	protected SimpleSearchModel<T> createTableModel(QCriteria<T> criteria) {
		return new SimpleSearchModel<>(this, criteria);
	}

	protected void search(ITableModel<T> ssm) throws Exception {
		DataTable<T> table = m_table;
		if(null == table) {
			RowRenderer<T> rr = createRowRenderer();
			customize(rr);
			customizeRowClicked(rr);
			table = m_table = new DataTable<>(ssm, rr);
			m_panel.add(table);
			m_panel.add(new DataPager(table));
			table.setPageSize(20);

			if(DomUtil.hasOverridden(getClass(), AbstractListPage.class, "onSelect", m_typeClass)) {
				rr.setRowClicked(this::onSelect);
			}
		} else {
			table.setModel(ssm);
		}
	}

	/**
	 * If the row renderer has no click handler set add one provided onSelect() has been
	 * overridden.
	 */
	protected void customizeRowClicked(RowRenderer<T> rr) {
		if(rr.getRowClicked() == null) {
			if(DomUtil.hasOverridden(getClass(), AbstractListPage.class, "onSelect", m_typeClass)) {
				rr.setRowClicked(c -> onSelect(c));
			}
		}
	}

	@Nonnull protected RowRenderer<T> createRowRenderer() {
		RowRenderer<T> rr = new RowRenderer<>(m_typeClass);
		return rr;
	}

	/**
	 * Override to add extra stuff to a LookupForm or to change its behavior.
	 */
	protected void customize(SearchPanel<T> lf) throws Exception {
	}

	/**
	 * Override to add extra selection criteria. By default this adds the organization criterium
	 * if the target table has an organisation property. Just override this with empty to skip
	 * that.
	 */
	protected void customize(QCriteria<T> criteria) throws Exception {
	}

	/**
	 * Customize the row renderer to override the defaults from metadata.
	 */
	protected void customize(RowRenderer<T> rr) throws Exception {
	}

	protected void createPageHeader() throws Exception {
	}

	/**
	 * Override to create a different lookupform.
	 */
	protected SearchPanel<T> createLookupForm(QCriteria<T> criteria) throws Exception {
		SearchPanel<T> lf = new SearchPanel<>(criteria);
		return lf;
	}

	/**
	 * Override to create special criteria.
	 * @return
	 */
	@Nonnull protected QCriteria<T> createBaseCriteria() throws Exception {
		return QCriteria.create(m_typeClass);
	}

	/**
	 * When set the form will show results immediately when opened.
	 */
	public void setOptionSearchImmediately(boolean optionSearchImmediately) {
		m_optionSearchImmediately = optionSearchImmediately;
	}

	/**
	 * When set this allows an empty search (search all).
	 * @param optionEmptySearch
	 */
	public void setOptionEmptySearch(boolean optionEmptySearch) {
		m_optionEmptySearch = optionEmptySearch;
	}

	/**
	 * Override to handle select of a row.
	 */
	protected void onSelect(T selectedRow) {
	}

	/**
	 * Override to add a "new" button which will then call this.
	 */
	protected void onNew() {
	}

	@Override protected void onShelve() throws Exception {
		resetAllSharedContexts();
	}
}

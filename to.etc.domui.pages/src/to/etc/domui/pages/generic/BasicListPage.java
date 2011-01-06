package to.etc.domui.pages.generic;

import to.etc.domui.component.lookup.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.query.*;

/**
 * Generic page handling some cruddy stuff. FIXME Example only; VP specific one should exist.
 *
 * @author vmijic
 * Created on 29 Jul 2009
 */
abstract public class BasicListPage<T> extends BasicPage<T> {
	private LookupForm<T> m_lookupForm;

	private DataTable<T> m_result;

	private DataPager m_pager;

	private boolean m_allowEmptySearch;

	private boolean m_searchImmediately;

	private IQueryHandler<T> m_queryHandler;

	private IRowRenderer<T> m_rowRenderer;

	/**
	 * Implement to handle a selection of a record that was found.
	 * @param rcord
	 * @throws Exception
	 */
	abstract public void onSelect(T rcord) throws Exception;

	/**
	 * Implement to handle pressing the "new record" button.
	 * @throws Exception
	 */
	abstract protected void onNew() throws Exception;

	public BasicListPage(Class<T> clz) {
		super(clz);
	}

	public BasicListPage(Class<T> baseClass, String txt) {
		super(baseClass, txt);
	}

	/**
	 * Override this to customize the lookup form. No need to call super. method.
	 * @param lf
	 */
	protected void customizeLookupForm(LookupForm<T> lf) throws Exception {}

	@Override
	public void createContent() throws Exception {
		super.createContent();

		//-- Lookup thingy.
		m_lookupForm = new LookupForm<T>(getBaseClass());
		add(m_lookupForm);
		m_lookupForm.setClicked(new IClicked<LookupForm<T>>() {
			public void clicked(LookupForm<T> b) throws Exception {
				search(b);
			}
		});
		if(hasEditRight()) {
			m_lookupForm.setOnNew(new IClicked<LookupForm<T>>() {
				public void clicked(LookupForm<T> b) throws Exception {
					onNew();
				}
			});
		}
		m_lookupForm.setOnClear(new IClicked<LookupForm<T>>() {
			public void clicked(LookupForm<T> b) throws Exception {
				onLookupFormClear(b);
			}
		});

		customizeLookupForm(m_lookupForm);

		if(m_result != null) {
			add(m_result);
			add(m_pager);
		}

		if(isSearchImmediately()) {
			search(m_lookupForm);
		}
	}

	void search(LookupForm<T> lf) throws Exception {
		QCriteria<T> c = lf.getEnteredCriteria();
		if(c == null) // Some error has occured?
			return; // Don't do anything (errors will have been registered)
		clearGlobalMessage(Msgs.V_MISSING_SEARCH);
		if(!lf.hasUserDefinedCriteria() && !isAllowEmptySearch()) {
			addGlobalMessage(UIMessage.error(Msgs.BUNDLE, Msgs.V_MISSING_SEARCH)); // Missing inputs
			return;
		} else {
			clearGlobalMessage();
		}
		setTableQuery(c);
	}

	protected void adjustCriteria(QCriteria<T> crit) {}

	private void setTableQuery(QCriteria<T> qc) throws Exception {
		adjustCriteria(qc);
		ITableModel<T> model;
		if(m_queryHandler == null) {
			QDataContextFactory src = QContextManager.getDataContextFactory(getPage());
			model = new SimpleSearchModel<T>(src, qc);
		} else {
			model = new SimpleSearchModel<T>(m_queryHandler, qc);
		}

		if(m_result == null) {
			// Create a table
			m_result = new DataTable<T>(model, getRowRenderer());

			add(m_result);
			m_result.setPageSize(20);
			m_result.setTableWidth("100%");
			m_result.setTestID("resultBasicVpListPage");

			//-- Add the pager,
			m_pager = new DataPager(m_result);

			add(m_pager);
			m_pager.setTestID("pagerBasicVpListPage");

		} else {
			m_result.setModel(model); // Change the model
		}
	}

	@Override
	protected void onShelve() throws Exception {
		QContextManager.closeSharedContext(getPage().getConversation());
	}

	/**
	 * Override to do extra things when the lookupform's "clear" button is pressed. Can be used to
	 * set items to defaults after their input has been cleared. When this is called all inputs in
	 * the form have <i>already</i> been set to null (empty) - so do <b>not</b> call {@link LookupForm#clearInput()}.
	 * @param lf
	 * @throws Exception
	 */
	protected void onLookupFormClear(LookupForm<T> lf) throws Exception {
	//lf.clearInput(); jal 20091002 DO NOT ADD BACK!!!! Pressing the clear button ALREADY CALLS this.
	}

	/**
	 * Get the row renderer to use for the request.
	 * @return
	 */
	public IRowRenderer<T> getRowRenderer() throws Exception {
		if(m_rowRenderer == null) {
			m_rowRenderer = new BasicRowRenderer<T>(getBaseClass()); // Create a default one
		}

		//-- jal 20091111 It is required that any search result has clickable rows. If no row click handler is set set one to call onNew.
		if(m_rowRenderer instanceof AbstractRowRenderer< ? >) { // Silly ? is needed even though cast cant do anything with it. Idiots.
			AbstractRowRenderer<T> arrh = (AbstractRowRenderer<T>) m_rowRenderer;
			if(arrh.getRowClicked() == null) {
				arrh.setRowClicked(new ICellClicked<T>() {
					public void cellClicked(Page pg, NodeBase tr, T val) throws Exception {
						onSelect(val);
					}
				});
			}
		}
		return m_rowRenderer;
	}

	/**
	 * Override to provide your own Row Renderer. If not set a BasicRowRenderer with reasonable
	 * defaults will be created for you.
	 */
	public void setRowRenderer(IRowRenderer<T> rr) {
		m_rowRenderer = rr;
	}

	/**
	 * When set to TRUE this makes the form immediately execute a query with all
	 * empty lookup fields, meaning it will immediately show a list of rows.
	 * @return
	 */
	public boolean isSearchImmediately() {
		return m_searchImmediately;
	}

	/**
	 * When set to TRUE this makes the form immediately execute a query with all
	 * empty lookup fields, meaning it will immediately show a list of rows.
	 * @param showDefaultSearch
	 */
	public void setSearchImmediately(boolean searchImmediately) {
		m_searchImmediately = searchImmediately;
	}


	/**
	 * When set to T this allows searching a set without any specified criteria.
	 * @return
	 */
	public boolean isAllowEmptySearch() {
		return m_allowEmptySearch;
	}

	/**
	 * When set to T this allows searching a set without any specified criteria.
	 * @param allowEmptySearch
	 */
	public void setAllowEmptySearch(boolean allowEmptySearch) {
		m_allowEmptySearch = allowEmptySearch;
	}

	public boolean hasEditRight() {
		return true;
	}

	protected LookupForm<T> getLookupForm() {
		return m_lookupForm;
	}

	protected void setLookupForm(LookupForm<T> lookupForm) {
		m_lookupForm = lookupForm;
	}

	protected IQueryHandler<T> getQueryHandler() {
		return m_queryHandler;
	}

	protected void setQueryHandler(IQueryHandler<T> queryHandler) {
		m_queryHandler = queryHandler;
	}
}

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
	private DataTable m_result;

	private DataPager m_pager;

	private boolean m_allowEmptySearch;

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
	abstract protected void doNew() throws Exception;

	public BasicListPage(Class<T> clz) {
		super(clz);
	}

	public BasicListPage(Class<T> clz, String titlekey) {
		super(clz, titlekey);
	}

	/**
	 * Override this to customize the lookup form. No need to call super. method.
	 * @param lf
	 */
	protected void customizeLookupForm(LookupForm<T> lf) {}

	/**
	 * Override to provide your own Row Renderer; this version returns a SimpleRowRenderer() using full
	 * metadata for the class.
	 * @return
	 */
	protected SimpleRowRenderer provideRowRenderer() {
		return new SimpleRowRenderer(getBaseClass());
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

	@Override
	public void createContent() throws Exception {
		super.createContent();

		//-- Lookup thingy.
		final LookupForm<T> lf = new LookupForm<T>(getBaseClass());
		add(lf);
		lf.setClicked(new IClicked<LookupForm<T>>() {
			public void clicked(LookupForm<T> b) throws Exception {
				search(b);
			}
		});
		lf.setOnNew(new IClicked<LookupForm<T>>() {
			public void clicked(LookupForm<T> b) throws Exception {
				doNew();
			}
		});

		customizeLookupForm(lf);

		if(m_result != null) {
			add(m_result);
			add(m_pager);
		}
	}

	void search(LookupForm<T> lf) throws Exception {
		QCriteria<T> c = lf.getEnteredCriteria();
		if(c == null) // Some error has occured?
			return; // Don't do anything (errors will have been registered)
		clearGlobalMessage(Msgs.V_MISSING_SEARCH);
		if(!c.hasRestrictions() && !isAllowEmptySearch()) {
			addGlobalMessage(UIMessage.error(Msgs.BUNDLE, Msgs.V_MISSING_SEARCH)); // Missing inputs
			return;
		} else
			clearGlobalMessage();
		setTableQuery(c);
	}

	private void setTableQuery(QCriteria<T> qc) {
		QDataContextFactory src = QContextManager.getDataContextFactory(getPage());
		ITableModel<T> model = new SimpleSearchModel<T>(src, qc);

		if(m_result == null) {
			//-- We do not yet have a result table -> create one.
			SimpleRowRenderer rr = provideRowRenderer();
			m_result = new DataTable(model, rr);
			add(m_result);
			m_result.setPageSize(20);
			m_result.setTableWidth("100%");

			//** FIXME : make some protected overidable method that specifies if row should get row click functionality.
			/*			rr.setRowClicked(new ICellClicked<T>() {
				public void cellClicked(Page pg, NodeBase tr, T val) throws Exception {
					onSelect(val);
				}
			});*/

			//-- Add the pager,
			m_pager = new DataPager(m_result);
			add(m_pager);
		} else {
			m_result.setModel(model); // Change the model
		}
	}

	@Override
	protected void onShelve() throws Exception {
		QContextManager.closeSharedContext(getPage().getConversation());
	}
}

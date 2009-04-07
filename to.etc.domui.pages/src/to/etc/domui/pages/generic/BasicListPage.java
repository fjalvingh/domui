package to.etc.domui.pages.generic;

import to.etc.domui.component.lookup.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.query.*;

/**
 * Generic page handling some cruddy stuff.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 16, 2008
 */
abstract public class BasicListPage<T> extends BasicPage<T> {
	private DataTable			m_result;
	private DataPager			m_pager;
	private boolean				m_allowEmptySearch;

	abstract public void		onSelect(T rcord) throws Exception;
	abstract protected void		doNew() throws Exception;

	public BasicListPage(Class<T> clz, String titlekey) {
		super(clz, titlekey);
	}
	public boolean isAllowEmptySearch() {
		return m_allowEmptySearch;
	}
	public void setAllowEmptySearch(boolean allowEmptySearch) {
		m_allowEmptySearch = allowEmptySearch;
	}
	@Override
	public void createContent() throws Exception {
		super.createContent();

		//-- Lookup thingy.
		final LookupForm<T>	lf	= new LookupForm<T>(getBaseClass());
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
		if(m_result != null) {
			add(m_result);
			add(m_pager);
		}
	}

	void search(LookupForm<T> lf) throws Exception {
		QCriteria<T>	c	= lf.getEnteredCriteria();
		if(c == null)									// Some error has occured?
			return;										// Don't do anything (errors will have been registered)
		clearGlobalMessage(Msgs.V_MISSING_SEARCH);
		if(c.getOperatorCount() == 0 && ! isAllowEmptySearch()) {
			addGlobalMessage(MsgType.ERROR, Msgs.V_MISSING_SEARCH);		// Missing inputs
			return;
		} else
			clearGlobalMessage();
		setTableQuery(c);
	}

	private void	setTableQuery(QCriteria<T> qc) {
		QDataContextSource	src	= QContextManager.getSource(getPage());
		ITableModel<T>		model = new SimpleSearchModel<T>(src, qc);

		if(m_result == null) {
			//-- We do not yet have a result table -> create one.
			SimpleRowRenderer	rr = new SimpleRowRenderer(getBaseClass());
			m_result = new DataTable(model, rr);
			add(m_result);
			m_result.setPageSize(20);
			m_result.setTableWidth("100%");

			rr.setRowClicked(new ICellClicked<T>() {
				public void cellClicked(Page pg, NodeBase tr, T val) throws Exception {
					onSelect(val);
				}
			});

			//-- Add the pager,
			m_pager = new DataPager(m_result);
			add(m_pager);
		} else {
			m_result.setModel(model);				// Change the model
		}
	}
	@Override
	protected void onShelve() throws Exception {
		QContextManager.closeSharedContext(getPage().getConversation());
	}
	
//	protected void	doNew() throws Exception {
//		
//		
//	}
	
}

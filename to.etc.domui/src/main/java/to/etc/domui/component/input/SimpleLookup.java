/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.input;

import to.etc.domui.component.layout.IWindowClosed;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.ICellClicked;
import to.etc.domui.component.tbl.IQueryHandler;
import to.etc.domui.component.tbl.ITableModel;
import to.etc.domui.component.tbl.SimpleRowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.dom.errors.IErrorMessageListener;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IValueSelected;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.Msgs;
import to.etc.webapp.query.QContextManager;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContextFactory;

import javax.annotation.Nonnull;

/**
 * Represents simple lookup dialog that enables single item selection.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Aug 3, 2010
 */

public class SimpleLookup<T> extends AbstractFloatingLookup<T> {
	private SearchPanel<T> m_externalSearchPanel;

	DataTable<T> m_result;

	private IQueryManipulator<T> m_queryManipulator;

	private IQueryHandler<T> m_queryHandler;

	private String m_lookupTitle;

	private String[] m_resultColumns;

	private IErrorMessageListener m_customErrorMessageListener;

	private boolean m_allowEmptyQuery;

	private IValueSelected<T> m_onValueSelected;

	private boolean m_renderAsCollapsed;

	/* temporary solution to allow use of same test IDs as it was used withing LookupInput. */
	private boolean m_usedWithinLookupInput;

	private boolean m_showDefaultSearch;

	public SimpleLookup(Class<T> lookupClass, ClassMetaModel metaModel, String[] resultColumns) {
		this(lookupClass, metaModel);
		m_resultColumns = resultColumns;
	}

	public SimpleLookup(Class<T> lookupClass, String[] resultColumns) {
		this(lookupClass, (ClassMetaModel) null);
		m_resultColumns = resultColumns;
	}

	/**
	 * Lookup a POJO Java bean persistent class.
	 * @param lookupClass
	 */
	public SimpleLookup(Class<T> lookupClass) {
		this(lookupClass, (ClassMetaModel) null);
	}

	public SimpleLookup(Class<T> lookupClass, ClassMetaModel metaModel) {
		super(lookupClass, metaModel);
	}


	@Override
	public void createContent() throws Exception {
		setWindowTitle(getLookupTitle() == null ? Msgs.BUNDLE.getString(Msgs.UI_LUI_TTL) : getLookupTitle());
		super.createContent();

		setHeight("90%");
		setIcon("THEME/btnFind.png");
		if(getTestID() == null) {
			setTestID("simpleLookup");
		}

		//in case when external error message listener is set
		if(m_customErrorMessageListener != null && m_customErrorMessageListener instanceof NodeBase) {
			setErrorFence();
			add((NodeBase) m_customErrorMessageListener);
			DomUtil.getMessageFence(this).addErrorListener(m_customErrorMessageListener);
		}
		SearchPanel<T> lf = getExternalSearchPanel() != null ? getExternalSearchPanel() : new SearchPanel<T>(getLookupClass(), getMetaModel());

		lf.setCollapsed(m_renderAsCollapsed);
		lf.forceRebuild(); // jal 20091002 Force rebuild to remove any state from earlier invocations of the same form. This prevents the form from coming up in "collapsed" state if it was left that way last time it was used (Lenzo).
		add(lf);
		setOnClose(new IWindowClosed() {
			@Override
			public void closed(@Nonnull String closeReason) throws Exception {
				clearGlobalMessage(Msgs.V_MISSING_SEARCH);
				m_result = null;
			}
		});

		lf.setClicked(new IClicked<SearchPanel<T>>() {
			@Override
			public void clicked(@Nonnull SearchPanel<T> b) throws Exception {
				search(b);
			}
		});

		lf.setOnCancel(new IClicked<SearchPanel<T>>() {
			@Override
			public void clicked(@Nonnull SearchPanel<T> b) throws Exception {
				closePressed();
			}
		});

		if(m_showDefaultSearch) {
			search(lf);
		}
	}

	void search(SearchPanel<T> lf) throws Exception {
		QCriteria<T> c = lf.getCriteria();
		if(c == null) // Some error has occured?
			return; // Don't do anything (errors will have been registered)

		if(getQueryManipulator() != null) {
			c = getQueryManipulator().adjustQuery(c); // Adjust the query where needed,
			if(c == null) {
				//in case of cancelled search by query manipulator return null
				return;
			}
		}
		clearGlobalMessage(Msgs.V_MISSING_SEARCH);
		if(!lf.hasUserDefinedCriteria() && !isAllowEmptyQuery()) {
			addGlobalMessage(UIMessage.error(Msgs.BUNDLE, Msgs.V_MISSING_SEARCH)); // Missing inputs
			return;
		} else
			clearGlobalMessage();
		setTableQuery(c);
	}

	private void setTableQuery(QCriteria<T> qc) {
		ITableModel<T> model;
		if(m_queryHandler == null) {
			QDataContextFactory src = QContextManager.getDataContextFactory(QContextManager.DEFAULT, getPage().getConversation());		// FIXME Bad default handling
			model = new SimpleSearchModel<T>(src, qc);
		} else {
			model = new SimpleSearchModel<T>(m_queryHandler, qc);
		}
		setResultModel(model);
	}

	private void setResultModel(ITableModel<T> model) {
		if(m_result == null) {
			//-- We do not yet have a result table -> create one.
			SimpleRowRenderer<T> rr = null;
			if(m_resultColumns != null) {
				rr = new SimpleRowRenderer<T>(getLookupClass(), getMetaModel(), m_resultColumns);
			} else {
				rr = new SimpleRowRenderer<T>(getLookupClass(), getMetaModel());
			}

			m_result = new DataTable<T>(model, rr);
			add(m_result);
			m_result.setPageSize(20);
			m_result.setTableWidth("100%");

			rr.setRowClicked(new ICellClicked<T>() {
				@Override
				public void cellClicked(@Nonnull T val) throws Exception {
					clearGlobalMessage(Msgs.V_MISSING_SEARCH);
					close();
					if(getOnValueSelected() != null) {
						getOnValueSelected().valueSelected(val);
					}
				}
			});

			if(isUseStretchedLayout()) {
				m_result.setStretchHeight(true);
			}
			//-- Add the pager,
			DataPager pg = new DataPager(m_result);
			add(pg);
		} else {
			m_result.setModel(model); // Change the model
		}
		if(isUsedWithinLookupInput()) {
			m_result.setTestID("resultTableLookupInput");
		} else {
			m_result.setTestID("resultTableSimpleLookup");
		}
	}

	@Override
	public void closePressed() throws Exception {
		super.closePressed();
		if(getOnValueSelected() != null) {
			getOnValueSelected().valueSelected(null);
		}
	}

	public void startLookup(NodeBase parent, IValueSelected<T> callback) {
		setOnValueSelected(callback);
		UrlPage body = parent.getPage().getBody();
		body.add(this);
	}

	/**
	 * When set the specified manipulator will be called before a query is sent to the database. The query
	 * can be altered to add extra restrictions for instance.
	 * @return
	 */
	public IQueryManipulator<T> getQueryManipulator() {
		return m_queryManipulator;
	}

	/**
	 * The query handler to use, if a special one is needed. The default query handler will use the
	 * normal conversation-associated DataContext to issue the query.
	 * @return
	 */
	public IQueryHandler<T> getQueryHandler() {
		return m_queryHandler;
	}

	public void setQueryHandler(IQueryHandler<T> queryHandler) {
		m_queryHandler = queryHandler;
	}

	/**
	 * When set this defines the title of the lookup window.
	 * @return
	 */
	public String getLookupTitle() {
		return m_lookupTitle;
	}

	/**
	 * When set this defines the title of the lookup window.
	 *
	 * @param lookupTitle
	 */
	public void setLookupTitle(String lookupTitle) {
		m_lookupTitle = lookupTitle;
	}

	/**
	 * When T the user can press search even when no criteria are entered.
	 * @return
	 */
	public boolean isAllowEmptyQuery() {
		return m_allowEmptyQuery;
	}

	public void setAllowEmptyQuery(boolean allowEmptyQuery) {
		m_allowEmptyQuery = allowEmptyQuery;
	}

	/**
	 * When set the specified manipulator will be called before a query is sent to the database. The query
	 * can be altered to add extra restrictions for instance.
	 *
	 * @param queryManipulator
	 */
	public void setQueryManipulator(IQueryManipulator<T> queryManipulator) {
		m_queryManipulator = queryManipulator;
	}

	public SearchPanel<T> getExternalSearchPanel() {
		return m_externalSearchPanel;
	}

	public void setExternalSearchPanel(SearchPanel<T> externalSearchPanel) {
		m_externalSearchPanel = externalSearchPanel;
	}

	public String[] getResultColumns() {
		return m_resultColumns;
	}

	public void setResultColumns(String[] resultColumns) {
		m_resultColumns = resultColumns;
	}

	public IErrorMessageListener getCustomErrorMessageListener() {
		return m_customErrorMessageListener;
	}

	public void setCustomErrorMessageListener(IErrorMessageListener customErrorMessageListener) {
		m_customErrorMessageListener = customErrorMessageListener;
	}

	protected IValueSelected<T> getOnValueSelected() {
		return m_onValueSelected;
	}

	private void setOnValueSelected(IValueSelected<T> onValueSelected) {
		m_onValueSelected = onValueSelected;
	}

	public boolean isUsedWithinLookupInput() {
		return m_usedWithinLookupInput;
	}

	public void setUsedWithinLookupInput(boolean usedWithinLookupInput) {
		m_usedWithinLookupInput = usedWithinLookupInput;
	}

	public boolean isRenderAsCollapsed() {
		return m_renderAsCollapsed;
	}

	public void setRenderAsCollapsed(boolean renderAsCollapsed) {
		m_renderAsCollapsed = renderAsCollapsed;
	}

	public boolean isShowDefaultSearch() {
		return m_showDefaultSearch;
	}

	public void setShowDefaultSearch(boolean showDefaultSearch) {
		m_showDefaultSearch = showDefaultSearch;
	}
}

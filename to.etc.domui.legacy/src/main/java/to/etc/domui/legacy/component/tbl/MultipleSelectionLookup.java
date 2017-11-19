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
package to.etc.domui.legacy.component.tbl;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.AbstractFloatingLookup;
import to.etc.domui.component.input.IQueryManipulator;
import to.etc.domui.component.layout.IWindowClosed;
import to.etc.domui.component.lookup.IMultiSelectionResult;
import to.etc.domui.component.lookup.LookupForm;
import to.etc.domui.component.lookup.LookupForm.ButtonMode;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.ICellClicked;
import to.etc.domui.component.tbl.IQueryHandler;
import to.etc.domui.component.tbl.ITableModel;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.dom.errors.IErrorMessageListener;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.Msgs;
import to.etc.webapp.query.QContextManager;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContextFactory;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 * Dialog that enables multiple lookup selection.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 27 Oct 2009
 */
public class MultipleSelectionLookup<T> extends AbstractFloatingLookup<T> {
	static final private int WIDTH = 740;

	private LookupForm<T> m_externalLookupForm;

	MultipleSelectionDataTable<T> m_queryResultTable;

	String m_title;

	IMultiSelectionResult<T> m_onReceiveResult;

	private IQueryHandler<T> m_queryHandler;

	private IQueryManipulator<T> m_queryManipulator;

	private IErrorMessageListener m_customErrorMessageListener;

	private String[] m_resultColumns = new String[0];

	private boolean m_allowEmptyQuery;

	public MultipleSelectionLookup(Class<T> lookupClass, ClassMetaModel metaModel, boolean isModal, String title, IMultiSelectionResult<T> onReceiveResult) {
		super(isModal, title, lookupClass, metaModel);
		setCssClass("ui-flw");
		//		m_selectionResult = new ArrayList<T>();
		if(getWidth() == null) {
			setWidth(WIDTH + "px");
		}
		m_onReceiveResult = onReceiveResult;
	}

	public MultipleSelectionLookup(Class<T> lookupClass, boolean isModal, String title, IMultiSelectionResult<T> onReceiveResult) {
		this(lookupClass, null, isModal, title, onReceiveResult);
	}

	public void show(NodeBase parent) {
		UrlPage body = parent.getPage().getBody();
		body.add(this);
	}


	@Override
	public void createContent() throws Exception {
		super.createContent();
		setHeight("90%");
		setIcon("THEME/btnFind.png");

		//in case when external error message listener is set
		if(m_customErrorMessageListener != null && m_customErrorMessageListener instanceof NodeBase) {
			setErrorFence();
			add((NodeBase) m_customErrorMessageListener);
			DomUtil.getMessageFence(this).addErrorListener(m_customErrorMessageListener);
		}

		LookupForm<T> lf = getExternalLookupForm() != null ? getExternalLookupForm() : new LookupForm<T>(getLookupClass(), getMetaModel());
		if(m_onReceiveResult != null) {
			//-- Add a "confirm" button to the lookup form
			DefaultButton b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CONFIRM));
			b.setIcon("THEME/btnConfirm.png");
			b.setTestID("confirmButton");
			b.setClicked(new IClicked<NodeBase>() {
				@Override
				public void clicked(final @Nonnull NodeBase xb) throws Exception {
					close();
					m_onReceiveResult.onReturnResult((m_queryResultTable != null) ? m_queryResultTable.getAccumulatedResults() : Collections.EMPTY_LIST);
				}
			});
			lf.addButtonItem(b, 600, ButtonMode.BOTH);
		}
		lf.forceRebuild(); // jal 20091002 Force rebuild to remove any state from earlier invocations of the same form. This prevents the form from coming up in "collapsed" state if it was left that way last time it was used (Lenzo).
		add(lf);
		setOnClose(new IWindowClosed() {
			@Override
			public void closed(@Nonnull String closeReason) throws Exception {
				clearGlobalMessage(Msgs.V_MISSING_SEARCH);
				if(m_onReceiveResult != null) {
					m_onReceiveResult.onReturnResult(Collections.EMPTY_LIST);
				}
			}
		});

		lf.setClicked(new IClicked<LookupForm<T>>() {
			@Override
			public void clicked(@Nonnull LookupForm<T> b) throws Exception {
				search(b);
			}
		});

		lf.setOnCancel(new IClicked<LookupForm<T>>() {
			@Override
			public void clicked(@Nonnull LookupForm<T> b) throws Exception {
				closePressed();
			}
		});
	}

	protected void search(LookupForm<T> lf) throws Exception {
		QCriteria<T> c = lf.getEnteredCriteria();
		if(c == null) // Some error has occured?
			return; // Don't do anything (errors will have been registered)

		if(getQueryManipulator() != null) {
			c = getQueryManipulator().adjustQuery(c); // Adjust the query where needed,
		}

		clearGlobalMessage(Msgs.V_MISSING_SEARCH);
		if(!lf.hasUserDefinedCriteria() && !isAllowEmptyQuery()) {
			addGlobalMessage(UIMessage.error(Msgs.BUNDLE, Msgs.V_MISSING_SEARCH)); // Missing inputs
			return;
		} else
			clearGlobalMessage();
		setTableQuery(c);
	}

	private void setTableQuery(QCriteria<T> qc) throws Exception {
		ITableModel<T> model;
		if(m_queryHandler == null) {
			QDataContextFactory src = QContextManager.getDataContextFactory(QContextManager.DEFAULT, getPage().getConversation());		// FIXME Bad context handling
			model = new SimpleSearchModel<T>(src, qc);
		} else {
			model = new SimpleSearchModel<T>(m_queryHandler, qc);
		}

		if(m_queryResultTable == null) {
			//-- We do not yet have a result table -> create one.
			MultipleSelectionRowRenderer<T> rr = new MultipleSelectionRowRenderer<T>(getLookupClass(), getMetaModel(), m_resultColumns) {
				@Override
				public int getRowWidth() {
					int pxw = DomUtil.pixelSize(getWidth());
					return (pxw == -1 ? WIDTH : pxw) - 4;
				}

				@Override
				public int getSelectionColWidth() {
					return 20;
				}
			};

			m_queryResultTable = new MultipleSelectionDataTable<T>(getLookupClass(), model, rr);
			add(m_queryResultTable);
			m_queryResultTable.setPageSize(10);
			m_queryResultTable.setTableWidth("100%");

			rr.setRowClicked(new ICellClicked<T>() {
				@Override
				public void cellClicked(@Nonnull T val) throws Exception {
					m_queryResultTable.handleRowClicked(val);
				}
			});

			if(isUseStretchedLayout()) {
				m_queryResultTable.setStretchHeight(true);
			}

			//-- Add the pager,
			DataPager pg = new DataPager(m_queryResultTable);
			add(pg);
		} else {
			m_queryResultTable.setModel(model); // Change the model
		}
		m_queryResultTable.setTestID("queryResultTable");
	}

	public LookupForm<T> getExternalLookupForm() {
		return m_externalLookupForm;
	}

	public void setExternalLookupForm(LookupForm<T> externalLookupForm) {
		m_externalLookupForm = externalLookupForm;
	}

	@Override
	public String getTitle() {
		return m_title;
	}

	@Override
	public void setTitle(String title) {
		m_title = title;
	}

	public IErrorMessageListener getCustomErrorMessageListener() {
		return m_customErrorMessageListener;
	}

	public void setCustomErrorMessageListener(IErrorMessageListener customErrorMessageListener) {
		m_customErrorMessageListener = customErrorMessageListener;
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

	public IQueryManipulator<T> getQueryManipulator() {
		return m_queryManipulator;
	}

	public void setQueryManipulator(IQueryManipulator<T> queryManipulator) {
		m_queryManipulator = queryManipulator;
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
}

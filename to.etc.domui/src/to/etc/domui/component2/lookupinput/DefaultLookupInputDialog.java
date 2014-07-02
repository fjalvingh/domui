package to.etc.domui.component2.lookupinput;

import javax.annotation.*;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.lookup.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.query.*;

public class DefaultLookupInputDialog<QT, OT> extends Dialog {
	@Nullable
	private LookupForm<QT> m_lookupForm;

	@Nullable
	private DataTable<OT> m_result;

	@Nullable
	private String m_formTitle;

	/** When T (default) you can press search on an empty popup form. 20120511 jal Default set to true. */
	private boolean m_allowEmptyQuery = true;

	private boolean m_searchImmediately;

	/**
	 * Default T. When set, table result would be stretched to use entire available height on FloatingWindow.
	 */
	private boolean m_useStretchedLayout = true;

	@Nullable
	private IErrorMessageListener m_customErrorMessageListener;

	/**
	 * When set this defines the {@link IRowRenderer}&lt;OT&gt; to use to render rows when the popup lookup form is used.
	 */
	@Nullable
	private IClickableRowRenderer<OT> m_formRowRenderer;

	/**
	 * Internal: the actual form row renderer used by the code. This will be set to a {@link BasicRowRenderer} if the user
	 * did not specify a row renderer.
	 */
	@Nullable
	private IClickableRowRenderer<OT> m_actualFormRowRenderer;


	@Override
	public void createContent() throws Exception {
		setWidth("740px");
		setHeight("90%");
		setIcon("THEME/btnFind.png");
		setTestID(getTestID() + "_floaterWindowLookupInput");

		//in case when external error message listener is set
		IErrorMessageListener cerl = m_customErrorMessageListener;
		if(cerl != null && cerl instanceof NodeBase) {
			setErrorFence();
			add((NodeBase) cerl);
			DomUtil.getMessageFence(this).addErrorListener(cerl);
		}
		LookupForm<QT> lf = getLookupForm();
		if(lf == null) {
			lf = new LookupForm<QT>(getQueryClass(), getQueryMetaModel());
			if(m_searchPropertyList != null && m_searchPropertyList.size() != 0)
				lf.setSearchProperties(m_searchPropertyList);
		}

		lf.setCollapsed(keySearchModel != null && keySearchModel.getRows() > 0);
		lf.forceRebuild(); // jal 20091002 Force rebuild to remove any state from earlier invocations of the same form. This prevents the form from coming up in "collapsed" state if it was left that way last time it was used (Lenzo).

		add(lf);
		setOnClose(new IWindowClosed() {
			@Override
			public void closed(@Nonnull String closeReason) throws Exception {
				clearGlobalMessage(Msgs.V_MISSING_SEARCH);
				m_floater = null;
				m_result = null;
			}
		});

		lf.setClicked(new IClicked<LookupForm<QT>>() {
			@Override
			public void clicked(@Nonnull LookupForm<QT> b) throws Exception {
				search(b);
			}
		});

		lf.setOnCancel(new IClicked<LookupForm<QT>>() {
			@Override
			public void clicked(@Nonnull LookupForm<QT> b) throws Exception {
				f.closePressed();
			}
		});

		if(keySearchModel != null && keySearchModel.getRows() > 0) {
			setResultModel(keySearchModel);
		} else if(isSearchImmediately()) {
			search(lf);
		}


	}

	private void search(LookupForm<QT> lf) throws Exception {
		QCriteria<QT> c = lf.getEnteredCriteria();
		if(c == null)						// Some error has occured?
			return;							// Don't do anything (errors will have been registered)

		c = manipulateCriteria(c);
		if(c == null) {
			//in case of cancelled search by query manipulator return
			return;
		}

		clearGlobalMessage(Msgs.V_MISSING_SEARCH);
		if(!lf.hasUserDefinedCriteria() && !isAllowEmptyQuery()) {
			addGlobalMessage(UIMessage.error(Msgs.BUNDLE, Msgs.V_MISSING_SEARCH)); // Missing inputs
			return;
		} else
			clearGlobalMessage();
		setTableQuery(c);
	}

	private void setTableQuery(@Nonnull QCriteria<QT> qc) throws Exception {
		ITableModel<OT> model = createTableModel(qc);					// Ask derived to convert the query into my output model
		setResultModel(model);
	}

	private void setResultModel(@Nonnull ITableModel<OT> model) throws Exception {
		DataTable<OT> dt = m_result;
		if(dt == null) {
			//-- We do not yet have a result table -> create one.
			dt = m_result = new DataTable<OT>(model, getActualFormRowRenderer());

			add(dt);
			dt.setPageSize(20);
			dt.setTableWidth("100%");
			initSelectionModel();
			if(isUseStretchedLayout()) {
				dt.setStretchHeight(true);
			}

			//-- Add the pager,
			DataPager pg = new DataPager(m_result);
			getFloater().add(pg);
			dt.setTestID("resultTableLookupInput");
		} else {
			dt.setModel(model); // Change the model
		}
	}

	protected void initSelectionModel() throws Exception {
		// DEFAULT EMPTY IMPLEMENTATION.
	}

	/**
	 * Either use the user-specified popup form row renderer or create one using resultColumns or the default metadata.
	 * @return
	 */
	@Nonnull
	private IRowRenderer<OT> getActualFormRowRenderer() {
		IClickableRowRenderer<OT> actualFormRowRenderer = m_actualFormRowRenderer;
		if(null == actualFormRowRenderer) {
			//-- Is a form row renderer specified by the user - then use it, else create a default one.
			actualFormRowRenderer = m_actualFormRowRenderer = getFormRowRenderer();
			if(null == actualFormRowRenderer) {
				actualFormRowRenderer = m_actualFormRowRenderer = new BasicRowRenderer<OT>(getOutputClass(), getOutputMetaModel());
			}

			//-- Always set a click handler on the row renderer, so we can accept the selected record.
			actualFormRowRenderer.setRowClicked(new ICellClicked<OT>() {
				@Override
				public void cellClicked(@Nonnull NodeBase tr, @Nonnull OT val) throws Exception {
					clearGlobalMessage(Msgs.V_MISSING_SEARCH);
					if(!getDataTable().isMultiSelectionVisible()) {
						LookupInputBase.this.toggleFloater(null);
					}
					handleSetValue(val);
				}
			});
		}
		return actualFormRowRenderer;
	}


	/**
	 * Add column specs for the full query form's result list, according to the specifications as defined by {@link BasicRowRenderer}.
	 * @param columns
	 */
	public void addFormColumns(@Nonnull Object... columns) {
		IRowRenderer<OT> rr = getActualFormRowRenderer();
		if(rr instanceof BasicRowRenderer) {
			((BasicRowRenderer<OT>) rr).addColumns(columns);
		} else
			throw new IllegalStateException("The row renderer for the form is set to something else than a BasicRowRenderer.");
	}

	/**
	 * Can be set by a specific lookup form to use when the full query popup is shown. If unset the code will create
	 * a LookupForm using metadata.
	 * @return
	 */
	@Nullable
	public LookupForm<QT> getLookupForm() {
		return m_lookupForm;
	}

	public void setLookupForm(@Nullable LookupForm<QT> externalLookupForm) {
		m_lookupForm = externalLookupForm;
	}


	protected DataTable<OT> getDataTable() {
		return m_result;
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
	 * When T this will immediately search with an empty query.
	 * @return
	 */
	public boolean isSearchImmediately() {
		return m_searchImmediately;
	}

	public void setSearchImmediately(boolean searchImmediately) {
		m_searchImmediately = searchImmediately;
		if(searchImmediately)
			setAllowEmptyQuery(true);
	}

	/**
	 * Returns T if we are using stretching of result table height to all remained parent height.
	 */
	public boolean isUseStretchedLayout() {
		return m_useStretchedLayout;
	}

	/**
	 * Set to F to disable stretching of result table height.
	 * @param useStretchedLayout
	 */
	public void setUseStretchedLayout(boolean value) {
		if(value == m_useStretchedLayout) {
			return;
		}
		m_useStretchedLayout = value;
		if(isBuilt()) {
			forceRebuild();
		}
	}

	/**
	 * When set this defines the title of the lookup window.
	 * @return
	 */
	@Nullable
	public String getFormTitle() {
		return m_formTitle;
	}

	/**
	 * When set this defines the title of the lookup window.
	 *
	 * @param lookupTitle
	 */
	public void setFormTitle(@Nullable String lookupTitle) {
		m_formTitle = lookupTitle;
	}

	@Nullable
	public IErrorMessageListener getCustomErrorMessageListener() {
		return m_customErrorMessageListener;
	}

	public void setCustomErrorMessageListener(@Nullable IErrorMessageListener customErrorMessageListener) {
		m_customErrorMessageListener = customErrorMessageListener;
	}

	/**
	 * When set this defines the {@link IClickableRowRenderer}&lt;OT&gt; to use to render rows when the popup lookup form is used.
	 *
	 * @return
	 */
	@Nullable
	public IClickableRowRenderer<OT> getFormRowRenderer() {
		return m_formRowRenderer;
	}

	/**
	 * When set this defines the {@link IClickableRowRenderer}&lt;OT&gt; to use to render rows when the popup lookup form is used.
	 * @param lookupFormRenderer
	 */
	public void setFormRowRenderer(@Nullable IClickableRowRenderer<OT> lookupFormRenderer) {
		m_formRowRenderer = lookupFormRenderer;
	}

}

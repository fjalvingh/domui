package to.etc.domui.component2.lookupinput;

import to.etc.domui.component.input.IQueryManipulator;
import to.etc.domui.component.layout.Dialog;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.tbl.BasicRowRenderer;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.ICellClicked;
import to.etc.domui.component.tbl.IClickableRowRenderer;
import to.etc.domui.component.tbl.IQueryHandler;
import to.etc.domui.component.tbl.IRowRenderer;
import to.etc.domui.component.tbl.ITableModel;
import to.etc.domui.component.tbl.PageQueryHandler;
import to.etc.domui.dom.errors.IErrorMessageListener;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.Msgs;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class DefaultLookupInputDialog<QT, OT> extends Dialog {
	@Nullable
	private SearchPanel<QT> m_lookupForm;

	@Nullable
	private DataTable<OT> m_result;

	@Nullable
	private String m_formTitle;

	/** When T (default) you can press search on an empty popup form. 20120511 jal Default set to true. */
	private boolean m_allowEmptyQuery = true;

	private boolean m_searchImmediately;

	/**
	 * Default false for backward compatibility . Controls if the serach panel is initially collapsed or not
	 */
	private boolean m_initiallyCollapsed;


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

	private ITableModelFactory<QT, OT> m_modelFactory;

	@Nullable
	private IQueryManipulator<QT> m_queryManipulator;

	@Nullable
	private IQueryHandler<QT> m_queryHandler;

	/** The search properties to use in the lookup form when created. If null uses the default attributes on the class. */
	@Nullable
	private List<SearchPropertyMetaModel> m_searchPropertyList;

	/**
	 * The metamodel to use to handle the query data in this class. For Javabean data classes this is automatically
	 * obtained using MetaManager; for meta-based data models this gets passed as a constructor argument.
	 */
	@Nonnull
	final private ClassMetaModel m_queryMetaModel;

	/**
	 * The metamodel for output (display) objects.
	 */
	@Nonnull
	final private ClassMetaModel m_outputMetaModel;

	/**
	 * Data set used for initial showing, if specified. Opens popup with hidden search panel, showing only this predefined data as default search results.
	 * Usually used to support showing of keyword search matches.
	 */
	@Nullable
	private ITableModel<OT> m_initialModel;

	/** The selected value or null if no selection made (yet) */
	@Nullable
	private OT m_value;

	/**
	 * The handler that is called when a selection is made / unmade.
	 */
	@Nullable
	private IClicked<DefaultLookupInputDialog<QT, OT>> m_onSelection;

	public DefaultLookupInputDialog(@Nonnull ClassMetaModel queryMetaModel, @Nonnull ClassMetaModel outputMetaModel, @Nonnull ITableModelFactory<QT, OT> modelFactory) {
		m_queryMetaModel = queryMetaModel;
		m_outputMetaModel = outputMetaModel;
		m_modelFactory = modelFactory;
		m_initiallyCollapsed = false;
	}

	@Override
	public void createContent() throws Exception {
		if(getWidth() == null)
			setWidth("740px");
		if(getHeight() == null)
			setHeight("90%");
		setIcon("THEME/ttlFind.png");
		setTestID(getTestID() + "_floaterWindowLookupInput");

		//in case when external error message listener is set
		IErrorMessageListener cerl = m_customErrorMessageListener;
		if(cerl != null && cerl instanceof NodeBase) {
			setErrorFence();
			add((NodeBase) cerl);
			DomUtil.getMessageFence(this).addErrorListener(cerl);
		}
		SearchPanel<QT> lf = getSearchPanel();
		if(lf == null) {
			lf = new SearchPanel<QT>((Class<QT>) getQueryMetaModel().getActualClass(), getQueryMetaModel());
			if(m_searchPropertyList != null && m_searchPropertyList.size() != 0)
				lf.setSearchProperties(m_searchPropertyList);
		}

		ITableModel<OT> initialModel = m_initialModel;

		//-- Ordered!
		lf.forceRebuild(); 										// jal 20091002 Force rebuild to remove any state from earlier invocations of the same form. This prevents the form from coming up in "collapsed" state if it was left that way last time it was used (Lenzo).

		// this collapse search fields by configuration or if we enter the lookup popup with some already pre set results, for example given by search as you type.
		lf.setCollapsed(m_initiallyCollapsed || (initialModel != null && initialModel.getRows() > 0));
		//-- end ordered

		add(lf);
		//setOnClose(new IWindowClosed() {
		//	@Override
		//	public void closed(@Nonnull String closeReason) throws Exception {
		//		clearGlobalMessage(Msgs.V_MISSING_SEARCH);
		//		m_floater = null;
		//		m_result = null;
		//	}
		//});

		lf.setClicked((IClicked<SearchPanel<QT>>) b -> search(b));

		lf.setOnCancel(b -> closePressed());

		if(initialModel != null && initialModel.getRows() > 0) {
			setResultModel(initialModel);
		} else if(isSearchImmediately()) {
			search(lf);
		}


	}

	public void setInitiallyCollapsed(boolean initiallyCollapsed) {
		m_initiallyCollapsed = initiallyCollapsed;
	}

	private void search(@Nonnull SearchPanel<QT> lf) throws Exception {
		QCriteria<QT> c = lf.getCriteria();
		if(c == null)						// Some error has occured?
			return;							// Don't do anything (errors will have been registered)

		IQueryManipulator<QT> m = m_queryManipulator;
		if(null != m) {
			c = m.adjustQuery(c);
			if(c == null) {					// Cancelled by manipulator?
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

	private void setTableQuery(@Nonnull QCriteria<QT> qc) throws Exception {
		ITableModel<OT> model = createTableModel(qc);					// Ask derived to convert the query into my output model
		setResultModel(model);
	}

	@Nonnull
	private ITableModel<OT> createTableModel(@Nonnull QCriteria<QT> qc) throws Exception {
		ITableModelFactory<QT, OT> factory = m_modelFactory;
		if(null == factory)
			throw new IllegalStateException("Table model factory unset");
		return factory.createTableModel(getQueryHandler(), qc);
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
				NodeContainer delegate = getDelegate();
				if (null != delegate && !delegate.isStretchHeight()){
					delegate.setStretchHeight(true);
				}
			}

			//-- Add the pager,
			DataPager pg = new DataPager(m_result);
			add(pg);
			dt.setTestID("resultTableLookupInput");
		} else {
			dt.setModel(model); // Change the model
		}
	}

	private void initSelectionModel() throws Exception {
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
				actualFormRowRenderer = m_actualFormRowRenderer = new BasicRowRenderer<OT>((Class<OT>) getOutputMetaModel().getActualClass(), getOutputMetaModel());
			}

			//-- Always set a click handler on the row renderer, so we can accept the selected record.
			actualFormRowRenderer.setRowClicked(new ICellClicked<OT>() {
				@Override
				public void cellClicked(@Nonnull OT val) throws Exception {
					rowSelected(val);
				}
			});
		}
		return actualFormRowRenderer;
	}

	/**
	 * Called when a row is selected in the form. By default this sets the selected value
	 * in {@link value}, closes the dialog and sends the onSelection event.
	 * @param value
	 * @throws Exception
	 */
	protected void rowSelected(@Nonnull OT value) throws Exception {
		clearGlobalMessage(Msgs.V_MISSING_SEARCH);
		close();
		setValue(value);
		callOnSelection();
	}

	private void callOnSelection() throws Exception {
		IClicked<DefaultLookupInputDialog<QT, OT>> clicked = m_onSelection;
		if(null != clicked) {
			clicked.clicked(this);
		}
	}

	/**
	 * When the dialog is closed we clear the value, and send the onSelection event.
	 * @see to.etc.domui.component.layout.FloatingDiv#onClosed(java.lang.String)
	 */
	@Override
	protected void onClosed(String closeReason) throws Exception {
		setValue(null);
		callOnSelection();
		super.onClosed(closeReason);
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

	@Nonnull
	public ClassMetaModel getQueryMetaModel() {
		return m_queryMetaModel;
	}

	@Nonnull
	public ClassMetaModel getOutputMetaModel() {
		return m_outputMetaModel;
	}

	/**
	 * Can be set by a specific lookup form to use when the full query popup is shown. If unset the code will create
	 * a SearchPanel using metadata.
	 * @return
	 */
	@Nullable
	public SearchPanel<QT> getSearchPanel() {
		return m_lookupForm;
	}

	public void setSearchPanel(@Nullable SearchPanel<QT> externalSearchPanel) {
		m_lookupForm = externalSearchPanel;
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

	/**
	 * Set the list of lookup properties to use for lookup in the lookup form, when shown.
	 * @return
	 */
	public List<SearchPropertyMetaModel> getSearchProperties() {
		return m_searchPropertyList;
	}

	public void setSearchProperties(List<SearchPropertyMetaModel> searchPropertyList) {
		m_searchPropertyList = searchPropertyList;
	}

	@Nullable
	public OT getValue() {
		return m_value;
	}

	public void setValue(@Nullable OT value) {
		m_value = value;
	}

	/**
	 * The handler to call when a selection is made or the dialog is closed; if the dialog
	 * is closed the value will be null.
	 * @return
	 */
	@Nullable
	public IClicked<DefaultLookupInputDialog<QT, OT>> getOnSelection() {
		return m_onSelection;
	}

	public void setOnSelection(@Nullable IClicked<DefaultLookupInputDialog<QT, OT>> onSelection) {
		m_onSelection = onSelection;
	}

	@Nullable
	public IQueryManipulator<QT> getQueryManipulator() {
		return m_queryManipulator;
	}

	public void setQueryManipulator(IQueryManipulator<QT> queryManipulator) {
		m_queryManipulator = queryManipulator;
	}

	@Nonnull
	public IQueryHandler<QT> getQueryHandler() {
		IQueryHandler<QT> handler = m_queryHandler;
		if(null == handler)
			handler = new PageQueryHandler<QT>(this);
		return handler;
	}

	public void setQueryHandler(IQueryHandler<QT> queryHandler) {
		m_queryHandler = queryHandler;
	}

	public void setInitialModel(@Nullable ITableModel<OT> initialModel) {
		if (m_initialModel != initialModel) {
			m_initialModel = initialModel;
			if(isBuilt()) {
				forceRebuild();
			}
		}
	}
}

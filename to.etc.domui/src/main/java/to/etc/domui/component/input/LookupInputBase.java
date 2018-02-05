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

import to.etc.domui.component.layout.Dialog;
import to.etc.domui.component.layout.FloatingWindow;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.tbl.BasicRowRenderer;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.ICellClicked;
import to.etc.domui.component.tbl.IClickableRowRenderer;
import to.etc.domui.component.tbl.IRowRenderer;
import to.etc.domui.component.tbl.ITableModel;
import to.etc.domui.component2.lookupinput.DefaultLookupInputDialog;
import to.etc.domui.dom.errors.IErrorMessageListener;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IHasModifiedIndication;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IExecute;
import to.etc.domui.util.LookupInputPropertyRenderer;
import to.etc.domui.util.Msgs;
import to.etc.util.RuntimeConversions;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.query.IIdentifyable;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QLiteral;
import to.etc.webapp.query.QOperation;
import to.etc.webapp.query.QPropertyComparison;
import to.etc.webapp.query.QRestrictor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;

abstract public class LookupInputBase<QT, OT> extends AbstractLookupInputBase<QT, OT> implements IControl<OT>, ITypedControl<OT>, IHasModifiedIndication {
	public static final String MAGIC_ID_MARKER = "?id?";

	@Nullable
	private SearchPanel<QT> m_lookupForm;

	@Nullable
	private FloatingWindow m_floater;

	@Nullable
	private DataTable<OT> m_result;

	@Nullable
	private String m_formTitle;

	@Nullable
	private IErrorMessageListener m_customErrorMessageListener;

	@Nullable
	private IActionAllowed m_isLookupAllowed;

	@Nullable
	private KeyWordSearchInput<OT> m_keySearch;

	@Nullable
	private IKeyWordSearchQueryFactory<QT> m_keyWordSearchHandler;

	/** When T (default) you can press search on an empty popup form. 20120511 jal Default set to true. */
	private boolean m_allowEmptyQuery = true;

	/**
	 * When T, it sets default lookup popup with by default collapsed search fields.
	 */
	private boolean m_popupInitiallyCollapsed;

	/**
	 * When T, it sets default lookup popup to search immediately.
	 */
	private boolean m_searchImmediately;

	private int m_keyWordSearchPopupWidth;

	///**
	// * SPECIAL QUIRK MODE, USUALLY YOU DO NOT NEED IT.
	// * When T (default is F), it renders lookup input in a way that pop-up with search as you type results rolls down exceeding the boundaries of parent control.
	// * This is useful when your LookupInput is last control in pop-up Windows, and you want to avoid scroll-bar in dialog. However, mode is not applicable in all other regular cases since
	// * it interfere rendering of LookupInput that goes over controls bellow it.
	// */
	//private boolean m_absolutePopupLayoutQuirkMode;

	/** The search properties to use in the lookup form when created. If null uses the default attributes on the class. */
	@Nullable
	private List<SearchPropertyMetaModel> m_searchPropertyList;

	/**
	 * Provides alternative lookup popup factory.
	 */
	@Nullable
	private IPopupOpener m_popupOpener;

	/**
	 * Default T. When set, table result would be stretched to use entire available height on FloatingWindow.
	 */
	private boolean m_useStretchedLayout = true;

	/**
	 * If set, enables custom init code on SearchPanel that is in use for this component, triggers before SearchPanel is shown
	 */
	@Nullable
	private ILookupFormModifier<QT> m_lookupFormInitialization;

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

	/** The row renderer used to render rows in the quick search dropdown box showing the results of the quick search. */
	@Nullable
	private KeyWordPopupRowRenderer<OT> m_dropdownRowRenderer;

	/**
	 * Interface provides assess to used lookup form initialization method.
	 *
	 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
	 * Created on 19 Jul 2011
	 */
	public interface ILookupFormModifier<T> {
		/**
		 * Sends SearchPanel for initialization.
		 * @param lf
		 */
		void initialize(@Nonnull SearchPanel<T> lf) throws Exception;
	}


	/**
	 * Factory for the lookup dialog, to be shown when the lookup button is pressed.
	 *
	 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
	 * Created on Sep 1, 2017
	 */
	public interface IPopupOpener {
		@Nullable <A, B, L extends LookupInputBase<A, B>> Dialog createDialog(@Nonnull L control, @Nullable ITableModel<B> initialModel, @Nonnull IExecute callOnWindowClose);
	}

	/**
	 * This must create the table model for the output type from the query on the input type.
	 * @param query
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	abstract protected ITableModel<OT> createTableModel(@Nonnull QCriteria<QT> query) throws Exception;

	/**
	 * Create a lookup control that shows the specified column set in both quick lookup mode and form lookup
	 * mode.
	 * @param queryClass
	 * @param resultClass
	 * @param resultColumns
	 */
	public LookupInputBase(@Nonnull Class<QT> queryClass, @Nonnull Class<OT> resultClass, @Nonnull String... resultColumns) {
		this(null, queryClass, resultClass, null, null);
		setResultColumns(resultColumns);
	}

	/**
	 * Lookup a POJO Java bean persistent class.
	 * @param queryClass
	 */
	public LookupInputBase(@Nonnull Class<QT> queryClass, @Nonnull Class<OT> resultClass) {
		this(null, queryClass, resultClass, null, null);
	}

	public LookupInputBase(@Nonnull QCriteria<QT> rootCriteria, @Nonnull Class<OT> resultClass) {
		this(rootCriteria, DomUtil.nullChecked(rootCriteria.getBaseClass()), resultClass, null, null);
	}

	public LookupInputBase(@Nullable QCriteria<QT> rootCriteria, @Nonnull Class<QT> queryClass, @Nonnull Class<OT> resultClass, @Nullable ClassMetaModel queryMetaModel,
		@Nullable ClassMetaModel outputMetaModel) {
		super(rootCriteria, queryClass, resultClass, queryMetaModel, outputMetaModel);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Quick Search code (KeySearch)						*/
	/*--------------------------------------------------------------*/

	/**
	 * @return true either when query control is manually implemented by keyWordSearchHandler, or if keyword search meta data is defined.
	 */
	@Override
	protected boolean isKeyWordSearchDefined() {
		if(getKeyWordSearchHandler() != null) {
			return true;
		}

		if(getKeywordLookupPropertyList() != null)
			return true;
		List<SearchPropertyMetaModel> spml = getQueryMetaModel().getKeyWordSearchProperties();
		return spml.size() > 0;
	}

	/**
	 * Render the "current value" display as an input box or display box with clear and select buttons.
	 */
	@Override
	protected void renderKeyWordSearch() {
		//Div sdiv = new Div("ui-lui-lookupf");
		//add(sdiv);
		addKeySearchField(this);
	}

	private void addKeySearchField(NodeContainer parent) {
		KeyWordSearchInput<OT> ks = new KeyWordSearchInput<>(getKeyWordSearchCssClass());
		setKeySearch(ks);
		ks.setPopupWidth(getKeyWordSearchPopupWidth());
		//ks.setAbsolutePopupLayoutQuirkMode(m_absolutePopupLayoutQuirkMode);
		KeyWordPopupRowRenderer<OT> rr = getDropdownRowRenderer();
		rr.setRowClicked(val -> handleSetValue(val));
		ks.setResultsHintPopupRowRenderer(rr);

		ks.setOnLookupTyping(component -> {
			ITableModel<OT> keySearchModel = searchKeyWord(component.getKeySearchValue());
			component.showResultsHintPopup(null);
			if(keySearchModel == null) {
				//in case of insufficient searchString data cancel search and return.
				component.setResultsCount(-1);
				component.setFocus(); //focus must be set manually.
				return;
			}
			if(keySearchModel.getRows() == 1) {
				//in case of single match select value.
				handleSetValue(keySearchModel.getItems(0, 1).get(0));
			} else {
				//show results count info
				component.setResultsCount(keySearchModel.getRows());
				if((keySearchModel.getRows() > 0) && (keySearchModel.getRows() < 10)) {
					component.showResultsHintPopup(keySearchModel);
				}
			}
		});

		ks.setOnShowResults(component -> {
			ITableModel<OT> keySearchModel = searchKeyWord(component.getKeySearchValue());
			component.showResultsHintPopup(null);
			if(keySearchModel == null) {
				//in case of insufficient searchString data cancel search and popup clean search dialog.
				component.setResultsCount(-1);
				toggleFloater(null);
				return;
			}
			if(keySearchModel.getRows() == 1) {
				//in case of single match select value.
				handleSetValue(keySearchModel.getItems(0, 1).get(0));
			} else {
				//in case of more results show narrow result in search popup.
				component.setResultsCount(keySearchModel.getRows());
				toggleFloater(keySearchModel);
			}
		});
		parent.add(ks);
		String kscss = getKeyWordSearchCssClass();
		if(kscss != null) {
			addCssClass(kscss);
		}
		String hint = getKeySearchHint();
		ks.setHint(Msgs.BUNDLE.formatMessage(Msgs.UI_KEYWORD_SEARCH_HINT, (hint != null) ? hint : getDefaultKeySearchHint()));
	}

	@Override
	@Nullable
	protected KeyWordSearchInput<OT> getKeySearch() {
		return m_keySearch;
	}

	@Override protected void clearKeySearch() {
		m_keySearch = null;
	}

	/**
	 * Return the special row renderer used to display the quick-search results in the small
	 * dropdown below the quicksearch input box.
	 * @return
	 */
	@Nonnull
	private KeyWordPopupRowRenderer<OT> getDropdownRowRenderer() {
		if(null == m_dropdownRowRenderer) {
			m_dropdownRowRenderer = new KeyWordPopupRowRenderer<OT>(getOutputMetaModel());
		}
		return DomUtil.nullChecked(m_dropdownRowRenderer);
	}

	/**
	 * Returns data that matches keyword search string.
	 * @param searchString
	 * @return Matching data or null in case that search is cancelled because of insufficient number of characters typed into keyword search field.
	 * @throws Exception
	 */
	@Nullable
	private ITableModel<OT> searchKeyWord(@Nullable String searchString) throws Exception {
		if(searchString == null || searchString.trim().length() == 0) {
			return null;
		}
		searchString = DomUtil.nullChecked(searchString.replace("*", "%"));
		QCriteria<QT> searchQuery;

		Long magicId = getMagicString(searchString);
		if(magicId != null) {
			searchQuery = createTestQuery(magicId);
		} else {
			searchQuery = createStandardQuery(searchString);
		}

		if(searchQuery == null) {
			return null;
		}
		searchQuery = adjustQuery(searchQuery);
		if(searchQuery == null) {
			//in case of cancelled search by query manipulator return
			return null;
		}

		return createTableModel(searchQuery);
	}

	/**
	 * Extracting object id from magic string.
	 * @param searchString
	 * @return
	 */
	@Nullable
	private Long getMagicString(@Nonnull String searchString) {
		if(searchString.startsWith(MAGIC_ID_MARKER) //
			&& searchString.endsWith(MAGIC_ID_MARKER)) {
			try {
				int l = MAGIC_ID_MARKER.length();
				String id = searchString.substring(l, searchString.length() - l);
				return Long.valueOf(id.trim());
			} catch(NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Create query for filling up lookup by IIdentifyable id.</br>
	 * Used for speeding up tests
	 */
	@Nullable
	private QCriteria<QT> createTestQuery(@Nonnull Long magicId) throws Exception {
		if(IIdentifyable.class.isAssignableFrom(getQueryClass())) {
			QCriteria<QT> searchQuery = (QCriteria<QT>) getQueryMetaModel().createCriteria();
			searchQuery.eq("id", magicId);
			return searchQuery;
		}
		throw new IllegalArgumentException("This instance cannot be used for filling in lookup using magic string: " + getQueryClass());
	}

	@Nullable
	private QCriteria<QT> createStandardQuery(String searchString) throws Exception {
		QCriteria<QT> searchQuery;
		IKeyWordSearchQueryFactory<QT> ksh = getKeyWordSearchHandler();
		if(ksh != null) {
			searchQuery = ksh.createQuery(searchString);
			if(searchQuery == null) {
				//in case of cancelled search return null
				return null;
			}
		} else {
			searchString = DomUtil.nullChecked(searchString.replace("*", "%"));
			if(searchString.startsWith("$$") && searchString.length() > 2) {
				String idString = searchString.substring(2);
				PropertyMetaModel<?> primaryKey = getQueryMetaModel().getPrimaryKey();
				if(null != primaryKey) {
					Class<?> pkType = primaryKey.getActualType();
					Object pk = RuntimeConversions.convertTo(idString, pkType);
					if(null != pk) {
						searchQuery = (QCriteria<QT>) getQueryMetaModel().createCriteria();
						searchQuery.eq(primaryKey.getName(), pk);
						return searchQuery;
					}
				}
			}

			//-- Has default meta?
			List<SearchPropertyMetaModel> kwl = getKeywordLookupPropertyList();
			List<SearchPropertyMetaModel> spml = kwl == null ? getQueryMetaModel().getKeyWordSearchProperties() : kwl;
			searchQuery = (QCriteria<QT>) getQueryMetaModel().createCriteria();

			QRestrictor<QT> r = searchQuery.or();
			int ncond = 0;
			if(spml.size() > 0) {
				for(SearchPropertyMetaModel spm : spml) {
					if(spm.getMinLength() <= searchString.length()) {

						//-- Abort on invalid metadata; never continue with invalid data.
						PropertyMetaModel<?> pmm = spm.getProperty();
						if(pmm == null)
							throw new ProgrammerErrorException("The quick lookup properties for " + getQueryMetaModel() + " are invalid: the property name is null");

						//It is required that lookup by id is also available, for now only integer based types and BigDecimal interpreted as Long (fix for 1228) are supported
						if(DomUtil.isIntegerType(pmm.getActualType()) || pmm.getActualType() == BigDecimal.class) {
							if(searchString.contains("%") && !pmm.isTransient()) {
								r.add(new QPropertyComparison(QOperation.LIKE, pmm.getName(), new QLiteral(searchString)));
							} else {
								try {
									Object value = RuntimeConversions.convertTo(searchString, pmm.getActualType());
									if(null != value) {
										r.eq(pmm.getName(), value);
										ncond++;
									}
								} catch(Exception ex) {
									//just ignore this since it means that it is not correct Long condition.
								}
							}
						} else if(pmm.getActualType().isAssignableFrom(String.class)) {
							if(spm.isIgnoreCase()) {
								r.ilike(pmm.getName(), searchString + "%");
							} else {
								r.like(pmm.getName(), searchString + "%");
							}
							ncond++;
						}
					}
				}
			}
			if(ncond == 0) {
				return null;        //no search meta data is matching minimal lenght condition, search is cancelled
			}
		}
		return searchQuery;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Full search popup window code..						*/
	/*--------------------------------------------------------------*/

	/**
	 * Toggle the full search popup window.
	 * @throws Exception
	 */
	@Override
	protected void openPopupWithClick() throws Exception {
		KeyWordSearchInput<OT> keySearch = getKeySearch();
		if(keySearch != null) {
			toggleFloater(searchKeyWord(keySearch.getKeySearchValue()));
		} else {
			toggleFloater(null);
		}
	}

	/**
	 * Show the full search window, and if a model is passed populate the search result list
	 * with the contents of that model.
	 *
	 * @param keySearchModel
	 * @throws Exception
	 */
	private void toggleFloater(@Nullable ITableModel<OT> keySearchModel) throws Exception {
		if(m_floater != null) {
			m_floater.close();
			m_floater = null;
			m_result = null;
			return;
		}

		//In case that action is resolved as not allowed then do nothing.
		if(m_isLookupAllowed != null && !m_isLookupAllowed.isAllowed()) {
			return;
		}

		IPopupOpener popupOpener = getPopupOpener();
		if(null != popupOpener) {
			Dialog floater = popupOpener.createDialog(this, keySearchModel, () -> {
			});
			if(null != floater) {
				floater.modal();
				add(floater);
				decoratePopup(floater);
			}
			return;
		}

		final FloatingWindow f = m_floater = FloatingWindow.create(this, getFormTitle() == null ? getDefaultTitle() : getFormTitle());
		f.setWidth("740px");
		f.setHeight("90%");
		f.setIcon("THEME/ttlFind.png");
		f.setTestID(getTestID() + "_floaterWindowLookupInput");

		//in case when external error message listener is set
		IErrorMessageListener cerl = m_customErrorMessageListener;
		if(cerl != null && cerl instanceof NodeBase) {
			f.setErrorFence();
			f.add((NodeBase) cerl);
			DomUtil.getMessageFence(f).addErrorListener(cerl);
		}
		SearchPanel<QT> lf = getSearchPanel();
		if(lf == null) {
			QCriteria<QT> rootCriteria = getRootCriteria();
			if(null != rootCriteria)
				lf = new SearchPanel<>(rootCriteria);
			else
				lf = new SearchPanel<>(getQueryClass(), getQueryMetaModel());
			if(m_searchPropertyList != null && m_searchPropertyList.size() != 0)
				lf.setSearchProperties(m_searchPropertyList);
			setSearchPanel(lf);
		}

		boolean collapsed = isPopupInitiallyCollapsed();

		lf.forceRebuild(); // jal 20091002 Force rebuild to remove any state from earlier invocations of the same form. This prevents the form from coming up in "collapsed" state if it was left that way last time it was used (Lenzo).
		lf.setCollapsed(collapsed || keySearchModel != null && keySearchModel.getRows() > 0);

		if(getSearchPanelInitialization() != null) {
			getSearchPanelInitialization().initialize(lf);
		}
		f.add(lf);
		f.setOnClose(closeReason -> {
			f.clearGlobalMessage(Msgs.V_MISSING_SEARCH);
			m_floater = null;
			m_result = null;
		});

		lf.setClicked((IClicked<SearchPanel<QT>>) b -> search(b));

		lf.setOnCancel(b -> f.closePressed());

		if(keySearchModel != null && keySearchModel.getRows() > 0) {
			setResultModel(keySearchModel);
		} else if(isSearchImmediately()) {
			search(lf);
		}
	}

	private void decoratePopup(@Nonnull Dialog floater) {
		if(isPopupInitiallyCollapsed() && floater instanceof DefaultLookupInputDialog) {
			((DefaultLookupInputDialog<?, ?>) floater).setInitiallyCollapsed(true);
		}

		if(isSearchImmediately() && floater instanceof DefaultLookupInputDialog) {
			((DefaultLookupInputDialog<?, ?>) floater).setSearchImmediately(true);
		}
	}

	protected void setKeySearch(@Nullable KeyWordSearchInput<OT> keySearch) {
		m_keySearch = keySearch;
	}

	/**
	 * Contruct a default title for this LookupInput
	 */
	@Nonnull
	private String getDefaultTitle() {
		String entity = getOutputMetaModel().getUserEntityName();
		if(entity != null)
			return Msgs.BUNDLE.formatMessage(Msgs.UI_LUI_TTL_WEN, entity);

		return Msgs.BUNDLE.getString(Msgs.UI_LUI_TTL);
	}

	@Nonnull
	public FloatingWindow getFloater() {
		if(null != m_floater)
			return m_floater;
		throw new IllegalStateException("Floating search window is not currently present");
	}

	private void search(SearchPanel<QT> lf) throws Exception {
		QCriteria<QT> c = lf.getCriteria();
		if(c == null)                        // Some error has occured?
			return;                            // Don't do anything (errors will have been registered)

		c = adjustQuery(c);
		if(c == null) {
			//in case of cancelled search by query manipulator return
			return;
		}

		getFloater().clearGlobalMessage(Msgs.V_MISSING_SEARCH);
		if(!lf.hasUserDefinedCriteria() && !isAllowEmptyQuery()) {
			getFloater().addGlobalMessage(UIMessage.error(Msgs.BUNDLE, Msgs.V_MISSING_SEARCH)); // Missing inputs
			return;
		} else
			getFloater().clearGlobalMessage();
		setTableQuery(c);
	}

	private void setTableQuery(@Nonnull QCriteria<QT> qc) throws Exception {
		ITableModel<OT> model = createTableModel(qc);                    // Ask derived to convert the query into my output model
		setResultModel(model);
	}

	private void setResultModel(@Nonnull ITableModel<OT> model) throws Exception {
		DataTable<OT> dt = m_result;
		if(dt == null) {
			//-- We do not yet have a result table -> create one.
			dt = m_result = new DataTable<OT>(model, getActualFormRowRenderer());

			getFloater().add(dt);
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
	public IRowRenderer<OT> getActualFormRowRenderer() {
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
				public void cellClicked(@Nonnull OT val) throws Exception {
					getFloater().clearGlobalMessage(Msgs.V_MISSING_SEARCH);
					if(!getDataTable().isMultiSelectionVisible()) {
						LookupInputBase.this.toggleFloater(null);
					}
					handleSetValue(val);
				}
			});
		}
		return actualFormRowRenderer;
	}

	@Override
	protected String getKeySearchValue() {
		KeyWordSearchInput<OT> ks = getKeySearch();
		if(null == ks)
			return null;
		return ks.getKeySearchValue();
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

	@Nullable
	public IErrorMessageListener getCustomErrorMessageListener() {
		return m_customErrorMessageListener;
	}

	public void setCustomErrorMessageListener(@Nullable IErrorMessageListener customErrorMessageListener) {
		m_customErrorMessageListener = customErrorMessageListener;
	}

	@Nullable
	public IActionAllowed getIsLookupAllowed() {
		return m_isLookupAllowed;
	}

	public void setIsLookupAllowed(@Nullable IActionAllowed isLookupAllowed) {
		m_isLookupAllowed = isLookupAllowed;
	}

	@Nullable
	public IKeyWordSearchQueryFactory<QT> getKeyWordSearchHandler() {
		return m_keyWordSearchHandler;
	}

	public void setKeyWordSearchHandler(@Nullable IKeyWordSearchQueryFactory<QT> keyWordSearchManipulator) {
		m_keyWordSearchHandler = keyWordSearchManipulator;
	}

	/**
	 * Set hint to keyword search input. Usually says how search condition is resolved.
	 */
	@Override
	public void setKeySearchHint(@Nullable String keySearchHint) {
		super.setKeySearchHint(keySearchHint);
		KeyWordSearchInput<OT> keySearch = getKeySearch();
		if(keySearch != null)
			keySearch.setHint(keySearchHint); // Remove the hint on null.
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

	public int getKeyWordSearchPopupWidth() {
		return m_keyWordSearchPopupWidth;
	}

	public void setKeyWordSearchPopupWidth(int keyWordSearchPopupWidth) {
		m_keyWordSearchPopupWidth = keyWordSearchPopupWidth;
	}

	//public void setAbsolutePopupLayoutQuirkMode(boolean value) {
	//	m_absolutePopupLayoutQuirkMode = value;
	//}

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
	 * @See  {@link LookupInput#m_lookupFormInitialization}.
	 * @return
	 */
	public ILookupFormModifier<QT> getSearchPanelInitialization() {
		return m_lookupFormInitialization;
	}

	/**
	 * @See  {@link LookupInput#m_lookupFormInitialization}.
	 * @return
	 */
	public void setSearchPanelInitialization(ILookupFormModifier<QT> lookupFormInitialization) {
		m_lookupFormInitialization = lookupFormInitialization;
	}

	/**
	 * Define the columns to show in "display current value" mode. This actually creates a
	 * content renderer (a {@link LookupInputPropertyRenderer}) to render the fields.
	 *
	 * @param columns
	 */
	public void setValueColumns(String... columns) {
		setValueRenderer(new LookupInputPropertyRenderer<OT>(getOutputClass(), columns));
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
	 * Define the full column spec in the format described for {@link BasicRowRenderer} for the dropdown box
	 * showing quick search results.
	 * @param columns
	 */
	public void addDropdownColumns(@Nonnull Object... columns) {
		getDropdownRowRenderer().addColumns(columns);
	}

	/**
	 * DO NOT USE - this sets both dropdown columns AND full lookup form columns to the column spec passed... It
	 * is preferred to separate those.
	 *
	 * @param resultColumns
	 */
	public void setResultColumns(@Nonnull String... resultColumns) {
		addDropdownColumns((Object[]) resultColumns);
		addFormColumns((Object[]) resultColumns);
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

	protected DataTable<OT> getDataTable() {
		return m_result;
	}

	protected void closePopup() throws Exception {
		if(m_floater != null) {
			toggleFloater(null);
		}
	}

	@Override
	protected boolean isPopupShown() {
		return m_floater != null;
	}

	@Nullable
	public IPopupOpener getPopupOpener() {
		return m_popupOpener;
	}

	public void setPopupOpener(@Nullable IPopupOpener popupOpener) {
		if(isBuilt()) {
			throw new ProgrammerErrorException("can't set popup opener on built component!");
		}
		m_popupOpener = popupOpener;
	}

	public boolean isPopupInitiallyCollapsed() {
		return m_popupInitiallyCollapsed;
	}

	public void setPopupInitiallyCollapsed(boolean popupInitiallyCollapsed) {
		m_popupInitiallyCollapsed = popupInitiallyCollapsed;
	}
}

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
package to.etc.domui.component2.lookupinput;

import to.etc.domui.component.event.INotify;
import to.etc.domui.component.input.AbstractLookupInputBase;
import to.etc.domui.component.input.IQueryManipulator;
import to.etc.domui.component.input.ITypedControl;
import to.etc.domui.component.input.SimpleLookupInputRenderer;
import to.etc.domui.component.layout.Dialog;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component.tbl.BasicRowRenderer;
import to.etc.domui.component.tbl.IQueryHandler;
import to.etc.domui.component.tbl.ITableModel;
import to.etc.domui.component.tbl.ITruncateableDataModel;
import to.etc.domui.component.tbl.PageQueryHandler;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IForTarget;
import to.etc.domui.dom.html.IHasModifiedIndication;
import to.etc.domui.dom.html.IReturnPressed;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IExecute;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.Msgs;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

abstract public class LookupInputBase2<QT, OT> extends AbstractLookupInputBase<QT, OT> implements IControl<OT>, ITypedControl<OT>, IHasModifiedIndication, IQueryManipulator<QT>, IForTarget {
	/** If set, the complete title for the popup window shown when the 'find' button is pressed. */
	@Nullable
	private String m_defaultTitle;

	protected void setKeySearch(@Nullable SearchInput2 keySearch) {
		m_keySearch = keySearch;
	}

	private ITableModelFactory<QT, OT> m_modelFactory;

	@Nullable
	private Dialog m_floater;

	@Nullable
	private SearchInput2 m_keySearch;

	@Nullable
	private IStringQueryFactory<QT> m_stringQueryFactory;

	private int m_keyWordSearchPopupWidth;

	@Nullable
	private INotify<Dialog> m_onPopupOpen;

	@Nullable
	private IPopupOpener m_popupOpener;

	/**
	 * When T, it sets default lookup popup with by default collapsed search fields.
	 */
	private boolean m_popupInitiallyCollapsed;

	/**
	 * When T, it sets default lookup popup to search immediately.
	 */
	private boolean m_popupSearchImmediately;

	/**
	 * Factory for the lookup dialog, to be shown when the lookup button
	 * is pressed.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jul 8, 2014
	 */
	public interface IPopupOpener {
		@Nonnull
		<A, B, L extends LookupInputBase2<A, B>> Dialog createDialog(@Nonnull L control, @Nullable ITableModel<B> initialModel, @Nonnull IExecute callOnWindowClose);
	}

	/**
	 * Lookup a POJO Java bean persistent class.
	 * @param queryClass
	 */
	public LookupInputBase2(@Nonnull ITableModelFactory<QT, OT> modelFactory, @Nonnull Class<QT> queryClass, @Nonnull Class<OT> resultClass) {
		this(modelFactory, queryClass, resultClass, null, null);
	}

	public LookupInputBase2(@Nonnull ITableModelFactory<QT, OT> modelFactory, @Nonnull QCriteria<QT> rootCriteria, @Nonnull Class<OT> resultClass) {
		this(rootCriteria, modelFactory, DomUtil.nullChecked(rootCriteria.getBaseClass()), resultClass, null, null);
	}

	public LookupInputBase2(@Nonnull ITableModelFactory<QT, OT> modelFactory, @Nonnull Class<QT> queryClass, @Nonnull Class<OT> resultClass, @Nullable ClassMetaModel queryMetaModel,
		@Nullable ClassMetaModel outputMetaModel) {
		this(null, modelFactory, queryClass, resultClass, queryMetaModel, outputMetaModel);
		m_modelFactory = modelFactory;
		setQueryHandler(new PageQueryHandler<QT>(this));
	}

	public LookupInputBase2(@Nullable QCriteria<QT> rootCriteria, @Nonnull ITableModelFactory<QT, OT> modelFactory, @Nonnull Class<QT> queryClass, @Nonnull Class<OT> resultClass, @Nullable ClassMetaModel queryMetaModel,
		@Nullable ClassMetaModel outputMetaModel) {
		super(rootCriteria, queryClass, resultClass, queryMetaModel, outputMetaModel);
		m_modelFactory = modelFactory;
		setQueryHandler(new PageQueryHandler<QT>(this));
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Quick Search code (KeySearch)						*/
	/*--------------------------------------------------------------*/
	/**
	 * @return true either when query control is manually implemented by keyWordSearchHandler, or if keyword search meta data is defined.
	 */
	@Override
	protected boolean isKeyWordSearchDefined() {
		if(getKeywordLookupPropertyList() != null)
			return true;
		List<SearchPropertyMetaModel> spml = getQueryMetaModel().getKeyWordSearchProperties();
		return spml.size() > 0;
	}

	/**
	 * Render the "current value" display as an input box.
	 */
	@Override
	protected void renderKeyWordSearch() {
		SearchInput2 ks = new SearchInput2(getKeyWordSearchCssClass());
		add(ks);
		setKeySearch(ks);

		ks.setPopupWidth(getKeyWordSearchPopupWidth());

		ks.setOnLookupTyping(component -> {
			ITableModel<OT> keySearchModel = searchKeyWord(component.getValue());
			showResults(keySearchModel);
		});

		ks.setReturnPressed((IReturnPressed<SearchInput2>) this::handleSelection);

		//if(m_keyWordSearchCssClass != null) {				// jal: 20171123 Already set on control, do not set on outer!
		//	addCssClass(m_keyWordSearchCssClass);
		//}
		String hint = getKeySearchHint();
		ks.setHint(Msgs.BUNDLE.formatMessage(Msgs.UI_KEYWORD_SEARCH_HINT, (hint != null) ? hint : getDefaultKeySearchHint()));
	}

	private void handleSelection(@Nonnull SearchInput2 node) throws Exception {
		SelectOnePanel<OT> sp = m_selectPanel;
		if(sp != null) {
			OT value = sp.getValue();                        // Is a value selected?
			if(null != value) {
				clearResult();
				handleSetValue(value);
				return;
			}
		}
		ITableModel<OT> keySearchModel = searchKeyWord(node.getValue());
		openPopup(keySearchModel);
	}

	@Override
	@Nullable
	protected SearchInput2 getKeySearch() {
		return m_keySearch;
	}

	@Override protected void clearKeySearch() {
		m_keySearch = null;
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
		IStringQueryFactory<QT> ksh = getStringQueryFactory();
		QCriteria<QT> searchQuery = ksh.createQuery(searchString);
		if(searchQuery == null) {								// Search cancelled
			return null;
		}

		searchQuery = adjustQuery(searchQuery);					// Manipulate if needed
		if(searchQuery == null) {								// Manipulate cancelled
			return null;
		}

		return createTableModel(searchQuery);
	}

	@Nonnull
	private ITableModel<OT> createTableModel(@Nonnull QCriteria<QT> qc) throws Exception {
		ITableModelFactory<QT, OT> factory = m_modelFactory;
		if(null == factory)
			throw new IllegalStateException("Table model factory unset");
		IQueryHandler<QT> queryHandler = getQueryHandler();
		if(null == queryHandler) {
			queryHandler = new PageQueryHandler<>(this);
			setQueryHandler(queryHandler);
		}
		return factory.createTableModel(queryHandler, qc);
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
		ITableModel<OT> initialModel = null;
		SearchInput2 keySearch = getKeySearch();
		if(keySearch != null) {
			initialModel = searchKeyWord(keySearch.getValue());
		}
		openPopup(initialModel);
	}

	private void closePopup() {
		Dialog floater = m_floater;
		if(floater == null)
			return;
		floater.close();
		m_floater = null;
	}

	private void openPopup(@Nullable ITableModel<OT> initialModel) throws Exception {
		if(m_floater != null) {
			return;
		}
		IPopupOpener po = m_popupOpener;
		if(null == po) {
			po = createPopupOpener();
		}
		Dialog floater = m_floater = po.createDialog(this, initialModel, () -> m_floater = null);
		floater.setCssClass("ui-lui2-dlg");
		floater.modal();
		add(floater);

		INotify<Dialog> onPopupOpen = getOnPopupOpen();

		decoratePopup(floater);

		if(null != onPopupOpen)
			onPopupOpen.onNotify(floater);
	}

	private void decoratePopup(@Nonnull Dialog floater) {
		if (isPopupInitiallyCollapsed() && floater instanceof DefaultLookupInputDialog) {
			((DefaultLookupInputDialog<?, ?>) floater).setInitiallyCollapsed(true);
		}

		if (isPopupSearchImmediately() && floater instanceof DefaultLookupInputDialog) {
			((DefaultLookupInputDialog<?, ?>) floater).setSearchImmediately(true);
		}
	}

	@Nullable
	public INotify<Dialog> getOnPopupOpen() {
		return m_onPopupOpen;
	}

	public void setOnPopupOpen(@Nullable INotify<Dialog> onPopupOpen) {
		m_onPopupOpen = onPopupOpen;
	}

	@Nonnull
	private IPopupOpener createPopupOpener() {
		return new DefaultPopupOpener();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Showing the key typed search result.				*/
	/*--------------------------------------------------------------*/
	@Nullable
	private SelectOnePanel<OT> m_selectPanel;

	@Nullable
	private Div m_pnlMessage;

	public void showResults(@Nullable ITableModel<OT> model) throws Exception {
		clearResult();

		if(model == null) {
			//-- No search done- clear all presentation.
			return;
		}

		int size = model.getRows();
		if(size == 0) {
			openMessagePanel("ui-lui-result-none", Msgs.UI_KEYWORD_SEARCH_NO_MATCH);
		} else if (size == 1){ //in case of single match select value
			handleSetValue(model.getItems(0, 1).get(0));
		} else if(size > 10) {
			String count = Integer.toString(size);
			if(model instanceof ITruncateableDataModel) {
				if(((ITruncateableDataModel) model).isTruncated())
					count = "> " + count;
			}
			openMessagePanel("ui-lui-result-count", Msgs.UI_KEYWORD_SEARCH_COUNT, count);
		} else {
			openResultsPopup(model);
		}
	}

	private void openResultsPopup(@Nonnull ITableModel<OT> model) throws Exception {
		List<OT> list = model.getItems(0, model.getRows());
		IRenderInto<OT> renderer = new DefaultPopupRowRenderer<OT>(getOutputMetaModel());

		SelectOnePanel<OT> pnl = m_selectPanel = new SelectOnePanel<OT>(list, renderer);
		DomUtil.nullChecked(getKeySearch()).add(pnl);

		pnl.setOnValueChanged((IValueChanged<SelectOnePanel<OT>>) component -> {
			clearResult();
			OT selection = component.getValue();
			if(null != selection)
				handleSetValue(selection);
		});

		pnl.setClicked(clickednode -> {
			//we just need to deliver selected value here, that is why we have empty click handler
		});
	}

	private void openMessagePanel(@Nonnull String cssClass, @Nonnull String code, String... parameters) {
		String message = Msgs.BUNDLE.formatMessage(code, parameters);
		Div pnl = m_pnlMessage;
		if(pnl == null) {
			pnl = m_pnlMessage = new Div();
			Objects.requireNonNull(getKeySearch()).add(pnl);
			//add(pnl);
		}
		pnl.setCssClass("ui-srip-message " + cssClass);
		pnl.setText(message);
	}

	private void clearResult() {
		Div div = m_pnlMessage;
		if(null != div) {
			div.remove();
			m_pnlMessage = null;
		}
		SelectOnePanel<OT> panel = m_selectPanel;
		if(null != panel) {
			panel.remove();
			m_selectPanel = null;
		}
	}


	/**
	 * Construct a default title for this LookupInput
	 *
	 * @return
	 */
	@Nonnull
	public String getDefaultTitle() {
		String popupTitle = m_defaultTitle;
		if(null != popupTitle)
			return popupTitle;

		String entity = getOutputMetaModel().getUserEntityName();
		if(entity != null)
			return Msgs.BUNDLE.formatMessage(Msgs.UI_LUI_TTL_WEN, entity);

		return Msgs.BUNDLE.getString(Msgs.UI_LUI_TTL);
	}

	public void setDefaultTitle(@Nullable String defaultTitle) {
		m_defaultTitle = defaultTitle;
	}

	@Override
	protected String getKeySearchValue() {
		SearchInput2 ks = getKeySearch();
		if(null == ks)
			return null;
		return ks.getValue();
	}

	/**
	 * EXPERIMENTAL This callback must be called by the popup once a selection is made.
	 * @param value
	 * @throws Exception
	 */
	public final void setDialogSelection(@Nullable OT value) throws Exception {
		if(null == value)							// Null means: no selection made, so retain the current one
			return;
		m_floater = null;							// ORDERED: see getOnValueChanged kludge
		handleSetValue(value);						// ORDERED
	}


	@Nonnull
	public IStringQueryFactory<QT> getStringQueryFactory() {
		IStringQueryFactory<QT> factory = m_stringQueryFactory;
		if(null == factory) {
			m_stringQueryFactory = factory = new DefaultStringQueryFactory<QT>(getQueryMetaModel());
		}
		return factory;
	}

	public void setStringQueryFactory(@Nonnull IStringQueryFactory<QT> keyWordSearchManipulator) {
		m_stringQueryFactory = keyWordSearchManipulator;
	}

	/**
	 * Set hint to keyword search input. Usually says how search condition is resolved.
	 * @param keySearchHint
	 */
	@Override
	public void setKeySearchHint(@Nullable String keySearchHint) {
		super.setKeySearchHint(keySearchHint);
		SearchInput2 keySearch = getKeySearch();
		if(keySearch != null)
			keySearch.setHint(keySearchHint); // Remove the hint on null.
	}


	public int getKeyWordSearchPopupWidth() {
		return m_keyWordSearchPopupWidth;
	}

	public void setKeyWordSearchPopupWidth(int keyWordSearchPopupWidth) {
		m_keyWordSearchPopupWidth = keyWordSearchPopupWidth;
	}

	/**
	 * Define the columns to show in "display current value" mode. This actually creates a
	 * content renderer (a {@link SimpleLookupInputRenderer}) to render the fields.
	 *
	 * @param columns
	 */
	public void setValueColumns(String... columns) {
		setValueRenderer(new SimpleLookupInputRenderer<OT>(getOutputClass(), columns));
	}

	/**
	 * Define the full column spec in the format described for {@link BasicRowRenderer} for the dropdown box
	 * showing quick search results.
	 */
//	public void addDropdownColumns(@Nonnull Object... columns) {
//		getDropdownRowRenderer().addColumns(columns);
//	}

	@Override
	protected boolean isPopupShown() {
		return m_floater != null;
	}

	@Nonnull
	public ITableModelFactory<QT, OT> getModelFactory() {
		ITableModelFactory<QT, OT> modelFactory = m_modelFactory;
		if(null == modelFactory)
			throw new IllegalStateException("The model factory is not set");
		return modelFactory;
	}

	public void setModelFactory(@Nonnull ITableModelFactory<QT, OT> modelFactory) {
		m_modelFactory = modelFactory;
	}

	@Nullable
	public IPopupOpener getPopupOpener() {
		return m_popupOpener;
	}

	public void setPopupOpener(IPopupOpener popupOpener) {
		if (isBuilt()){
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

	public boolean isPopupSearchImmediately() {
		return m_popupSearchImmediately;
	}

	public void setPopupSearchImmediately(boolean popupSearchImmediatelly) {
		m_popupSearchImmediately = popupSearchImmediatelly;
	}

}

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

import java.math.*;
import java.util.*;

import javax.annotation.*;
import javax.annotation.Nullable;

import org.jetbrains.annotations.*;
import to.etc.domui.component.binding.*;
import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.lookup.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.themes.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.*;
import to.etc.webapp.query.*;

abstract public class LookupInputBase<QT, OT> extends Div implements IControl<OT>, ITypedControl<OT>, IHasModifiedIndication {

	public static final String MAGIC_ID_MARKER = "?id?";

	static public final IRenderInto<Object> DEFAULT_RENDERER = new SimpleLookupInputRenderer<>();

	/**
	 * Interface provides assess to used lookup form initialization method.
	 *
	 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
	 * Created on 19 Jul 2011
	 */
	public interface ILookupFormModifier<T> {
		/**
		 * Sends LookupForm for initialization.
		 * @param lf
		 */
		void initialize(@Nonnull LookupForm<T> lf) throws Exception;
	}

	/**
	 * The query class/type. For Java classes this usually also defines the metamodel to use; for generic meta this should
	 * be the value record class type.
	 */
	@Nonnull
	final private Class<QT> m_queryClass;

	@Nonnull
	final private Class<OT> m_outputClass;

	/**
	 * The metamodel to use to handle the query data in this class. For Javabean data classes this is automatically
	 * obtained using MetaManager; for meta-based data models this
	 * gets passed as a constructor argument.
	 */
	@Nonnull
	final private ClassMetaModel m_queryMetaModel;

	/**
	 * The metamodel for output (display) objects.
	 */
	@Nonnull
	final private ClassMetaModel m_outputMetaModel;

	@Nullable
	private LookupForm<QT> m_lookupForm;

	@Nullable
	private HoverButton m_selButton;

	@Nullable
	private HoverButton m_clearButton;

	@Nullable
	private FloatingWindow m_floater;

	@Nullable
	private DataTable<OT> m_result;

	@Nullable
	private OT m_value;

	private Table m_table;

	private boolean m_mandatory;

	private boolean m_readOnly;

	private boolean m_disabled;

	@Nullable
	private IQueryManipulator<QT> m_queryManipulator;

	@Nullable
	private IQueryHandler<QT> m_queryHandler;

	@Nullable
	private String m_formTitle;

	/**
	 * The content renderer to use to render the current value.
	 */
	@Nullable
	private IRenderInto<OT> m_valueRenderer;

	@Nullable
	private IErrorMessageListener m_customErrorMessageListener;

	@Nullable
	private IActionAllowed m_isLookupAllowed;

	@Nullable
	private KeyWordSearchInput<OT> m_keySearch;

	@Nullable
	private String m_keySearchHint;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	private boolean m_modifiedByUser;

	@Nullable
	private IKeyWordSearchQueryFactory<QT> m_keyWordSearchHandler;

	/** When T (default) you can press search on an empty popup form. 20120511 jal Default set to true. */
	private boolean m_allowEmptyQuery = true;

	private boolean m_searchImmediately;

	@Nullable
	private String m_keyWordSearchCssClass;

	private int m_keyWordSearchPopupWidth;

	@Nullable
	private String m_selectionCssClass;

	/**
	 * SPECIAL QUIRK MODE, USUALLY YOU DO NOT NEED IT.
	 * When T (default is F), it renders lookup input in a way that pop-up with search as you type results rolls down exceeding the boundaries of parent control.
	 * This is useful when your LookupInput is last control in pop-up Windows, and you want to avoid scroll-bar in dialog. However, mode is not applicable in all other regular cases since
	 * it interfere rendering of LookupInput that goes over controls bellow it.
	 */
	private boolean m_absolutePopupLayoutQuirkMode;

	/**
	 * By default set to true.
	 * Set to false in cases when keyword search functionality should be disabled regardless if metadata for this feature is defined or not.
	 */
	private boolean m_allowKeyWordSearch = true;

	/** Contains manually added quicksearch properties. Is null if none are added. */
	@Nullable
	private List<SearchPropertyMetaModel> m_keywordLookupPropertyList;

	/** The search properties to use in the lookup form when created. If null uses the default attributes on the class. */
	@Nullable
	private List<SearchPropertyMetaModel> m_searchPropertyList;

	private enum RebuildCause {
		CLEAR, SELECT
	};

	/**
	 * When we trigger forceRebuild, we can specify reason for this, and use this later to resolve focus after content is re-rendered.
	 */
	@Nullable
	private RebuildCause m_rebuildCause;

	/**
	 * Default T. When set, table result would be stretched to use entire available height on FloatingWindow.
	 */
	private boolean m_useStretchedLayout = true;

	/**
	 * If set, enables custom init code on LookupForm that is in use for this component, triggers before LookupForm is shown
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


	@Nullable
	private QCriteria<QT> m_rootCriteria;

	private boolean m_doFocus;

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
		this(queryClass, resultClass, (ClassMetaModel) null, (ClassMetaModel) null);
		setResultColumns(resultColumns);
	}

	/**
	 * Lookup a POJO Java bean persistent class.
	 * @param queryClass
	 */
	public LookupInputBase(@Nonnull Class<QT> queryClass, @Nonnull Class<OT> resultClass) {
		this(queryClass, resultClass, (ClassMetaModel) null, (ClassMetaModel) null);
	}

	public LookupInputBase(@Nonnull QCriteria<QT> rootCriteria, @Nonnull Class<OT> resultClass) {
		this(DomUtil.nullChecked(rootCriteria.getBaseClass()), resultClass, (ClassMetaModel) null, (ClassMetaModel) null);
		m_rootCriteria = rootCriteria;
	}

	public LookupInputBase(@Nonnull Class<QT> queryClass, @Nonnull Class<OT> resultClass, @Nullable ClassMetaModel queryMetaModel, @Nullable ClassMetaModel outputMetaModel) {
		m_queryClass = queryClass;
		m_outputClass = resultClass;
		m_queryMetaModel = queryMetaModel != null ? queryMetaModel : MetaManager.findClassMeta(queryClass);
		m_outputMetaModel = outputMetaModel != null ? outputMetaModel : MetaManager.findClassMeta(resultClass);
		HoverButton b = m_selButton = new HoverButton(Theme.BTN_HOVERPOPUPLOOKUP);
		b.addCssClass("ui-lui-sel-btn");
		b.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(@Nonnull NodeBase b) throws Exception {
				toggleFloaterByClick();
			}
		});

		b = m_clearButton = new HoverButton(Theme.BTN_HOVERCLEARLOOKUP, new IClicked<HoverButton>() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void clicked(@Nonnull HoverButton b) throws Exception {
				handleSetValue(null);
			}
		});
		b.addCssClass("ui-lui-clear-btn");
		setCssClass("ui-lui");
	}

	@Nonnull
	private HoverButton getSelButton() {
		if(null != m_selButton)
			return m_selButton;
		throw new IllegalStateException("Selection button is not there.");
	}

	@Nonnull
	public HoverButton getClearButton() {
		if(null != m_clearButton)
			return m_clearButton;
		throw new IllegalStateException("Clear button is not there.");
	}

	@Override
	public void createContent() throws Exception {
		Table table = m_table = new Table("ui-lui-tbl");
		add(table);

		table.setCellSpacing("0");
		table.setCellPadding("0");
		m_keySearch = null;

		removeCssClass("ui-ro");
		OT value = m_value;
		if(value == null) {
			if(isAllowKeyWordSearch() && isKeyWordSearchDefined()){
				//Key word search rendering should be generic, no need for customization possibilities.
				if(isReadOnly() || isDisabled()) {
					renderEmptySelection();
					addCssClass("ui-ro");
				} else {
					renderKeyWordSearch();
				}
			} else {
				//-- Render "no selection"
				renderEmptySelection();
			}
		} else {
			//-- Nonnull render: render a value in the table's 1st cell
			TD td = table.getBody().addRowAndCell("ui-lui-v");

			IRenderInto<OT> r = getValueRenderer();
			if(r == null)
				r = (IRenderInto<OT>) DEFAULT_RENDERER; // Prevent idiotic generics error
			r.render(td, value);
			//r.render(this, m_value, isReadOnly() || isDisabled() ? null : m_selButton);

			handleSelectionCss();
		}

		appendLookupButtons();

		HoverButton clearButton = getClearButton();

		if(m_rebuildCause == RebuildCause.CLEAR) {
			//User clicked clear button, so we can try to set focus to input search if possible.
			if(m_keySearch != null) {
				m_keySearch.setFocus();
			}
		} else if(m_rebuildCause == RebuildCause.SELECT) {
			//User did reselected value, so we can try to set focus to clear button if possible.
			if(clearButton != null && !clearButton.isDisabled()) {
				if(getPage().getFocusComponent() == null)
					clearButton.setFocus();
			}
		}
		m_rebuildCause = null;

		if(m_doFocus) {
			m_doFocus = false;
			if(m_keySearch != null)
				m_keySearch.setFocus();
			else if(m_clearButton != null)
				m_clearButton.setFocus();
		}
		//if(m_absolutePopupLayoutQuirkMode) {
		//	getSelButton().setMarginLeft("103px");
		//}

	}

	private void appendLookupButtons() {
		if(isReadOnly() || isDisabled())
			return;

		//-- Lookup button is always there
		TR tr = m_table.getBody().getRow(0);
		TD cell = tr.addCell("ui-lui-btntd");
		Div d = new Div("ui-lui-btn-c");
		cell.add(d);
		d.add(getSelButton());

		cell = tr.addCell("ui-lui-btntd");
		d = new Div("ui-lui-btn-c");
		cell.add(d);
		d.add(getClearButton());
		getClearButton().setDisabled(m_value == null);

		getSelButton().setTestID(calcTestID() + "-lookup");
		getClearButton().setTestID(calcTestID() + "-clear");

// jal 20121025 temp disabled
//			//FIXME: vmijic, not suitable for larger button images, see is this can be resolved by introducing span container for buttons.
//			if(clearButton.getDisplay() == DisplayType.NONE) {
//				clearButton.getParent().setMinWidth("24px");
//			} else {
//				clearButton.getParent().setMinWidth("58px");
//			}
	}

	private void handleSelectionCss() {
		String selectionCssClass = getSelectionCssClass();
		if (!StringTool.isBlank(selectionCssClass)) {
			//zilla 7548 -> if selected value txt is too large, we should be enabled to limit it, in some situations. So we use css for that.
			//When text is cutoff by that css, we have to show entire text in hover.
			//We use internal ui-lui-vcell style here, since it can not be provided from INodeContentRenderer itself :(
			//Since this is internal component code too, relaying on this internal details of renderer are not too bad
			getParent().appendShowOverflowTextAsTitleJs("." + selectionCssClass + " td.ui-lui-vcell");
		}
	}

	private void appendParameters(@Nonnull TD cell, @Nonnull Object parameters) {
		TD tdParameters = new TD();
		cell.appendAfterMe(tdParameters);
		tdParameters.setCssClass("ui-lui-btntd");
		tdParameters.setValign(TableVAlign.TOP);
		tdParameters.add((NodeBase) parameters); // Add the button,
	}

	/**
	 * Render the presentation for empty/unselected input.
	 */
	private void renderEmptySelection() {
		Table table = m_table;
		table.removeAllChildren();
		TD td = table.getBody().addRowAndCell();
		//td.setValign(TableVAlign.TOP);
		td.setCssClass("ui-lui-empty");
		td.add(new Span(Msgs.BUNDLE.getString(Msgs.UI_LOOKUP_EMPTY)));
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Quick Search code (KeySearch)						*/
	/*--------------------------------------------------------------*/
	/**
	 * @return true either when query control is manually implemented by keyWordSearchHandler, or if keyword search meta data is defined.
	 */
	private boolean isKeyWordSearchDefined() {
		if(getKeyWordSearchHandler() != null) {
			return true;
		}

		if(m_keywordLookupPropertyList != null)
			return true;
		List<SearchPropertyMetaModel> spml = getQueryMetaModel().getKeyWordSearchProperties();
		return spml.size() > 0;
	}

	/**
	 * Render the "current value" display as an input box or display box with clear and select buttons.
	 * @param parameters
	 */
	private void renderKeyWordSearch() {
		m_table.removeAllChildren();
		TD td = m_table.getBody().addRowAndCell();
		//td.setValign(TableVAlign.TOP);
		td.setCssClass("ui-lui-lookupf");
//		td.setWidth("100%"); jal 20121025 Width should not be set but style should be used?
		addKeySearchField(td);
	}

	private void addKeySearchField(NodeContainer parent) {
		KeyWordSearchInput<OT> ks = m_keySearch = new KeyWordSearchInput<OT>(m_keyWordSearchCssClass);
		ks.setPopupWidth(getKeyWordSearchPopupWidth());
		ks.setAbsolutePopupLayoutQuirkMode(m_absolutePopupLayoutQuirkMode);
		KeyWordPopupRowRenderer<OT> rr = getDropdownRowRenderer();
		rr.setRowClicked(new ICellClicked<OT>() {
			@Override
			public void cellClicked(@Nonnull OT val) throws Exception {
				handleSetValue(val);
			}
		});
		ks.setResultsHintPopupRowRenderer(rr);

		ks.setOnLookupTyping(new IValueChanged<KeyWordSearchInput<OT>>() {

			@Override
			public void onValueChanged(@Nonnull KeyWordSearchInput<OT> component) throws Exception {
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
			}
		});

		ks.setOnShowResults(new IValueChanged<KeyWordSearchInput<OT>>() {
			@Override
			public void onValueChanged(@Nonnull KeyWordSearchInput<OT> component) throws Exception {
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
			}
		});
		parent.add(ks);
		if(m_keyWordSearchCssClass != null) {
			addCssClass(m_keyWordSearchCssClass);
		}
		ks.setHint(Msgs.BUNDLE.formatMessage(Msgs.UI_KEYWORD_SEARCH_HINT, (m_keySearchHint != null) ? m_keySearchHint : getDefaultKeySearchHint()));
	}

	@Nonnull
	private KeyWordSearchInput<OT> getKeySearch() {
		if(null != m_keySearch)
			return m_keySearch;
		throw new IllegalStateException("keySearch is null");
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

	private String getDefaultKeySearchHint() {
		List<SearchPropertyMetaModel> spml = m_keywordLookupPropertyList != null ? m_keywordLookupPropertyList : getQueryMetaModel().getKeyWordSearchProperties();
		if(spml.size() <= 0)
			return null;

		StringBuilder sb = new StringBuilder(128);
		for(int i = 0; i < spml.size(); i++) {
			if(sb.length() > 0)
				sb.append(", ");
			SearchPropertyMetaModel spm = spml.get(i);
			if(null == spm)
				throw new IllegalStateException("null entry in keyword search list");

			if(spm.getLookupLabel() != null) {
				sb.append(spm.getLookupLabel());
			} else {
				//FIXME: vmijic 20110906 Scheduled for delete. We add extra tests and logging in code just to be sure if such cases can happen in production.
				//This should be removed soon after we are sure that problem is solved.
				String propertyName = spm.getPropertyName();
				if(propertyName == null)
					throw new IllegalStateException("Search property name is null");
				PropertyMetaModel< ? > pmm = getQueryMetaModel().findProperty(propertyName);
				if(pmm == null)
					throw new IllegalStateException(propertyName + ": undefined property in " + getQueryMetaModel());
				if(pmm.getDefaultLabel() != null)
					sb.append(pmm.getDefaultLabel());
				else
					sb.append(pmm.getName());
			}
		}
		return sb.toString();
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
		searchQuery = manipulateCriteria(searchQuery);
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
	 * @param searchString
	 * @return
	 * @throws Exception
	 */
	@Nullable
	private QCriteria<QT> createTestQuery(@Nonnull Long magicId) throws Exception {
		if(IIdentifyable.class.isAssignableFrom(m_queryClass)) {
			QCriteria<QT> searchQuery = (QCriteria<QT>) getQueryMetaModel().createCriteria();
			searchQuery.eq("id", magicId);
			return searchQuery;
		}
		throw new RuntimeException("This instance cannot be used for filling in lookup using magic string: " + m_queryClass);
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
			List<SearchPropertyMetaModel> spml = m_keywordLookupPropertyList == null ? getQueryMetaModel().getKeyWordSearchProperties() : getKeywordLookupPropertyList();
			searchQuery = (QCriteria<QT>) getQueryMetaModel().createCriteria();

			QRestrictor<QT> r = searchQuery.or();
			int ncond = 0;
			if(spml.size() > 0) {
				for(SearchPropertyMetaModel spm : spml) {
					if(spm.getMinLength() <= searchString.length()) {

						//-- Abort on invalid metadata; never continue with invalid data.
						if(spm.getPropertyName() == null)
							throw new ProgrammerErrorException("The quick lookup properties for " + getQueryMetaModel() + " are invalid: the property name is null");

						List<PropertyMetaModel< ? >> pl = MetaManager.parsePropertyPath(getQueryMetaModel(), spm.getPropertyName()); // This will return an empty list on empty string input
						if(pl.size() == 0)
							throw new ProgrammerErrorException("Unknown/unresolvable lookup property " + spm.getPropertyName() + " on " + getQueryMetaModel());

						//It is required that lookup by id is also available, for now only integer based types and BigDecimal interpreted as Long (fix for 1228) are supported
						PropertyMetaModel< ? > pmm = pl.get(pl.size() - 1);
						if(DomUtil.isIntegerType(pmm.getActualType()) || pmm.getActualType() == BigDecimal.class) {
							if(searchString.contains("%") && !pmm.isTransient()) {
								r.add(new QPropertyComparison(QOperation.LIKE, spm.getPropertyName(), new QLiteral(searchString)));
							} else {
								try {
									Object value = RuntimeConversions.convertTo(searchString, pmm.getActualType());
									if(null != value) {
										r.eq(spm.getPropertyName(), value);
										ncond++;
									}
								} catch(Exception ex) {
									//just ignore this since it means that it is not correct Long condition.
								}
							}
						} else if(pmm.getActualType().isAssignableFrom(String.class)) {
							if(spm.isIgnoreCase()) {
								r.ilike(spm.getPropertyName(), searchString + "%");
							} else {
								r.like(spm.getPropertyName(), searchString + "%");
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

	@Nullable
	private QCriteria<QT> manipulateCriteria(@Nonnull QCriteria<QT> enteredCriteria) {
		IQueryManipulator<QT> qm = getQueryManipulator();
		QCriteria<QT> result = enteredCriteria;
		if(qm != null) {
			result = qm.adjustQuery(enteredCriteria);
			if(result == null) {
				//in case of cancelled search by query manipulator return
				return null;
			}
		}

		//-- Join any root criteria, if applicable
		QCriteria<QT> root = m_rootCriteria;
		if(null != root) {
			//-- We merge the "root" criteria inside the "child" criteria. We do that by a complete "and", as follows:
			//-- result = (root criteria) AND (entered criteria), and we ignore any "other" part of the root criterion.
			result.mergeCriteria(root);
		}
		return result;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Full search popup window code..						*/
	/*--------------------------------------------------------------*/
	/**
	 * Toggle the full search popup window.
	 * @throws Exception
	 */
	private void toggleFloaterByClick() throws Exception {
		if(m_keySearch != null) {
			toggleFloater(searchKeyWord(m_keySearch.getKeySearchValue()));
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
		LookupForm<QT> lf = getLookupForm();
		if(lf == null) {
			QCriteria<QT> rootCriteria = m_rootCriteria;
			if(null != rootCriteria)
				lf = new LookupForm<QT>(rootCriteria);
			else
				lf = new LookupForm<QT>(getQueryClass(), getQueryMetaModel());
			if(m_searchPropertyList != null && m_searchPropertyList.size() != 0)
				lf.setSearchProperties(m_searchPropertyList);
		}

		lf.setCollapsed(keySearchModel != null && keySearchModel.getRows() > 0);
		lf.forceRebuild(); // jal 20091002 Force rebuild to remove any state from earlier invocations of the same form. This prevents the form from coming up in "collapsed" state if it was left that way last time it was used (Lenzo).

		if(getLookupFormInitialization() != null) {
			getLookupFormInitialization().initialize(lf);
		}
		f.add(lf);
		f.setOnClose(new IWindowClosed() {
			@Override
			public void closed(@Nonnull String closeReason) throws Exception {
				f.clearGlobalMessage(Msgs.V_MISSING_SEARCH);
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

	/**
	 * Contruct a default title for this LookupInput
	 *
	 * @return
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

	private void search(LookupForm<QT> lf) throws Exception {
		QCriteria<QT> c = lf.getEnteredCriteria();
		if(c == null)						// Some error has occured?
			return;							// Don't do anything (errors will have been registered)

		c = manipulateCriteria(c);
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
		ITableModel<OT> model = createTableModel(qc);					// Ask derived to convert the query into my output model
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

	/**
	 * Set a hint text for this control, for some reason only on the select button??
	 * @param text
	 */
	public void setHint(@Nonnull String text) {
		if(m_selButton != null)
			m_selButton.setTitle(text);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMandatory(boolean mandatory) {
		m_mandatory = mandatory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnly() {
		return m_readOnly;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setReadOnly(boolean readOnly) {
		if(m_readOnly == readOnly)
			return;
		m_readOnly = readOnly;
		updateRoStyle();
		forceRebuild();
	}

	private void updateRoStyle() {
		if((m_disabled || m_readOnly) && m_value != null)
			addCssClass("ui-lui-selected-ro");
		else
			removeCssClass("ui-lui-selected-ro");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		m_disabled = disabled;
		updateRoStyle();
		forceRebuild();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IControl implementation.							*/
	/*--------------------------------------------------------------*/
	@Nullable
	private IValueChanged< ? > m_onValueChanged;

	@Nullable
	public OT getBindValue() {
		if(m_value == null && isMandatory()) {
			throw new ValidationException(Msgs.MANDATORY);
		}
		return m_value;
	}

	public void setBindValue(@Nullable OT value) {
		setValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	@Override
	public OT getValue() {
		if(m_value == null && isMandatory()) {
			setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.MANDATORY));
			throw new ValidationException(Msgs.MANDATORY);
		}
		return m_value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Nullable
	public OT getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	/**
	 * The value without any consequences
	 * @return
	 */
	public OT getWorkValue() {
		OT valueSafe = getValueSafe();
		clearMessage();
		return valueSafe;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasError() {
		getValueSafe();
		return super.hasError();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(@Nullable OT v) {
		KeyWordSearchInput<OT> ks = m_keySearch;
		if(DomUtil.isEqual(m_value, v) && (ks == null || ks.getKeySearchValue() == null))
			return;
		OT old = m_value;
		m_value = v;
		if(v != null) {
			getClearButton().setDisabled(false);
			clearMessage();
			addCssClass("ui-lui-selected");
			String selectionCss = getSelectionCssClass();
			if (!StringTool.isBlank(selectionCss)){
				addCssClass(DomUtil.nullChecked(selectionCss));
			}
		} else {
			getClearButton().setDisabled(true);
			removeCssClass("ui-lui-selected");
		}
		updateRoStyle();
		forceRebuild();
	}

	/**
	 * In case that new value is different than one previously selected, set modified flag, selected value and trigger onValueChange event if defined.
	 * @param value
	 * @throws Exception
	 */
	void handleSetValue(@Nullable OT value) throws Exception {
		if(!MetaManager.areObjectsEqual(value, m_value, null)) {
			DomUtil.setModifiedFlag(this);
			setValue(value);

			try {
				OldBindingHandler.controlToModel(this);
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}

			//-- Handle onValueChanged
			IValueChanged< ? > onValueChanged = getOnValueChanged();
			if(onValueChanged != null) {
				((IValueChanged<NodeBase>) onValueChanged).onValueChanged(this);
			}
		}
		m_rebuildCause = value == null ? RebuildCause.CLEAR : RebuildCause.SELECT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Nullable
	public IValueChanged< ? > getOnValueChanged() {
		if(m_floater != null) {
			//Fix for FF: prevent onchange event to be propagate on control when return key is pressed and popup is opened.
			//This does not happen on IE. Be sure that it is executed after popup is already closed.
			return null;
		}
		return m_onValueChanged;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOnValueChanged(@Nullable IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	/**
	 * When set the specified manipulator will be called before a query is sent to the database. The query
	 * can be altered to add extra restrictions for instance.
	 * @return
	 */
	@Nullable
	public IQueryManipulator<QT> getQueryManipulator() {
		return m_queryManipulator;
	}

	/**
	 * The query handler to use, if a special one is needed. The default query handler will use the
	 * normal conversation-associated DataContext to issue the query.
	 * @return
	 */
	@Nullable
	public IQueryHandler<QT> getQueryHandler() {
		return m_queryHandler;
	}

	public void setQueryHandler(@Nullable IQueryHandler<QT> queryHandler) {
		m_queryHandler = queryHandler;
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
	 * When set the specified manipulator will be called before a query is sent to the database. The query
	 * can be altered to add extra restrictions for instance.
	 *
	 * @param queryManipulator
	 */
	public void setQueryManipulator(@Nullable IQueryManipulator<QT> queryManipulator) {
		m_queryManipulator = queryManipulator;
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

	@Nullable
	public IErrorMessageListener getCustomErrorMessageListener() {
		return m_customErrorMessageListener;
	}

	public void setCustomErrorMessageListener(@Nullable IErrorMessageListener customErrorMessageListener) {
		m_customErrorMessageListener = customErrorMessageListener;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IHasModifiedIndication impl							*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the modified-by-user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#isModified()
	 */
	@Override
	public boolean isModified() {
		return m_modifiedByUser;
	}

	/**
	 * Set or clear the modified by user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#setModified(boolean)
	 */
	@Override
	public void setModified(boolean as) {
		m_modifiedByUser = as;
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

	@Nullable
	public String getKeyWordSearchCssClass() {
		return m_keyWordSearchCssClass;
	}

	/**
	 * Set custom css that would be applied only in case that component is rendering keyWordSearch.
	 * Used for example in row inline rendering, where width and min-width should be additionally customized.
	 * @param cssClass
	 */
	public void setKeyWordSearchCssClass(@Nullable String cssClass) {
		m_keyWordSearchCssClass = cssClass;
	}

	@Nullable
	public String getSelectionCssClass() {
		return m_selectionCssClass;
	}

	/**
	 * Set custom css that would be applied only in case that component is rendering selected value.
	 * Used for example where max-width should be additionally customized.
	 * @param cssClass
	 */
	public void setSelectionCssClass(@Nullable String cssClass) {
		m_selectionCssClass = cssClass;
	}

	/**
	 * Getter for property {@link LookupInput#m_allowKeyWordSearch}.
	 * @return
	 */
	public boolean isAllowKeyWordSearch() {
		return m_allowKeyWordSearch;
	}

	/**
	 * Setter for property {@link LookupInput#m_allowKeyWordSearch}.
	 * @return
	 */
	public void setAllowKeyWordSearch(boolean allowKeyWordSearch) {
		m_allowKeyWordSearch = allowKeyWordSearch;
	}

	/**
	 * Getter for keyword search hint. See {@link LookupInput#setKeySearchHint}.
	 * @param hint
	 */
	@Nullable
	public String getKeySearchHint() {
		return m_keySearchHint;
	}

	/**
	 * Set hint to keyword search input. Usually says how search condition is resolved.
	 * @param hint
	 */
	public void setKeySearchHint(@Nullable String keySearchHint) {
		m_keySearchHint = keySearchHint;
		if(m_keySearch != null)
			m_keySearch.setHint(keySearchHint); // Remove the hint on null.
	}

	/**
	 * Define a property to use for quick search. When used this overrides any metadata-defined
	 * properties.
	 * @param name
	 * @param minlen
	 */
	public void addKeywordProperty(@Nonnull String name, int minlen) {
		if(m_keywordLookupPropertyList == null)
			m_keywordLookupPropertyList = new ArrayList<SearchPropertyMetaModel>();
		SearchPropertyMetaModelImpl si = new SearchPropertyMetaModelImpl(getQueryMetaModel());
		if(minlen > 0)
			si.setMinLength(minlen);
		si.setPropertyName(name);
		si.setIgnoreCase(true);
		DomUtil.nullChecked(m_keywordLookupPropertyList).add(si);
	}

	/**
	 * Not normally used; use {@link #addKeywordProperty(String, int)} instead.
	 * @param keywordLookupPropertyList
	 */
	public void setKeywordSearchProperties(@Nonnull List<SearchPropertyMetaModel> keywordLookupPropertyList) {
		m_keywordLookupPropertyList = keywordLookupPropertyList;
	}

	@Nonnull
	public List<SearchPropertyMetaModel> getKeywordLookupPropertyList() {
		if(null != m_keywordLookupPropertyList)
			return m_keywordLookupPropertyList;
		throw new NullPointerException("No keyword properties set.");
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

	/**
	 * Define a property to use for quick search. When used this overrides any metadata-defined
	 * properties.
	 *
	 * @param name
	 */
	public void addKeywordProperty(@Nonnull String name) {
		addKeywordProperty(name, -1);
	}

	public int getKeyWordSearchPopupWidth() {
		return m_keyWordSearchPopupWidth;
	}

	public void setKeyWordSearchPopupWidth(int keyWordSearchPopupWidth) {
		m_keyWordSearchPopupWidth = keyWordSearchPopupWidth;
	}

	public void setAbsolutePopupLayoutQuirkMode(boolean value) {
		m_absolutePopupLayoutQuirkMode = value;
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
	 * @See  {@link LookupInput#m_lookupFormInitialization}.
	 * @return
	 */
	public ILookupFormModifier<QT> getLookupFormInitialization() {
		return m_lookupFormInitialization;
	}

	/**
	 * @See  {@link LookupInput#m_lookupFormInitialization}.
	 * @return
	 */
	public void setLookupFormInitialization(ILookupFormModifier<QT> lookupFormInitialization) {
		m_lookupFormInitialization = lookupFormInitialization;
	}

	@Nonnull
	public Class<OT> getOutputClass() {
		return m_outputClass;
	}

	@Nonnull @Override public Class<OT> getActualType() {
		return m_outputClass;
	}

	@Nonnull
	public Class<QT> getQueryClass() {
		return m_queryClass;
	}

	@Nonnull
	public ClassMetaModel getOutputMetaModel() {
		return m_outputMetaModel;
	}

	@Nonnull
	public ClassMetaModel getQueryMetaModel() {
		return m_queryMetaModel;
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
	 * The content renderer to use to render the current value.
	 * @return
	 */
	@Nullable
	public IRenderInto<OT> getValueRenderer() {
		return m_valueRenderer;
	}

	public void setValueRenderer(@Nullable IRenderInto<OT> contentRenderer) {
		m_valueRenderer = contentRenderer;
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

	protected boolean isPopupShown() {
		return m_floater != null;
	}

	@Override
	public boolean isFocusable() {
		return false;
	}

	@Override
	public void setFocus() {
		if(null != m_keySearch)
			m_keySearch.setFocus();
		else if(!isBuilt())
			m_doFocus = true;
	}
}

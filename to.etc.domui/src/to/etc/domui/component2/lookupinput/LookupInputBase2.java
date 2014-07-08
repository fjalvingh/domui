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

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.input.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.themes.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.webapp.query.*;

abstract public class LookupInputBase2<QT, OT> extends Div implements IControl<OT>, IHasModifiedIndication, IQueryManipulator<QT> {
	/** The properties bindable for this component. */
	static private final Set<String> BINDABLE_SET = createNameSet("value", "disabled");

	static public final INodeContentRenderer<Object> DEFAULT_RENDERER = new SimpleLookupInputRenderer2<Object>();

	public interface IPopupOpener {
		public <A, B, L extends LookupInputBase2<A, B>> Dialog createDialog(@Nonnull L control, @Nullable ITableModel<B> initialModel);
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
	 * obtained using MetaManager; for meta-based data models this gets passed as a constructor argument.
	 */
	@Nonnull
	final private ClassMetaModel m_queryMetaModel;

	/**
	 * The metamodel for output (display) objects.
	 */
	@Nonnull
	final private ClassMetaModel m_outputMetaModel;

	@Nonnull
	final private SmallImgButton m_selButton;

	@Nonnull
	final private SmallImgButton m_clearButton;

	@Nullable
	private Dialog m_floater;

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

	/**
	 * The content renderer to use to render the current value.
	 */
	@Nullable
	private INodeContentRenderer<OT> m_valueRenderer;

	@Nullable
	private KeyWordSearchInput<OT> m_keySearch;

	@Nullable
	private String m_keySearchHint;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	private boolean m_modifiedByUser;

	@Nullable
	private IStringQueryFactory<QT> m_stringQueryFactory;

	@Nullable
	private String m_keyWordSearchCssClass;

	private int m_keyWordSearchPopupWidth;

	/**
	 * By default set to true.
	 * Set to false in cases when keyword search functionality should be disabled regardless if metadata for this feature is defined or not.
	 */
	private boolean m_allowKeyWordSearch = true;

	/** Contains manually added quicksearch properties. Is null if none are added. */
	@Nullable
	private List<SearchPropertyMetaModel> m_keywordLookupPropertyList;

	private enum RebuildCause {
		CLEAR, SELECT
	};

	/**
	 * When we trigger forceRebuild, we can specify reason for this, and use this later to resolve focus after content is re-rendered.
	 */
	@Nullable
	private RebuildCause m_rebuildCause;

	/** The row renderer used to render rows in the quick search dropdown box showing the results of the quick search. */
	@Nullable
	private KeyWordPopupRowRenderer<OT> m_dropdownRowRenderer;

	@Nullable
	private QCriteria<QT> m_rootCriteria;

	private boolean m_doFocus;

	@Nullable
	private IPopupOpener m_popupOpener;

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
//	public LookupInputBase(@Nonnull Class<QT> queryClass, @Nonnull Class<OT> resultClass, @Nonnull String... resultColumns) {
//		this(queryClass, resultClass, (ClassMetaModel) null, (ClassMetaModel) null);
//		setResultColumns(resultColumns);
//	}

	/**
	 * Lookup a POJO Java bean persistent class.
	 * @param queryClass
	 */
	public LookupInputBase2(@Nonnull Class<QT> queryClass, @Nonnull Class<OT> resultClass) {
		this(queryClass, resultClass, (ClassMetaModel) null, (ClassMetaModel) null);
	}

	public LookupInputBase2(@Nonnull QCriteria<QT> rootCriteria, @Nonnull Class<OT> resultClass) {
		this(DomUtil.nullChecked(rootCriteria.getBaseClass()), resultClass, (ClassMetaModel) null, (ClassMetaModel) null);
	}

	public LookupInputBase2(@Nonnull Class<QT> queryClass, @Nonnull Class<OT> resultClass, @Nullable ClassMetaModel queryMetaModel, @Nullable ClassMetaModel outputMetaModel) {
		m_queryClass = queryClass;
		m_outputClass = resultClass;
		m_queryMetaModel = queryMetaModel != null ? queryMetaModel : MetaManager.findClassMeta(queryClass);
		m_outputMetaModel = outputMetaModel != null ? outputMetaModel : MetaManager.findClassMeta(resultClass);
		SmallImgButton b = m_selButton = new SmallImgButton(Theme.BTN_POPUPLOOKUP);
		b.setTestID("selButtonInputLookup");
		b.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(@Nonnull NodeBase b) throws Exception {
				openPopupWithClick();
			}
		});

		b = m_clearButton = new SmallImgButton(Theme.BTN_CLEARLOOKUP, new IClicked<SmallImgButton>() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void clicked(@Nonnull SmallImgButton b) throws Exception {
				handleSetValue(null);
			}
		});
		b.setTestID("clearButtonInputLookup");
		b.setDisplay(DisplayType.NONE);
		setCssClass("ui-lui");
	}

	@Nonnull
	private SmallImgButton getSelButton() {
		if(null != m_selButton)
			return m_selButton;
		throw new IllegalStateException("Selection button is not there.");
	}

	@Nonnull
	public SmallImgButton getClearButton() {
		if(null != m_clearButton)
			return m_clearButton;
		throw new IllegalStateException("Clear button is not there.");
	}

	@Override
	@Nonnull
	public Set<String> getBindableProperties() {
		return BINDABLE_SET;
	}

	@Override
	public void createContent() throws Exception {
		m_table = new Table();
		m_table.setCellSpacing("0");
		m_table.setCellPadding("0");
		add(m_table);
		m_keySearch = null;
		removeCssClass("ui-ro");
		if(m_value == null && isAllowKeyWordSearch() && isKeyWordSearchDefined()) {
			//Key word search rendering should be generic, no need for customization posibilities.
			if(isReadOnly() || isDisabled()) {
				renderEmptySelection();
				addCssClass("ui-ro");
			} else {
				renderKeyWordSearch(m_selButton);
			}
		} else {
			//In case of rendring selected values it is possible to use customized renderers. If no customized rendered is defined then use default one.
			INodeContentRenderer<OT> r = getValueRenderer();
			if(r == null)
				r = (INodeContentRenderer<OT>) DEFAULT_RENDERER; // Prevent idiotic generics error
			r.renderNodeContent(this, this, m_value, isReadOnly() || isDisabled() ? null : m_selButton);
		}

		SmallImgButton clearButton = getClearButton();
		if(!isReadOnly() && !isDisabled()) {
			if(!getSelButton().isAttached()) { // If the above did not add the button do it now.
				/*
				 * jal 20090925 Bugfix: when a renderer does not add the button (as it should) we need to add it manually, but
				 * it must be in a valid table structure! So we need to ensure that a tbody, tr and td are present to add the
				 * node to. This fixes the problem where IE did not show the buttons because the rendered xhtml was invalid.
				 */
				TBody tb = m_table.getBody();
				TR tr;
				if(tb.getChildCount() == 0)
					tr = tb.addRow();
				else
					tr = (TR) tb.getChild(0);

				TD cell = tr.addCell();
				cell.add(getSelButton());
			}
			getSelButton().appendAfterMe(clearButton);
			//This code is needed for proper control alignment.

// jal 20121025 temp disabled
//			//FIXME: vmijic, not suitable for larger button images, see is this can be resolved by introducing span container for buttons.
//			if(clearButton.getDisplay() == DisplayType.NONE) {
//				clearButton.getParent().setMinWidth("24px");
//			} else {
//				clearButton.getParent().setMinWidth("58px");
//			}
		}
		if(m_rebuildCause == RebuildCause.CLEAR) {
			//User clicked clear button, so we can try to set focus to input search if possible.
			if(m_keySearch != null) {
				m_keySearch.setFocus();
			}
		} else if(m_rebuildCause == RebuildCause.SELECT) {
			//User did reselected value, so we can try to set focus to clear button if possible.
			if(clearButton != null && clearButton.getDisplay() != DisplayType.NONE) {
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
		TD td = m_table.getBody().addRowAndCell();
		td.setValign(TableVAlign.TOP);
		td.setCssClass("ui-lui-v");
		String txt = Msgs.BUNDLE.getString(Msgs.UI_LOOKUP_EMPTY);
		td.add(txt);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Quick Search code (KeySearch)						*/
	/*--------------------------------------------------------------*/
	/**
	 * @return true either when query control is manually implemented by keyWordSearchHandler, or if keyword search meta data is defined.
	 */
	private boolean isKeyWordSearchDefined() {
		if(getStringQueryFactory() != null) {
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
	private void renderKeyWordSearch(@Nullable Object parameters) {
		TD td = m_table.getBody().addRowAndCell();
		td.setValign(TableVAlign.TOP);
		td.setCssClass("ui-lui-v");
//		td.setWidth("100%"); jal 20121025 Width should not be set but style should be used?
		addKeySearchField(td);

		//-- parameters is either the button, or null if this is a readonly version.
		if(parameters != null) {
			appendParameters(td, parameters);
		}
	}

	private void addKeySearchField(NodeContainer parent) {
		KeyWordSearchInput<OT> ks = m_keySearch = new KeyWordSearchInput<OT>(m_keyWordSearchCssClass);
		ks.setPopupWidth(getKeyWordSearchPopupWidth());
		KeyWordPopupRowRenderer<OT> rr = getDropdownRowRenderer();
		rr.setRowClicked(new ICellClicked<OT>() {
			@Override
			public void cellClicked(@Nonnull NodeBase tr, @Nonnull OT val) throws Exception {
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
					openPopup(null);
					return;
				}
				if(keySearchModel.getRows() == 1) {
					//in case of single match select value.
					handleSetValue(keySearchModel.getItems(0, 1).get(0));
				} else {
					//in case of more results show narrow result in search popup.
					component.setResultsCount(keySearchModel.getRows());
					openPopup(keySearchModel);
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

	@Override
	@Nullable
	public QCriteria<QT> adjustQuery(@Nonnull QCriteria<QT> enteredCriteria) {
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
	private void openPopupWithClick() throws Exception {
		ITableModel<OT> initialModel = null;
		if(m_keySearch != null) {
			initialModel = searchKeyWord(m_keySearch.getKeySearchValue());
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
		if(m_floater != null)
			throw new IllegalStateException("Trying to re-open the popup, but it's already visible");
		IPopupOpener po = m_popupOpener;
		if(null == po) {
			po = createPopupOpener();
		}
		m_floater = po.createDialog(this, initialModel);
	}

	@Nonnull
	private IPopupOpener createPopupOpener() {
		return new DefaultPopupOpener();
	}

	/**
	 * Construct a default title for this LookupInput
	 *
	 * @return
	 */
	@Nonnull
	public String getDefaultTitle() {
		String entity = getOutputMetaModel().getUserEntityName();
		if(entity != null)
			return Msgs.BUNDLE.formatMessage(Msgs.UI_LUI_TTL_WEN, entity);

		return Msgs.BUNDLE.getString(Msgs.UI_LUI_TTL);
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
		fireModified("disabled", Boolean.valueOf(!disabled), Boolean.valueOf(disabled));
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IControl implementation.							*/
	/*--------------------------------------------------------------*/
	@Nullable
	private IValueChanged< ? > m_onValueChanged;

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
			getClearButton().setDisplay(DisplayType.INLINE);
			clearMessage();
			setCssClass("ui-lui-selected");
		} else {
			getClearButton().setDisplay(DisplayType.NONE);
			setCssClass("ui-lui");
		}
		updateRoStyle();
		forceRebuild();
		fireModified("value", old, v);
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
			//-- Handle onValueChanged
			IValueChanged< ? > onValueChanged = getOnValueChanged();
			if(onValueChanged != null) {
				((IValueChanged<NodeBase>) onValueChanged).onValueChanged(this);
			}
		}
		m_rebuildCause = value == null ? RebuildCause.CLEAR : RebuildCause.SELECT;
	}

	/**
	 * EXPERIMENTAL This callback must be called by the popup once a selection is made.
	 * @param value
	 * @throws Exception
	 */
	public final void setDialogSelection(@Nullable OT value) throws Exception {
		if(null == value)							// Null means: no selection made, so retain the current one
			return;
		handleSetValue(value);
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
	 * When set the specified manipulator will be called before a query is sent to the database. The query
	 * can be altered to add extra restrictions for instance.
	 *
	 * @param queryManipulator
	 */
	public void setQueryManipulator(@Nullable IQueryManipulator<QT> queryManipulator) {
		m_queryManipulator = queryManipulator;
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


	/*--------------------------------------------------------------*/
	/*	CODING:	IBindable interface.								*/
	/*--------------------------------------------------------------*/

	@Nullable
	private List<SimpleBinder> m_bindingList;

	@Override
	public @Nonnull
	IBinder bind() {
		return bind("value");
	}

	@Override
	@Nonnull
	public IBinder bind(@Nonnull String componentProperty) {
		List<SimpleBinder> list = m_bindingList;
		if(list == null)
			list = m_bindingList = new ArrayList<SimpleBinder>(1);
		SimpleBinder binder = new SimpleBinder(this, componentProperty);
		list.add(binder);
		return binder;
	}

	@Override
	@Nullable
	public List<SimpleBinder> getBindingList() {
		return m_bindingList;
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

	@Nullable
	public String getKeyWordSearchCssClass() {
		return m_keyWordSearchCssClass;
	}

	/**
	 * Set custom css that would be applied only in case that component is rendering keyWordSearch.
	 * Used for example in row inline rendering, where width and min-width should be additionaly customized.
	 * @param cssClass
	 */
	public void setKeyWordSearchCssClass(@Nullable String cssClass) {
		m_keyWordSearchCssClass = cssClass;
	}

	/**
	 * Getter for property {@link LookupInput2#m_allowKeyWordSearch}.
	 * @return
	 */
	public boolean isAllowKeyWordSearch() {
		return m_allowKeyWordSearch;
	}

	/**
	 * Setter for property {@link LookupInput2#m_allowKeyWordSearch}.
	 * @return
	 */
	public void setAllowKeyWordSearch(boolean allowKeyWordSearch) {
		m_allowKeyWordSearch = allowKeyWordSearch;
	}

	/**
	 * Getter for keyword search hint. See {@link LookupInput2#setKeySearchHint}.
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
	 * Define a property to use for quick search. When used this overrides any metadata-defined
	 * properties.
	 *
	 * @param name
	 */
	public void addKeywordProperty(@Nonnull String name) {
		addKeywordProperty(name, -1);
	}

	/**
	 * Should not exist! Remove asap.
	 * @return
	 */
	@Deprecated
	@Nonnull
	public Table getTable() {
		if(m_table == null) {
			throw new IllegalStateException("m_table is not created yet!");
		}
		return m_table;
	}

	/**
	 * Should not exist! Remove asap.
	 * @return
	 */
	@Deprecated
	@Nonnull
	public TBody getBody() {
		if(m_table == null) {
			throw new IllegalStateException("m_table is not created yet!");
		}
		return m_table.getBody();
	}

	public int getKeyWordSearchPopupWidth() {
		return m_keyWordSearchPopupWidth;
	}

	public void setKeyWordSearchPopupWidth(int keyWordSearchPopupWidth) {
		m_keyWordSearchPopupWidth = keyWordSearchPopupWidth;
	}

	@Nonnull
	public Class<OT> getOutputClass() {
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
	public INodeContentRenderer<OT> getValueRenderer() {
		return m_valueRenderer;
	}

	public void setValueRenderer(@Nullable INodeContentRenderer<OT> contentRenderer) {
		m_valueRenderer = contentRenderer;
	}

	/**
	 * Define the full column spec in the format described for {@link BasicRowRenderer} for the dropdown box
	 * showing quick search results.
	 * @param columns
	 */
	public void addDropdownColumns(@Nonnull Object... columns) {
		getDropdownRowRenderer().addColumns(columns);
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

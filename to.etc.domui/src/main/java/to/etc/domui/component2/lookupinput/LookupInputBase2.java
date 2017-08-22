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

import to.etc.domui.component.binding.*;
import to.etc.domui.component.buttons.*;
import to.etc.domui.component.event.*;
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
import to.etc.util.*;
import to.etc.webapp.*;
import to.etc.webapp.query.*;

import javax.annotation.*;
import java.util.*;

abstract public class LookupInputBase2<QT, OT> extends Div implements IControl<OT>, ITypedControl<OT>, IHasModifiedIndication, IQueryManipulator<QT> {
	/** If set, the complete title for the popup window shown when the 'find' button is pressed. */
	@Nullable
	private String m_defaultTitle;

	/**
	 * EXPERIMENTAL Factory for the lookup dialog, to be shown when the lookup button
	 * is pressed.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jul 8, 2014
	 */
	public interface IPopupOpener {
		@Nonnull
		public <A, B, L extends LookupInputBase2<A, B>> Dialog createDialog(@Nonnull L control, @Nullable ITableModel<B> initialModel, @Nonnull IExecute callOnWindowClose);
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

	private ITableModelFactory<QT, OT> m_modelFactory;

	@Nonnull
	final private HoverButton m_selButton;

	@Nonnull
	final private HoverButton m_clearButton;

	@Nullable
	private Dialog m_floater;

	@Nullable
	private OT m_value;

	private boolean m_mandatory;

	private boolean m_readOnly;

	private boolean m_disabled;

	@Nullable
	private String m_disabledBecause;

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
	private SearchInput2 m_keySearch;

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

	@Nullable
	private INotify<Dialog> m_onPopupOpen;

	/**
	 * When we trigger forceRebuild, we can specify reason for this, and use this later to resolve focus after content is re-rendered.
	 */
	@Nullable
	private RebuildCause m_rebuildCause;

	@Nullable
	private QCriteria<QT> m_rootCriteria;

	private boolean m_doFocus;

	@Nullable
	private IPopupOpener m_popupOpener;

	@Nullable
	private NodeContainer m_valueNode;

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
	public LookupInputBase2(@Nonnull ITableModelFactory<QT, OT> modelFactory, @Nonnull Class<QT> queryClass, @Nonnull Class<OT> resultClass) {
		this(modelFactory, queryClass, resultClass, (ClassMetaModel) null, (ClassMetaModel) null);
	}

	public LookupInputBase2(@Nonnull ITableModelFactory<QT, OT> modelFactory, @Nonnull QCriteria<QT> rootCriteria, @Nonnull Class<OT> resultClass) {
		this(modelFactory, DomUtil.nullChecked(rootCriteria.getBaseClass()), resultClass, (ClassMetaModel) null, (ClassMetaModel) null);
		m_rootCriteria = rootCriteria;
	}

	public LookupInputBase2(@Nonnull ITableModelFactory<QT, OT> modelFactory, @Nonnull Class<QT> queryClass, @Nonnull Class<OT> resultClass, @Nullable ClassMetaModel queryMetaModel,
		@Nullable ClassMetaModel outputMetaModel) {
		m_queryClass = queryClass;
		m_outputClass = resultClass;
		m_queryMetaModel = queryMetaModel != null ? queryMetaModel : MetaManager.findClassMeta(queryClass);
		m_outputMetaModel = outputMetaModel != null ? outputMetaModel : MetaManager.findClassMeta(resultClass);
		m_modelFactory = modelFactory;
		HoverButton b = m_selButton = new HoverButton(Theme.BTN_HOVERPOPUPLOOKUP);
		b.setTestID("selButtonInputLookup");
		b.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(@Nonnull NodeBase b) throws Exception {
				openPopupWithClick();
			}
		});

		b = m_clearButton = new HoverButton(Theme.BTN_HOVERCLEARLOOKUP, new IClicked<HoverButton>() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void clicked(@Nonnull HoverButton b) throws Exception {
				handleSetValue(null);
			}
		});
		b.setTestID("clearButtonInputLookup");
		b.setDisplay(DisplayType.NONE);
		setCssClass("ui-lui2");
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

	@Nonnull
	private NodeContainer getValueNode() {
		NodeContainer node = m_valueNode;
		if(node == null) {
			Span span = new Span();
			m_valueNode = node = span;
			span.setCssClass("ui-lui2-vspan");
		}
		return node;
	}

	@Override
	public void createContent() throws Exception {
		m_keySearch = null;
		removeCssClass("ui-ro");
		if(m_value == null && isAllowKeyWordSearch() && isKeyWordSearchDefined()) {
			//Key word search rendering should be generic, no need for customization possibilities.
			if(isReadOnly() || isDisabled()) {
				add(0, getValueNode());
				renderEmptySelection();
				addCssClass("ui-ro");
			} else {
				renderKeyWordSearch();
			}
		} else {
			//In case of rendering selected values it is possible to use customized renderers. If no customized rendered is defined then use default one.
			INodeContentRenderer<OT> r = getValueRenderer();
			if(r == null)
				r = new SimpleLookupInputRenderer2<>(getOutputMetaModel());
			NodeContainer valueNode = getValueNode();
			valueNode.removeAllChildren();
			r.renderNodeContent(this, valueNode, m_value, null);
			add(0, valueNode);
		}

		HoverButton clearButton = getClearButton();
		if(!isReadOnly() && !isDisabled()) {
			//-- Append the select/clear buttons
			add(getSelButton());
			add(clearButton);
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

	/**
	 * Depending on what is present return the ID of a component that can
	 * receive focus.
	 * @return
	 */
	@Nullable
	@Override
	protected String getFocusID() {
		SearchInput2 keySearch = m_keySearch;
		if(null != keySearch && keySearch.isAttached())
			return keySearch.getActualID();
		HoverButton selButton = m_selButton;
		if(null != selButton && selButton.isAttached())
			return selButton.getActualID();
		HoverButton clearButton = m_clearButton;
		if(null != clearButton && clearButton.isAttached())
			return clearButton.getActualID();
		return null;
	}

	/**
	 * Render the presentation for empty/unselected input.
	 */
	private void renderEmptySelection() {
		String txt = Msgs.BUNDLE.getString(Msgs.UI_LOOKUP_EMPTY);
		getValueNode().setText(txt);
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
	 * Render the "current value" display as an input box.
	 */
	private void renderKeyWordSearch() {
		getValueNode().remove();
		SearchInput2 ks = m_keySearch = new SearchInput2(m_keyWordSearchCssClass);
		add(0, ks);

		ks.setPopupWidth(getKeyWordSearchPopupWidth());

		ks.setOnLookupTyping(new IValueChanged<SearchInput2>() {
			@Override
			public void onValueChanged(@Nonnull SearchInput2 component) throws Exception {
				ITableModel<OT> keySearchModel = searchKeyWord(component.getValue());
				showResults(keySearchModel);
			}
		});

		ks.setReturnPressed(new IReturnPressed<SearchInput2>() {
			@Override
			public void returnPressed(SearchInput2 node) throws Exception {
				handleSelection(node);
			}
		});

		if(m_keyWordSearchCssClass != null) {
			addCssClass(m_keyWordSearchCssClass);
		}
		ks.setHint(Msgs.BUNDLE.formatMessage(Msgs.UI_KEYWORD_SEARCH_HINT, (m_keySearchHint != null) ? m_keySearchHint : getDefaultKeySearchHint()));
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

	@Nonnull
	private SearchInput2 getKeySearch() {
		if(null != m_keySearch)
			return m_keySearch;
		throw new IllegalStateException("keySearch is null");
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

	@Nonnull
	private ITableModel<OT> createTableModel(@Nonnull QCriteria<QT> qc) throws Exception {
		ITableModelFactory<QT, OT> factory = m_modelFactory;
		if(null == factory)
			throw new IllegalStateException("Table model factory unset");
		return factory.createTableModel(getQueryHandler(), qc);
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
			initialModel = searchKeyWord(m_keySearch.getValue());
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

		INotify<Dialog> onPopupOpen = m_onPopupOpen;
		if(null != onPopupOpen)
			onPopupOpen.onNotify(floater);
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
			openMessagePanel(Msgs.UI_KEYWORD_SEARCH_NO_MATCH);
		} else if(size > 10) {
			String count = Integer.toString(size);
			if(model instanceof ITruncateableDataModel) {
				if(((ITruncateableDataModel) model).isTruncated())
					count = "> " + count;
			}
			openMessagePanel(Msgs.UI_KEYWORD_SEARCH_COUNT, count);
		} else {
			openResultsPopup(model);
		}
	}

	private void openResultsPopup(@Nonnull ITableModel<OT> model) throws Exception {
		List<OT> list = model.getItems(0, model.getRows());
		INodeContentRenderer<OT> renderer = new DefaultPopupRowRenderer<OT>(m_outputMetaModel);

		SelectOnePanel<OT> pnl = m_selectPanel = new SelectOnePanel<OT>(list, renderer);
		DomUtil.nullChecked(m_keySearch).add(pnl);

		pnl.setOnValueChanged(new IValueChanged<SelectOnePanel<OT>>() {
			@Override
			public void onValueChanged(SelectOnePanel<OT> component) throws Exception {
				clearResult();
				OT selection = component.getValue();
				if(null != selection)
					handleSetValue(selection);
			}
		});

		pnl.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(@Nonnull NodeBase clickednode) throws Exception {
				//we just need to deliver selected value here, that is why we have empty click handler
			}
		});
	}

	private void openMessagePanel(@Nonnull String code, String... parameters) {
		String message = Msgs.BUNDLE.formatMessage(code, parameters);
		Div pnl = m_pnlMessage;
		if(pnl == null) {
			pnl = m_pnlMessage = new Div();
			add(pnl);
		}
		pnl.setCssClass("ui-srip-message");
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
			addCssClass("ui-lui2-selected-ro");
		else
			removeCssClass("ui-lui2-selected-ro");
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

	@Nullable
	public String getDisabledBecause() {
		return m_disabledBecause;
	}

	public void setDisabledBecause(@Nullable String msg) {
		if(Objects.equals(msg, m_disabledBecause)) {
			return;
		}
		m_disabledBecause = msg;
		setOverrideTitle(msg);
		setDisabled(msg != null);
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
		SearchInput2 ks = m_keySearch;
		if(DomUtil.isEqual(m_value, v) && (ks == null || ks.getValue() == null))
			return;
		OT old = m_value;
		m_value = v;
		getClearButton().setDisplay(DisplayType.INLINE);
		setCssClass("ui-lui2-selected");
		if(v != null) {
			getClearButton().setDisabled(false);
			clearMessage();
		} else {
			getClearButton().setDisabled(true);
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
	 * The query handler to use. The default query handler {@link PageQueryHandler} will use the
	 * normal conversation-associated DataContext to issue the query.
	 * @return
	 */
	@Nonnull
	public IQueryHandler<QT> getQueryHandler() {
		IQueryHandler<QT> handler = m_queryHandler;
		if(null == handler)
			handler = new PageQueryHandler<QT>(this);
		return handler;
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
	 */
	@Nullable
	public String getKeySearchHint() {
		return m_keySearchHint;
	}

	/**
	 * Set hint to keyword search input. Usually says how search condition is resolved.
	 * @param keySearchHint
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
	 * content renderer (a {@link to.etc.domui.component2.lookupinput.SimpleLookupInputRenderer2}) to render the fields.
	 *
	 * @param columns
	 */
	public void setValueColumns(String... columns) {
		setValueRenderer(new SimpleLookupInputRenderer2<OT>(getOutputClass(), columns));
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
//	public void addDropdownColumns(@Nonnull Object... columns) {
//		getDropdownRowRenderer().addColumns(columns);
//	}

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

}

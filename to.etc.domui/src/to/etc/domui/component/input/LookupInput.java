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

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.lookup.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.webapp.*;
import to.etc.webapp.query.*;

/**
 * Lookup input field component.
 *
 * Additional description on use of stylesheets:
 * LookupInput can have this states:
 * <UL>
 * <LI>selected editable -> ui-lui-selected</LI>
 * <LI>selected readonly||disabled -> ui-lui-selected ui-lui-selected-ro</LI>
 * <LI>unselected editable -> ui-lui-v</LI>
 * <LI>unselected readonly -> ui-lui-v ui-ro</LI>
 * </UL>
 *
 * Thing is that when LookupInput is selected (has value), it is
 * rendered as table inside div.
 * When we have readonly LookupInput, then we want only to override
 * background color (not a border), so for that we have additional class =ui-lui-selected-ro.
 * Unselected readonly LookupInput has only one TD in table, so there we
 * can use simple ui-ro, since there we can set border (it would look
 * ackward if we use style named ui-lui-selected-ro for unselected rendering).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class LookupInput<T> extends Div implements IInputNode<T>, IHasModifiedIndication {
	/**
	 * The result class. For Java classes this usually also defines the metamodel to use; for generic meta this should
	 * be the value record class type.
	 */
	final private Class<T> m_lookupClass;

	/**
	 * The metamodel to use to handle the data in this class. For Javabean data classes this is automatically
	 * obtained using MetaManager; for meta-based data models this gets passed as a constructor argument.
	 */
	final private ClassMetaModel m_metaModel;

	private LookupForm<T> m_externalLookupForm;

	private SmallImgButton m_selButton;

	private SmallImgButton m_clearButton;

	FloatingWindow m_floater;

	DataTable<T> m_result;

	T m_value;

	Table m_table;

	private boolean m_mandatory;

	private boolean m_readOnly;

	private boolean m_disabled;

	private INodeContentRenderer<T> m_contentRenderer;

	private IQueryManipulator<T> m_queryManipulator;

	private IQueryHandler<T> m_queryHandler;

	private String m_lookupTitle;

	//	private String m_hint;

	private String[] m_resultColumns;

	private IErrorMessageListener m_customErrorMessageListener;

	private IActionAllowed m_isLookupAllowed;

	private KeyWordSearchInput<T> m_keySearch;

	private String m_keySearchHint;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	boolean m_modifiedByUser;

	private IKeyWordSearchQueryFactory<T> m_keyWordSearchHandler;

	private boolean m_allowEmptyQuery;

	private String m_keyWordSearchCssClass;

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

	public LookupInput(Class<T> lookupClass, String[] resultColumns) {
		this(lookupClass, (ClassMetaModel) null);
		m_resultColumns = resultColumns;
	}

	/**
	 * Lookup a POJO Java bean persistent class.
	 * @param lookupClass
	 */
	public LookupInput(Class<T> lookupClass) {
		this(lookupClass, (ClassMetaModel) null);
	}

	public LookupInput(Class<T> lookupClass, ClassMetaModel metaModel) {
		m_lookupClass = lookupClass;
		m_metaModel = metaModel != null ? metaModel : MetaManager.findClassMeta(lookupClass);
		m_selButton = new SmallImgButton("THEME/btn-popuplookup.png");
		m_selButton.setTestID("selButtonInputLookup");
		m_selButton.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(NodeBase b) throws Exception {
				toggleFloaterByClick();
			}
		});

		m_clearButton = new SmallImgButton("THEME/btnClearLookup.png", new IClicked<SmallImgButton>() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void clicked(SmallImgButton b) throws Exception {
				handleSetValue(null);
				if(m_keySearch != null) {
					m_keySearch.setFocus();
				}
			}
		});
		m_clearButton.setTestID("clearButtonInputLookup");
		m_clearButton.setDisplay(DisplayType.NONE);

		setCssClass("ui-lui");
	}


	public Class<T> getLookupClass() {
		return m_lookupClass;
	}

	public ClassMetaModel getMetaModel() {
		return m_metaModel;
	}

	public INodeContentRenderer<T> getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(INodeContentRenderer<T> contentRenderer) {
		m_contentRenderer = contentRenderer;
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
				renderKeyWordSearch(null, m_selButton);
			}
		} else {
			//In case of rendring selected values it is possible to use customized renderers. If no customized rendered is defined then use default one.
			INodeContentRenderer<T> r = getContentRenderer();
			if(r == null)
				r = (INodeContentRenderer<T>) DEFAULT_RENDERER; // Prevent idiotic generics error
			r.renderNodeContent(this, this, m_value, isReadOnly() || isDisabled() ? null : m_selButton);
		}

		if(!isReadOnly() && !isDisabled()) {
			if(m_selButton.getPage() == null) { // If the above did not add the button do it now.
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
				cell.add(m_selButton);
			}
			m_selButton.appendAfterMe(m_clearButton);
			//This code is needed for proper control alignment.
			//FIXME: vmijic, not suitable for larger button images, see is this can be resolved by introducing span container for buttons.
			if(m_clearButton.getDisplay() == DisplayType.NONE) {
				m_clearButton.getParent().setMinWidth("24px");
			} else {
				m_clearButton.getParent().setMinWidth("58px");
			}
		}
	}

	/**
	 * @return true either when query control is manually implemented by keyWordSearchHandler, or if keyword search meta data is defined.
	 */
	private boolean isKeyWordSearchDefined() {
		if(getKeyWordSearchHandler() != null) {
			return true;
		}

		if(m_keywordLookupPropertyList != null)
			return true;
		List<SearchPropertyMetaModel> spml = getMetaModel().getKeyWordSearchProperties();
		return spml.size() > 0;
	}

	private void appendParameters(TD cell, Object parameters) {
		TD tdParameters = new TD();
		cell.appendAfterMe(tdParameters);
		tdParameters.setValign(TableVAlign.TOP);
		tdParameters.setMinWidth("24px");
		tdParameters.setTextAlign(TextAlign.RIGHT);
		tdParameters.add((NodeBase) parameters); // Add the button,
	}

	private void renderKeyWordSearch(T value, Object parameters) {
		TD td = m_table.getBody().addRowAndCell();
		td.setValign(TableVAlign.TOP);
		td.setCssClass("ui-lui-v");
		td.setWidth("100%");
		addKeySearchField(td, value);
		//-- parameters is either the button, or null if this is a readonly version.
		if(parameters != null) {
			appendParameters(td, parameters);
		}
	}

	private void renderEmptySelection() {
		TD td = m_table.getBody().addRowAndCell();
		td.setValign(TableVAlign.TOP);
		td.setCssClass("ui-lui-v");
		String txt = Msgs.BUNDLE.getString(Msgs.UI_LOOKUP_EMPTY);
		td.add(txt);
	}

	private void addKeySearchField(NodeContainer parent, T value) {
		m_keySearch = new KeyWordSearchInput<T>(m_keyWordSearchCssClass);
		m_keySearch.setWidth("100%");
		KeyWordPopupRowRenderer<T> rr = null;
		if(m_resultColumns != null) {
			rr = new KeyWordPopupRowRenderer<T>(getLookupClass(), getMetaModel(), m_resultColumns);
		} else {
			rr = new KeyWordPopupRowRenderer<T>(getLookupClass(), getMetaModel());
		}

		rr.setRowClicked(new ICellClicked<T>() {
			@Override
			public void cellClicked(NodeBase tr, T val) throws Exception {
				handleSetValue(val);
			}
		});
		m_keySearch.setResultsHintPopupRowRenderer(rr);


		m_keySearch.setOnLookupTyping(new IValueChanged<KeyWordSearchInput<T>>() {

			@Override
			public void onValueChanged(KeyWordSearchInput<T> component) throws Exception {
				ITableModel<T> keySearchModel = searchKeyWord(component.getKeySearchValue());
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

		m_keySearch.setOnShowResults(new IValueChanged<KeyWordSearchInput<T>>() {

			@Override
			public void onValueChanged(KeyWordSearchInput<T> component) throws Exception {
				ITableModel<T> keySearchModel = searchKeyWord(component.getKeySearchValue());
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
		parent.add(m_keySearch);
		if(m_keyWordSearchCssClass != null) {
			addCssClass(m_keyWordSearchCssClass);
		}
		m_keySearch.setHint(Msgs.BUNDLE.formatMessage(Msgs.UI_KEYWORD_SEARCH_HINT, (m_keySearchHint != null) ? m_keySearchHint : getDefaultKeySearchHint()));
	}

	private String getDefaultKeySearchHint() {
		List<SearchPropertyMetaModel> spml = getMetaModel().getKeyWordSearchProperties();
		if(spml.size() <= 0)
			return null;

		StringBuilder sb = new StringBuilder(128);
		for(int i = 0; i < spml.size(); i++) {
			if(sb.length() > 0)
				sb.append(", ");
			SearchPropertyMetaModel spm = spml.get(i);
			if(spm.getLookupLabel() != null) {
				sb.append(spm.getLookupLabel());
			} else {
				sb.append(getMetaModel().findProperty(spm.getPropertyName()).getDefaultLabel());
			}
		}
		return sb.toString();
	}

	/**
	 * Returns data that matches keyword search string.
	 * @param searchString
	 * @return mathcing data or null in case that search is canceled because of insufficient number of characters typed into keyword search field.
	 * @throws Exception
	 */
	ITableModel<T> searchKeyWord(String searchString) throws Exception {
		if(searchString == null || searchString.trim().length() == 0) {
			return null;
		}
		QCriteria<T> searchQuery;

		if(getKeyWordSearchHandler() != null) {
			searchQuery = getKeyWordSearchHandler().createQuery(searchString);
			if(searchQuery == null) {
				//in case of cancelled search return null
				return null;
			}
		} else {
			//-- Has default meta?
			List<SearchPropertyMetaModel> spml = m_keywordLookupPropertyList == null ? getMetaModel().getKeyWordSearchProperties() : m_keywordLookupPropertyList;
			searchQuery = (QCriteria<T>) getMetaModel().createCriteria();

			QRestrictor<T> r = searchQuery.or();
			int ncond = 0;
			if(spml.size() > 0) {
				for(SearchPropertyMetaModel spm : spml) {
					if(spm.getMinLength() < searchString.length()) {

						//-- Abort on invalid metadata; never continue with invalid data.
						if(spm.getPropertyName() == null)
							throw new ProgrammerErrorException("The quick lookup properties for " + getMetaModel() + " are invalid: the property name is null");

						List<PropertyMetaModel< ? >> pl = MetaManager.parsePropertyPath(getMetaModel(), spm.getPropertyName()); // This will return an empty list on empty string input
						if(pl.size() == 0)
							throw new ProgrammerErrorException("Unknown/unresolvable lookup property " + spm.getPropertyName() + " on " + getMetaModel());

						//It is required that lookup by id is also available, for now only Long type and BigDecimal interpretated as Long (fix for 1228) are supported
						//FIXME: see if it is possible to generalize things for all integer based types... (DomUtil.isIntegerType(pmm.getActualType()))
						if(pl.get(0).getActualType() == Long.class || pl.get(0).getActualType() == BigDecimal.class) {
							if(searchString.contains("%") && !pl.get(0).isTransient()) {
								r.add(new QPropertyComparison(QOperation.LIKE, spm.getPropertyName(), new QLiteral(searchString)));
							} else {
								try {
									Long val = Long.valueOf(searchString);
									if(val != null) {
										r.eq(spm.getPropertyName(), val.longValue());
										ncond++;
									}
								} catch(NumberFormatException ex) {
									//just ignore this since it means that it is not correct Long condition.
								}
							}
						} else if(pl.get(0).getActualType().isAssignableFrom(String.class)) {
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
				return null;//no search meta data is matching minimal lenght condition, search is cancelled
			}
		}

		if(getQueryManipulator() != null) {
			searchQuery = getQueryManipulator().adjustQuery(searchQuery);
			if(searchQuery == null) {
				//in case of cancelled search by query manipulator return
				return null;
			}
		}

		if(m_queryHandler == null) {
			QDataContextFactory src = QContextManager.getDataContextFactory(getPage().getConversation());
			return new SimpleSearchModel<T>(src, searchQuery);
		} else {
			return new SimpleSearchModel<T>(m_queryHandler, searchQuery);
		}
	}

	void toggleFloaterByClick() throws Exception {
		if(m_keySearch != null) {
			toggleFloater(searchKeyWord(m_keySearch.getKeySearchValue()));
		} else {
			toggleFloater(null);
		}
	}

	void toggleFloater(ITableModel<T> keySearchModel) throws Exception {
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

		m_floater = FloatingWindow.create(this, getLookupTitle() == null ? Msgs.BUNDLE.getString(Msgs.UI_LUI_TTL) : getLookupTitle());

		m_floater.setHeight("90%");
		m_floater.setIcon("THEME/btnFind.png");
		m_floater.setTestID(getTestID() + "_floaterWindowLookupInput");

		//in case when external error message listener is set
		if(m_customErrorMessageListener != null && m_customErrorMessageListener instanceof NodeBase) {
			m_floater.setErrorFence();
			m_floater.add((NodeBase) m_customErrorMessageListener);
			DomUtil.getMessageFence(m_floater).addErrorListener(m_customErrorMessageListener);
		}
		LookupForm<T> lf;
		if(getExternalLookupForm() != null) {
			lf = getExternalLookupForm();
		} else {
			lf = new LookupForm<T>(getLookupClass(), getMetaModel());
			if(m_searchPropertyList != null && m_searchPropertyList.size() != 0)
				lf.setSearchProperties(m_searchPropertyList);
		}

		lf.setRenderAsCollapsed(keySearchModel != null && keySearchModel.getRows() > 0);
		lf.forceRebuild(); // jal 20091002 Force rebuild to remove any state from earlier invocations of the same form. This prevents the form from coming up in "collapsed" state if it was left that way last time it was used (Lenzo).
		m_floater.add(lf);
		m_floater.setOnClose(new IClicked<FloatingWindow>() {
			@Override
			public void clicked(FloatingWindow b) throws Exception {
				m_floater.clearGlobalMessage(Msgs.V_MISSING_SEARCH);
				m_floater = null;
				m_result = null;
			}
		});

		lf.setClicked(new IClicked<LookupForm<T>>() {
			@Override
			public void clicked(LookupForm<T> b) throws Exception {
				search(b);
			}
		});

		lf.setOnCancel(new IClicked<LookupForm<T>>() {
			@Override
			public void clicked(LookupForm<T> b) throws Exception {
				m_floater.closePressed();
			}
		});

		if(keySearchModel != null && keySearchModel.getRows() > 0) {
			setResultModel(keySearchModel);
		}
	}

	void search(LookupForm<T> lf) throws Exception {
		QCriteria<T> c = lf.getEnteredCriteria();
		if(c == null) // Some error has occured?
			return; // Don't do anything (errors will have been registered)

		if(getQueryManipulator() != null) {
			c = getQueryManipulator().adjustQuery(c); // Adjust the query where needed,
			if(c == null) {
				//in case of cancelled search by query manipulator return null
				return;
			}
		}
		m_floater.clearGlobalMessage(Msgs.V_MISSING_SEARCH);
		if(!lf.hasUserDefinedCriteria() && !isAllowEmptyQuery()) {
			m_floater.addGlobalMessage(UIMessage.error(Msgs.BUNDLE, Msgs.V_MISSING_SEARCH)); // Missing inputs
			return;
		} else
			m_floater.clearGlobalMessage();
		setTableQuery(c);
	}

	private void setTableQuery(QCriteria<T> qc) {
		ITableModel<T> model;
		if(m_queryHandler == null) {
			QDataContextFactory src = QContextManager.getDataContextFactory(getPage().getConversation());
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
			m_floater.add(m_result);
			m_result.setPageSize(20);
			m_result.setTableWidth("100%");

			rr.setRowClicked(new ICellClicked<T>() {
				@Override
				public void cellClicked(NodeBase tr, T val) throws Exception {
					//					MsgBox.message(getPage(), "Selection made", "Geselecteerd: "+val);
					m_floater.clearGlobalMessage(Msgs.V_MISSING_SEARCH);
					LookupInput.this.toggleFloater(null);
					handleSetValue(val);
				}
			});

			//-- Add the pager,
			DataPager pg = new DataPager(m_result);
			m_floater.add(pg);
		} else {
			m_result.setModel(model); // Change the model
		}
		m_result.setTestID("resultTableLookupInput");
	}

	public void setHint(String text) {
		//		m_hint = text;
		if(m_selButton != null)
			m_selButton.setTitle(text);
	}

	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	@Override
	public void setMandatory(boolean mandatory) {
		m_mandatory = mandatory;
	}

	@Override
	public boolean isReadOnly() {
		return m_readOnly;
	}

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

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		m_disabled = disabled;
		updateRoStyle();
		forceRebuild();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IInputNode implementation.							*/
	/*--------------------------------------------------------------*/
	private IValueChanged< ? > m_onValueChanged;

	/**
	 * @see to.etc.domui.dom.html.IInputNode#getValue()
	 */
	@Override
	public T getValue() {
		if(m_value == null && isMandatory()) {
			setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.MANDATORY));
			throw new ValidationException(Msgs.MANDATORY);
		}
		return m_value;
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#getValueSafe()
	 */
	@Override
	public T getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#hasError()
	 */
	@Override
	public boolean hasError() {
		getValueSafe();
		return super.hasError();
	}

	/**
	 * Sets a new value. This re-renders the entire control's contents always.
	 *
	 * @see to.etc.domui.dom.html.IInputNode#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(T v) {
		if(DomUtil.isEqual(m_value, v) && (m_keySearch == null || m_keySearch.getKeySearchValue() == null))
			return;
		m_value = v;
		if(m_value != null) {
			m_clearButton.setDisplay(DisplayType.INLINE);
			clearMessage();
			setCssClass("ui-lui-selected");
		} else {
			m_clearButton.setDisplay(DisplayType.NONE);
			setCssClass("ui-lui");
		}
		updateRoStyle();
		forceRebuild();
	}

	/**
	 * In case that new value is different than one previously selected, set modified flag, selected value and trigger onValueChange event if defined.
	 * @param value
	 * @throws Exception
	 */
	void handleSetValue(T value) throws Exception {
		if(!MetaManager.areObjectsEqual(value, m_value, null)) {
			DomUtil.setModifiedFlag(this);
			setValue(value);
			//-- Handle onValueChanged
			if(getOnValueChanged() != null) {
				((IValueChanged<NodeBase>) getOnValueChanged()).onValueChanged(this);
			}
		}
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#getOnValueChanged()
	 */
	@Override
	public IValueChanged< ? > getOnValueChanged() {
		if(m_floater != null) {
			//Fix for FF: prevent onchange event to be propagate on control when return key is pressed and popup is opened.
			//This does not happen on IE. Be sure that it is executed after popup is already closed.
			return null;
		}
		return m_onValueChanged;
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#setOnValueChanged(to.etc.domui.dom.html.IValueChanged)
	 */
	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
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

	static public final INodeContentRenderer<Object> DEFAULT_RENDERER = new SimpleLookupInputRenderer<Object>();

	public LookupForm<T> getExternalLookupForm() {
		return m_externalLookupForm;
	}

	public void setExternalLookupForm(LookupForm<T> externalLookupForm) {
		m_externalLookupForm = externalLookupForm;
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
	/*	CODING:	IBindable interface (EXPERIMENTAL)					*/
	/*--------------------------------------------------------------*/

	/** When this is bound this contains the binder instance handling the binding. */
	private SimpleBinder m_binder;

	/**
	 * Return the binder for this control.
	 * @see to.etc.domui.component.input.IBindable#bind()
	 */
	@Override
	public IBinder bind() {
		if(m_binder == null)
			m_binder = new SimpleBinder(this);
		return m_binder;
	}

	/**
	 * Returns T if this control is bound to some data value.
	 *
	 * @see to.etc.domui.component.input.IBindable#isBound()
	 */
	@Override
	public boolean isBound() {
		return m_binder != null && m_binder.isBound();
	}

	public IActionAllowed getIsLookupAllowed() {
		return m_isLookupAllowed;
	}

	public void setIsLookupAllowed(IActionAllowed isLookupAllowed) {
		m_isLookupAllowed = isLookupAllowed;
	}

	public IKeyWordSearchQueryFactory<T> getKeyWordSearchHandler() {
		return m_keyWordSearchHandler;
	}

	public void setKeyWordSearchHandler(IKeyWordSearchQueryFactory<T> keyWordSearchManipulator) {
		m_keyWordSearchHandler = keyWordSearchManipulator;
	}

	public String getKeyWordSearchCssClass() {
		return m_keyWordSearchCssClass;
	}

	/**
	 * Set custom css that would be applied only in case that component is rendering keyWordSearch.
	 * Used for example in row inline rendering, where width and min-width should be additionaly customized.
	 * @param cssClass
	 */
	public void setKeyWordSearchCssClass(String cssClass) {
		m_keyWordSearchCssClass = cssClass;
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
	public String getKeySearchHint() {
		return m_keySearchHint;
	}

	/**
	 * Set hint to keyword search input. Usually says how search condition is resolved.
	 * @param hint
	 */
	public void setKeySearchHint(String keySearchHint) {
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
	public void addKeywordProperty(String name, int minlen) {
		if(m_keywordLookupPropertyList == null)
			m_keywordLookupPropertyList = new ArrayList<SearchPropertyMetaModel>();
		SearchPropertyMetaModelImpl si = new SearchPropertyMetaModelImpl(getMetaModel());
		if(minlen > 0)
			si.setMinLength(minlen);
		si.setPropertyName(name);
		si.setIgnoreCase(true);
		m_keywordLookupPropertyList.add(si);
	}

	public void setKeywordSearchProperties(List<SearchPropertyMetaModel> keywordLookupPropertyList) {
		m_keywordLookupPropertyList = keywordLookupPropertyList;
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
	public void addKeywordProperty(String name) {
		addKeywordProperty(name, -1);
	}

	public Table getTable() {
		if(m_table == null) {
			throw new IllegalStateException("m_table is not created yet!");
		}
		return m_table;
	}

	public TBody getBody() {
		if(m_table == null) {
			throw new IllegalStateException("m_table is not created yet!");
		}
		return m_table.getBody();
	}
}

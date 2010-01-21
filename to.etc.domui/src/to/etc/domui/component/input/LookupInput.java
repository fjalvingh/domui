package to.etc.domui.component.input;

import java.util.*;

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

public class LookupInput<T> extends Table implements IInputNode<T>, IHasModifiedIndication {
	private LookupForm<T> m_externalLookupForm;

	private SmallImgButton m_selButton;

	private SmallImgButton m_clearButton;

	private Class<T> m_lookupClass;

	FloatingWindow m_floater;

	DataTable<T> m_result;

	T m_value;

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

	private KeyWordSearchInput m_keySearch;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	boolean m_modifiedByUser;

	private IKeyWordSearchQueryManipulator<T> m_keyWordSearchHandler;

	//	QCriteria<T> m_keySearchCriteria;

	ITableModel<T> m_keySearchModel;

	private boolean m_allowEmptyQuery;

	private String m_keyWordSearchCssClass;

	/**
	 * By default set to true.
	 * Set to false in cases when keyword search functionality should be disabled regardless if metadata for this feature is defined or not.   
	 */
	private boolean m_allowKeyWordSearch = true;

	public LookupInput(Class<T> lookupClass, String[] resultColumns) {
		this(lookupClass);
		m_resultColumns = resultColumns;
	}

	public LookupInput(Class<T> lookupClass) {
		m_lookupClass = lookupClass;
		m_selButton = new SmallImgButton("THEME/btn-popuplookup.png");
		m_selButton.setTestID("selButtonInputLookup");
		m_selButton.setClicked(new IClicked<NodeBase>() {
			public void clicked(NodeBase b) throws Exception {
				toggleFloater();
			}
		});

		m_clearButton = new SmallImgButton("THEME/btnClearLookup.png", new IClicked<SmallImgButton>() {
			@SuppressWarnings("synthetic-access")
			public void clicked(SmallImgButton b) throws Exception {
				if(m_value != null) {
					DomUtil.setModifiedFlag(LookupInput.this);
				}
				setValue(null);
				if(m_keySearch != null) {
					m_keySearch.setFocus();
				}
				//-- Handle onValueChanged
				if(getOnValueChanged() != null) {
					((IValueChanged<NodeBase>) getOnValueChanged()).onValueChanged(LookupInput.this);
				}

			}
		});
		m_clearButton.setTestID("clearButtonInputLookup");
		m_clearButton.setDisplay(DisplayType.NONE);

		setCssClass("ui-lui");
		setCellPadding("0");
		setCellSpacing("0");
	}

	public INodeContentRenderer<T> getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(INodeContentRenderer<T> contentRenderer) {
		m_contentRenderer = contentRenderer;
	}

	@Override
	public void createContent() throws Exception {
		m_keySearch = null;
		//		m_keySearchCriteria = null;
		m_keySearchModel = null;
		if(m_value == null && isAllowKeyWordSearch() && isKeyWordSearchDefined()) {
			//Key word search rendering should be generic, no need for customization posibilities.
			if(!isReadOnly() && !isDisabled()) {
				renderKeyWordSearch(null, m_selButton);
			} else {
				renderEmptySelection();
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
				TBody tb = getBody();
				TR tr;
				if(tb.getChildCount() == 0)
					tr = tb.addRow();
				else
					tr = (TR) tb.getChild(0);

				TD cell = tr.addCell();
				cell.add(m_selButton);
			}
			m_selButton.appendAfterMe(m_clearButton);
			if(m_clearButton.getDisplay() == DisplayType.NONE) {
				m_clearButton.getParent().setMinWidth("24px");
			} else {
				m_clearButton.getParent().setMinWidth("56px");
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
		ClassMetaModel cmm = MetaManager.findClassMeta(m_lookupClass);
		if(cmm != null) {
			//-- Has default meta?
			List<SearchPropertyMetaModelImpl> spml = cmm.getKeyWordSearchProperties();
			if(spml.size() > 0) {
				return true;
			}
		}
		return false;
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
		TD td = getBody().addRowAndCell();
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
		TD td = getBody().addRowAndCell();
		td.setValign(TableVAlign.TOP);
		td.setCssClass("ui-lui-v");
		String txt = Msgs.BUNDLE.getString(Msgs.UI_LOOKUP_EMPTY);
		td.add(txt);
	}

	private void addKeySearchField(NodeContainer parent, T value) {
		m_keySearch = new KeyWordSearchInput(m_keyWordSearchCssClass);
		m_keySearch.setWidth("100%");
		m_keySearch.setOnTyping(new IValueChanged<KeyWordSearchInput>() {

			@Override
			public void onValueChanged(KeyWordSearchInput component) throws Exception {
				String searchstring = component.getKeySearchValue();
				boolean canceled = (searchstring == null || searchstring.trim().length() == 0);
				if(!canceled) {
					m_keySearchModel = searchKeyWord(searchstring);
					canceled = m_keySearchModel == null;
				}
				if(canceled) {
					//in case of insufficient searchString data cancel search and return. 
					m_keySearchModel = null;
					//					m_keySearchCriteria = null;
					component.setResultsCount(-1);
					component.setFocus();
					return;
				}
				if(m_keySearchModel.getRows() == 1) {
					//in case of single match insufficient searchString data cancel search and return. 
					LookupInput.this.setValue(m_keySearchModel.getItems(0, 1).get(0));
					if(LookupInput.this.getOnValueChanged() != null) {
						((IValueChanged<NodeBase>) LookupInput.this.getOnValueChanged()).onValueChanged(LookupInput.this);
					}
				} else {
					component.setResultsCount(m_keySearchModel.getRows());
				}
			}
		});

		m_keySearch.setOnShowResults(new IValueChanged<KeyWordSearchInput>() {

			@Override
			public void onValueChanged(KeyWordSearchInput component) throws Exception {
				String searchstring = component.getKeySearchValue();
				if(searchstring == null || searchstring.trim().length() == 0) {
					m_keySearchModel = null;
					component.setResultsCount(-1);
					toggleFloater();
					return;
				}
				m_keySearchModel = searchKeyWord(component.getKeySearchValue());
				if(m_keySearchModel.getRows() == 1) {
					LookupInput.this.setValue(m_keySearchModel.getItems(0, 1).get(0));
					if(LookupInput.this.getOnValueChanged() != null) {
						((IValueChanged<NodeBase>) LookupInput.this.getOnValueChanged()).onValueChanged(LookupInput.this);
					}
				} else {
					component.setResultsCount(m_keySearchModel.getRows());
					toggleFloater();
				}
			}
		});
		parent.add(m_keySearch);
		if(m_keyWordSearchCssClass != null) {
			addCssClass(m_keyWordSearchCssClass);
		}
	}

	/**
	 * Returns data that matches keyword search string. 
	 * @param searchString
	 * @return mathcing data or null in case that search is canceled because of insufficient number of characters typed into keyword search field.
	 * @throws Exception
	 */
	ITableModel<T> searchKeyWord(String searchString) throws Exception {
		QCriteria<T> searchQuery = QCriteria.create(m_lookupClass);

		if(getKeyWordSearchHandler() != null) {
			searchQuery = getKeyWordSearchHandler().adjustQuery(searchQuery, searchString);
		} else {
			ClassMetaModel cmm = MetaManager.findClassMeta(m_lookupClass);
			if(cmm != null) {
				//-- Has default meta?
				List<SearchPropertyMetaModelImpl> spml = cmm.getKeyWordSearchProperties();
				List<String> metaConditions = new ArrayList<String>();
				if(spml.size() > 0) {
					for(SearchPropertyMetaModelImpl spm : spml) {
						if(spm.getMinLength() < searchString.length()) {
						
							//-- Abort on invalid metadata; never continue with invalid data.
							if(spm.getPropertyName() == null)
								throw new ProgrammerErrorException("The quick lookup properties for " + cmm + " are invalid: the property name is null");

							List<PropertyMetaModel> pl = MetaManager.parsePropertyPath(cmm, spm.getPropertyName()); // This will return an empty list on empty string input
							if(pl.size() == 0)
								throw new ProgrammerErrorException("Unknown/unresolvable lookup property " + spm.getPropertyName() + " on " + cmm);

							if(spm.isIgnoreCase()) {
								metaConditions.add("ilike");
							} else {
								metaConditions.add("like");
							}
							metaConditions.add(spm.getPropertyName());
						}
					}
				}
				if(metaConditions.size() == 0) {
					return null;//no search meta data is matching minimal lenght condition, search is cancelled 
				}
				QRestrictor<T> r = searchQuery;
				if(metaConditions.size() > 1) {
					r = r.or(); // Multiple fields are combined using OR
				}
				for(int i = 0; i < metaConditions.size(); i = i + 2) {
					if(metaConditions.get(i).equals("ilike")) {
						r.ilike(metaConditions.get(i + 1), searchString + "%");
					} else {
						r.like(metaConditions.get(i + 1), searchString + "%");
					}
				}
			}
		}

		if(getQueryManipulator() != null) {
			searchQuery = getQueryManipulator().adjustQuery(searchQuery);
		}

		if(m_queryHandler == null) {
			QDataContextFactory src = QContextManager.getDataContextFactory(getPage().getConversation());
			return new SimpleSearchModel<T>(src, searchQuery);
		} else {
			return new SimpleSearchModel<T>(m_queryHandler, searchQuery);
		}
	}

	void toggleFloater() throws Exception {
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
		LookupForm<T> lf = getExternalLookupForm() != null ? getExternalLookupForm() : new LookupForm<T>(m_lookupClass);
		lf.setRenderAsCollapsed(m_keySearchModel != null && m_keySearchModel.getRows() > 0);
		lf.forceRebuild(); // jal 20091002 Force rebuild to remove any state from earlier invocations of the same form. This prevents the form from coming up in "collapsed" state if it was left that way last time it was used (Lenzo).
		m_floater.add(lf);
		m_floater.setOnClose(new IClicked<FloatingWindow>() {
			public void clicked(FloatingWindow b) throws Exception {
				m_floater.clearGlobalMessage(Msgs.V_MISSING_SEARCH);
				m_floater = null;
				m_result = null;
			}
		});

		lf.setClicked(new IClicked<LookupForm<T>>() {
			public void clicked(LookupForm<T> b) throws Exception {
				search(b);
			}
		});

		lf.setOnCancel(new IClicked<LookupForm<T>>() {
			public void clicked(LookupForm<T> b) throws Exception {
				m_floater.closePressed();
			}
		});

		if(m_keySearchModel != null && m_keySearchModel.getRows() > 0) {
			setResultModel(m_keySearchModel);
		}
	}

	void search(LookupForm<T> lf) throws Exception {
		QCriteria<T> c = lf.getEnteredCriteria();
		if(c == null) // Some error has occured?
			return; // Don't do anything (errors will have been registered)

		if(getQueryManipulator() != null) {
			c = getQueryManipulator().adjustQuery(c); // Adjust the query where needed,
		}
		m_floater.clearGlobalMessage(Msgs.V_MISSING_SEARCH);
		if(!c.hasRestrictions() && !isAllowEmptyQuery()) {
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
				rr = new SimpleRowRenderer<T>(m_lookupClass, m_resultColumns);
			} else {
				rr = new SimpleRowRenderer<T>(m_lookupClass);
			}

			m_result = new DataTable<T>(model, rr);
			m_floater.add(m_result);
			m_result.setPageSize(20);
			m_result.setTableWidth("100%");

			rr.setRowClicked(new ICellClicked<T>() {
				public void cellClicked(Page pg, NodeBase tr, T val) throws Exception {
					//					MsgBox.message(getPage(), "Selection made", "Geselecteerd: "+val);
					m_floater.clearGlobalMessage(Msgs.V_MISSING_SEARCH);
					LookupInput.this.toggleFloater();
					if(!MetaManager.areObjectsEqual(val, m_value, null)) {
						DomUtil.setModifiedFlag(LookupInput.this);
					}
					setValue(val);

					//-- Handle onValueChanged
					if(getOnValueChanged() != null) {
						((IValueChanged<NodeBase>) getOnValueChanged()).onValueChanged(LookupInput.this);
					}
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

	public boolean isMandatory() {
		return m_mandatory;
	}

	public void setMandatory(boolean mandatory) {
		m_mandatory = mandatory;
	}

	public boolean isReadOnly() {
		return m_readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		if(m_readOnly == readOnly)
			return;
		m_readOnly = readOnly;
		forceRebuild();
	}

	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		m_disabled = disabled;
		forceRebuild();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IInputNode implementation.							*/
	/*--------------------------------------------------------------*/
	private IValueChanged< ? > m_onValueChanged;

	/**
	 * @see to.etc.domui.dom.html.IInputNode#getValue()
	 */
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
	public void setValue(T v) {
		if(DomUtil.isEqual(m_value, v))
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
		forceRebuild();
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#getOnValueChanged()
	 */
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

	static public final INodeContentRenderer< ? > DEFAULT_RENDERER = new INodeContentRenderer<Object>() {
		public void renderNodeContent(NodeBase component, NodeContainer node, Object object, Object parameters) throws Exception {
			String txt;
			TBody tbl = ((Table) node).getBody();
			if(object != null) {
				ClassMetaModel cmm = MetaManager.findClassMeta(object.getClass());
				if(cmm != null) {
					//-- Has default meta?
					List<DisplayPropertyMetaModel> l = cmm.getTableDisplayProperties();
					if(l.size() == 0)
						l = cmm.getComboDisplayProperties();
					if(l.size() > 0) {
						//-- Expand the thingy: render a single line separated with BRs
						List<ExpandedDisplayProperty> xpl = ExpandedDisplayProperty.expandDisplayProperties(l, cmm, null);
						xpl = ExpandedDisplayProperty.flatten(xpl);

						//						node.add(tbl);
						tbl.setCssClass("ui-lui-v");
						int c = 0;
						int mw = 0;
						for(ExpandedDisplayProperty xp : xpl) {
							String val = xp.getPresentationString(object);
							if(val == null || val.length() == 0)
								continue;
							TR tr = new TR();
							tbl.add(tr);
							TD td = new TD(); // Value thingy.
							tr.add(td);
							td.setCssClass("ui-lui-vcell");
							td.setValign(TableVAlign.TOP);
							td.add(val);
							int len = val.length();
							if(len > mw)
								mw = len;

							td = new TD();
							tr.add(td);
							td.setValign(TableVAlign.TOP);
							td.setCssClass("ui-lui-btncell");
							td.setWidth("1%");
							if(c++ == 0 && parameters != null) {
								td.add((NodeBase) parameters); // Add the button,
							}
						}
						mw += 4;
						if(mw > 40)
							mw = 40;
						tbl.setWidth(mw + "em");
						return;
					}
				}
				txt = object.toString();
			} else
				txt = Msgs.BUNDLE.getString(Msgs.UI_LOOKUP_EMPTY);
			TR r = new TR();
			tbl.add(r);
			TD td = new TD();
			r.add(td);
			td.setValign(TableVAlign.TOP);
			td.setCssClass("ui-lui-v");
			td.add(txt);

			//-- parameters is either the button, or null if this is a readonly version.
			if(parameters != null) {
				td = new TD();
				r.add(td);
				td.setValign(TableVAlign.TOP);
				td.setWidth("1%");
				td.add((NodeBase) parameters); // Add the button,
			}
		}
	};

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
	public boolean isModified() {
		return m_modifiedByUser;
	}

	/**
	 * Set or clear the modified by user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#setModified(boolean)
	 */
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
	public boolean isBound() {
		return m_binder != null && m_binder.isBound();
	}

	public IActionAllowed getIsLookupAllowed() {
		return m_isLookupAllowed;
	}

	public void setIsLookupAllowed(IActionAllowed isLookupAllowed) {
		m_isLookupAllowed = isLookupAllowed;
	}

	public IKeyWordSearchQueryManipulator<T> getKeyWordSearchHandler() {
		return m_keyWordSearchHandler;
	}

	public void setKeyWordSearchHandler(IKeyWordSearchQueryManipulator<T> keyWordSearchManipulator) {
		m_keyWordSearchHandler = keyWordSearchManipulator;
	}

	public String getKeyWordSearchCssClass() {
		return m_keyWordSearchCssClass;
	}

	/**
	 * Set custom css that would be applied only in case that component is rendering keyWordSearch. 
	 * Used for example in row inline rendering, where width and min-width should be additionaly customized.
	 * @param keWordSearchCssClass
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
}

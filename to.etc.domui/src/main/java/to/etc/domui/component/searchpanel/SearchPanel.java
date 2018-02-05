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
package to.etc.domui.component.searchpanel;

import org.jetbrains.annotations.NotNull;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.IQueryFactory;
import to.etc.domui.component.layout.ButtonFactory;
import to.etc.domui.component.layout.IButtonContainer;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component.meta.impl.SearchPropertyMetaModelImpl;
import to.etc.domui.component.searchpanel.lookupcontrols.FactoryPair;
import to.etc.domui.component.searchpanel.lookupcontrols.ILookupQueryBuilder;
import to.etc.domui.component.searchpanel.lookupcontrols.LookupControlRegistry2;
import to.etc.domui.component.searchpanel.lookupcontrols.LookupQueryBuilderResult;
import to.etc.domui.component.searchpanel.lookupcontrols.ObjectLookupQueryBuilder;
import to.etc.domui.dom.Animations;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.themes.Theme;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IExecute;
import to.etc.domui.util.Msgs;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.annotations.GProperty;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * The SearchPanel is a panel which shows a form of labels and controls that
 * can be used to search for some database entity. The SearchPanel creates a
 * {@link QCriteria} object which encapsulates the query that would be needed
 * to search for the data, created from the search inputs by the user.
 *
 * @author jal at 2017-12-13.
 */
public class SearchPanel<T> extends Div implements IButtonContainer {
	/** This factory defines the default form builder to use. */
	@Nullable
	private volatile static Supplier<ISearchFormBuilder> m_defaultFormBuilderFactory;

	@Nullable
	private QCriteria<T> m_rootCriteria;

	/** The data class we're looking for */
	@Nonnull
	private Class<T> m_lookupClass;

	/** The metamodel for the class. */
	@Nonnull
	private ClassMetaModel m_metaModel;

	/** The primary list of defined lookup items. */
	@Nonnull
	private final List<Object> m_itemList = new ArrayList<>(20);

	/** The list of buttons to show on the button row. */
	private List<ButtonRowItem> m_buttonItemList = Collections.EMPTY_LIST;

	@Nullable
	private ISearchFormBuilder m_formBuilder;

	private IClicked<SearchPanel<T>> m_clicker;

	private IClicked<SearchPanel<T>> m_onNew;

	private DefaultButton m_newBtn;

	private IClicked<? extends SearchPanel<T>> m_onClear;

	private IClicked<SearchPanel<T>> m_onCancel;

	private DefaultButton m_cancelBtn;

	private DefaultButton m_collapseButton;

	private DefaultButton m_clearButton;

	@Nullable
	private DefaultButton m_filterButton;

	//@Nonnull
	//private List<SavedFilter> m_savedFilters = Collections.EMPTY_LIST;

	//private boolean m_searchFilterEnabled;

	//@Nullable
	//private static ILookupFilterHandler m_lookupFilterHandler;
	//
	//@Nullable
	//private LookupFormSavedFilterFragment m_lookupFormSavedFilterFragment;

	private Div m_content;

	private NodeContainer m_collapsedPanel;

	private Div m_buttonRow;

	private ButtonFactory m_buttonFactory = new ButtonFactory(this);

	/**
	 * T in case that control is rendered as collapsed (meaning that search panel is hidden).
	 * It is usually used when lookup form have to popup with initial search results already shown.
	 */
	private boolean m_collapsed;

	/**
	 * Calculated by entered search criteria, T in case that exists any field resulting with {@link AppendCriteriaResult#VALID} in LookupForm fields.
	 */
	private boolean m_hasUserDefinedCriteria;

	/**
	 * After restore action on LookupForm.
	 */
	private IClicked<NodeBase> m_onAfterRestore;

	/**
	 * After collapse action on LookupForm.
	 */
	private IClicked<NodeBase> m_onAfterCollapse;

	private IQueryFactory<T> m_queryFactory;

	@Nullable
	private UIMessage m_newBtnDisableReason;

	public enum ButtonMode {
		/** Show this button only when the lookup form is expanded */
		NORMAL,

		/** Show this button only when the lookup form is collapsed */
		COLLAPSED,

		/** Always show this button. */
		BOTH
	}

	/**
	 * A button that needs to be present @ the button bar.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Nov 3, 2009
	 */
	private static class ButtonRowItem {
		private final int m_order;

		private final ButtonMode m_mode;

		private final NodeBase m_thingy;

		public ButtonRowItem(int order, ButtonMode mode, NodeBase thingy) {
			m_order = order;
			m_mode = mode;
			m_thingy = thingy;
		}

		public ButtonMode getMode() {
			return m_mode;
		}

		public int getOrder() {
			return m_order;
		}

		public NodeBase getThingy() {
			return m_thingy;
		}
	}

	//private Map<String, ILookupControlInstance<?>> getFilterItems() {
	//	Map<String, ILookupControlInstance<?>> filterValues = new HashMap<>();
	//	for(LookupLine item : m_itemList) {
	//		String propertyName = item.getPropertyName() != null ? item.getPropertyName() : item.getLabelText();
	//		filterValues.put(propertyName, item.getInstance());
	//	}
	//	return filterValues;
	//}

	//public Map<String, ?> getFilterValues() {
	//	Map<String, ILookupControlInstance<?>> filterItems = getFilterItems();
	//	Map<String, Object> filterValues = new HashMap<>();
	//	for(Entry<String, ILookupControlInstance<?>> entry : filterItems.entrySet()) {
	//		if(entry.getValue().getValue() != null) {
	//			filterValues.put(entry.getKey(), entry.getValue().getValue());
	//		}
	//	}
	//	return filterValues;
	//}

	public SearchPanel(@Nonnull final Class<T> lookupClass, @GProperty String... propertyList) {
		this(lookupClass, null, propertyList);
	}

	/**
	 * Create a LookupForm to find instances of the specified class.
	 */
	public SearchPanel(@Nonnull final Class<T> lookupClass, @Nullable final ClassMetaModel cmm, String... propertyList) {
		m_rootCriteria = null;
		m_lookupClass = lookupClass;
		m_metaModel = cmm != null ? cmm : MetaManager.findClassMeta(lookupClass);
		for(String prop : propertyList) {
			internalAddByPropertyName(prop);
		}
		defineDefaultButtons();
	}

	public SearchPanel(@Nonnull QCriteria<T> rootCriteria, String... propertyList) {
		this(DomUtil.nullChecked(rootCriteria.getBaseClass()), null, propertyList);
		m_rootCriteria = rootCriteria;
	}

	/**
	 * Actually show the thingy.
	 */
	@Override
	public void createContent() throws Exception {
		//if(isSearchFilterEnabled()) {
		//	addFilterButton();
		//	loadSearchQueries();
		//}
		//-- If a page title is present render the search block in a CaptionedPanel, else present in its own div.
		Div sroot = m_content = new Div();
		add(sroot);
		sroot.setCssClass("ui-lf-mainContent");

		//-- Ok, we need the items we're going to show now.
		if(m_itemList.size() == 0)                            // If we don't have an item set yet....
			internalAddMetadata();

		//-- Start populating the lookup form with lookup items.
		ISearchFormBuilder formBuilder = getFormBuilder();
		formBuilder.setTarget(this);
		for(Object o : m_itemList) {
			if(o instanceof SearchControlLine) {
				SearchControlLine<?> it = (SearchControlLine<?>) o;
				it.clear();
				formBuilder.append(it);
			} else if(o instanceof  IExecute) {
				((IExecute) o).execute();
			}
		}
		formBuilder.finish();

		////-- The saved filters are shown next
		//if(isSearchFilterEnabled()) {
		//	if(containsItemBreaks(m_itemList)) {
		//		throw new IllegalStateException("Not implemented yet. It is not possible to show the saved searched filter in combination with a split lookupform");
		//	}
		//	addFilterFragment(searchContainer);
		//}

		//-- The button bar.
		Div d = m_buttonRow = new Div();
		add(d);
		d.setCssClass("ui-lf-ebb");

		//20091127 vmijic - since LookupForm can be reused each new rebuild should execute restore if previous state of form was collapsed.
		//20100118 vmijic - since LookupForm can be by default rendered as collapsed check for m_collapsed is added.
		if(!m_collapsed && m_collapsedPanel != null) {
			restore();
		} else if(m_collapsed && m_content.getDisplay() != DisplayType.NONE) {
			collapse();
			//Focus must be set, otherwise IE reports javascript problem since focus is requested on not displayed input tag.
			if(m_cancelBtn != null) {
				m_cancelBtn.setFocus();
			} else if(m_collapseButton != null) {
				m_collapseButton.setFocus();
			}
		} else {
			createButtonRow(d, false);
		}

		//-- Add a RETURN PRESSED handler to allow pressing RETURN on search fields.
		setReturnPressed(node -> {
			if(m_clicker != null)
				m_clicker.clicked(SearchPanel.this);
		});
	}

	//private void addFilterFragment(NodeContainer searchContainer) {
	//	TD anotherSearchRootCell = new TD();
	//	searchContainer.appendAfterMe(anotherSearchRootCell);
	//	LookupFormSavedFilterFragment div = m_lookupFormSavedFilterFragment = new LookupFormSavedFilterFragment(m_savedFilters);
	//	div.onFilterClicked(new INotify<SavedFilter>() {
	//		@Override
	//		public void onNotify(@Nonnull SavedFilter sender) throws Exception {
	//			clearInput();
	//			fillSearchFields(sender);
	//			if(m_clicker != null) {
	//				m_clicker.clicked(SearchPanel.this);
	//			}
	//		}
	//	});
	//	div.onFilterDeleted(new INotify<SavedFilter>() {
	//		@Override
	//		public void onNotify(@Nonnull SavedFilter sender) throws Exception {
	//			deleteSavedFilter(sender);
	//		}
	//	});
	//
	//	anotherSearchRootCell.add(div);
	//}

	//private void loadSearchQueries() throws Exception {
	//	ILookupFilterHandler lookupFilterHandler = getLookupFilterHandler();
	//	final List<SavedFilter> savedFilters = lookupFilterHandler.load(getSharedContext(), getPage().getBody().getClass().getName());
	//	setSavedFilters(savedFilters);
	//}

	//private void deleteSavedFilter(SavedFilter filter) throws Exception {
	//	ILookupFilterHandler lookupFilterHandler = getLookupFilterHandler();
	//	try(QDataContext unmanagedContext = QContextManager.createUnmanagedContext()) { // We create a separate context because we don't want to commit other transactions
	//		lookupFilterHandler.delete(unmanagedContext, filter.getRecordId());
	//		unmanagedContext.commit();
	//	}
	//}
	//
	//public synchronized static void setLookupFilterHandler(final @Nullable ILookupFilterHandler filterSaver) {
	//	m_lookupFilterHandler = filterSaver;
	//}
	//
	//@Nonnull
	//private synchronized static ILookupFilterHandler getLookupFilterHandler() {
	//	ILookupFilterHandler lookupFilterHandler = m_lookupFilterHandler;
	//	if(lookupFilterHandler == null) {
	//		throw new IllegalStateException("There is no code to handle the saved filter.");
	//	}
	//	return lookupFilterHandler;
	//}
	//
	//private void fillSearchFields(SavedFilter filter) throws Exception {
	//	//final Map<String, ILookupControlInstance<?>> formLookupFilterItems = getFilterItems();
	//	//final Map<String, ?> savedFilterValues = LookupFilterTranslator.deserialize(getSharedContext(), filter.getFilterValue());
	//	//for(Entry<String, ?> entry : savedFilterValues.entrySet()) {
	//	//	final String property = entry.getKey();
	//	//	final ILookupControlInstance<Object> controlInstance = (ILookupControlInstance<Object>) formLookupFilterItems.get(property);
	//	//	if(controlInstance == null) {
	//	//		continue; // to avoid possible NPE
	//	//	}
	//	//	controlInstance.setValue(entry.getValue());
	//	//}
	//}

	protected void defineDefaultButtons() {
		DefaultButton b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_SEARCH));
		b.setIcon("THEME/btnFind.png");
		b.setTestID("searchButton");
		b.setTitle(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_SEARCH_TITLE));
		b.css("is-primary");
		b.setClicked(bx -> {
			if(m_clicker != null)
				m_clicker.clicked(SearchPanel.this);
		});
		addButtonItem(b, 100, ButtonMode.NORMAL);

		m_clearButton = b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CLEAR));
		b.setIcon("THEME/btnClear.png");
		b.setTestID("clearButton");
		b.setTitle(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CLEAR_TITLE));
		b.setClicked(xb -> {
			clearInput();
			if(getOnClear() != null)
				((IClicked<SearchPanel<T>>) getOnClear()).clicked(SearchPanel.this); // FIXME Another generics snafu, fix.
		});
		addButtonItem(b, 200, ButtonMode.NORMAL);

		//-- Collapse button thingy
		m_collapseButton = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_COLLAPSE), "THEME/btnHideLookup.png", bx -> collapse());
		m_collapseButton.setTestID("hideButton");
		m_collapseButton.setTitle(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_COLLAPSE_TITLE));
		addButtonItem(m_collapseButton, 300, ButtonMode.BOTH);
	}

	public void addFilterButton() {
		if(m_filterButton == null) { // Only add the button if it doesn't exist already
			m_filterButton = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_SAVE_SEARCH), Theme.BTN_SAVE, clickednode -> saveSearchQuery());
			addButtonItem(m_filterButton, 400, ButtonMode.NORMAL);
		}
	}

	private void saveSearchQuery() throws Exception {
		//SaveSearchFilterDialog dialog = new SaveSearchFilterDialog(DomUtil.nullChecked(m_lookupFilterHandler), getPage().getBody().getClass().getName(), getFilterValues());
		//dialog.onFilterSaved(sender -> {
		//	m_savedFilters.add(sender);
		//	if(m_lookupFormSavedFilterFragment != null) {
		//		m_lookupFormSavedFilterFragment.forceRebuild();
		//	}
		//});
		//dialog.modal();
		//add(dialog);
	}

	/**
	 * This hides the search panel and adds a small div containing only the (optional) new and restore buttons.
	 * @throws Exception
	 */
	void collapse() throws Exception {
		if((m_content.getDisplay() == DisplayType.NONE))
			return;

		if(m_content.isBuilt()) {
			Animations.slideUp(m_content);
		} else {
			m_content.setDisplay(DisplayType.NONE);
		}
		m_collapsedPanel = new Div();
		m_collapsedPanel.setCssClass("ui-lf-coll");
		add(m_collapsedPanel);
		m_collapsed = true;

		//-- Collapse button thingy
		m_collapseButton.setText(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_RESTORE));
		m_collapseButton.setIcon("THEME/btnShowLookup.png");
		m_collapseButton.setClicked((IClicked<DefaultButton>) bx -> restore());
		createButtonRow(m_collapsedPanel, true);
		//trigger after collapse event is set
		if(getOnAfterCollapse() != null) {
			getOnAfterCollapse().clicked(this);
		}
	}

	void restore() throws Exception {
		if(m_collapsedPanel == null)
			return;
		m_collapsedPanel.remove();
		m_collapsedPanel = null;
		createButtonRow(m_buttonRow, false);

		m_collapseButton.setText(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_COLLAPSE));
		m_collapseButton.setIcon("THEME/btnHideLookup.png");
		m_collapseButton.setClicked((IClicked<DefaultButton>) bx -> collapse());

		m_content.setDisplay(DisplayType.BLOCK);
		m_collapsed = false;

		//trigger after restore event is set
		if(getOnAfterRestore() != null) {
			getOnAfterRestore().clicked(this);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Altering/defining the lookup items.					*/
	/*--------------------------------------------------------------*/

	/**
	 * Clear out the entire definition for this lookup form. After this it needs to be recreated completely.
	 */
	public void reset() {
		forceRebuild();
		m_itemList.clear();
	}

	/**
	 * This checks all of the search fields for data. For every field that contains search
	 * data we check if the data is suitable for searching (not too short for instance); if
	 * it is we report errors. If the data is suitable <b>and</b> at least one field is filled
	 * we create a Criteria containing the search criteria.
	 *
	 * If anything goes wrong (one of the above mentioned errors occurs) ths returns null.
	 * If none of the input fields have data this will return a Criteria object, but the
	 * restrictions count in it will be zero. This can be used to query but will return all
	 * records.
	 *
	 * <h2>Internal working</h2>
	 * <p>Internally this just walks the list of thingies added when the components were added
	 * to the form. Each thingy refers to the input components used to register the search on a
	 * property, and knows how to convert that thingy to a criteria fragment.
	 * </p>
	 */
	@Nullable
	public QCriteria<T> getCriteria() throws Exception {
		m_hasUserDefinedCriteria = false;
		QCriteria<T> root;
		IQueryFactory<T> queryFactory = getQueryFactory();
		if(queryFactory != null) {
			root = queryFactory.createQuery();
		} else {
			root = (QCriteria<T>) getMetaModel().createCriteria();
			QCriteria<T> rootCriteria = m_rootCriteria;
			if(null != rootCriteria)
				root.mergeCriteria(rootCriteria);
		}
		boolean success = true;
		for(Object obj : m_itemList) {
			if(obj instanceof SearchControlLine) {
				SearchControlLine<?> it = (SearchControlLine<?>) obj;
				LookupQueryBuilderResult res = appendCriteria(root, it);
				if(res == LookupQueryBuilderResult.INVALID) {
					success = false;
				} else if(res == LookupQueryBuilderResult.VALID) {
					m_hasUserDefinedCriteria = true;
				}
			}
		}
		if(!success) {                                        // Some input failed to validate their input criteria?
			m_hasUserDefinedCriteria = false;
			return null;                                    // Then exit null -> should only display errors.
		}
		return root;
	}

	private <D> LookupQueryBuilderResult appendCriteria(QCriteria<T> criteria, SearchControlLine<D> it) {
		IControl<D> control = it.getControl();
		ILookupQueryBuilder<D> builder = it.getQueryBuilder();
		if(null != control && null != builder) {
			return builder.appendCriteria(criteria, control.getValue());
		}
		return LookupQueryBuilderResult.EMPTY;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Silly and small methods.							*/
	/*--------------------------------------------------------------*/

	/**
	 * Tells all input items to clear their content, clearing all user choices from the form. After
	 * this call, the form should return an empty QCriteria without any restrictions.
	 */
	public void clearInput() {
		for(Object o : m_itemList) {
			if(o instanceof SearchControlLine) {
				((SearchControlLine<?>) o).clear();
			}
		}
	}

	/**
	 * Sets the onNew handler. When set this will render a "new" button in the form's button bar.
	 * @return
	 */
	public IClicked<SearchPanel<T>> getOnNew() {
		return m_onNew;
	}

	/**
	 * Returns the onNew handler. When set this will render a "new" button in the form's button bar.
	 * @param onNew
	 */
	public void setOnNew(final IClicked<SearchPanel<T>> onNew) {
		if(m_onNew != onNew) {
			m_onNew = onNew;
			if(m_onNew != null && m_newBtn == null) {
				m_newBtn = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_NEW));
				m_newBtn.setIcon(Theme.BTN_NEW);
				m_newBtn.setTestID("newButton");
				m_newBtn.setTitle(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_NEW_TITLE));
				m_newBtn.setClicked(new IClicked<NodeBase>() {
					@Override
					public void clicked(final @Nonnull NodeBase xb) throws Exception {
						if(getOnNew() != null) {
							getOnNew().clicked(SearchPanel.this);
						}
					}
				});
				m_newBtn.setDisabled(m_newBtnDisableReason);
				addButtonItem(m_newBtn, 500, ButtonMode.BOTH);
			} else if(m_onNew == null && m_newBtn != null) {
				for(ButtonRowItem bri : m_buttonItemList) {
					if(bri.getThingy() == m_newBtn) {
						m_buttonItemList.remove(bri);
						break;
					}
				}
				m_newBtn = null;
			}
			forceRebuild();
		}
	}

	/**
	 * Set the handler to call when the "Search" button is clicked.
	 * @see NodeBase#setClicked(IClicked)
	 */
	@Override
	public void setClicked(final @Nullable IClicked<?> clicked) {
		m_clicker = (IClicked<SearchPanel<T>>) clicked;
	}

	public IClicked<SearchPanel<T>> getSearchClicked() {
		return m_clicker;
	}

	public IClicked<? extends SearchPanel<T>> getOnClear() {
		return m_onClear;
	}

	/**
	 * Listener to call when the "clear" button is pressed.
	 * @param onClear
	 */
	public void setOnClear(IClicked<? extends SearchPanel<T>> onClear) {
		m_onClear = onClear;
	}

	/**
	 * When set, this causes a "cancel" button to be added to the form. When that button is pressed this handler gets called.
	 * @param onCancel
	 */
	public void setOnCancel(IClicked<SearchPanel<T>> onCancel) {
		if(m_onCancel != onCancel) {
			m_onCancel = onCancel;
			if(m_onCancel != null && m_cancelBtn == null) {
				m_cancelBtn = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CANCEL));
				m_cancelBtn.setIcon(Theme.BTN_CANCEL);
				m_cancelBtn.setTestID("cancelButton");
				m_cancelBtn.setTitle(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CANCEL_TITLE));
				m_cancelBtn.setClicked(new IClicked<NodeBase>() {
					@Override
					public void clicked(final @Nonnull NodeBase xb) throws Exception {

						if(getOnCancel() != null) {
							getOnCancel().clicked(SearchPanel.this);
						}
					}
				});
				addButtonItem(m_cancelBtn, 400, ButtonMode.BOTH);
			} else if(m_onCancel == null && m_cancelBtn != null) {
				for(ButtonRowItem bri : m_buttonItemList) {
					if(bri.getThingy() == m_cancelBtn) {
						m_buttonItemList.remove(bri);
						break;
					}
				}
				m_cancelBtn = null;
			}
			forceRebuild();
		}
	}

	public IClicked<SearchPanel<T>> getOnCancel() {
		return m_onCancel;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Button row code.									*/
	/*--------------------------------------------------------------*/

	public void addButtonItem(NodeBase b) {
		addButtonItem(b, m_buttonItemList.size(), ButtonMode.BOTH);
	}

	/**
	 * Add a button (or other item) to show on the button row. The item will
	 * be visible always.
	 * @param b
	 * @param order
	 */
	public void addButtonItem(NodeBase b, int order) {
		addButtonItem(b, order, ButtonMode.BOTH);
	}

	/**
	 * Add a button (or other item) to show on the button row.
	 *
	 * @param b
	 * @param order
	 * @param both
	 */
	public void addButtonItem(NodeBase b, int order, ButtonMode both) {
		if(m_buttonItemList == Collections.EMPTY_LIST)
			m_buttonItemList = new ArrayList<>(10);
		m_buttonItemList.add(new ButtonRowItem(order, both, b));
	}

	/**
	 * Add all buttons, both default and custom to buttom row.
	 * @param c
	 * @param iscollapsed
	 */
	private void createButtonRow(NodeContainer c, boolean iscollapsed) {
		Collections.sort(m_buttonItemList, new Comparator<ButtonRowItem>() { // Sort in ascending order,
			@Override
			public int compare(ButtonRowItem o1, ButtonRowItem o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});

		for(ButtonRowItem bi : m_buttonItemList) {
			if((iscollapsed && (bi.getMode() == ButtonMode.BOTH || bi.getMode() == ButtonMode.COLLAPSED)) || (!iscollapsed && (bi.getMode() == ButtonMode.BOTH || bi.getMode() == ButtonMode.NORMAL))) {
				c.add(bi.getThingy());
			}
		}
	}

	/**
	 * Method {@link SearchPanel#getCriteria} MUST BE EXECUTED BEFORE checking for this property value!
	 * This is T when the user has actually entered something in one of the search components. Any restriction
	 * that has been added by code that is not depending on user input is ignored.
	 * @return
	 */
	public boolean hasUserDefinedCriteria() {
		return m_hasUserDefinedCriteria;
	}

	/**
	 * Returns if LookupForm is collapsed.
	 *
	 * @return
	 */
	public boolean isCollapsed() {
		return m_collapsed;
	}

	/**
	 * Use to collapse/restore LookupForm search panel.
	 *
	 * @param collapsed
	 * @throws Exception
	 */
	public void setCollapsed(boolean collapsed) throws Exception {
		if(m_collapsed == collapsed)
			return;
		if(!isBuilt()) {
			m_collapsed = collapsed;
			return;
		}
		if(isBuilt()) {
			if(collapsed) {
				collapse();
			} else {
				restore();
			}
		}
	}

	/**
	 * Returns listener to after restore event.
	 *
	 * @return the onAfterRestore
	 */
	public IClicked<NodeBase> getOnAfterRestore() {
		return m_onAfterRestore;
	}

	/**
	 * Attach listener to after restore event.
	 *
	 * @param onAfterRestore the onAfterRestore to set
	 */
	public void setOnAfterRestore(IClicked<NodeBase> onAfterRestore) {
		m_onAfterRestore = onAfterRestore;
	}

	/**
	 * Returns listener to after collapse event.
	 *
	 * @return the onAfterCollpase
	 */
	public IClicked<NodeBase> getOnAfterCollapse() {
		return m_onAfterCollapse;
	}

	/**
	 * Attach listener to after collpase event.
	 *
	 * @param onAfterCollapse the onAfterCollapse to set
	 */
	public void setOnAfterCollapse(IClicked<NodeBase> onAfterCollapse) {
		m_onAfterCollapse = onAfterCollapse;
	}

	/**
	 * Returns custom query factory.
	 * @return
	 */
	public IQueryFactory<T> getQueryFactory() {
		return m_queryFactory;
	}

	/**
	 * Specifies custom query factory.
	 * @param queryFactory
	 */
	public void setQueryFactory(IQueryFactory<T> queryFactory) {
		m_queryFactory = queryFactory;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Button container handling.							*/
	/*--------------------------------------------------------------*/

	/**
	 *
	 * @see IButtonContainer#addButton(NodeBase, int)
	 */
	@Override
	public void addButton(@Nonnull NodeBase thing, int order) {
		if(order < 0)
			addButtonItem(thing);
		else
			addButtonItem(thing, order);
	}

	@Nonnull
	public ButtonFactory getButtonFactory() {
		return m_buttonFactory;
	}

	public void setNewBtnDisableReason(@Nullable UIMessage rsn) {
		m_newBtnDisableReason = rsn;
		if(null != m_newBtn) {
			m_newBtn.setDisabled(rsn);
		}
	}

	//private void setSavedFilters(List<SavedFilter> savedFilters) {
	//	m_savedFilters = savedFilters;
	//}
	//
	//public boolean isSearchFilterEnabled() {
	//	return m_searchFilterEnabled;
	//}
	//
	//public void setSearchFilterEnabled(boolean searchFilterEnabled) {
	//	m_searchFilterEnabled = searchFilterEnabled;
	//}

	@Nullable
	public DefaultButton getClearButton() {
		return m_clearButton;
	}

	@Nonnull public ISearchFormBuilder getFormBuilder() {
		ISearchFormBuilder builder = m_formBuilder;
		if(null == builder) {
			Supplier<ISearchFormBuilder> factory = m_defaultFormBuilderFactory;
			if(null != factory) {
				m_formBuilder = builder = factory.get();
			} else {
				m_formBuilder = builder = new DefaultSearchFormBuilder();
			}
		}
		return builder;
	}

	public void setFormBuilder(@Nullable ISearchFormBuilder formBuilder) {
		m_formBuilder = formBuilder;
	}

	/**
	 * Set the default form factory to use when forms are generated. This is a global parameter
	 * and should only be set from {@link to.etc.domui.server.DomApplication#initialize(ConfigParameters)}
	 */
	static public void setDefaultSearchFormBuilder(Supplier<ISearchFormBuilder> factory) {
		m_defaultFormBuilderFactory = factory;
	}


	/**
	 * Return the metamodel that this class uses to get its data from.
	 */
	@Nonnull
	public ClassMetaModel getMetaModel() {
		return m_metaModel;
	}

	/**
	 * Returns the class whose instances we're looking up (a persistent class somehow).
	 */
	@Nonnull
	public Class<T> getLookupClass() {
		return m_lookupClass;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Property/item builder.										*/
	/*----------------------------------------------------------------------*/

	@Nullable
	private SearchItemBuilder<T> m_currentBuilder;

	/**
	 * Start a builder adding a new lookup item to the form. The builder should be finished properly
	 * or an error occurs.
	 * <h2>Warning:</h2>
	 * <p>When adding lookup items manually, all metadata-added properties disappear. If you want to have the
	 * metadata-provided lookup items too then call addDefault() before or after the call(s) to this method.</p>
	 */
	public SearchItemBuilder<T> add() {
		if(m_currentBuilder != null)
			throw new IllegalStateException(this + ": The builder " + m_currentBuilder + " has not yet been finished");
		SearchItemBuilder<T> item = new SearchItemBuilder<>(this);
		m_currentBuilder = item;
		return item;
	}

	@Nonnull <D> SearchControlLine<D> finishBuilder(SearchItemBuilder<T> builder) {
		if(m_currentBuilder != builder)
			throw new IllegalStateException(this + ": the item builder " + builder + " is not the 'current' item - call order problem");
		SearchControlLine<D> line = createLine(builder);
		forceRebuild();
		m_currentBuilder = null;
		return line;
	}

	public void addAction(SearchItemBuilder<T> builder, IExecute action) {
		if(m_currentBuilder != builder)
			throw new IllegalStateException(this + ": the item builder " + builder + " is not the 'current' item - call order problem");
		m_currentBuilder = null;
		forceRebuild();
		m_itemList.add(action);
	}

	@Nonnull
	private <D> SearchControlLine<D> createLine(SearchItemBuilder<T> builder) {
		IExecute action = builder.getAction();
		if(action != null) {
			throw new IllegalStateException("Combining action with control is not allowed");
		}

		IControl<D> control = (IControl<D>) builder.getControl();
		ILookupQueryBuilder<D> qb = builder.getQueryBuilder();
		PropertyMetaModel<?> property = builder.getProperty();
		if(null == control) {
			//-- No control, no query builder: use the property to get one
			if(null == property)
				throw new ProgrammerErrorException(builder + ": needs to specify a property");
			if(qb != null)
				throw new ProgrammerErrorException(builder + ": cannot set a query builder without also specifying a control.");

			SearchPropertyMetaModelImpl spm = mergePropertyModels(builder, property);
			FactoryPair<D> pair = (FactoryPair<D>) LookupControlRegistry2.INSTANCE.findControlPair(spm);
			if(null == pair)
				throw new ProgrammerErrorException(builder + ": no lookup control factory found for property " + spm);
			control = pair.getControl();
			qb = pair.getQueryBuilder();
		} else if(null == qb) {
			//-- Use the equals query builder by default. For this we REQUIRE a property.
			if(null == property)
				throw new ProgrammerErrorException(builder + ": when specifying a control you need to either define a property or a query builder, otherwise I do not know how to search");
			qb = new ObjectLookupQueryBuilder<>(property.getName());
		}

		String testId = builder.getTestId();
		if(null != testId)
			control.setTestID(testId);

		//-- Try to get a label
		NodeContainer labelNode = builder.getLabelNode();
		String labelText = null;
		if(null == labelNode) {
			labelText = builder.getLabelText();
			if(null == labelText) {
				if(property != null) {
					labelText = property.getDefaultLabel();
				}
			}
			if(null != labelText) {
				labelNode = new Label((NodeBase) control, labelText);
			}
		}

		//String hint = builder.getLookupHint();
		//if(null != hint) {
		//	FIXME cannot set hint here because setHint is not part of IControl
		//}

		SearchControlLine<D> ll = new SearchControlLine<>(control, qb, property, builder.getDefaultValue(), labelNode, false);
		assignCalcTestID(ll, property, labelText);
		addLookupLine(ll);
		return ll;
	}

	private SearchPropertyMetaModelImpl mergePropertyModels(SearchItemBuilder<T> builder, PropertyMetaModel<?> property) {
		SearchPropertyMetaModelImpl m = new SearchPropertyMetaModelImpl(getMetaModel(), property);
		if(builder.isIgnoreCase())
			m.setIgnoreCase(true);
		if(builder.isInitiallyCollapsed())
			m.setPopupInitiallyCollapsed(true);
		if(builder.isSearchImmediately())
			m.setPopupSearchImmediately(true);
		if(builder.getMinLength() > 0)
			m.setMinLength(builder.getMinLength());
		return m;
	}

	/**
	 * Use {@link #addDefault()} instead.
	 * This adds all properties that are defined as "search" properties in either this control or the metadata
	 * to the item list. The list is cleared before that!
	 */
	@Deprecated
	public void setDefaultItems() {
		addDefault();
	}

	@NotNull private List<SearchPropertyMetaModel> getMetadataSearchPropertyList() {
		List<SearchPropertyMetaModel> list = getMetaModel().getSearchProperties();
		if(list == null || list.size() == 0) {
			list = MetaManager.calculateSearchProperties(getMetaModel()); // 20100416 jal EXPERIMENTAL
			if(list == null || list.size() == 0)
				return Collections.emptyList();
		}
		return list;
	}

	private void internalAddMetadata() {
		List<SearchPropertyMetaModel> list = getMetadataSearchPropertyList();
		if(list.size() == 0)
			throw new IllegalStateException(getMetaModel() + " has no search properties defined in its meta data.");
		appendMetadataProperties(list);
	}

	/**
	 * Add all default lookup items by scanning the metadata for the class. This is
	 * only needed if you want to add items to existing metadata. Please take note:
	 * when adding metadata, metadata will only add controls for properties that
	 * do not yet exist as lookup items.
	 */
	public void addDefault() {
		List<SearchPropertyMetaModel> list = getMetadataSearchPropertyList();
		if(list.size() > 0)
			appendMetadataProperties(list);
	}

	private void appendMetadataProperties(List<SearchPropertyMetaModel> list) {
		for(SearchPropertyMetaModel spm : list) {
			addMetadataProperty(spm, true);
		}
	}

	/**
	 * Called when property names are provided in the constructor.
	 */
	private void internalAddByPropertyName(String prop) {
		PropertyMetaModel<?> pmm = m_metaModel.getProperty(prop);
		SearchPropertyMetaModelImpl spm = new SearchPropertyMetaModelImpl(m_metaModel, pmm);
		addMetadataProperty(spm, false);
	}

	private <D> SearchControlLine<D> addMetadataProperty(SearchPropertyMetaModel spm, boolean fromMetadata) {
		PropertyMetaModel<?> property = spm.getProperty();
		FactoryPair<D> pair = (FactoryPair<D>) LookupControlRegistry2.INSTANCE.findControlPair(spm);
		if(null == pair)
			throw new ProgrammerErrorException("No lookup control factory found for property " + spm);
		IControl<D> control = pair.getControl();
		ILookupQueryBuilder<D> qb = pair.getQueryBuilder();

		//-- Try to get a label
		String labelText = property.getDefaultLabel();
		Label labelNode = new Label((NodeBase) control, labelText);

		//String hint = builder.getLookupHint();
		//if(null != hint) {
		//	FIXME cannot set hint here because setHint is not part of IControl
		//}

		SearchControlLine<D> ll = new SearchControlLine<>(control, qb, property, null, labelNode, fromMetadata);
		assignCalcTestID(ll, property, labelText);
		addLookupLine(ll);
		return ll;
	}

	/**
	 * Set the search properties to use from a list of metadata properties.
	 * @param list
	 */
	public void setSearchProperties(List<SearchPropertyMetaModel> list) {
		for(SearchPropertyMetaModel sp : list) { // The list is already in ascending order, so just add items;
			addMetadataProperty(sp, false);
		}
	}

	private <D> void addLookupLine(SearchControlLine<D> line) {
		PropertyMetaModel<?> property = line.getProperty();
		if(null != property) {
			//-- Do we already have this one?
			for(int i = m_itemList.size(); --i >= 0; ) {
				Object obj = m_itemList.get(i);
				if(obj instanceof SearchControlLine) {
					SearchControlLine<?> old = (SearchControlLine<?>) obj;
					if(old.getProperty() == property) {
						//-- Existing property: if this one comes from metadata do not add it
						if(line.isFromMetadata())
							return;

						//-- If the old item is from metadata: replace it
						if(old.isFromMetadata()) {
							m_itemList.set(i, line);
							return;
						}

						//-- None of the above -> just add a copy.
					}
				}
			}
		}
		m_itemList.add(line);
		forceRebuild();
	}

	private <D> void assignCalcTestID(@Nonnull SearchControlLine<D> item, @Nullable PropertyMetaModel<?> pmm, @Nullable String labelText) {
		String testID = item.getControl().getTestID();
		if(null != testID)
			return;
		String lbl = labelText;
		if(pmm != null) {
			lbl = pmm.getName();
		}
		item.getControl().setTestID(lbl);
	}

}

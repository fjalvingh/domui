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
package to.etc.domui.component.lookupform2;

import org.jetbrains.annotations.NotNull;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.event.INotify;
import to.etc.domui.component.input.IQueryFactory;
import to.etc.domui.component.input.LookupInputBase;
import to.etc.domui.component.layout.ButtonFactory;
import to.etc.domui.component.layout.CaptionedPanel;
import to.etc.domui.component.layout.IButtonContainer;
import to.etc.domui.component.lookup.AbstractLookupControlImpl;
import to.etc.domui.component.lookup.ILookupControlFactory;
import to.etc.domui.component.lookup.ILookupControlInstance;
import to.etc.domui.component.lookup.ILookupControlInstance.AppendCriteriaResult;
import to.etc.domui.component.lookup.ILookupFilterHandler;
import to.etc.domui.component.lookup.LookupFormSavedFilterFragment;
import to.etc.domui.component.lookup.SaveSearchFilterDialog;
import to.etc.domui.component.lookup.SavedFilter;
import to.etc.domui.component.lookup.filter.LookupFilterTranslator;
import to.etc.domui.component.lookupform2.lookupcontrols.FactoryPair;
import to.etc.domui.component.lookupform2.lookupcontrols.ILookupFactory;
import to.etc.domui.component.lookupform2.lookupcontrols.ILookupQueryBuilder;
import to.etc.domui.component.lookupform2.lookupcontrols.LookupControlRegistry2;
import to.etc.domui.component.lookupform2.lookupcontrols.LookupQueryBuilderResult;
import to.etc.domui.component.lookupform2.lookupcontrols.ObjectLookupQueryBuilder;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.MetaUtils;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component.meta.impl.SearchPropertyMetaModelImpl;
import to.etc.domui.component2.lookupinput.LookupInputBase2;
import to.etc.domui.dom.Animations;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.css.VerticalAlignType;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IReturnPressed;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.domui.dom.html.TableVAlign;
import to.etc.domui.server.DomApplication;
import to.etc.domui.themes.Theme;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.Msgs;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.annotations.GProperty;
import to.etc.webapp.query.QContextManager;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QRestrictor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 *
 * @author jal at 2017-12-13.
 */
public class LookupForm2<T> extends Div implements IButtonContainer {
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
	private final List<LookupLine<?>> m_itemList = new ArrayList<>(20);

	/** The list of buttons to show on the button row. */
	private List<ButtonRowItem> m_buttonItemList = Collections.EMPTY_LIST;

	@Nullable
	private IFormBuilder m_formBuilder;

	private IClicked<LookupForm2<T>> m_clicker;

	private IClicked<LookupForm2<T>> m_onNew;

	private DefaultButton m_newBtn;

	private IClicked< ? extends LookupForm2<T>> m_onClear;

	private IClicked<LookupForm2<T>> m_onCancel;

	private DefaultButton m_cancelBtn;

	private DefaultButton m_collapseButton;

	private DefaultButton m_clearButton;

	@Nullable
	private DefaultButton m_filterButton;

	@Nonnull
	private List<SavedFilter> m_savedFilters = Collections.EMPTY_LIST;

	private boolean m_searchFilterEnabled;

	@Nullable
	private static ILookupFilterHandler m_lookupFilterHandler;

	@Nullable
	private LookupFormSavedFilterFragment m_lookupFormSavedFilterFragment;

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

	/** When adding properties yourself: do not clear metadata beforehand. */
	private boolean m_keepMetaData;

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

	public LookupForm2(@Nonnull final Class<T> lookupClass, @GProperty String... propertyList) {
		this(lookupClass, null, propertyList);
	}

	/**
	 * Create a LookupForm to find instances of the specified class.
	 * @param lookupClass
	 */
	public LookupForm2(@Nonnull final Class<T> lookupClass, @Nullable final ClassMetaModel cmm, String... propertyList) {
		m_rootCriteria = null;
		m_lookupClass = lookupClass;
		m_metaModel = cmm != null ? cmm : MetaManager.findClassMeta(lookupClass);
		for(String prop : propertyList)
			addProperty(prop);
		defineDefaultButtons();
	}

	public LookupForm2(@Nonnull QCriteria<T> rootCriteria, String... propertyList) {
		this(DomUtil.nullChecked(rootCriteria.getBaseClass()), null, propertyList);
		m_rootCriteria = rootCriteria;
	}

	/**
	 * Actually show the thingy.
	 */
	@Override
	public void createContent() throws Exception {
		if(isSearchFilterEnabled()) {
			addFilterButton();
			loadSearchQueries();
		}
		//-- If a page title is present render the search block in a CaptionedPanel, else present in it;s own div.
		Div sroot = m_content = new Div();
		add(sroot);
		sroot.setCssClass("ui-lf-mainContent");

		//-- Ok, we need the items we're going to show now.
		if(m_itemList.size() == 0)							// If we don't have an item set yet....
			internalAddMetadata();

		//-- Start populating the lookup form with lookup items.
		for(LookupLine it : m_itemList) {
			internalAddLookupItem(it);
		}

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
		setReturnPressed(new IReturnPressed<Div>() {
			@Override
			public void returnPressed(final @Nonnull Div node) throws Exception {
				if(m_clicker != null)
					m_clicker.clicked(LookupForm2.this);
			}
		});
	}

	private void addFilterFragment(NodeContainer searchContainer) {
		TD anotherSearchRootCell = new TD();
		searchContainer.appendAfterMe(anotherSearchRootCell);
		LookupFormSavedFilterFragment div = m_lookupFormSavedFilterFragment = new LookupFormSavedFilterFragment(m_savedFilters);
		div.onFilterClicked(new INotify<SavedFilter>() {
			@Override
			public void onNotify(@Nonnull SavedFilter sender) throws Exception {
				clearInput();
				fillSearchFields(sender);
				if(m_clicker != null) {
					m_clicker.clicked(LookupForm2.this);
				}
			}
		});
		div.onFilterDeleted(new INotify<SavedFilter>() {
			@Override
			public void onNotify(@Nonnull SavedFilter sender) throws Exception {
				deleteSavedFilter(sender);
			}
		});

		anotherSearchRootCell.add(div);
	}

	private void loadSearchQueries() throws Exception {
		ILookupFilterHandler lookupFilterHandler = getLookupFilterHandler();
		final List<SavedFilter> savedFilters = lookupFilterHandler.load(getSharedContext(), getPage().getBody().getClass().getName());
		setSavedFilters(savedFilters);
	}

	private void deleteSavedFilter(SavedFilter filter) throws Exception {
		ILookupFilterHandler lookupFilterHandler = getLookupFilterHandler();
		try(QDataContext unmanagedContext = QContextManager.createUnmanagedContext()) { // We create a separate context because we don't want to commit other transactions
			lookupFilterHandler.delete(unmanagedContext, filter.getRecordId());
			unmanagedContext.commit();
		}
	}

	public synchronized static void setLookupFilterHandler(final @Nullable ILookupFilterHandler filterSaver) {
		m_lookupFilterHandler = filterSaver;
	}

	@Nonnull
	private synchronized static ILookupFilterHandler getLookupFilterHandler() {
		ILookupFilterHandler lookupFilterHandler = m_lookupFilterHandler;
		if(lookupFilterHandler == null) {
			throw new IllegalStateException("There is no code to handle the saved filter.");
		}
		return lookupFilterHandler;
	}

	private void fillSearchFields(SavedFilter filter) throws Exception {
		//final Map<String, ILookupControlInstance<?>> formLookupFilterItems = getFilterItems();
		//final Map<String, ?> savedFilterValues = LookupFilterTranslator.deserialize(getSharedContext(), filter.getFilterValue());
		//for(Entry<String, ?> entry : savedFilterValues.entrySet()) {
		//	final String property = entry.getKey();
		//	final ILookupControlInstance<Object> controlInstance = (ILookupControlInstance<Object>) formLookupFilterItems.get(property);
		//	if(controlInstance == null) {
		//		continue; // to avoid possible NPE
		//	}
		//	controlInstance.setValue(entry.getValue());
		//}
	}

	protected void defineDefaultButtons() {
		DefaultButton b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_SEARCH));
		b.setIcon("THEME/btnFind.png");
		b.setTestID("searchButton");
		b.setTitle(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_SEARCH_TITLE));
		b.css("is-primary");
		b.setClicked(bx -> {
			if(m_clicker != null)
				m_clicker.clicked(LookupForm2.this);
		});
		addButtonItem(b, 100, ButtonMode.NORMAL);

		m_clearButton = b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CLEAR));
		b.setIcon("THEME/btnClear.png");
		b.setTestID("clearButton");
		b.setTitle(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CLEAR_TITLE));
		b.setClicked(xb -> {
			clearInput();
			if(getOnClear() != null)
				((IClicked<LookupForm2<T>>) getOnClear()).clicked(LookupForm2.this); // FIXME Another generics snafu, fix.
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

	/*--------------------------------------------------------------*/
	/*	CODING:	Internal.											*/
	/*--------------------------------------------------------------*/

	/**
	 * This adds the item to the item list, and tries to resolve all of the stuff needed to display
	 * the item. This means that the default label and the hint are calculated if missing, and that
	 * the lookup property is resolved if needed etc.
	 */
	private void addAndFinish(LookupLine it) {
		forceRebuild();
		m_itemList.add(it);
	}

	//private void addNonControlItem(@Nonnull LookupLine it) {
	//	////-- Create left and/or right cells,
	//	TR tr = new TR();
	//	m_tbody.add(tr);
	//	TD td = tr.addCell();
	//	NodeBase itLeft = it.getLeft();
	//	if(itLeft != null) {
	//		td.add(itLeft);
	//		if(it.isEntireRow()) {
	//			td.setColspan(2);
	//		}
	//	}
	//	NodeBase itRight = it.getRight();
	//	if(itRight != null) {
	//		TD tdRight = tr.addCell();
	//		tdRight.add(itRight);
	//	}
	//}
	//
	//private void updateUI(@Nonnull LookupLine it) {
	//	//-- jal 20130528 This component quite sucks balls- the interface is not able to add on-the-fly.
	//	if(m_tbody != null)
	//		internalAddLookupItem(it);
	//}

	/**
	 * Create the lookup item, depending on its kind.
	 */
	private void internalAddLookupItem(LookupLine it) {
		if(!it.isControl()) {
			addNonControlItem(it);
			return;
		}
		if(it.getInstance() == null) {
			//-- Create everything using a control creation factory,
			ILookupControlInstance<?> lci = createControlFor(it);
			if(lci == null)
				return;
			it.setInstance(lci);
		}
		if(it.getInstance() == null)
			throw new IllegalStateException("No idea how to create a lookup control for " + it);

		//-- Assign error locations to all input controls
		if(!DomUtil.isBlank(it.getErrorLocation()) ) {
			for(NodeBase ic : it.getInstance().getInputControls())
				ic.setErrorLocation(it.getErrorLocation());
		}

		if(it.isForcedDisabled()) {
			it.getInstance().setDisabled(true);
		} else if(it.isForcedEnabled()) {
			it.getInstance().setDisabled(false);
		}

		//-- Assign test id. If single control is created, testId as it is will be applied,
		//   if multiple component control is created, testId with suffix number will be applied.
		if(!DomUtil.isBlank(it.getTestId())) {
			if(it.getInstance().getInputControls().length == 1) {
				it.getInstance().getInputControls()[0].setTestID(it.getTestId());
			} else if(it.getInstance().getInputControls().length > 1) {
				int controlCounter = 1;
				for(NodeBase ic : it.getInstance().getInputControls()) {
					ic.setTestID(it.getTestId() + "_" + controlCounter);
					controlCounter++;
				}
			}
		}

		addItemToTable(it); // Create visuals.
	}

	/**
	 * Add the visual representation of the item: add a row with a cell containing a label
	 * and another cell containing the lookup controls. This tries all the myriad ways of
	 * getting the label for the control.
	 *
	 * @param it	The fully completed item definition to add.
	 */
	private void addItemToTable(LookupLine it) {
		ILookupControlInstance<?> qt = it.getInstance();

		//-- Create control && label cells,
		TR tr = new TR();
		m_tbody.add(tr);
		TD lcell = new TD(); // Label cell
		tr.add(lcell);
		lcell.setCssClass("ui-f4-lbl ui-f4-lbl-v");

		TD ccell = new TD(); // Control cell
		tr.add(ccell);
		ccell.setCssClass("ui-f-in ui-f4-ctl ui-f4-ctl-v");

		//-- Now add the controls and shtuff..
		NodeBase labelcontrol = qt.getLabelControl();
		for(NodeBase b : qt.getInputControls()) { // Add all nodes && try to find label control if unknown.
			ccell.add(b);
			assignCalcTestID(it, b);
			if(labelcontrol == null && b instanceof IControl< ? >)
				labelcontrol = b;
		}
		if(labelcontrol == null)
			labelcontrol = qt.getInputControls()[0];

		//-- Finally: add the label
		if(it.getLabelText() != null && it.getLabelText().length() > 0) {
			Label l = new Label(labelcontrol, it.getLabelText());
			//			if(l.getForNode() == null)
			//				l.setForNode(labelcontrol);
			lcell.add(l);
		}
	}

	private void assignCalcTestID(@Nonnull LookupLine item, @Nonnull NodeBase b) {
		if(b.getTestID() != null)
			return;
		String lbl = item.getPropertyName();
		if(null == lbl)
			lbl = item.getLabelText();
		if(null == lbl)
			lbl = DomUtil.getClassNameOnly(b.getClass());
		b.setCalculcatedId(lbl);
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
	 *
	 * @return
	 */
	@Nullable
	public QCriteria<T> getEnteredCriteria() throws Exception {
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
		for(LookupLine<?> it : m_itemList) {
			LookupQueryBuilderResult res = appendCriteria(root, it);
			if(res == LookupQueryBuilderResult.INVALID) {
				success = false;
			} else if(res == LookupQueryBuilderResult.VALID) {
				m_hasUserDefinedCriteria = true;
			}
		}
		if(!success) { 										// Some input failed to validate their input criteria?
			m_hasUserDefinedCriteria = false;
			return null; 									// Then exit null -> should only display errors.
		}
		return root;
	}

	private <D> LookupQueryBuilderResult appendCriteria(QCriteria<T> criteria, LookupLine<D> it) {
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
		for(LookupLine it : m_itemList) {
			IControl control = it.getControl();
			if(null != control) {
				control.setValue(it.getDefaultValue());
			}
		}
	}

	/**
	 * Sets the onNew handler. When set this will render a "new" button in the form's button bar.
	 * @return
	 */
	public IClicked<LookupForm2<T>> getOnNew() {
		return m_onNew;
	}

	/**
	 * Returns the onNew handler. When set this will render a "new" button in the form's button bar.
	 * @param onNew
	 */
	public void setOnNew(final IClicked<LookupForm2<T>> onNew) {
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
							getOnNew().clicked(LookupForm2.this);
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
	public void setClicked(final @Nullable IClicked< ? > clicked) {
		m_clicker = (IClicked<LookupForm2<T>>) clicked;
	}

	public IClicked<LookupForm2<T>> getSearchClicked() {
		return m_clicker;
	}

	public IClicked< ? extends LookupForm2<T>> getOnClear() {
		return m_onClear;
	}

	/**
	 * Listener to call when the "clear" button is pressed.
	 * @param onClear
	 */
	public void setOnClear(IClicked< ? extends LookupForm2<T>> onClear) {
		m_onClear = onClear;
	}

	/**
	 * When set, this causes a "cancel" button to be added to the form. When that button is pressed this handler gets called.
	 * @param onCancel
	 */
	public void setOnCancel(IClicked<LookupForm2<T>> onCancel) {
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
							getOnCancel().clicked(LookupForm2.this);
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

	public IClicked<LookupForm2<T>> getOnCancel() {
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
	 * Method {@link LookupForm2#getEnteredCriteria} MUST BE EXECUTED BEFORE checking for this property value!
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

	public void setNewBtnDisableReason(@Nullable UIMessage rsn){
		m_newBtnDisableReason = rsn;
		if (null != m_newBtn){
			m_newBtn.setDisabled(rsn);
		}
	}

	private void setSavedFilters(List<SavedFilter> savedFilters) {
		m_savedFilters = savedFilters;
	}

	public boolean isSearchFilterEnabled() {
		return m_searchFilterEnabled;
	}

	public void setSearchFilterEnabled(boolean searchFilterEnabled) {
		m_searchFilterEnabled = searchFilterEnabled;
	}

	@Nullable
	public DefaultButton getClearButton() {
		return m_clearButton;
	}

	@Nonnull public IFormBuilder getFormBuilder() {
		return Objects.requireNonNull(m_formBuilder);
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
		if(null == m_lookupClass)
			throw new NullPointerException("The LookupForm's 'lookupClass' cannot be null");
		return m_lookupClass;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Property/item builder.										*/
	/*----------------------------------------------------------------------*/

	@Nullable
	private LookupBuilder<T> m_currentBuilder;

	/**
	 * Start a builder adding a new lookup item to the form. The builder should be finished properly
	 * or an error occurs.
	 * <h2>Warning:</h2>
	 * <p>When adding lookup items manually, all metadata-added properties disappear. If you want to have the
	 * metadata-provided lookup items too then call addDefault() before or after the call(s) to this method.</p>
	 */
	public LookupBuilder<T> add() {
		if(m_currentBuilder != null)
			throw new IllegalStateException(this + ": The builder " + m_currentBuilder + " has not yet been finished");
		LookupBuilder item = new LookupBuilder(this);
		m_currentBuilder = item;
		return item;
	}

	@Nonnull
	<D> LookupLine<D> finishBuilder(LookupBuilder<T> builder) {
		if(m_currentBuilder != builder)
			throw new IllegalStateException(this + ": the item builder " + builder + " is not the 'current' item - call order problem");
		LookupLine<D> line = createLine(builder);
		m_itemList.add(line);
		forceRebuild();
		return line;
	}

	private <D> LookupLine<D> createLine(LookupBuilder<T> builder) {
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

		//-- Try to get a label
		NodeBase labelNode = builder.getLabelNode();
		if(null == labelNode) {
			String labelText = builder.getLabelText();
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

		LookupLine<D> ll = new LookupLine<>(control, qb, builder.getDefaultValue(), labelNode);
		addLookupLine(ll);
		return ll;
	}

	private SearchPropertyMetaModelImpl mergePropertyModels(LookupBuilder<T> builder, PropertyMetaModel<?> property) {
		SearchPropertyMetaModelImpl m = new SearchPropertyMetaModelImpl(getMetaModel(), MetaManager.parsePropertyPath(getMetaModel(), property.getName()));
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
			addMetadataProperty(spm);
		}
	}

	private <D> LookupLine<D> addMetadataProperty(SearchPropertyMetaModel spm) {
		PropertyMetaModel<?> property = m_metaModel.getProperty(spm.getPropertyName());
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

		LookupLine<D> ll = new LookupLine<>(control, qb, null, labelNode);
		addLookupLine(ll);
		return ll;
	}

	private <D> void addLookupLine(LookupLine<D> line) {
		m_itemList.add(line);
		forceRebuild();
	}

}

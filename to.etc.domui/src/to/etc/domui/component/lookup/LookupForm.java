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
package to.etc.domui.component.lookup;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.controlfactory.*;
import to.etc.domui.component.input.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.lookup.ILookupControlInstance.AppendCriteriaResult;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.themes.*;
import to.etc.domui.util.*;
import to.etc.webapp.*;
import to.etc.webapp.annotations.*;
import to.etc.webapp.query.*;

/**
 * Creates a search box to enter search criteria. This only presents the search part of the
 * form, constructed by metadata where needed, and the "search", "clear fields" and optional
 * "new" buttons. The actual searching must be done by the user of this component.
 * <p>The component will return a QCriteria query representing the search query constructed
 * by the user. This QCriteria object can, after retrieval, be used to add extra search
 * restrictions easily.</p>
 * <p>When used as-is, this form will use the class' metadata to discover any defined search
 * properties, and then populate the form with lookup controls which allow searches on those
 * properties. This is for "default" lookup screens. For more complex screens or lookup parts
 * that have controls interact with eachother you can manually define the contents of the
 * lookup form. By adding lookup items manually you <i>disable</i> the automatic discovery of
 * search options. This is proper because no form should <b>ever</b> depend on the content,
 * structure or order of metadata-defined lookup items!!! So if you want to manipulate the
 * lookup form's contents you have to define it's layout by hand.</p>
 * <p>Defining a form by hand is easy. To just add a property to search for to the form call
 * addProperty(String propname). This will create the default lookup input thing and label
 * for the property, as defined by metadata and factories. If you need more control you can
 * also call one of the addManualXXXX methods which allow full control over the controls
 * and search criteria used by the form.</p>
 * <p>Each search item added will usually return a LookupForm.Item. This is a handle to the
 * created lookup control and associated data and can be used to manipulate the control or
 * it's presentation at runtime.</p>
 * <p>The constructor for this control accepts an ellipsis list of property names to quickly
 * create a lookup using user-specified properties.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2008
 */
public class LookupForm<T> extends Div implements IButtonContainer {
	@Nullable
	private QCriteria<T> m_rootCriteria;

	/** The data class we're looking for */
	@Nonnull
	private Class<T> m_lookupClass;

	/** The metamodel for the class. */
	@Nonnull
	private ClassMetaModel m_metaModel;

	private String m_title;

	IClicked<LookupForm<T>> m_clicker;

	private IClicked<LookupForm<T>> m_onNew;

	private DefaultButton m_newBtn;

	private IClicked< ? extends LookupForm<T>> m_onClear;

	private IClicked<LookupForm<T>> m_onCancel;

	private DefaultButton m_cancelBtn;

	private DefaultButton m_collapseButton;

	private DefaultButton m_clearButton;

	public @Nullable
	DefaultButton getClearButton() {
		return m_clearButton;
	}

	private Table m_table;

	private TBody m_tbody;

	private Div m_content;

	private NodeContainer m_collapsedPanel;

	private NodeContainer m_buttonRow;

	private ControlBuilder m_builder;

	private ButtonFactory m_buttonFactory = new ButtonFactory(this);

	/**
	 * T in case that control is rendered as collapsed (meaning that search panel is hidden).
	 * It is usually used when lookup form have to popup with initial search results already shown.
	 */
	private boolean m_collapsed;

	/**
	 * Calculated by entered search criterias, T in case that exists any field resulting with {@link AppendCriteriaResult#VALID} in LookupForm fields.
	 */
	private boolean m_hasUserDefinedCriteria;

	/**
	 * After restore action on LookupForm.
	 */
	private IClicked<NodeBase> m_onAfterRestore;

	/**
	 * After collpase action on LookupForm.
	 */
	private IClicked<NodeBase> m_onAfterCollapse;

	private IQueryFactory<T> m_queryFactory;
	/**
	 * This is the definition for an Item to look up. A list of these
	 * will generate the actual lookup items on the screen, in the order
	 * specified by the item definition list.
	 *
	 * FIXME Should this actually be public??
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jul 31, 2009
	 */
	public static class Item implements SearchPropertyMetaModel {
		/**
		 * FIXME jal 20110221 I want to discuss this because I do not understand it's treestate nature.
		 *
		 * Determines behavior of inputs inside one lookup field definition. Used internally to persists state of inputs that is changed in runtime.
		 *
		 * @author <a href="mailto:imilovanovic@execom.eu">Igor Milovanović</a>
		 * Created on Feb 21, 2011
		 */
		enum InputBehaviorType {
			/**
			 * Unchanged behavior.
			 */
			DEFAULT,
			/**
			 * Force all input controls for certain lookup field to become enabled for user input.
			 */
			FORCE_ENABLED,
			/**
			 * Force all input controls for certain lookup field to become disabled for user input.
			 */
			FORCE_DISABLED;
		}

		private String m_propertyName;

		private List<PropertyMetaModel< ? >> m_propertyPath;

		private ILookupControlInstance m_instance;

		private boolean m_ignoreCase = true;

		private int m_minLength;

		private String m_labelText;

		private String m_lookupHint;

		private String m_errorLocation;

		private int m_order;

		private String testId;

		private InputBehaviorType m_inputsBehavior = InputBehaviorType.DEFAULT;

		@Override
		public String getPropertyName() {
			return m_propertyName;
		}

		public void setPropertyName(String propertyName) {
			m_propertyName = propertyName;
		}

		@Override
		public List<PropertyMetaModel< ? >> getPropertyPath() {
			return m_propertyPath;
		}

		public void setPropertyPath(List<PropertyMetaModel< ? >> propertyPath) {
			m_propertyPath = propertyPath;
		}

		public PropertyMetaModel< ? > getLastProperty() {
			if(m_propertyPath == null || m_propertyPath.size() == 0)
				return null;
			return m_propertyPath.get(m_propertyPath.size() - 1);
		}

		@Override
		public boolean isIgnoreCase() {
			return m_ignoreCase;
		}

		public void setIgnoreCase(boolean ignoreCase) {
			m_ignoreCase = ignoreCase;
		}

		@Override
		public int getMinLength() {
			return m_minLength;
		}

		public void setMinLength(int minLength) {
			m_minLength = minLength;
		}

		public String getLabelText() {
			return m_labelText;
		}

		public void setLabelText(String labelText) {
			m_labelText = labelText;
		}

		@Override
		public String getLookupLabel() {
			return m_labelText;
		}

		public String getErrorLocation() {
			return m_errorLocation;
		}

		public void setErrorLocation(String errorLocation) {
			m_errorLocation = errorLocation;
		}

		ILookupControlInstance getInstance() {
			return m_instance;
		}

		void setInstance(ILookupControlInstance instance) {
			m_instance = instance;
		}

		/**
		 * Unused; only present to satisfy the interface.
		 * @see to.etc.domui.component.meta.SearchPropertyMetaModel#getOrder()
		 */
		@Override
		public int getOrder() {
			return m_order;
		}

		void setOrder(int order) {
			m_order = order;
		}

		@Override
		public String getLookupHint() {
			return m_lookupHint;
		}

		public void setLookupHint(String lookupHint) {
			m_lookupHint = lookupHint;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Item:");
			if(m_propertyName != null) {
				sb.append(" property: ");
				sb.append(m_propertyName);
			}
			if(m_labelText != null) {
				sb.append(" label: ");
				sb.append(m_labelText);
			}
			return sb.toString();
		}

		public String getTestId() {
			return testId;
		}

		public void setTestId(String testId) {
			this.testId = testId;
		}

		public void setDisabled(boolean disabled) {
			m_inputsBehavior = disabled ? InputBehaviorType.FORCE_DISABLED : InputBehaviorType.FORCE_ENABLED;
			m_instance.setDisabled(disabled);
		}

		public boolean isForcedDisabled() {
			return m_inputsBehavior == InputBehaviorType.FORCE_DISABLED;
		}

		public boolean isForcedEnabled() {
			return m_inputsBehavior == InputBehaviorType.FORCE_ENABLED;
		}

		public void clear() {
			m_instance.clearInput();
		}
	}

	/**
	 * Sets rendering of search fields into two columns. It is in use only in case when search fields are loaded from metadata and loaded items count is bigger then one specified in m_twoColumnsModeMinimalItems.
	 */
	private boolean m_twoColumnsMode;

	/**
	 * Minimal number of items that would cause two column rendering. Always set with m_twoColumnsMode.
	 */
	private int m_minSizeForTwoColumnsMode;

	/**
	 * Item that is used internally by LookupForm to mark table break when creating search field components.
	 *
	 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
	 * Created on 13 Oct 2009
	 */
	static private class ItemBreak extends Item {
		public ItemBreak() {}
	}

	/** The primary list of defined lookup items. */
	private final List<Item> m_itemList = new ArrayList<Item>(20);

	static public enum ButtonMode {
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

	/** The list of buttons to show on the button row. */
	private List<ButtonRowItem> m_buttonItemList = Collections.EMPTY_LIST;

	public LookupForm(@Nonnull final Class<T> lookupClass, @GProperty String... propertyList) {
		this(lookupClass, (ClassMetaModel) null, propertyList);
	}

	/**
	 * Create a LookupForm to find instances of the specified class.
	 * @param lookupClass
	 */
	public LookupForm(@Nonnull final Class<T> lookupClass, @Nullable final ClassMetaModel cmm, String... propertyList) {
		m_rootCriteria = null;
		m_lookupClass = lookupClass;
		m_metaModel = cmm != null ? cmm : MetaManager.findClassMeta(lookupClass);
		m_builder = DomApplication.get().getControlBuilder();
		for(String prop : propertyList)
			addProperty(prop);
		defineDefaultButtons();
	}

	public LookupForm(@Nonnull QCriteria<T> rootCriteria, String... propertyList) {
		this(DomUtil.nullChecked(rootCriteria.getBaseClass()), (ClassMetaModel) null, propertyList);
		m_rootCriteria = rootCriteria;
	}

	/**
	 * Return the metamodel that this class uses to get it's data from.
	 * @return
	 */
	@Nonnull
	public ClassMetaModel getMetaModel() {
		return m_metaModel;
	}

	/**
	 * Returns the class whose instances we're looking up (a persistent class somehow).
	 * @return
	 */
	@Nonnull
	public Class<T> getLookupClass() {
		if(null == m_lookupClass)
			throw new NullPointerException("The LookupForm's 'lookupClass' cannot be null");
		return m_lookupClass;
	}

	/**
	 * Actually show the thingy.
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		//-- If a page title is present render the search block in a CaptionedPanel, else present in it;s own div.
		Div sroot = new Div();
		sroot.setCssClass("ui-lf-mainContent");
		if(getPageTitle() != null) {
			CaptionedPanel cp = new CaptionedPanel(getPageTitle(), sroot);
			add(cp);
			m_content = cp;
		} else {
			add(sroot);
			m_content = sroot;
		}

		//-- Ok, we need the items we're going to show now.
		if(m_itemList.size() == 0) // If we don't have an item set yet....
			setDefaultItems(); // ..define it from metadata, and abort if there is nothing there

		NodeContainer searchContainer = sroot;
		if(containsItemBreaks(m_itemList)) {
			Table searchRootTable = new Table();
			searchRootTable.setCssClass("ui-lf-multi");
			sroot.add(searchRootTable);
			TBody searchRootTableBody = new TBody();
			searchRootTable.add(searchRootTableBody);
			TR searchRootRow = new TR();
			searchRootTableBody.add(searchRootRow);
			TD searchRootCell = new TD();
			searchRootCell.setValign(TableVAlign.TOP);
			searchRootRow.add(searchRootCell);
			searchContainer = searchRootCell;
		}

		//-- Walk all search fields
		m_table = new Table();
		m_table.setCssClass("ui-lf-st");
		searchContainer.add(m_table);
		m_tbody = new TBody();
		m_tbody.setTestID("tableBodyLookupForm");
		m_table.add(m_tbody);

		//-- Start populating the lookup form with lookup items.
		for(Item it : m_itemList) {
			if(it instanceof ItemBreak) {
				TD anotherSearchRootCell = new TD();
				anotherSearchRootCell.setValign(TableVAlign.TOP);
				searchContainer.appendAfterMe(anotherSearchRootCell);
				searchContainer = anotherSearchRootCell;
				m_table = new Table();
				m_table.setCssClass("ui-lf-st");
				searchContainer.add(m_table);
				m_tbody = new TBody();
				m_tbody.setTestID("tableBodyLookupForm");
				m_table.add(m_tbody);
			} else {
				internalAddLookupItem(it);
			}
		}

		//-- The button bar.
		Div d = new Div();
		d.setTestID("buttonBar");
		d.setCssClass("ui-lf-ebb");
		sroot.add(d);
		m_buttonRow = d;

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
					m_clicker.clicked(LookupForm.this);
			}
		});
	}

	protected void defineDefaultButtons() {
		DefaultButton b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_SEARCH));
		b.setIcon("THEME/btnFind.png");
		b.setTestID("searchButton");
		b.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(final @Nonnull NodeBase bx) throws Exception {
				if(m_clicker != null)
					m_clicker.clicked(LookupForm.this);
			}
		});
		addButtonItem(b, 100, ButtonMode.NORMAL);

		m_clearButton = b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CLEAR));
		b.setIcon("THEME/btnClear.png");
		b.setTestID("clearButton");
		b.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(final @Nonnull NodeBase xb) throws Exception {
				clearInput();
				if(getOnClear() != null)
					((IClicked<LookupForm<T>>) getOnClear()).clicked(LookupForm.this); // FIXME Another generics snafu, fix.
			}
		});
		addButtonItem(b, 200, ButtonMode.NORMAL);

		//-- Collapse button thingy
		m_collapseButton = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_COLLAPSE), "THEME/btnHideLookup.png", new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton bx) throws Exception {
				collapse();
			}
		});
		m_collapseButton.setTestID("hideButton");
		addButtonItem(m_collapseButton, 300, ButtonMode.BOTH);
	}

	private boolean containsItemBreaks(List<Item> itemList) {
		for(Item item : itemList) {
			if(item instanceof ItemBreak) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This hides the search panel and adds a small div containing only the (optional) new and restore buttons.
	 * @throws Exception
	 */
	void collapse() throws Exception {
		if((m_content.getDisplay() == DisplayType.NONE))
			return;

		m_content.slideUp();
		m_collapsedPanel = new Div();
		m_collapsedPanel.setCssClass("ui-lf-coll");
		add(m_collapsedPanel);
		m_collapsed = true;

		//-- Collapse button thingy
		m_collapseButton.setText(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_RESTORE));
		m_collapseButton.setIcon("THEME/btnShowLookup.png");
		m_collapseButton.setClicked(new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton bx) throws Exception {
				restore();
			}
		});
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
		m_collapseButton.setClicked(new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton bx) throws Exception {
				collapse();
			}
		});

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
	 * This adds all properties that are defined as "search" properties in either this control or the metadata
	 * to the item list. The list is cleared before that!
	 */
	public void setDefaultItems() {
		m_itemList.clear();
		List<SearchPropertyMetaModel> list = getMetaModel().getSearchProperties();
		if(list == null || list.size() == 0) {
			list = MetaManager.calculateSearchProperties(getMetaModel()); // 20100416 jal EXPERIMENTAL
			if(list == null || list.size() == 0)
				throw new IllegalStateException(getMetaModel() + " has no search properties defined in it's meta data.");
		}
		setSearchProperties(list);
	}

	/**
	 * Set the search properties to use from a list of metadata properties.
	 * @param list
	 */
	public void setSearchProperties(List<SearchPropertyMetaModel> list) {
		int totalCount = list.size();
		for(SearchPropertyMetaModel sp : list) { // The list is already in ascending order, so just add items;
			Item it = new Item();
			it.setIgnoreCase(sp.isIgnoreCase());
			it.setMinLength(sp.getMinLength());
			it.setPropertyName(sp.getPropertyName());
			it.setPropertyPath(sp.getPropertyPath());
			it.setLabelText(sp.getLookupLabel()); // If a lookup label is defined use it.
			it.setLookupHint(sp.getLookupHint()); // If a lookup hint is defined use it.
			addAndFinish(it);
			if(m_twoColumnsMode && (totalCount >= m_minSizeForTwoColumnsMode) && m_itemList.size() == (totalCount + 1) / 2) {
				m_itemList.add(new ItemBreak());
			}
			updateUI(it);

		}
	}

	/**
	 * Add a property to look up to the list. The controls et al will be added using the factories.
	 * @param path		The property name (or path to some PARENT property) to search on, relative to the lookup class.
	 * @param minlen
	 * @param ignorecase
	 */
	public Item addProperty(String path, int minlen, boolean ignorecase) {
		return addProperty(path, null, minlen, Boolean.valueOf(ignorecase));
	}

	/**
	 * Add a property to look up to the list. The controls et al will be added using the factories.
	 * @param path		The property name (or path to some PARENT property) to search on, relative to the lookup class.
	 * @param minlen
	 */
	public Item addProperty(String path, int minlen) {
		return addProperty(path, null, minlen, null);
	}

	/**
	 * Add a property to look up to the list with user-specified label. The controls et al will be added using the factories.
	 * @param path	The property name (or path to some PARENT property) to search on, relative to the lookup class.
	 * @param label	The label text to use. Use the empty string to prevent a label from being generated. This still adds an empty cell for the label though.
	 */
	public Item addProperty(String path, String label) {
		return addProperty(path, label, 0, null);
	}

	/**
	 * Add a property to look up to the list. The controls et al will be added using the factories.
	 * @param path	The property name (or path to some PARENT property) to search on, relative to the lookup class.
	 */
	public Item addProperty(String path) {
		return addProperty(path, null, 0, null);
	}

	/**
	 * Add a property manually.
	 * @param path		The property name (or path to some PARENT property) to search on, relative to the lookup class.
	 * @param minlen
	 * @param ignorecase
	 */
	private Item addProperty(String path, String label, int minlen, Boolean ignorecase) {
		for(Item it : m_itemList) { // FIXME Useful?
			if(it.getPropertyName() != null && path.equals(it.getPropertyName())) // Already present there?
				throw new ProgrammerErrorException("The property " + path + " is already part of the search field list.");
		}

		//-- Define the item.
		Item it = new Item();
		it.setPropertyName(path);
		it.setLabelText(label);
		it.setIgnoreCase(ignorecase == null ? true : ignorecase.booleanValue());
		it.setMinLength(minlen);
		addAndFinish(it);
		updateUI(it);
		return it;
	}

	public void addItemBreak() {
		ItemBreak itemBreak = new ItemBreak();
		m_itemList.add(itemBreak);
	}

	/**
	 * Add a manually-created lookup control instance to the item list.
	 * @return
	 */
	public Item addManual(ILookupControlInstance lci) {
		Item it = new Item();
		it.setInstance(lci);
		addAndFinish(it);
		updateUI(it);
		return it;
	}

	/**
	 * Add a manually created control and link it to some property. The controls's configuration must be fully
	 * done by the caller; this will ask control factories to provide an ILookupControlInstance for the property
	 * and control passed in. The label for the lookup will come from property metadata.
	 *
	 * @param <X>
	 * @param property
	 * @param control
	 * @return
	 */
	public <VT, X extends NodeBase & IControl<VT>> Item addManual(String property, X control) {
		Item it = new Item();
		it.setPropertyName(property);
		addAndFinish(it);

		//-- Add the generic thingy
		ILookupControlFactory lcf = m_builder.getLookupQueryFactory(it, control);
		ILookupControlInstance qt = lcf.createControl(it, control);
		if(qt == null || qt.getInputControls() == null || qt.getInputControls().length == 0)
			throw new IllegalStateException("Lookup factory " + lcf + " did not link thenlookup thingy for property " + it.getPropertyName());
		it.setInstance(qt);
		updateUI(it);
		return it;
	}

	/**
	 * Add a manually-created lookup control instance with user-specified label to the item list.
	 * @return
	 */
	public Item addManualTextLabel(String labelText, ILookupControlInstance lci) {
		Item it = new Item();
		it.setInstance(lci);
		it.setLabelText(labelText);
		addAndFinish(it);
		updateUI(it);
		return it;
	}

	/**
	 * Adds a manually-defined control, and use the specified property as the source for it's default label.
	 * @param property
	 * @param lci
	 * @return
	 */
	public Item addManualPropertyLabel(String property, ILookupControlInstance lci) {
		PropertyMetaModel< ? > pmm = getMetaModel().findProperty(property);
		if(null == pmm)
			throw new ProgrammerErrorException(property + ": undefined property for class=" + getLookupClass());
		return addManualTextLabel(pmm.getDefaultLabel(), lci);
	}

	/**
	 * Add lookup control instance for search properties on child list (oneToMany relation)
	 * members. This adds a query by using the "exists" subquery for the child record. See
	 * <a href="http://www.domui.org/wiki/bin/view/Tutorial/QCriteriaRulez">QCriteria rules</a> for
	 * details.
	 *
	 * @param propPath
	 * 		Must be <b>parentprop.childprop</b> dotted form. Label is used from parent property meta.
	 */
	public Item addChildProperty(String propPath) {
		return addChildPropertyLabel(null, propPath);
	}


	/**
	 * Add lookup control instance for search properties on child list (oneToMany relation)
	 * members. This adds a query by using the "exists" subquery for the child record. See
	 * <a href="http://www.domui.org/wiki/bin/view/Tutorial/QCriteriaRulez">QCriteria rules</a> for
	 * details.
	 * @param label
	 * 		Label that is displayed. If null, default label from parent property meta is used.
	 * @param propPath
	 * 		Must be <b>parentprop.childprop</b> dotted form.
	 */
	public Item addChildPropertyLabel(String label, String propPath) {

		final List<PropertyMetaModel< ? >> pl = MetaManager.parsePropertyPath(m_metaModel, propPath);

		if(pl.size() != 2) {
			throw new ProgrammerErrorException("Property path does not contain parent.child path: " + propPath);
		}

		final PropertyMetaModel< ? > parentPmm = pl.get(0);
		final PropertyMetaModel< ? > childPmm = pl.get(1);

		SearchPropertyMetaModelImpl spmm = new SearchPropertyMetaModelImpl(m_metaModel);
		spmm.setPropertyName(childPmm.getName());
		spmm.setPropertyPath(pl);

		ILookupControlFactory lcf = m_builder.getLookupControlFactory(spmm);
		final ILookupControlInstance lookupInstance = lcf.createControl(spmm, null);

		AbstractLookupControlImpl thingy = new AbstractLookupControlImpl(lookupInstance.getInputControls()) {
			@Override
			public @Nonnull AppendCriteriaResult appendCriteria(@Nonnull QCriteria< ? > crit) throws Exception {

				QCriteria< ? > r = QCriteria.create(childPmm.getClassModel().getActualClass());
				AppendCriteriaResult subRes = lookupInstance.appendCriteria(r);

				if(subRes == AppendCriteriaResult.INVALID) {
					return subRes;
				} else if(r.hasRestrictions()) {
					QRestrictor< ? > exists = crit.exists(childPmm.getClassModel().getActualClass(), parentPmm.getName());
					exists.setRestrictions(r.getRestrictions());
					return AppendCriteriaResult.VALID;
				} else {
					return AppendCriteriaResult.EMPTY;
				}
			}

			@Override
			public void clearInput() {
				lookupInstance.clearInput();
			}
		};

		return this.addManualTextLabel(label == null ? parentPmm.getDefaultLabel() : label, thingy);
	}

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
	private void addAndFinish(Item it) {
		m_itemList.add(it);

		//-- 1. If a property name is present but the path is unknown calculate the path
		if(it.getPropertyPath() == null && it.getPropertyName() != null && it.getPropertyName().length() > 0) {
			List<PropertyMetaModel< ? >> pl = MetaManager.parsePropertyPath(getMetaModel(), it.getPropertyName());
			if(pl.size() == 0)
				throw new ProgrammerErrorException("Unknown/unresolvable lookup property " + it.getPropertyName() + " on class=" + getLookupClass());
			it.setPropertyPath(pl);
		}

		//-- 2. Calculate/determine a label text if empty from metadata, else ignore
		PropertyMetaModel< ? > pmm = MetaUtils.findLastProperty(it); // Try to get metamodel
		if(it.getLabelText() == null) {
			if(pmm == null)
				it.setLabelText(it.getPropertyName()); // Last resort: default to property name if available
			else
				it.setLabelText(pmm.getDefaultLabel());
		}

		//-- 3. Calculate a default hint
		if(it.getLookupHint() == null) {
			if(pmm != null)
				it.setLookupHint(pmm.getDefaultHint());
		}

		//-- 4. Set an errorLocation
		if(it.getErrorLocation() == null) {
			it.setErrorLocation(it.getLabelText());
		}

	}

	private void updateUI(@Nonnull Item it) {
		//-- jal 20130528 This component quite sucks balls- the interface is not able to add on-the-fly.
		if(m_tbody != null)
			internalAddLookupItem(it);
	}


	/**
	 * Create the lookup item, depending on it's kind.
	 * @param it
	 */
	private void internalAddLookupItem(Item it) {
		if(it.getInstance() == null) {
			//-- Create everything using a control creation factory,
			ILookupControlInstance lci = createControlFor(it);
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
	private void addItemToTable(Item it) {
		ILookupControlInstance qt = it.getInstance();

		//-- Create control && label cells,
		TR tr = new TR();
		m_tbody.add(tr);
		TD lcell = new TD(); // Label cell
		tr.add(lcell);
		lcell.setCssClass("ui-f-lbl");

		TD ccell = new TD(); // Control cell
		tr.add(ccell);
		ccell.setCssClass("ui-f-in");

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

	private void assignCalcTestID(@Nonnull Item item, @Nonnull NodeBase b) {
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
	 * Create the optimal control using metadata for a property. This can only be called for an item
	 * containing a property with metadata.
	 *
	 * @param container
	 * @param name
	 * @param pmm
	 * @return
	 */
	private ILookupControlInstance createControlFor(Item it) {
		PropertyMetaModel< ? > pmm = it.getLastProperty();
		if(pmm == null)
			throw new IllegalStateException("property cannot be null when creating using factory.");
		ILookupControlFactory lcf = m_builder.getLookupControlFactory(it);
		ILookupControlInstance qt = lcf.createControl(it, null);
		if(qt == null || qt.getInputControls() == null || qt.getInputControls().length == 0)
			throw new IllegalStateException("Lookup factory " + lcf + " did not create a lookup thingy for property " + it.getPropertyName());
		return qt;
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
		if(getQueryFactory() != null) {
			root = getQueryFactory().createQuery();
		} else {
			root = (QCriteria<T>) getMetaModel().createCriteria();
			QCriteria<T> rootCriteria = m_rootCriteria;
			if(null != rootCriteria)
				root.mergeCriteria(rootCriteria);
		}
		boolean success = true;
		for(Item it : m_itemList) {
			ILookupControlInstance li = it.getInstance();
			if(li != null) { // FIXME Is it reasonable to allow null here?? Should we not abort?
				AppendCriteriaResult res = li.appendCriteria(root);
				if(res == AppendCriteriaResult.INVALID) {
					success = false;
				} else if(res == AppendCriteriaResult.VALID) {
					m_hasUserDefinedCriteria = true;
				}
			}
		}
		if(!success) { // Some input failed to validate their input criteria?
			m_hasUserDefinedCriteria = false;
			return null; // Then exit null -> should only display errors.
		}
		return root;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Silly and small methods.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Tells all input items to clear their content, clearing all user choices from the form. After
	 * this call, the form should return an empty QCriteria without any restrictions.
	 */
	public void clearInput() {
		for(Item it : m_itemList) {
			if(it.getInstance() != null)
				it.getInstance().clearInput();
		}
	}

	/**
	 * Sets the onNew handler. When set this will render a "new" button in the form's button bar.
	 * @return
	 */
	public IClicked<LookupForm<T>> getOnNew() {
		return m_onNew;
	}

	/**
	 * Returns the onNew handler. When set this will render a "new" button in the form's button bar.
	 * @param onNew
	 */
	public void setOnNew(final IClicked<LookupForm<T>> onNew) {
		if(m_onNew != onNew) {
			m_onNew = onNew;
			if(m_onNew != null && m_newBtn == null) {
				m_newBtn = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_NEW));
				m_newBtn.setIcon("THEME/btnNew.png");
				m_newBtn.setTestID("newButton");
				m_newBtn.setClicked(new IClicked<NodeBase>() {
					@Override
					public void clicked(final @Nonnull NodeBase xb) throws Exception {
						if(getOnNew() != null) {
							getOnNew().clicked(LookupForm.this);
						}
					}
				});
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
	 * Returns the search block's part title, if present. Returns null if the title is not set.
	 */
	public String getPageTitle() {
		return m_title;
	}

	/**
	 * Sets a part title for this search block. When unset the search block does not have a title, when set
	 * the search block will be shown inside a CaptionedPanel.
	 * @param title
	 */
	public void setPageTitle(final String title) {
		m_title = title;
	}

	/**
	 * Set the handler to call when the "Search" button is clicked.
	 * @see to.etc.domui.dom.html.NodeBase#setClicked(to.etc.domui.dom.html.IClicked)
	 */
	@Override
	public void setClicked(final @Nullable IClickBase< ? > clicked) {
		m_clicker = (IClicked<LookupForm<T>>) clicked;
	}

	public IClicked<LookupForm<T>> getSearchClicked() {
		return m_clicker;
	}

	public IClicked< ? extends LookupForm<T>> getOnClear() {
		return m_onClear;
	}

	/**
	 * Listener to call when the "clear" button is pressed.
	 * @param onClear
	 */
	public void setOnClear(IClicked< ? extends LookupForm<T>> onClear) {
		m_onClear = onClear;
	}

	/**
	 * When set, this causes a "cancel" button to be added to the form. When that button is pressed this handler gets called.
	 * @param onCancel
	 */
	public void setOnCancel(IClicked<LookupForm<T>> onCancel) {
		if(m_onCancel != onCancel) {
			m_onCancel = onCancel;
			if(m_onCancel != null && m_cancelBtn == null) {
				m_cancelBtn = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CANCEL));
				m_cancelBtn.setIcon(Theme.BTN_CANCEL);
				m_cancelBtn.setTestID("cancelButton");
				m_cancelBtn.setClicked(new IClicked<NodeBase>() {
					@Override
					public void clicked(final @Nonnull NodeBase xb) throws Exception {

						if(getOnCancel() != null) {
							getOnCancel().clicked(LookupForm.this);
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

	public IClicked<LookupForm<T>> getOnCancel() {
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
			m_buttonItemList = new ArrayList<ButtonRowItem>(10);
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
	 * Sets rendering of search fields into two columns. It is in use only in case when search fields are loaded from metadata and search fields count reach minSizeForTwoColumnsMode value.
	 * @param minSizeForTwoColumnsMode
	 */
	public void setTwoColumnsMode(int minSizeForTwoColumnsMode) {
		m_twoColumnsMode = true;
		m_minSizeForTwoColumnsMode = minSizeForTwoColumnsMode;
	}

	/**
	 * Method {@link LookupForm#getEnteredCriteria} MUST BE EXECUTED BEFORE checking for this property value!
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
	 * Use to collapse/restore LookupForm search pannel.
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
	 * @see to.etc.domui.component.layout.IButtonContainer#addButton(to.etc.domui.dom.html.NodeBase, int)
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


}

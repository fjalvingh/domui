package to.etc.domui.component.lookup;

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.form.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.webapp.*;
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
public class LookupForm<T> extends Div {
	/** The data class we're looking for */
	private Class<T> m_lookupClass;

	private String m_title;

	IClicked<LookupForm<T>> m_clicker;

	IClicked<LookupForm<T>> m_onNew;

	IClicked<LookupForm<T>> m_onClear;

	IClicked<LookupForm<T>> m_onCancel;

	private Table m_table;

	private TBody m_tbody;

	private Div m_content;

	private NodeContainer m_collapsed;

	private ControlBuilder m_builder;

	/**
	 * This is the definition for an Item to look up. A list of these
	 * will generate the actual lookup items on the screen, in the order
	 * specified by the item definition list.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jul 31, 2009
	 */
	public static class Item implements SearchPropertyMetaModel {
		private String m_propertyName;

		private List<PropertyMetaModel> m_propertyPath;

		//		private PropertyMetaModel m_property;

		private ILookupControlInstance m_instance;

		private boolean m_ignoreCase = true;

		private int m_minLength;

		private String m_labelText;

		private Label m_label;

		private int m_order;

		//		public PropertyMetaModel getProperty() {
		//			return m_property;
		//		}
		//
		//		void setProperty(PropertyMetaModel property) {
		//			m_property = property;
		//		}

		public String getPropertyName() {
			return m_propertyName;
		}

		public void setPropertyName(String propertyName) {
			m_propertyName = propertyName;
		}

		public List<PropertyMetaModel> getPropertyPath() {
			return m_propertyPath;
		}

		public void setPropertyPath(List<PropertyMetaModel> propertyPath) {
			m_propertyPath = propertyPath;
		}

		public PropertyMetaModel getLastProperty() {
			if(m_propertyPath == null || m_propertyPath.size() == 0)
				return null;
			return m_propertyPath.get(m_propertyPath.size() - 1);
		}

		public boolean isIgnoreCase() {
			return m_ignoreCase;
		}

		public void setIgnoreCase(boolean ignoreCase) {
			m_ignoreCase = ignoreCase;
		}

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

		public String getLookupLabel() {
			return m_labelText;
		}

		public Label getLabel() {
			return m_label;
		}

		public void setLabel(Label label) {
			m_label = label;
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
		public int getOrder() {
			return m_order;
		}

		void setOrder(int order) {
			m_order = order;
		}
	}

	/** The primary list of defined lookup items. */
	private List<Item> m_itemList = new ArrayList<Item>(20);

	/**
	 * Create a LookupForm to find instances of the specified class.
	 * @param lookupClass
	 */
	public LookupForm(final Class<T> lookupClass, String... propertyList) {
		m_builder = DomApplication.get().getControlBuilder();
		m_lookupClass = lookupClass;
		for(String prop : propertyList)
			addProperty(prop);
	}

	/**
	 * Actually show the thingy.
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		//-- If a page title is present render the search block in a CaptionedPanel, else present in it;s own div.
		Div sroot = new Div();
		if(getPageTitle() != null) {
			CaptionedPanel cp = new CaptionedPanel(getPageTitle(), sroot);
			add(cp);
			m_content = cp;
		} else {
			add(sroot);
			m_content = sroot;
		}

		//-- Walk all search fields
		m_table = new Table();
		sroot.add(m_table);
		m_tbody = new TBody();
		m_tbody.setTestID("tableBodyLookupForm");
		m_table.add(m_tbody);

		//-- Ok, we need the items we're going to show now.
		if(m_itemList.size() == 0) // If we don't have an item set yet....
			setItems(); // ..define it from metadata, and abort if there is nothing there

		//-- Start populating the lookup form with lookup items.
		for(Item it : m_itemList) {
			internalAddLookupItem(it);
		}

		//-- The button bar.
		Div d = new Div();
		d.setTestID("buttonBar");
		sroot.add(d);

		DefaultButton b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_SEARCH));
		d.add(b);
		b.setIcon("THEME/btnFind.png");
		b.setTestID("searchButton");
		b.setClicked(new IClicked<NodeBase>() {
			public void clicked(final NodeBase bx) throws Exception {
				if(m_clicker != null)
					m_clicker.clicked(LookupForm.this);
			}
		});

		b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CLEAR));
		d.add(b);
		b.setIcon("THEME/btnClear.png");
		b.setTestID("clearButton");
		b.setClicked(new IClicked<NodeBase>() {
			public void clicked(final NodeBase xb) throws Exception {
				clearInput();
				if(getOnClear() != null)
					getOnClear().clicked(LookupForm.this);
			}
		});

		if(getOnNew() != null) {
			b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_NEW));
			d.add(b);
			b.setIcon("THEME/btnNew.png");
			b.setTestID("newButton");
			b.setClicked(new IClicked<NodeBase>() {
				public void clicked(final NodeBase xb) throws Exception {
					getOnNew().clicked(LookupForm.this);
				}
			});
		}
		if(null != getOnCancel()) {
			b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CANCEL));
			d.add(b);
			b.setIcon("THEME/btnCancel.png");
			b.setTestID("cancelButton");
			b.setClicked(new IClicked<NodeBase>() {
				public void clicked(final NodeBase xb) throws Exception {

					if(getOnCancel() != null) {
						getOnCancel().clicked(LookupForm.this);
					}
				}
			});
		}

		//-- Collapse button thingy
		b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_COLLAPSE), "THEME/btnHideLookup.png", new IClicked<DefaultButton>() {
			public void clicked(DefaultButton bx) throws Exception {
				collapse();
			}
		});
		b.setTestID("hideButton");
		d.add(b);

		//-- Add a RETURN PRESSED handler to allow pressing RETURN on search fields.
		setReturnPressed(new IReturnPressed() {
			public void returnPressed(final Div node) throws Exception {
				if(m_clicker != null)
					m_clicker.clicked(LookupForm.this);
			}
		});
	}

	/**
	 * This hides the search panel and adds a small div containing only the (optional) new and restore buttons.
	 */
	void collapse() {
		if(m_content.getDisplay() == DisplayType.NONE)
			return;
		//		appendJavascript("$('#" + m_content.getActualID() + "').slideUp();");
		m_content.slideUp();

		//		m_content.setDisplay(DisplayType.NONE);
		m_collapsed = new Div();
		m_collapsed.setCssClass("ui-lf-coll");

		add(m_collapsed);

		if(getOnNew() != null) {
			DefaultButton b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_NEW));
			b.setTestID("newButtonCollapsed");
			m_collapsed.add(b);
			b.setIcon("THEME/btnNew.png");
			b.setClicked(new IClicked<NodeBase>() {
				public void clicked(final NodeBase xb) throws Exception {
					getOnNew().clicked(LookupForm.this);
				}
			});
		}
		if(null != getOnCancel()) {
			DefaultButton b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_CANCEL));
			b.setTestID("cancelButtonCollapsed");
			m_collapsed.add(b);
			b.setIcon("THEME/btnCancel.png");
			b.setClicked(new IClicked<NodeBase>() {
				public void clicked(final NodeBase xb) throws Exception {

					if(getOnCancel() != null) {
						getOnCancel().clicked(LookupForm.this);
					}
				}
			});
		}

		//-- Collapse button thingy
		DefaultButton b = new DefaultButton(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_RESTORE), "THEME/btnHideLookup.png", new IClicked<DefaultButton>() {
			public void clicked(DefaultButton bx) throws Exception {
				restore();
			}
		});
		b.setTestID("restoreButtonCollapsed");
		m_collapsed.add(b);
	}

	void restore() {
		if(m_collapsed == null)
			return;
		m_collapsed.remove();
		m_collapsed = null;
		m_content.setDisplay(DisplayType.BLOCK);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Altering/defining the lookup items.					*/
	/*--------------------------------------------------------------*/
	/**
	 * This adds all properties that are defined as "search" properties in the metadata
	 * to the item list. The list is cleared before that!
	 */
	private void setItems() {
		m_itemList.clear();
		ClassMetaModel cm = MetaManager.findClassMeta(getLookupClass());
		List<SearchPropertyMetaModelImpl> list = cm.getSearchProperties();
		if(list == null || list.size() == 0)
			throw new IllegalStateException("The class " + m_lookupClass + " has no search properties defined in it's meta data.");

		for(SearchPropertyMetaModel sp : list) { // The list is already in ascending order, so just add items;
			Item it = new Item();
			it.setIgnoreCase(sp.isIgnoreCase());
			it.setMinLength(sp.getMinLength());
			it.setPropertyName(sp.getPropertyName());
			it.setLabelText(sp.getLookupLabel()); // If a lookup label is defined use it.
			//			it.setProperty(sp.getProperty());
			m_itemList.add(it);
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

		//-- Get the property from the metadata
		ClassMetaModel cm = MetaManager.findClassMeta(getLookupClass());
		List<PropertyMetaModel> pl = MetaManager.parsePropertyPath(cm, path);
		if(pl.size() == 0)
			throw new ProgrammerErrorException("Unknown/unresolvable lookup property " + path + " on class=" + getLookupClass());

		//-- Define the item.
		Item it = new Item();
		it.setPropertyName(path);
		it.setPropertyPath(pl);
		if(label != null) {
			it.setLabelText(label);
		}
		if(ignorecase != null)
			it.setIgnoreCase(ignorecase.booleanValue());
		it.setMinLength(minlen);
		m_itemList.add(it);
		return it;
	}

	/**
	 * Add a manually-created lookup control instance to the item list.
	 * @return
	 */
	public Item addManual(ILookupControlInstance lci) {
		Item it = new Item();
		it.setInstance(lci);
		m_itemList.add(it);
		return it;
	}

	/**
	 * Add a manually-created lookup control instance with user-specified label to the item list.
	 * @return
	 */
	public Item addManualTextLabel(String labelText, ILookupControlInstance lci) {
		Item it = addManual(lci);
		it.setLabelText(labelText);
		return it;
	}

	/**
	 * Adds a manually-defined control, and use the specified property as the source for it's default label.
	 * @param property
	 * @param lci
	 * @return
	 */
	public Item addManualPropertyLabel(String property, ILookupControlInstance lci) {
		ClassMetaModel cm = MetaManager.findClassMeta(getLookupClass());
		PropertyMetaModel pmm = cm.findProperty(property);
		if(null == pmm)
			throw new ProgrammerErrorException(property + ": undefined property for class=" + getLookupClass());
		return addManualTextLabel(pmm.getDefaultLabel(), lci);
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
	 * Create the lookup item, depending on it's kind.
	 * @param it
	 */
	private void internalAddLookupItem(Item it) {
		if(it.getInstance() == null && it.getPropertyPath() != null && it.getPropertyPath().size() > 0) {
			//-- Create everything using a control creation factory,
			ILookupControlInstance lci = createControlFor(it);
			if(lci == null)
				return;
			it.setInstance(lci);
		}
		if(it.getInstance() == null)
			throw new IllegalStateException("No idea how to create a lookup control for " + it);

		//-- Assign error locations to all input controls
		String location = calculateLocation(it);
		if(location != null && location.trim().length() > 0) {
			for(NodeBase ic : it.getInstance().getInputControls())
				ic.setErrorLocation(location);
		}

		//-- Add the visual presentation.
		addItemToTable(it);
	}

	private String calculateLocation(Item it) {
		if(it.getLabelText() != null)
			return it.getLabelText();
		if(it.getPropertyPath() != null && it.getPropertyPath().size() > 0) {
			return it.getPropertyPath().get(it.getPropertyPath().size() - 1).getDefaultLabel();
		}
		return null;
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
			if(labelcontrol == null && b instanceof IInputNode< ? >)
				labelcontrol = b;
		}
		if(labelcontrol == null)
			labelcontrol = qt.getInputControls()[0];

		//-- Finally: add the label in some way...
		Label l = it.getLabel(); // Label provided by caller?
		if(l == null) {
			//-- Text provided?
			if(it.getLabelText() != null) {
				if(it.getLabelText().length() > 0)
					l = new Label(labelcontrol, it.getLabelText());
			} else {
				//-- No default label set. Do we have metadata?
				if(it.getPropertyPath() != null && it.getPropertyPath().size() > 0) {
					String dl = it.getPropertyPath().get(it.getPropertyPath().size() - 1).getDefaultLabel();

					if(dl != null && dl.length() > 0)
						l = new Label(labelcontrol, dl);
				}
			}
		}
		if(l != null) {
			if(l.getForNode() == null)
				l.setForNode(labelcontrol);
			lcell.add(l);
		}
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
		PropertyMetaModel pmm = it.getLastProperty();
		if(pmm == null)
			throw new IllegalStateException("property cannot be null when creating using factory.");
		IRequestContext rq = PageContext.getRequestContext();
		boolean viewable = true;
		boolean editable = true;

		viewable = MetaManager.isAccessAllowed(pmm.getViewRoles(), rq);
		editable = MetaManager.isAccessAllowed(pmm.getEditRoles(), rq);
		if(!viewable) {
			//-- Check edit stuff:
			if(pmm.getEditRoles() == null) // No edit roles at all -> exit
				return null;
			if(!editable)
				return null;
		}

		LookupControlFactory lcf = m_builder.getLookupControlFactory(pmm);
		ILookupControlInstance qt = lcf.createControl(it, it.getProperty());
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
	public QCriteria<T> getEnteredCriteria() throws Exception {
		QCriteria<T> root = QCriteria.create(m_lookupClass);
		boolean success = true;
		for(Item it : m_itemList) {
			ILookupControlInstance li = it.getInstance();
			if(li != null) { // FIXME Is it reasonable to allow null here?? Should we not abort?
				if(!li.appendCriteria(root))
					success = false;
			}
		}
		if(!success) // Some input failed to validate their input criteria?
			return null; // Then exit null -> should only display errors.
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
		m_onNew = onNew;
		forceRebuild();
	}

	/**
	 * Returns the class whose instances we're looking up (a persistent class somehow).
	 * @return
	 */
	public Class<T> getLookupClass() {
		if(null == m_lookupClass)
			throw new NullPointerException("The LookupForm's 'lookupClass' cannot be null");
		return m_lookupClass;
	}

	/**
	 * Change the class for which we are searching. This clear ALL definitions!
	 * @param lookupClass
	 */
	public void setLookupClass(final Class<T> lookupClass) {
		if(m_lookupClass == lookupClass)
			return;
		m_lookupClass = lookupClass;
		reset();
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
	public void setClicked(final IClicked< ? > clicked) {
		m_clicker = (IClicked<LookupForm<T>>) clicked;
	}

	public IClicked<LookupForm<T>> getOnClear() {
		return m_onClear;
	}

	/**
	 * Listener to call when the "clear" button is pressed.
	 * @param onClear
	 */
	public void setOnClear(IClicked<LookupForm<T>> onClear) {
		m_onClear = onClear;
	}

	/**
	 * When set, this causes a "cancel" button to be added to the form. When that button is pressed this handler gets called.
	 * @param onCancel
	 */
	public void setOnCancel(IClicked<LookupForm<T>> onCancel) {
		m_onCancel = onCancel;
	}

	public IClicked<LookupForm<T>> getOnCancel() {
		return m_onCancel;
	}
}

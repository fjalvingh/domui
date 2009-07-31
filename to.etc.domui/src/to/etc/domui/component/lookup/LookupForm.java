package to.etc.domui.component.lookup;

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.webapp.*;
import to.etc.webapp.query.*;

/**
 * Contains a search box to enter search criteria; the result of
 * the search is shown in a pageable list. The user can select
 * a single entry in the list; this selection becomes the value
 * of this form.
 * This form mostly gets it's data from metadata for a given
 * record type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2008
 */
public class LookupForm<T> extends Div {
	/** The data class we're looking for */
	private Class<T> m_lookupClass;

	private List<SearchPropertyMetaModel> m_searchProperties;

	//	private List<DisplayPropertyMetaModel> m_displayProperties;

	private String m_title;

	IClicked<LookupForm<T>> m_clicker;

	IClicked<LookupForm<T>> m_onNew;

	private Table m_table;

	/** The list of actual control instances on the form, when built. */
	private List<ILookupControlInstance> m_queryBuilder = Collections.EMPTY_LIST;

	private TBody m_tbody;

	private Map<Integer, CustomSearchField> insertCustomSearchControl;

	private List<CustomSearchField> addCustomSearchControl;

	private Map<String, CustomSearchField> overwriteDefaultSearchControl;

	/**
	 * This is the definition for an Item to look up. A list of these
	 * will generate the actual lookup items on the screen, in the order
	 * specified by the item definition list.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jul 31, 2009
	 */
	public static class Item {
		private PropertyMetaModel m_property;

		private ILookupControlInstance m_instance;

		private boolean m_ignoreCase = true;

		private int m_minLength;

		private String m_labelText;

		private Label m_label;

		public PropertyMetaModel getProperty() {
			return m_property;
		}

		void setProperty(PropertyMetaModel property) {
			m_property = property;
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
	}

	/** The primary list of defined lookup items. */
	private List<Item> m_itemList = new ArrayList<Item>(20);

	/**
	 * Create a LookupForm to find instances of the specified class.
	 * @param lookupClass
	 */
	public LookupForm(final Class<T> lookupClass, String... propertyList) {
		m_lookupClass = lookupClass;
		//		for(String prop : propertyList)
		//			addLookupProperty(prop);
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
		} else {
			add(sroot);
		}

		//-- Walk all search fields
		m_table = new Table();
		sroot.add(m_table);
		m_tbody = new TBody();
		m_table.add(m_tbody);

		//-- Ok, we need the items we're going to show now.
		if(m_itemList.size() == 0) // If we don't have an item set yet....
			setItems(); // ..define it from metadata, and abort if there is nothing there

		//-- Start populating the lookup form with lookup items.
		for(Item it : m_itemList) {
			internalAddLookupItem(it);
		}
		//		int currentSearchIndex = 1;
		//
		//		for(SearchPropertyMetaModel sm : list) {
		//			CustomSearchField insertCustomField = (insertCustomSearchControl != null) ? insertCustomSearchControl.get(new Integer(currentSearchIndex)) : null;
		//			while(insertCustomField != null) {
		//				addLookupFieldQueryBuilderForProperty(insertCustomField.getLabelCaption(), insertCustomField.getQueryBuilderThingy());
		//				currentSearchIndex++;
		//				insertCustomField = insertCustomSearchControl.get(new Integer(currentSearchIndex));
		//			}
		//
		//			if(overwriteDefaultSearchControl != null && overwriteDefaultSearchControl.containsKey(sm.getProperty().getName())) {
		//				CustomSearchField replaceCustomField = overwriteDefaultSearchControl.get(sm.getProperty().getName());
		//				if(replaceCustomField != null) {
		//					addLookupFieldQueryBuilderForProperty(replaceCustomField.getLabelCaption(), replaceCustomField.getQueryBuilderThingy());
		//					currentSearchIndex++;
		//				}
		//			} else {
		//				addPropertyControl(sm.getProperty().getName(), sm.getProperty().getDefaultLabel(NlsContext.getLocale()), sm.getProperty(), sm);
		//				currentSearchIndex++;
		//			}
		//		}

		if(addCustomSearchControl != null) {
			for(CustomSearchField addCustomField : addCustomSearchControl) {
				addItemToTable(addCustomField.getLabelCaption(), addCustomField.getQueryBuilderThingy());
			}
		}

		//-- The button bar.
		Div d = new Div();
		sroot.add(d);

		DefaultButton b = new DefaultButton("!Zoeken");
		d.add(b);
		b.setIcon("THEME/btnFind.png");
		b.setClicked(new IClicked<NodeBase>() {
			public void clicked(final NodeBase bx) throws Exception {
				if(m_clicker != null)
					m_clicker.clicked(LookupForm.this);
			}
		});

		b = new DefaultButton("!Wis");
		d.add(b);
		b.setIcon("THEME/btnClear.png");
		b.setClicked(new IClicked<NodeBase>() {
			public void clicked(final NodeBase xb) throws Exception {
				clearInput();
			}
		});

		if(getOnNew() != null) {
			b = new DefaultButton("!Toevoegen");
			d.add(b);
			b.setIcon("THEME/btnNew.png");
			b.setClicked(new IClicked<NodeBase>() {
				public void clicked(final NodeBase xb) throws Exception {
					getOnNew().clicked(LookupForm.this);
				}
			});
		}

		//-- Add a RETURN PRESSED handler to allow pressing RETURN on search fields.
		setReturnPressed(new IReturnPressed() {
			public void returnPressed(final Div node) throws Exception {
				if(m_clicker != null)
					m_clicker.clicked(LookupForm.this);
			}
		});
		//
		//		SmallImgButton	sb	= new SmallImgButton(true, "btnClear.png");
		//		d.add(sb);
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
		List<SearchPropertyMetaModel> list = cm.getSearchProperties();
		if(list == null || list.size() == 0)
			throw new IllegalStateException("The class " + m_lookupClass + " has no search properties defined in it's meta data.");

		for(SearchPropertyMetaModel sp : list) { // The list is already in ascending order, so just add items;
			Item it = new Item();
			it.setIgnoreCase(sp.isIgnoreCase());
			it.setMinLength(sp.getMinLength());
			it.setProperty(sp.getProperty());
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
		return addProperty(path, minlen, Boolean.valueOf(ignorecase));
	}

	/**
	 * Add a property to look up to the list. The controls et al will be added using the factories.
	 * @param path		The property name (or path to some PARENT property) to search on, relative to the lookup class.
	 * @param minlen
	 */
	public Item addProperty(String path, int minlen) {
		return addProperty(path, minlen, null);
	}

	/**
	 * Add a property to look up to the list. The controls et al will be added using the factories.
	 * @param path	The property name (or path to some PARENT property) to search on, relative to the lookup class.
	 */
	public Item addProperty(String path) {
		return addProperty(path, 0, null);
	}

	/**
	 * Add a property manually.
	 * @param path		The property name (or path to some PARENT property) to search on, relative to the lookup class.
	 * @param minlen
	 * @param ignorecase
	 */
	private Item addProperty(String path, int minlen, Boolean ignorecase) {
		for(Item it : m_itemList) { // FIXME Useful?
			if(it.getProperty() != null && path.equals(it.getProperty().getName())) // Already present there?
				throw new ProgrammerErrorException("The property " + path + " is already part of the search field list.");
		}

		//-- Get the property from the metadata
		ClassMetaModel cm = MetaManager.findClassMeta(getLookupClass());
		PropertyMetaModel pmm = cm.findProperty(path);
		if(pmm == null)
			throw new ProgrammerErrorException("Unknown property " + path + " on class=" + getLookupClass());

		//-- Define the item.
		Item	it	= new Item();
		it.setProperty(pmm);
		if(ignorecase != null)
			it.setIgnoreCase(ignorecase.booleanValue());
		it.setMinLength(minlen);
		m_itemList.add(it);
		return it;
	}

	/**
	 * Add a manually-created lookup control to the item list.
	 * @return
	 */
	public Item addManualControl(ILookupControlInstance lci) {
		Item it = new Item();
		it.setInstance(lci);
		m_itemList.add(it);
		return it;
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
		if(it.getProperty() != null) {
			//-- Create everything using a control creation factory,
			ILookupControlInstance lci = createControlFor(it);
			if(lci == null)
				return;
			it.setInstance(lci);
		}
		if(it.getInstance() == null)
			throw new IllegalStateException("No idea how to create a lookup control for " + it);

		//-- Add the visual presentation.




	}

	//	public void addPropertyControl(final String name, final String label, final PropertyMetaModel pmm, final SearchPropertyMetaModel spm) {
	//		addItemToTable(label, createControlFor(name, pmm, spm)); // Add the proper input control for that type && add to the cell
	//	}

	/**
	 * Add the item: add a row with a cell containing a label and another cell containing the lookup controls.
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
			if(labelcontrol == null && b instanceof IInputNode<?>)
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
				if(it.getProperty() != null) {
					String dl = it.getProperty().getDefaultLabel();
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
	 * Create the optimal control for the specified thingy.
	 * @param container
	 * @param name
	 * @param pmm
	 * @return
	 */
	private ILookupControlInstance createControlFor(final String name, final PropertyMetaModel pmm, final SearchPropertyMetaModel spm) {
		if(pmm == null || spm == null) {
			throw new IllegalStateException("Search properties are not defined, lookup control must be created externally,  missing implementation of createControlFor in extended class of "
				+ getClass().getName());
		}
		IRequestContext rq = PageContext.getRequestContext();
		boolean viewable = MetaManager.isAccessAllowed(pmm.getViewRoles(), rq);
		boolean editable = MetaManager.isAccessAllowed(pmm.getEditRoles(), rq);
		if(!viewable) {
			//-- Check edit stuff:
			if(pmm.getEditRoles() == null) // No edit roles at all -> exit
				return null;
			if(!editable)
				return null;
		}

		LookupControlFactory lcf = DomApplication.get().getLookupControlFactory(pmm);
		ILookupControlInstance qt = lcf.createControl(spm, pmm);
		if(qt == null || qt.getInputControls() == null || qt.getInputControls().length == 0)
			throw new IllegalStateException("Lookup factory " + lcf + " did not create a lookup thingy for property " + pmm);
		return qt;
	}

	private void add(final ILookupControlInstance t) {
		if(m_queryBuilder == Collections.EMPTY_LIST)
			m_queryBuilder = new ArrayList<ILookupControlInstance>();
		m_queryBuilder.add(t);
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
		for(ILookupControlInstance th : m_queryBuilder) {
			if(!th.appendCriteria(root))
				success = false;
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
		for(ILookupControlInstance th : m_queryBuilder) {
			th.clearInput();
			//			for(NodeBase nb : th.getInputControls()) {
			//				if(nb instanceof IInputNode<?>) {
			//					IInputNode<?> v = (IInputNode<?>) nb;
			//					v.setValue(null);
			//				}
			//			}
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

	public List<SearchPropertyMetaModel> getSearchProperties() {
		return m_searchProperties;
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
	public void setClicked(final IClicked<?> clicked) {
		m_clicker = (IClicked<LookupForm<T>>) clicked;
	}
}

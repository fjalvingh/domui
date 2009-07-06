package to.etc.domui.component.lookup;

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.webapp.nls.*;
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
	private Class<T> m_lookupClass;

	private QCriteria<T> m_basicCriteria;

	private List<SearchPropertyMetaModel> m_searchProperties;

	private List<DisplayPropertyMetaModel> m_displayProperties;

	private String m_title;

	IClicked<LookupForm<T>> m_clicker;

	IClicked<LookupForm<T>> m_onNew;

	private Table m_table;

	private List<LookupFieldQueryBuilderThingy> m_queryBuilder = Collections.EMPTY_LIST;

	private TBody m_tbody;

	public LookupForm(final Class<T> lookupClass) {
		m_lookupClass = lookupClass;
	}

	public Class<T> getLookupClass() {
		return m_lookupClass;
	}

	public void setLookupClass(final Class<T> lookupClass) {
		m_lookupClass = lookupClass;
	}

	public QCriteria<T> getBasicCriteria() {
		return m_basicCriteria;
	}

	public void setBasicCriteria(final QCriteria<T> basicCriteria) {
		m_basicCriteria = basicCriteria;
	}

	public List<SearchPropertyMetaModel> getSearchProperties() {
		return m_searchProperties;
	}

	public void setSearchProperties(final List<SearchPropertyMetaModel> searchProperties) {
		m_searchProperties = searchProperties;
	}

	public List<DisplayPropertyMetaModel> getDisplayProperties() {
		return m_displayProperties;
	}

	public void setDisplayProperties(final List<DisplayPropertyMetaModel> displayProperties) {
		m_displayProperties = displayProperties;
	}

	public String getPageTitle() {
		return m_title;
	}

	public void setPageTitle(final String title) {
		m_title = title;
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

		List<SearchPropertyMetaModel> list = getSearchProperties();
		if(list == null || list.size() == 0) {
			ClassMetaModel cm = MetaManager.findClassMeta(m_lookupClass);
			list = cm.getSearchProperties();
			if(list == null || list.size() == 0)
				throw new IllegalStateException("The class " + m_lookupClass + " has no search properties");
		}

		for(SearchPropertyMetaModel sm : list) {
			addPropertyControl(sm.getProperty().getName(), sm.getProperty().getDefaultLabel(NlsContext.getLocale()), sm.getProperty(), sm);
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

	@Override
	public void setClicked(final IClicked< ? > clicked) {
		m_clicker = (IClicked<LookupForm<T>>) clicked;
	}

	public void clearInput() {
		for(LookupFieldQueryBuilderThingy th : m_queryBuilder) {
			for(NodeBase nb : th.getInputControls()) {
				if(nb instanceof IInputNode< ? >) {
					IInputNode< ? > v = (IInputNode< ? >) nb;
					v.setValue(null);
				}
			}
		}
	}

	public void addPropertyControl(final String name, final String label, final PropertyMetaModel pmm, final SearchPropertyMetaModel spm) {
		LookupFieldQueryBuilderThingy qt = createControlFor(name, pmm, spm); // Add the proper input control for that type && add to the cell
		if(qt == null)
			return;

		//-- Create control && label
		TD ccell = new TD();
		ccell.setCssClass("ui-f-in");
		for(NodeBase b : qt.getInputControls())
			ccell.add(b);
		TD lcell = new TD();

		lcell.setCssClass("ui-f-lbl");
		lcell.add(new Label(qt.getInputControls()[0], label));
		TR tr = new TR();
		tr.add(lcell);
		tr.add(ccell);
		m_tbody.add(tr);
		add(qt);
	}

	/**
	 * Create the optimal control for the specified thingy.
	 * @param container
	 * @param name
	 * @param pmm
	 * @return
	 */
	public LookupFieldQueryBuilderThingy createControlFor(final String name, final PropertyMetaModel pmm, final SearchPropertyMetaModel spm) {
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
		LookupFieldQueryBuilderThingy qt = lcf.createControl(spm, pmm);
		if(qt == null)
			throw new IllegalStateException("Lookup factory " + lcf + " did not create a lookup thingy for property " + pmm);
		return qt;
	}

	protected void add(final LookupFieldQueryBuilderThingy t) {
		if(m_queryBuilder == Collections.EMPTY_LIST)
			m_queryBuilder = new ArrayList<LookupFieldQueryBuilderThingy>();
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
		for(LookupFieldQueryBuilderThingy th : m_queryBuilder) {
			if(!th.appendCriteria(root))
				success = false;
		}
		if(!success) // Some input failed to validate their input criteria?
			return null; // Then exit null -> should only display errors.
		return root;
	}

	public IClicked<LookupForm<T>> getOnNew() {
		return m_onNew;
	}

	public void setOnNew(final IClicked<LookupForm<T>> onNew) {
		m_onNew = onNew;
		forceRebuild();
	}
}

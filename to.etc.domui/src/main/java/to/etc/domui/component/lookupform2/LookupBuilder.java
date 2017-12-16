package to.etc.domui.component.lookupform2;

import to.etc.domui.component.lookup.AbstractLookupControlImpl;
import to.etc.domui.component.lookup.ILookupControlFactory;
import to.etc.domui.component.lookup.ILookupControlInstance;
import to.etc.domui.component.lookupform2.LookupForm2.ItemBreak;
import to.etc.domui.component.lookupform2.lookupcontrols.FactoryPair;
import to.etc.domui.component.lookupform2.lookupcontrols.LookupControlRegistry2;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component.meta.impl.SearchPropertyMetaModelImpl;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.NodeBase;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QRestrictor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-12-17.
 */
public class LookupBuilder {

	/**
	 * Set the search properties to use from a list of metadata properties.
	 * @param list
	 */
	public void setSearchProperties(List<SearchPropertyMetaModel> list) {
		int totalCount = list.size();
		for(SearchPropertyMetaModel sp : list) { // The list is already in ascending order, so just add items;
			LookupLine it = new LookupLine();
			it.setIgnoreCase(sp.isIgnoreCase());
			it.setMinLength(sp.getMinLength());
			it.setPropertyName(sp.getPropertyName());
			it.setPropertyPath(sp.getPropertyPath());
			it.setLabelText(sp.getLookupLabel()); // If a lookup label is defined use it.
			it.setLookupHint(sp.getLookupHint()); // If a lookup hint is defined use it.
			it.setPopupSearchImmediately(sp.isPopupSearchImmediately());
			it.setPopupInitiallyCollapsed(sp.isPopupInitiallyCollapsed());
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
	public LookupLine addProperty(String path, int minlen, boolean ignorecase) {
		return addProperty(path, null, minlen, Boolean.valueOf(ignorecase));
	}

	/**
	 * Add a property to look up to the list. The controls et al will be added using the factories.
	 * @param path		The property name (or path to some PARENT property) to search on, relative to the lookup class.
	 * @param minlen
	 */
	public LookupLine addProperty(String path, int minlen) {
		return addProperty(path, null, minlen, null);
	}

	/**
	 * Add a property to look up to the list with user-specified label. The controls et al will be added using the factories.
	 * @param path	The property name (or path to some PARENT property) to search on, relative to the lookup class.
	 * @param label	The label text to use. Use the empty string to prevent a label from being generated. This still adds an empty cell for the label though.
	 */
	public LookupLine addProperty(String path, String label) {
		return addProperty(path, label, 0, null);
	}

	/**
	 * Add a property to look up to the list. The controls et al will be added using the factories.
	 * @param path	The property name (or path to some PARENT property) to search on, relative to the lookup class.
	 */
	public LookupLine addProperty(String path) {
		return addProperty(path, null, 0, null);
	}

	/**
	 * Add a property manually.
	 * @param path		The property name (or path to some PARENT property) to search on, relative to the lookup class.
	 * @param minlen
	 * @param ignorecase
	 */
	private LookupLine addProperty(String path, String label, int minlen, Boolean ignorecase) {
		for(LookupLine it : m_itemList) { // FIXME Useful?
			if(it.getPropertyName() != null && path.equals(it.getPropertyName())) // Already present there?
				throw new ProgrammerErrorException("The property " + path + " is already part of the search field list.");
		}

		//-- Define the item.
		LookupLine it = new LookupLine();
		it.setPropertyName(path);
		it.setLabelText(label);
		it.setIgnoreCase(ignorecase == null || ignorecase.booleanValue());
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
	public LookupLine addManual(ILookupControlInstance<?> lci) {
		LookupLine it = new LookupLine();
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
	public <VT, X extends NodeBase & IControl<VT>> LookupLine addManual(String property, X control) {
		LookupLine it = new LookupLine();
		it.setPropertyName(property);
		addAndFinish(it);

		//-- Add the generic thingy
		ILookupControlFactory lcf = m_builder.getLookupQueryFactory(it, control);
		ILookupControlInstance<?> qt = lcf.createControl(it, control);
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
	public LookupLine addManualTextLabel(String labelText, ILookupControlInstance<?> lci) {
		LookupLine it = new LookupLine();
		it.setInstance(lci);
		it.setLabelText(labelText);
		addAndFinish(it);
		updateUI(it);
		return it;
	}

	/**
	 * Adds a manually-defined control, and use the specified property as the source for its default label.
	 * @param property
	 * @param lci
	 * @return
	 */
	public LookupLine addManualPropertyLabel(String property, ILookupControlInstance<?> lci) {
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
	public LookupLine addChildProperty(String propPath) {
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
	public LookupLine addChildPropertyLabel(String label, String propPath) {

		final List<PropertyMetaModel< ? >> pl = MetaManager.parsePropertyPath(m_metaModel, propPath);

		if(pl.size() != 2) {
			throw new ProgrammerErrorException("Property path does not contain parent.child path: " + propPath);
		}

		final PropertyMetaModel< ? > parentPmm = pl.get(0);
		final PropertyMetaModel< ? > childPmm = pl.get(1);

		SearchPropertyMetaModelImpl spmm = new SearchPropertyMetaModelImpl(m_metaModel);
		spmm.setPropertyName(childPmm.getName());
		spmm.setPropertyPath(pl);

		FactoryPair<?> controlPair = LookupControlRegistry2.INSTANCE.findControlPair(spmm);




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

		return addManualTextLabel(label == null ? parentPmm.getDefaultLabel() : label, thingy);
	}


	/**
	 * Create the optimal control using metadata for a property. This can only be called for an item
	 * containing a property with metadata.
	 *
	 * @param it
	 * @return
	 */
	private ILookupControlInstance<?> createControlFor(LookupLine it) {
		PropertyMetaModel< ? > pmm = it.getLastProperty();
		if(pmm == null)
			throw new IllegalStateException("property cannot be null when creating using factory.");
		ILookupControlFactory lcf = m_builder.getLookupControlFactory(it);
		ILookupControlInstance<?> qt = lcf.createControl(it, null);
		if(qt == null || qt.getInputControls() == null || qt.getInputControls().length == 0)
			throw new IllegalStateException("Lookup factory " + lcf + " did not create a lookup thingy for property " + it.getPropertyName());
		return qt;
	}

	/**
	 * With this method you can place a NodeBase in the table where it will fill the entire row.
	 * A colspan=2 will be added
	 */
	public void addItem(@Nullable NodeBase cell) {
		LookupLine item = new LookupLine(cell);
		m_itemList.add(item);
	}

}

package to.etc.domui.component.form;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.lookup.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * This singleton, reachable from DomApplication, maintains all metadata control builder lists and contains code to create controls from factories et al.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 18, 2009
 */
public class ControlBuilder {
	//	private DomApplication m_app;

	private List<ControlFactory> m_controlFactoryList = new ArrayList<ControlFactory>();

	private final LookupControlRegistry m_lookupControlRegistry = new LookupControlRegistry();

	private IControlLabelFactory m_controlLabelFactory = new DefaultControlLabelFactory();

	public ControlBuilder(DomApplication app) {
	//		m_app = app;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Control factories for editing..						*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @param cf
	 */
	public synchronized void registerControlFactory(final ControlFactory cf) {
		m_controlFactoryList = new ArrayList<ControlFactory>(m_controlFactoryList); // Dup original
		m_controlFactoryList.add(cf);
	}

	protected synchronized List<ControlFactory> getControlFactoryList() {
		return m_controlFactoryList;
	}

	/**
	 * Find the best control factory to use to create a control for the given property and mode.
	 * @param pmm		The property to find a control for
	 * @param editable	When false this is a displayonly control request.
	 * @return			null if no factory is found.
	 */
	public ControlFactory findControlFactory(final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass) {
		ControlFactory best = null;
		int score = 0;
		for(ControlFactory cf : getControlFactoryList()) {
			int v = cf.accepts(pmm, editable, controlClass);
			if(v > score) {
				score = v;
				best = cf;
			}
		}
		return best;
	}

	/**
	 * Find the best control factory to use to create a control for the given property and mode, throws
	 * an Exception if the factory cannot be found.
	 *
	 * @param pmm
	 * @param editable
	 * @return	The factory to use
	 */
	public ControlFactory getControlFactory(final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass) {
		ControlFactory cf = findControlFactory(pmm, editable, controlClass);
		if(cf == null)
			throw new IllegalStateException("Cannot get a control factory for " + pmm);
		return cf;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Label generation factory.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @return
	 */
	public synchronized IControlLabelFactory getControlLabelFactory() {
		return m_controlLabelFactory;
	}

	public synchronized void setControlLabelFactory(final IControlLabelFactory controlLabelFactory) {
		m_controlLabelFactory = controlLabelFactory;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Lookup Form control factories.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Add another LookupControlFactory to the registry.
	 * @param f
	 */
	public void register(final ILookupControlFactory f) {
		m_lookupControlRegistry.register(f);
	}

	public ILookupControlFactory findLookupControlFactory(final SearchPropertyMetaModel pmm) {
		return m_lookupControlRegistry.findFactory(pmm);
	}

	public ILookupControlFactory getLookupControlFactory(final SearchPropertyMetaModel pmm) {
		return m_lookupControlRegistry.getControlFactory(pmm);
	}

	public <X extends NodeBase & IInputNode< ? >> ILookupControlFactory getLookupQueryFactory(final SearchPropertyMetaModel pmm, X control) {
		return m_lookupControlRegistry.getLookupQueryFactory(pmm, control);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Utilities to help you to create controls..			*/
	/*--------------------------------------------------------------*/

	/**
	 * Main workhorse which creates input controls for forms, from metadata.
	 */
	public ControlFactory.Result createControlFor(final IReadOnlyModel< ? > model, final PropertyMetaModel pmm, final boolean editable) {
		ControlFactory cf = getControlFactory(pmm, editable, null);
		return cf.createControl(model, pmm, editable, null);
	}

	/**
	 *
	 * @param <T>
	 * @param controlClass
	 * @param dataClass
	 * @param propertyName
	 * @param editableWhen
	 * @return
	 */
	public <T> T createControl(Class<T> controlClass, Class< ? > dataClass, String propertyName, boolean editable) {
		PropertyMetaModel pmm = MetaManager.getPropertyMeta(dataClass, propertyName); // Must exist or throws exception.
		return createControl(controlClass, dataClass, pmm, editable);
	}

	/**
	 *
	 * @param <T>
	 * @param controlClass
	 * @param dataClass
	 * @param pmm
	 * @param editable
	 * @return
	 */
	public <T> T createControl(Class<T> controlClass, Class< ? > dataClass, PropertyMetaModel pmm, boolean editable) {
		if(controlClass == null)
			throw new IllegalArgumentException("controlClass cannot be null");
		ControlFactory cf = getControlFactory(pmm, editable, null);
		ControlFactory.Result r = cf.createControl(null, pmm, editable, controlClass);

		//-- This must have generated a single control of the specified type, so check...
		if(r.getNodeList().length != 1)
			throw new IllegalStateException("The control factory "+cf+" created != 1 components for a find-control-for-class query");
		NodeBase c = r.getNodeList()[0];
		if(!controlClass.isAssignableFrom(controlClass))
			throw new IllegalStateException("The control factory " + cf + " created a " + c + " which is NOT assignment-compatible with the requested class " + controlClass);
		return (T) c;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Creating all kinds of combo boxes.					*/
	/*--------------------------------------------------------------*/
	/**
	 * This creates a ComboFixed for some fixed-size domain class specified by type. This will allow any
	 * domain-valued type (as specified by metadata returning something for getDomainValues()). The domain
	 * value translations are done by class metadata <b>only</b> because the originating property is not
	 * known. This may cause values to be misrepresented.
	 *
	 * @param <T>
	 * @return
	 */
	public <T> ComboFixed<T> createComboFor(Class<T> type) {
		if(type == null)
			throw new IllegalArgumentException("type cannot be null");
		ClassMetaModel cmm = MetaManager.findClassMeta(type);
		T[] vals = (T[]) cmm.getDomainValues();
		if(vals == null || vals.length == 0)
			throw new IllegalArgumentException("The type " + type + " is not known as a fixed-size domain type");

		List<ValueLabelPair<T>> vl = new ArrayList<ValueLabelPair<T>>();
		for(T o : vals) {
			String label = cmm.getDomainLabel(NlsContext.getLocale(), o); // Label known to property?
			if(label == null)
				label = o == null ? "" : o.toString();
			vl.add(new ValueLabelPair<T>(o, label));
		}
		ComboFixed<T> c = new ComboFixed<T>(vl);
		return c;
	}

	/**
	 * This creates a ComboFixed for some fixed-size domain property specified by the metamodel. This will allow any
	 * domain-valued type (as specified by metadata returning something for getDomainValues()). This version will
	 * properly use per-property value labels if defined.
	 *
	 * @param pmm
	 * @return
	 */
	public ComboFixed< ? > createComboFor(PropertyMetaModel pmm, boolean editable) {
		if(pmm == null)
			throw new IllegalArgumentException("propertyMeta cannot be null");
		Object[] vals = pmm.getDomainValues();
		if(vals == null || vals.length == 0)
			throw new IllegalArgumentException("The type of property " + pmm + " (" + pmm.getActualType() + ") is not known as a fixed-size domain type");

		ClassMetaModel ecmm = null;
		List<ValueLabelPair<Object>> vl = new ArrayList<ValueLabelPair<Object>>();
		for(Object o : vals) {
			String label = pmm.getDomainValueLabel(NlsContext.getLocale(), o); // Label known to property?
			if(label == null) {
				if(ecmm == null)
					ecmm = MetaManager.findClassMeta(pmm.getActualType()); // Try to get the property's type.
				label = ecmm.getDomainLabel(NlsContext.getLocale(), o);
				if(label == null)
					label = o == null ? "" : o.toString();
			}
			vl.add(new ValueLabelPair<Object>(o, label));
		}

		ComboFixed< ? > c = new ComboFixed<Object>(vl);
		if(pmm.isRequired())
			c.setMandatory(true);
		if(!editable || pmm.getReadOnly() == YesNoType.YES)
			c.setDisabled(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			c.setTitle(s);
		return c;
	}

	/**
	 * This creates a ComboFixed for some fixed-size domain property specified by the metamodel. This will allow any
	 * domain-valued type (as specified by metadata returning something for getDomainValues()). This version will
	 * properly use per-property value labels if defined.
	 *
	 * @param dataClass		The class whose property is to be looked up
	 * @param property		The property path
	 * @return
	 */
	public ComboFixed< ? > createComboFor(Class< ? > dataClass, String property, boolean editable) {
		PropertyMetaModel pmm = MetaManager.getPropertyMeta(dataClass, property);
		return createComboFor(pmm, editable);
	}


}

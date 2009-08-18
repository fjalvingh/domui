package to.etc.domui.component.form;

import java.util.*;

import to.etc.domui.component.lookup.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

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
	public void register(final LookupControlFactory f) {
		m_lookupControlRegistry.register(f);
	}

	public LookupControlFactory findLookupControlFactory(final PropertyMetaModel pmm) {
		return m_lookupControlRegistry.findFactory(pmm);
	}

	public LookupControlFactory getLookupControlFactory(final PropertyMetaModel pmm) {
		return m_lookupControlRegistry.getControlFactory(pmm);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Utilities to help you to create controls..			*/
	/*--------------------------------------------------------------*/

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
		return null;


	}

}

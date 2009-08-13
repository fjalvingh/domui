package to.etc.domui.component.form;

import java.util.logging.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * Base class for form builder engines.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 13, 2009
 */
public class FormBuilderBase {
	static protected final Logger LOG = Logger.getLogger(FormBuilderBase.class.getName());

	/** If a concrete input class is known this contains it's type. */
	private Class< ? > m_currentInputClass;

	/** The current source model for the object containing the properties. */
	private IReadOnlyModel< ? > m_model;

	/** The concrete MetaModel to use for properties within this object. */
	private ClassMetaModel m_classMeta;

	private ModelBindings m_bindings = new ModelBindings();

	/** Thingy to help calculating access rights (delegate) */
	private final AccessCalculator m_calc = new AccessCalculator();

	private IControlLabelFactory m_controlLabelFactory;

	public FormBuilderBase() {}

	/**
	 * Constructor to immediately initialize for a given class and reader.
	 * @param <T>
	 * @param clz
	 * @param mdl
	 */
	public <T> FormBuilderBase(final Class<T> clz, final IReadOnlyModel<T> mdl) {
		setClassModel(clz, mdl);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple getters and internal stuff.					*/
	/*--------------------------------------------------------------*/

	/**
	 * Access the shared permissions calculator.
	 */
	protected AccessCalculator rights() {
		return m_calc;
	}

	/**
	 * Set or change the current base class and base model. This can be changed whenever needed.
	 *
	 * @param <T>
	 * @param clz
	 * @param mdl
	 */
	public <T> void setClassModel(final Class<T> clz, final IReadOnlyModel<T> mdl) {
		m_classMeta = MetaManager.findClassMeta(clz);
		m_currentInputClass = clz;
		m_model = mdl;
	}

	/**
	 * Sets the base metamodel and value source to use for obtaining properties.
	 *
	 * @param cmm
	 * @param source
	 */
	public void setMetaModel(final ClassMetaModel cmm, final IReadOnlyModel< ? > source) {
		m_classMeta = cmm;
		m_model = source;
		m_currentInputClass = null;
	}

	/**
	 * Return the currently active class metamodel (the model that properties are obtained from). This
	 * will never return null.
	 * @return
	 */
	protected ClassMetaModel getClassMeta() {
		if(m_classMeta == null)
			throw new IllegalStateException("No ClassMetaModel is known!");
		return m_classMeta;
	}

	/**
	 * Find a property relative to the current input class.
	 *
	 * @param name
	 * @return
	 */
	protected PropertyMetaModel resolveProperty(final String name) {
		PropertyMetaModel pmm = getClassMeta().findProperty(name);
		if(pmm == null)
			throw new IllegalStateException("Unknown property " + name);
		return pmm;
	}


	/**
	 * Return the current ReadOnlyModel which is the accessor to get the instance that will be edited. This
	 * will never return null.
	 * @return
	 */
	public IReadOnlyModel< ? > getModel() {
		if(m_model == null)
			throw new IllegalStateException("Usage error: you need to provide a 'model accessor'");
		return m_model;
	}


	public Class< ? > getCurrentInputClass() {
		if(m_currentInputClass == null)
			throw new IllegalStateException("Usage error: you need to provide a 'current input class' type!!");
		return m_currentInputClass;
	}

	public ModelBindings getBindings() {
		return m_bindings;
	}

	public void setBindings(final ModelBindings bindings) {
		if(m_bindings != null && m_bindings.size() > 0)
			LOG.warning("Setting new bindings but current binding list has bindings!! Make sure you use the old list to bind too!!");
		m_bindings = bindings;
	}

	/**
	 * Return the factory to use for creating control labels from metadata.
	 * @return
	 */
	public IControlLabelFactory getControlLabelFactory() {
		return m_controlLabelFactory;
	}

	public void setControlLabelFactory(final IControlLabelFactory controlLabelFactory) {
		m_controlLabelFactory = controlLabelFactory;
	}

}

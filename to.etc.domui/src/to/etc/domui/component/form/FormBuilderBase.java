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
package to.etc.domui.component.form;

import org.slf4j.*;

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
	static protected final Logger LOG = LoggerFactory.getLogger(FormBuilderBase.class);

	/** If a concrete input class is known this contains it's type. */
	private Class< ? > m_currentInputClass;

	/** The current source model for the object containing the properties. */
	private IReadOnlyModel< ? > m_model;

	/** The concrete MetaModel to use for properties within this object. */
	private ClassMetaModel m_classMeta;

	private Object m_context;

	private ModelBindings m_bindings = new ModelBindings();

	/** Thingy to help calculating access rights (delegate) */
	private final AccessCalculator m_calc = new AccessCalculator();

	private ControlBuilder m_builder;

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

	/**
	 * Initialize with a single unchangeable instance. Please consider using a model though
	 * as it is more resilient to changes.
	 * @param <T>
	 * @param instance
	 */
	public <T> FormBuilderBase(T instance) {
		setInstance(instance);
	}

	final public ControlBuilder getBuilder() {
		if(m_builder == null)
			m_builder = DomApplication.get().getControlBuilder();
		return m_builder;
	}

	/**
	 * Create the optimal control for the specified thingy, and return the binding for it.
	 *
	 * @param container		This will receive all nodes forming the control.
	 * @param model 		The content model used to obtain the Object instance whose property is being edited, for binding purposes.
	 * @param pmm			The property meta for the property to find an editor for.
	 * @param editable		When false this must make a displayonly control.
	 * @return				The binding to bind the control to it's valueset
	 */
	protected ControlFactoryResult createControlFor(final IReadOnlyModel< ? > model, final PropertyMetaModel< ? > pmm, final boolean editable, Object context) {
		return getBuilder().createControlFor(model, pmm, editable, context); // Delegate
	}

	protected ControlFactoryResult createControlFor(final IReadOnlyModel< ? > model, final PropertyMetaModel< ? > pmm, final boolean editable) {
		return createControlFor(model, pmm, editable, getContext());
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

	public Object getContext() {
		return m_context;
	}

	public void setContext(Object context) {
		m_context = context;
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

	public <T> void setInstance(final T instance) {
		if(instance == null)
			throw new IllegalArgumentException("Instance cannot be null");
		IReadOnlyModel<T> instanceModel = new IReadOnlyModel<T>() {
			@Override
			public T getValue() throws Exception {
				return instance;
			}
		};
		setClassModel((Class<T>) instance.getClass(), instanceModel); // I HATE Java Generics. What a bunch of shit.
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
	protected PropertyMetaModel< ? > resolveProperty(final String name) {
		PropertyMetaModel< ? > pmm = getClassMeta().findProperty(name);
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
			LOG.warn("Setting new bindings but current binding list has bindings!! Make sure you use the old list to bind too!!");
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

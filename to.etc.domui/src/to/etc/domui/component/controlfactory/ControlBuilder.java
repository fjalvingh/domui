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
package to.etc.domui.component.controlfactory;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.lookup.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
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
	@Nonnull
	private List<ControlFactory> m_controlFactoryList = new ArrayList<ControlFactory>();

	@Nonnull
	private final LookupControlRegistry m_lookupControlRegistry = new LookupControlRegistry();

	@Nonnull
	private IControlLabelFactory m_controlLabelFactory = new DefaultControlLabelFactory();

	@Nonnull
	private IControlErrorFragmentFactory m_errorFragmentfactory = new IControlErrorFragmentFactory() {
		@Override
		public NodeContainer createErrorFragment() {
			return new ErrorMessageDiv();
		}
	};

	public ControlBuilder(@Nonnull DomApplication app) {
	//		m_app = app;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Control factories for editing..						*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @param cf
	 */
	public synchronized void registerControlFactory(@Nonnull final ControlFactory cf) {
		m_controlFactoryList = new ArrayList<ControlFactory>(m_controlFactoryList); // Dup original
		m_controlFactoryList.add(cf);
	}

	@Nonnull
	protected synchronized List<ControlFactory> getControlFactoryList() {
		return m_controlFactoryList;
	}

	/**
	 * Find the best control factory to use to create a control for the given property and mode.
	 * @param pmm		The property to find a control for
	 * @param editable	When false this is a displayonly control request.
	 * @return			null if no factory is found.
	 */
	public ControlFactory findControlFactory(@Nonnull final PropertyMetaModel< ? > pmm, final boolean editable, @Nullable Class< ? > controlClass) {
		if(pmm.getControlFactory() != null)
			return pmm.getControlFactory();

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
	 * Locate a factory by it's full class name. If the factory is found it is registered if needed.
	 * @param name
	 * @return
	 */
	public synchronized ControlFactory findFactoryByName(@Nonnull String name) {
		//-- 1. Walk the registered factory list
		for(ControlFactory cf : m_controlFactoryList) {
			if(name.equals(cf.getClass().getName()))
				return cf;
		}

		//-- 2. Can we load this as a control factory class?
		Class< ? > clz = null;
		try {
			clz = getClass().getClassLoader().loadClass(name);
		} catch(Exception x) {}
		if(clz == null)
			return null;
		if(!ControlFactory.class.isAssignableFrom(clz))
			return null;

		//-- Try to instantiate and register.
		try {
			ControlFactory cf = (ControlFactory) clz.newInstance();
			registerControlFactory(cf);
			return cf;
		} catch(Exception x) {}
		return null;
	}

	/**
	 * Find the best control factory to use to create a control for the given property and mode, throws
	 * an Exception if the factory cannot be found.
	 *
	 * @param pmm
	 * @param editable
	 * @return	The factory to use
	 */
	@Nonnull
	public ControlFactory getControlFactory(@Nonnull final PropertyMetaModel< ? > pmm, final boolean editable, @Nullable Class< ? > controlClass) {
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
	@Nonnull
	public synchronized IControlLabelFactory getControlLabelFactory() {
		return m_controlLabelFactory;
	}

	public synchronized void setControlLabelFactory(@Nonnull final IControlLabelFactory controlLabelFactory) {
		m_controlLabelFactory = controlLabelFactory;
	}

	@Nonnull
	public synchronized IControlErrorFragmentFactory getErrorFragmentfactory() {
		return m_errorFragmentfactory;
	}

	public synchronized void setErrorFragmentfactory(@Nonnull IControlErrorFragmentFactory errorFragmentfactory) {
		if(errorFragmentfactory == null)
			throw new IllegalArgumentException("Cannot accept null");
		m_errorFragmentfactory = errorFragmentfactory;
	}

	public void addErrorFragment(@Nonnull NodeContainer nc) {
		NodeContainer lsn = getErrorFragmentfactory().createErrorFragment();
		nc.add(lsn);
		DomUtil.getMessageFence(nc).addErrorListener((IErrorMessageListener) lsn);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Lookup Form control factories.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Add another LookupControlFactory to the registry.
	 * @param f
	 */
	public void register(@Nonnull final ILookupControlFactory f) {
		m_lookupControlRegistry.register(f);
	}

	@Nullable
	public ILookupControlFactory findLookupControlFactory(@Nonnull final SearchPropertyMetaModel pmm) {
		return m_lookupControlRegistry.findFactory(pmm);
	}

	@Nonnull
	public ILookupControlFactory getLookupControlFactory(@Nonnull final SearchPropertyMetaModel pmm) {
		return m_lookupControlRegistry.getControlFactory(pmm);
	}

	@Nonnull
	public <T, X extends NodeBase & IInputNode<T>> ILookupControlFactory getLookupQueryFactory(@Nonnull final SearchPropertyMetaModel pmm, @Nonnull X control) {
		return m_lookupControlRegistry.getLookupQueryFactory(pmm, control);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Utilities to help you to create controls..			*/
	/*--------------------------------------------------------------*/

	/**
	 * Main workhorse which creates input controls for forms, from metadata.
	 */
	@Nonnull
	public ControlFactoryResult createControlFor(@Nonnull final IReadOnlyModel< ? > model, @Nonnull final PropertyMetaModel< ? > pmm, final boolean editable) {
		ControlFactory cf = getControlFactory(pmm, editable, null);
		return cf.createControl(pmm, editable, null);
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
	public <T> T createControl(@Nonnull Class<T> controlClass, @Nonnull Class< ? > dataClass, @Nonnull String propertyName, boolean editable) {
		PropertyMetaModel< ? > pmm = MetaManager.getPropertyMeta(dataClass, propertyName); // Must exist or throws exception.
		return createControl(controlClass, pmm, editable);
	}

	@Nonnull
	static private final IReadOnlyModel<Object> DUMMY_MODEL = new IReadOnlyModel<Object>() {
		@Override
		public Object getValue() throws Exception {
			throw new IllegalStateException("Should not ever call this");
		}
	};

	/**
	 *
	 * @param <T>
	 * @param controlClass
	 * @param dataClass
	 * @param pmm
	 * @param editable
	 * @return
	 */
	public <T> T createControl(@Nonnull Class<T> controlClass, @Nonnull PropertyMetaModel< ? > pmm, boolean editable) {
		if(controlClass == null)
			throw new IllegalArgumentException("controlClass cannot be null");
		ControlFactory cf = getControlFactory(pmm, editable, null);
		ControlFactoryResult r = cf.createControl(pmm, editable, controlClass);	// FIXME Bad, bad bug: I should be able to create a control without binding!!

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
	@Nonnull
	public ComboFixed< ? > createComboFor(PropertyMetaModel< ? > pmm, boolean editable) {
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
		PropertyMetaModel< ? > pmm = MetaManager.getPropertyMeta(dataClass, property);
		return createComboFor(pmm, editable);
	}


}

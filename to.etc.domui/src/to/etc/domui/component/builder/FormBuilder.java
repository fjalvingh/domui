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
package to.etc.domui.component.builder;

import javax.annotation.*;

import org.slf4j.*;

import to.etc.domui.component.form.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * Base class for form builder engines.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 13, 2009
 */
abstract public class FormBuilder {
	static protected final Logger LOG = LoggerFactory.getLogger(FormBuilder.class);

	@Nonnull
	private ModelBindings m_bindings = new ModelBindings();

	/** Thingy to help calculating access rights (delegate) */
	@Nonnull
	private final AccessCalculator m_calc = new AccessCalculator();

	@Nullable
	private ControlBuilder m_builder;

	@Nullable
	private IControlLabelFactory m_controlLabelFactory;

	private Object m_lastBuilderThingy;

	private FormData< ? > m_lastBuilder;

	protected FormBuilder() {}

	/**
	 * This is the actual workhorse doing the per-builder actual placement and layouting of a {control, label} pair.
	 *
	 * @param label
	 * @param labelnode
	 * @param list
	 * @param mandatory	T when the node is mandatory, needed by the label factory
	 * @param editable	T when the node is editable, needed by the label factory
	 * @param pmm
	 */
	abstract protected void addControl(@Nullable String label, NodeBase labelnode, NodeBase[] list, boolean mandatory, boolean editable, PropertyMetaModel< ? > pmm);

	abstract protected void addControl(@Nullable NodeBase label, NodeBase labelnode, NodeBase[] list, boolean mandatory, boolean editable, PropertyMetaModel< ? > pmm);

	abstract public void addContent(@Nullable NodeBase label, NodeBase[] control, boolean editable);

	/**
	 * Handle placement of a list of property names, all obeying the current mode in effect.
	 * @param editable
	 * @param names
	 */
	@Nonnull
	abstract protected IControl< ? >[] addListOfProperties(boolean editable, @Nonnull final String... names);


	@Nonnull
	final public ControlBuilder getControlBuilder() {
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
	protected ControlFactoryResult createControlFor(@Nonnull final IReadOnlyModel< ? > model, @Nonnull final PropertyMetaModel< ? > pmm, final boolean editable) {
		return getControlBuilder().createControlFor(model, pmm, editable); // Delegate
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


	@Nonnull
	public ModelBindings getBindings() {
		return m_bindings;
	}

	public void setBindings(@Nonnull final ModelBindings bindings) {
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

	@Nonnull
	public <T> FormData<T> data(@Nonnull T instance) {
		if(m_lastBuilderThingy == instance) {
			return (FormData<T>) m_lastBuilder;
		}
		FormData<T> b = new FormData<T>(this, instance);
		m_lastBuilder = b;
		m_lastBuilderThingy = instance;
		return b;
	}
}

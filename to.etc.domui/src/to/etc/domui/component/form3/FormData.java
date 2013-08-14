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
package to.etc.domui.component.form3;

import javax.annotation.*;

import to.etc.domui.component.controlfactory.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.databinding.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.annotations.*;

/**
 * This represents a builder to add components to edit a specific data class (T) inside a form builder. It
 * is typeful so that {@link GProperty} annotations can be used to allow autocomplete on those property
 * names, and it allows the <a target="_blank" href="http://domui.org/wiki/bin/view/Documentation/EclipsePlugin">DomUI Eclipse Plugin</a>
 * to check that those properties really exist.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 12, 2012
 */
public class FormData<T> {
	@Nonnull
	final private AbstractFormBuilder m_builder;

	@Nonnull
	final private IReadOnlyModel<T> m_model;

	/** The concrete MetaModel to use for properties within this object. */
	final private ClassMetaModel m_classMeta;

	protected FormData(@Nonnull AbstractFormBuilder bb, @Nonnull Class<T> clz, @Nonnull IReadOnlyModel<T> model) {
		if(model == null || clz == null || bb == null)
			throw new IllegalArgumentException("Cannot have nulls");
		m_builder = bb;
		m_model = model;
		m_classMeta = MetaManager.findClassMeta(clz);
	}

	protected FormData(@Nonnull AbstractFormBuilder bb, @Nonnull T instance) {
		if(instance == null)
			throw new IllegalArgumentException("Cannot have null instance");
		m_builder = bb;
		m_model = new InstanceReadOnlyModel<T>(instance);
		m_classMeta = MetaManager.findClassMeta(instance.getClass());
	}

	/**
	 * Return the currently active class metamodel (the model that properties are obtained from). This
	 * will never return null.
	 * @return
	 */
	@Nonnull
	protected ClassMetaModel getClassMeta() {
		return m_classMeta;
	}

	@Nonnull
	protected AbstractFormBuilder builder() {
		return m_builder;
	}

	/**
	 * Find a property relative to the current input class.
	 *
	 * @param name
	 * @return
	 */
	@Nonnull
	protected PropertyMetaModel< ? > resolveProperty(@Nonnull final String name) {
		return getClassMeta().getProperty(name);
	}


	/**
	 * Return the current ReadOnlyModel which is the accessor to get the instance that will be edited. This
	 * will never return null.
	 * @return
	 */
	@Nonnull
	public IReadOnlyModel< ? > getModel() {
		return m_model;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Adding property-based controls.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Add an input control and {@link IModelBinding} for the specified property of this instance's value type. The input model
	 * is default (using metadata) and the property is labeled using the metadata-provided label.
	 *
	 * @param name
	 * @throws Exception
	 */
	@Nonnull
	public IControl< ? > addProp(@Nonnull @GProperty final String name) throws Exception {
		return addProp(name, (String) null);
	}

	/**
	 * Add an input and {@link IModelBinding} for the specified property. The input model is default
	 * (using metadata) and the property is labeled.
	 *
	 * @param name
	 * @param label		The label text to use. Use the empty string to prevent a label from being generated. This still adds an empty cell for the label though.
	 * @throws Exception
	 */
	@Nonnull
	public IControl< ? > addProp(@Nonnull @GProperty final String name, @Nullable String label) throws Exception {
		PropertyMetaModel< ? > pmm = resolveProperty(name);
		boolean editable = true;
		if(pmm.getReadOnly() == YesNoType.YES)
			editable = false;
		return addPropertyControl(name, label, pmm, editable, pmm.isRequired());
	}

	/**
	 * Add an input, label and binding for the specified property. The input model is default
	 * (using metadata).
	 *
	 * @param name
	 * @param label Add custom label.
	 * @param editable When false this adds a display-only field, when true a fully editable control.
	 * @param mandatory Specify if field is mandatory. This <b>always</b> overrides the mandatoryness of the metadata which is questionable.
	 * @throws Exception
	 */
	@Nonnull
	public IControl< ? > addProp(@Nonnull @GProperty final String name, @Nonnull String label, final boolean editable, final boolean mandatory) throws Exception {
		PropertyMetaModel< ? > pmm = resolveProperty(name);
		return addPropertyControl(name, label, pmm, editable, mandatory);
	}

	/**
	 *
	 * @param name
	 * @param label
	 * @param pmm
	 * @param editable  when false, the rendered control will be display-only.
	 * @return	If the property was created and is controllable this will return an IFormControl instance. This will explicitly <i>not</i> be
	 * 			created if the control is display-only, not allowed by permissions or simply uncontrollable (the last one is uncommon).
	 */
	@Nonnull
	private <C> IControl<C> addPropertyControl(@Nonnull @GProperty final String name, @Nullable String label, @Nonnull final PropertyMetaModel<C> pmm, boolean editable, boolean mandatory) throws Exception {
		if(editable && builder().getRight() == AllowedRight.READ)
			editable = false;
		if(null == label)
			label = pmm.getDefaultLabel();

		final ControlFactoryResult r = builder().createControlFor(getModel(), pmm, editable); // Add the proper input control for that type
		builder().addControl(label, r.getLabelNode(), r.getNodeList(), mandatory, editable, pmm);

		//-- jal 20090924 Bug 624 Assign the control label to all it's node so it can specify it in error messages
		if(label != null) {
			for(NodeBase b : r.getNodeList())
				b.setErrorLocation(label);
		}
		builder().getBindingContext().joinbinding(getModel().getValue(), name, r.getFormControl(), "value");
		bindOptionals(r.getFormControl());
		r.getFormControl().setMandatory(mandatory);

		//		getBindings().add(new SimpleComponentPropertyBinding<C>(getModel(), pmm, (IControl<C>) r.getFormControl()));
		return (IControl<C>) r.getFormControl();
	}

	/**
	 *
	 * @param c
	 */
	private void bindOptionals(@Nullable IControl< ? > c) throws Exception {
		if(null == c)
			return;
		IObservableValue<Boolean> diso = builder().getDisabledObservable();
		if(null != diso) {
			Bind.from(diso).to(c, "disabled");
		}
	}

	/**
	 * Add an input or display-only control and an {@link IModelBinding} for the specified property. The input model is default
	 * (using metadata) and the property is labeled using the metadata-provided label.
	 *
	 * @param name
	 * @param editable When false add a display-only control, else add an editable control.
	 * @throws Exception
	 */
	@Nonnull
	public IControl< ? > addProp(@Nonnull @GProperty final String name, final boolean editable) throws Exception {
		if(editable) {
			return addProp(name);
		} else {
			return addDisplayProp(name);
		}
	}

	/**
	 * Add an input and {@link IModelBinding} for the specified property. The input model is default
	 * (using metadata) and the property is labeled using the metadata-provided label.
	 *
	 * @param name
	 * @param editable When false this adds a display-only field, when true a fully editable control.
	 * @param mandatory Specify if field is mandatory. This <b>always</b> overrides the mandatoryness of the metadata which is questionable.
	 * @throws Exception
	 */
	@Nonnull
	public IControl< ? > addProp(@Nonnull @GProperty final String name, final boolean editable, final boolean mandatory) throws Exception {
		PropertyMetaModel< ? > pmm = resolveProperty(name);
		String label = pmm.getDefaultLabel();
		return addProp(name, label, editable, mandatory);
	}

	/**
	 * Add a display-only control for the specified property. The field cannot be made editable.
	 *
	 * @param name
	 */
	@Nonnull
	public IDisplayControl< ? > addDisplayProp(@Nonnull @GProperty final String name) throws Exception {
		PropertyMetaModel< ? > pmm = resolveProperty(name);
		return addDisplayProp(name, pmm.getDefaultLabel());
	}

	/**
	 * Add an input for the specified property just as <code>addProp(String, String)</code>,
	 * only this input won't be editable (ever).
	 *
	 * @param name
	 * @param label
	 */
	@Nonnull
	public <T> IDisplayControl< ? > addDisplayProp(@Nonnull @GProperty final String name, @Nonnull String label) throws Exception {
		PropertyMetaModel<T> pmm = (PropertyMetaModel<T>) resolveProperty(name);
		DisplayValue<T> dv = new DisplayValue<T>(pmm.getActualType());
		builder().addControl(label, null, new NodeBase[]{dv}, false, false, pmm);

		if(label != null) {
			dv.setErrorLocation(label);
		}
		builder().getBindingContext().joinbinding(getModel().getValue(), name, dv, "value");
		return dv;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Adding user-created controls, with binding.			*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a user-specified control for a given property and add a {@link IModelBinding}. To add
	 * without a binding use {@link #addPropertyAndControl(String, NodeBase, boolean)}.
	 *
	 * @param propertyname
	 * @param ctl
	 * @throws Exception
	 */
	@Nonnull
	public <V, C extends NodeBase & IControl<V>> IControl<V> addProp(@Nonnull @GProperty final String propertyname, @Nonnull final C ctl) throws Exception {
		return addProp(propertyname, ctl, null);
	}

	@Nonnull
	public <V, C extends NodeBase & IControl<V>> IControl<V> addProp(@Nonnull @GProperty final String propertyname, @Nonnull final C ctl, @Nullable IJoinConverter< ? , ? > converter) throws Exception {
		return addProp(propertyname, null, ctl, converter);
	}

	/**
	 * Adds a user-created control that has a {@link IModelBinding} to the specified property, and a user-specified label.
	 *
	 * @param name
	 * @param label		The label text to use. Use the empty string to prevent a label from being generated. This still adds an empty cell for the label though.
	 * @param ctl
	 * @throws Exception
	 */
	@Nonnull
	public <V, C extends NodeBase & IControl<V>> IControl<V> addProp(@Nonnull @GProperty final String name, @Nonnull String label, @Nonnull final C ctl) throws Exception {
		return addProp(name, label, ctl, null);
	}

	@Nonnull
	public <V, C extends NodeBase & IControl<V>> IControl<V> addProp(@Nonnull String name, @Nullable String label, @Nonnull final C ctl, @Nullable IJoinConverter< ? , ? > converter) throws Exception {
		PropertyMetaModel<V> pmm = (PropertyMetaModel<V>) resolveProperty(name);
		if(null == label)
			label = pmm.getDefaultLabel();

		builder().addControl(label, ctl, new NodeBase[]{ctl}, ctl.isMandatory(), true, pmm); // Since this is a full control it is editable
		if(label != null)
			ctl.setErrorLocation(label);
		JoinBinding binding = builder().getBindingContext().joinbinding(getModel().getValue(), pmm.getName(), ctl, "value");
		if(null != converter)
			binding.converter(converter);

		bindOptionals(ctl);
		return ctl;
	}

	@Nonnull
	public <V, C extends NodeBase & IDisplayControl<V>> IDisplayControl<V> addDisplayProp(@Nonnull @GProperty final String propertyname, @Nonnull final C ctl) throws Exception {
		PropertyMetaModel<V> pmm = (PropertyMetaModel<V>) resolveProperty(propertyname);
		String label = pmm.getDefaultLabel();
		builder().addControl(label, ctl, new NodeBase[]{ctl}, false, true, pmm);
		if(label != null)
			ctl.setErrorLocation(label);
		builder().getBindingContext().joinbinding(getModel().getValue(), propertyname, ctl, "value");
		return ctl;
	}

	/**
	 * This adds whatever thing you want as a "control" to the control part of a form <b>without binding</b>. The label
	 * is obtained from the property. The only reason the property is passed is to use it's metadata to define it's
	 * default label.
	 *
	 * @param propertyName
	 * @param nb
	 * @param mandatory
	 */
	public void addPropertyLabelAndControl(@Nonnull @GProperty final String propertyName, @Nonnull final NodeBase nb, final boolean mandatory) {
		PropertyMetaModel< ? > pmm = resolveProperty(propertyName);
		String label = pmm.getDefaultLabel();

		// FIXME Kludge to determine if the control is meant to be editable!
		boolean editable = false;
		if(nb instanceof IControl< ? >) {
			IControl< ? > c = (IControl< ? >) nb;
			editable = !c.isDisabled();
			if(editable && builder().getRight() == AllowedRight.READ)
				editable = false;
		}

		builder().addControl(label, nb, new NodeBase[]{nb}, mandatory, editable, pmm);
		if(label != null)
			nb.setErrorLocation(label);
	}


	/**
	 *
	 * @param b
	 * @param names
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	private IControl< ? >[] addPropertyList(boolean editable, @Nonnull String[] names) throws Exception {
		builder().startBulkLayout();
		IControl< ? >[] res = new IControl< ? >[names.length];
		int ix = 0;
		for(String name : names) {
			if(editable)
				res[ix] = addProp(name);
			else
				res[ix] = addDisplayProp(name);
			ix++;
		}
		builder().endBulkLayout();
		return res;
	}


	/**
	 * Add the specified properties to the form, in the current mode. Watch out: if a
	 * MODIFIER is in place the modifier is obeyed for <b>all properties</b>, not for
	 * the first one only!! This means that when this gets called like:
	 * <pre>
	 * 	f.append().addProps("a", "b","c");
	 * </pre>
	 * all three fields are appended to the current row.
	 *
	 * @param names
	 * @throws Exception
	 */
	@Nonnull
	public IControl< ? >[] addProps(@Nonnull @GProperty final String... names) throws Exception {
		return addPropertyList(true, names);
	}

	/**
	 * Add the specified properties to the form as display-only properties, in the current
	 * mode. Watch out: if a MODIFIER is in place the modifier is obeyed for <b>all
	 * properties</b>, not for the first one only!! This means that when this gets called
	 * like:
	 * <pre>
	 * 	f.append().addProps("a", "b","c");
	 * </pre>
	 * all three fields are appended to the current row.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param names
	 * @throws Exception
	 */
	@Nonnull
	public IControl< ? >[] addDisplayProps(@Nonnull @GProperty final String... names) throws Exception {
		return addPropertyList(false, names);
	}

}

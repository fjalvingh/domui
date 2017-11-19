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

import to.etc.domui.component.controlfactory.ControlFactoryResult;
import to.etc.domui.component.controlfactory.DisplayOnlyPropertyBinding;
import to.etc.domui.component.controlfactory.IModelBinding;
import to.etc.domui.component.controlfactory.SimpleComponentPropertyBinding;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IDisplayControl;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.util.IReadOnlyModel;
import to.etc.webapp.query.QField;

import javax.annotation.Nonnull;

/**
 * Deprecated: use {@link to.etc.domui.component2.form4.FormBuilder}.
 * Encapsulates basic actions that can be done with all form builder implementations,
 * and delegates the actual parts that require layout decisions to the actual
 * implementation.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 13, 2009
 */
@Deprecated
abstract public class GenericFormBuilder extends FormBuilderBase {
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
	abstract protected void addControl(String label, NodeBase labelnode, NodeBase[] list, boolean mandatory, boolean editable, PropertyMetaModel< ? > pmm);

	abstract protected void addControl(NodeBase label, NodeBase labelnode, NodeBase[] list, boolean mandatory, boolean editable, PropertyMetaModel< ? > pmm);

	abstract public void addContent(NodeBase label, NodeBase[] control, boolean editable);

	/**
	 * Add any kind of content into the next "control area". The content can be anything but none of it is "maintained"
	 * by the formbuilder. Adding the content can be combined with the usual layout methods to force explicit placement
	 * of the item added here.
	 * @param item
	 */
	public void addContent(@Nonnull NodeBase item) {
		addContent(null, new NodeBase[]{item}, false);
	}

	/**
	 * Handle placement of a list of property names, all obeying the current mode in effect.
	 * @param editable
	 * @param names
	 */
	abstract protected IControl< ? >[] addListOfProperties(boolean editable, final String... names);

	/**
	 * Complete the visual representation of this form, and return the node representing it.
	 *
	 * @return
	 */
	abstract NodeContainer finish();

	/**
	 * Default ctor.
	 */
	public GenericFormBuilder() {}

	/**
	 * Create one primed with a model and class.
	 * @param <T>
	 * @param clz
	 * @param mdl
	 */
	public <T> GenericFormBuilder(Class<T> clz, IReadOnlyModel<T> mdl) {
		super(clz, mdl);
	}

	/**
	 * {@inheritDoc}
	 * @param <T>
	 * @param instance
	 */
	public <T> GenericFormBuilder(T instance) {
		super(instance);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Core shared public interface - all builders.		*/
	/*--------------------------------------------------------------*/
	/**
	 * Add an input for the specified property. The property is based at the current input
	 * class. The input model is default (using metadata) and the property is labeled using
	 * the metadata-provided label.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param name
	 */
	public IControl< ? > addProp(final String name) {
		return addProp(name, (String) null);
	}

	/**
	 * @param field
	 * @return
	 */
	public <T> IControl<T> addProp(final QField< ? , T> field) {
		return (IControl<T>) addProp(field.toString());
	}

	/**
	 * Add an input for the specified property. The property is based at the current input
	 * class. The input model is default (using metadata) and the property is labeled.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param name
	 * @param label		The label text to use. Use the empty string to prevent a label from being generated. This still adds an empty cell for the label though.
	 */
	public IControl< ? > addProp(final String name, String label) {
		PropertyMetaModel< ? > pmm = resolveProperty(name);
		if(label == null)
			label = pmm.getDefaultLabel();
		boolean editable = true;
		if(pmm.getReadOnly() == YesNoType.YES)
			editable = false;
		return addPropertyControl(name, label, pmm, editable, false);
	}

	/**
	 * Add an input for the specified property. The property is based at the current input
	 * class. The input model is default (using metadata) and the property is labeled using
	 * the metadata-provided label.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param name
	 * @param label Add custom label.
	 * @param editable When false this adds a display-only field, when true a fully editable control.
	 * @param mandatory Specify if field is mandatory. This <b>always</b> overrides the mandatoryness of the metadata which is questionable.
	 */
	@Nonnull
	public <C> IControl<C> addProp(final String name, String label, final boolean editable, final boolean mandatory) {
		PropertyMetaModel<C> pmm = (PropertyMetaModel<C>) resolveProperty(name);

		//-- Check control permissions: does it have view permissions?
		final ControlFactoryResult r = createControlFor(getModel(), pmm, editable); // Add the proper input control for that type
		addControl(label, r.getLabelNode(), r.getNodeList(), mandatory, editable, pmm);
		NodeBase formControl = r.getFormControl();
		((IControl<C>) formControl).setMandatory(mandatory);
		formControl.setCalculcatedId(name);

		//-- jal 20090924 Bug 624 Assign the control label to all it's node so it can specify it in error messages
		if(label != null) {
			for(NodeBase b : r.getNodeList())
				b.setErrorLocation(label);
		}
		getBindings().add(new SimpleComponentPropertyBinding<C>(getModel(), pmm, (IControl<C>) r.getFormControl()));

		//		IModelBinding binding = r.getBinding();
		//		if(binding != null)
		//			getBindings().add(binding);
		//		else
		//			throw new IllegalStateException("No binding for a " + r);
		return (IControl<C>) r.getFormControl();
	}

	/**
	 * Add an input for the specified property. The property is based at the current input
	 * class. The input model is default (using metadata) and the property is labeled using
	 * the metadata-provided label.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param name
	 * @param editable When false add a display-only control, else add an editable control.
	 */
	public IControl< ? > addProp(final String name, final boolean editable) {
		if(editable) {
			return addProp(name);
		} else {
			return addDisplayProp(name);
		}
	}

	/**
	 * Add an input for the specified property. The property is based at the current input
	 * class. The input model is default (using metadata) and the property is labeled using
	 * the metadata-provided label.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param name
	 * @param editable When false this adds a display-only field, when true a fully editable control.
	 * @param mandatory Specify if field is mandatory. This <b>always</b> overrides the mandatoryness of the metadata which is questionable.
	 */
	public IControl< ? > addProp(final String name, final boolean editable, final boolean mandatory) {
		PropertyMetaModel< ? > pmm = resolveProperty(name);
		String label = pmm.getDefaultLabel();
		return addProp(name, label, editable, mandatory);
	}

	public void addContent(NodeBase label, NodeBase control, boolean editable) {
		addContent(label, new NodeBase[]{control}, editable);
	}

	public void addContent(String label, NodeBase control, boolean editable) {
		addContent(new Label(control, label), new NodeBase[]{control}, editable);
	}

	/**
	 * Add a display-only field for the specified property. The field cannot be made
	 * editable.
	 *
	 * @param name
	 */
	public IControl< ? > addDisplayProp(final String name) {
		return addDisplayProp(name, (String) null);
	}

	/**
	 * Add an input for the specified property just as <code>addProp(String, String)</code>,
	 * only this input won't be editable (ever).
	 *
	 * @param name
	 * @param label
	 */
	public IControl< ? > addDisplayProp(final String name, String label) {
		PropertyMetaModel< ? > pmm = resolveProperty(name);
		if(label == null)
			label = pmm.getDefaultLabel();
		return addPropertyControl(name, label, pmm, false, true);
	}

	/**
	 * Add a user-specified control for a given property. This adds the control, using
	 * the property-specified label and creates a binding for the property on the
	 * control. <i>If you only want to add the proper structure and find the label for
	 * a property use {@link TabularFormBuilder#addPropertyAndControl(String, NodeBase, boolean)}.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param propertyname
	 * @param ctl
	 */
	public <V, T extends NodeBase & IControl<V>> IControl<V> addProp(final String propertyname, final T ctl) {
		PropertyMetaModel<V> pmm = (PropertyMetaModel<V>) resolveProperty(propertyname);
		String label = pmm.getDefaultLabel();
		addControl(label, ctl, new NodeBase[]{ctl}, ctl.isMandatory(), true, pmm); // Since this is a full control it is editable
		if(label != null)
			ctl.setErrorLocation(label);
		SimpleComponentPropertyBinding<V> b = new SimpleComponentPropertyBinding<V>(getModel(), pmm, ctl);
		getBindings().add(b);
		ctl.setCalculcatedId(propertyname);
		return ctl;
	}

	public <V, T extends NodeBase & IDisplayControl<V>> IControl<V> addDisplayProp(final String propertyname, final T ctl) {
		PropertyMetaModel<V> pmm = (PropertyMetaModel<V>) resolveProperty(propertyname);
		String label = pmm.getDefaultLabel();
		addControl(label, ctl, new NodeBase[]{ctl}, false, false, pmm); // Since this is a full control it is editable
		if(label != null)
			ctl.setErrorLocation(label);
		DisplayOnlyPropertyBinding<V> b = new DisplayOnlyPropertyBinding<V>(getModel(), pmm, ctl);
		getBindings().add(b);
		ctl.setCalculcatedId(propertyname);
		return ctl;
	}


	/**
	 * Add a user-specified control for a given property. This adds the control, using
	 * the specified label and creates a binding for the property on the
	 * control.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param name
	 * @param label		The label text to use. Use the empty string to prevent a label from being generated. This still adds an empty cell for the label though.
	 * @param ctl
	 */
	public <V, T extends NodeBase & IControl<V>> IControl<V> addProp(final String name, String label, final T ctl) {
		PropertyMetaModel<V> pmm = (PropertyMetaModel<V>) resolveProperty(name);
		addControl(label, ctl, new NodeBase[]{ctl}, ctl.isMandatory(), true, pmm); // Since this is a full control it is editable
		if(label != null)
			ctl.setErrorLocation(label);
		SimpleComponentPropertyBinding<V> b = new SimpleComponentPropertyBinding<V>(getModel(), pmm, ctl);
		getBindings().add(b);
		return ctl;
	}


	/**
	 * Add a fully manually specified label and control to the layout. This does not create any binding.
	 * @param label
	 * @param control
	 * @param mandatory
	 */
	public void addLabelAndControl(final String label, final NodeBase control, final boolean mandatory) {
		//-- jal 20090924 Bug 624 Assign the control label to all it's node so it can specify it in error messages
		if(label != null)
			control.setErrorLocation(label);

		// FIXME Kludge to determine if the control is meant to be editable!
		boolean editable = control instanceof IControl< ? >;

		addControl(label, control, new NodeBase[]{control}, mandatory, editable, null);
		if(null != label)
			control.setCalculcatedId(label);
	}

	/**
	 * Add a fully manually specified label and control to the layout. This does not create any binding. Since label caption can contain extra characters, error location can be assigned additionaly.
	 * @param label
	 * @param errorLocation
	 * @param control
	 * @param mandatory
	 */
	public void addLabelAndControl(final Label label, final String errorLocation, final NodeBase control, final boolean mandatory) {
		//-- jal 20090924 Bug 624 Assign the control label to all it's node so it can specify it in error messages
		if(errorLocation != null)
			control.setErrorLocation(errorLocation);

		// FIXME Kludge to determine if the control is meant to be editable!
		boolean editable = control instanceof IControl< ? >;

		addControl(label, control, new NodeBase[]{control}, mandatory, editable, null);
	}


	/**
	 *
	 * @param name
	 * @param label
	 * @param pmm
	 * @param editable  when false, the rendered control will be display-only.
	 * @return	If the property was created and is controllable this will return an IControl instance. This will explicitly <i>not</i> be
	 * 			created if the control is display-only, not allowed by permissions or simply uncontrollable (the last one is uncommon).
	 */
	protected <C> IControl<C> addPropertyControl(final String name, final String label, final PropertyMetaModel<C> pmm, final boolean editable, final boolean editableFixed) {
		//-- Check control permissions: does it have view permissions?
		final ControlFactoryResult r = createControlFor(getModel(), pmm, editable); // Add the proper input control for that type
		addControl(label, r.getLabelNode(), r.getNodeList(), pmm.isRequired(), editable, pmm);

		//-- jal 20090924 Bug 624 Assign the control label to all it's node so it can specify it in error messages
		if(label != null) {
			for(NodeBase b : r.getNodeList())
				b.setErrorLocation(label);
		}
		IModelBinding b;
		if(editableFixed && !editable) {
			b = new DisplayOnlyPropertyBinding<C>(getModel(), pmm, (IDisplayControl<C>) r.getFormControl());
		} else {
			b = new SimpleComponentPropertyBinding<C>(getModel(), pmm, (IControl<C>) r.getFormControl());
		}

		getBindings().add(b);
		IControl<C> formControl = (IControl<C>) r.getFormControl();
		if(formControl instanceof NodeBase) {
			((NodeBase) formControl).setCalculcatedId(name);
		}

		return formControl;
	}


	/**
	 * This adds a fully user-specified control for a given property with it's default label,
	 * without creating <i>any<i> binding. The only reason the property is passed is to use
	 * it's metadata to define it's access rights and default label.
	 *
	 * @param propertyName
	 * @param nb
	 * @param mandatory
	 */
	public void addPropertyAndControl(final String propertyName, final NodeBase nb, final boolean mandatory) {
		PropertyMetaModel< ? > pmm = resolveProperty(propertyName);
		String label = pmm.getDefaultLabel();

		// FIXME Kludge to determine if the control is meant to be editable!
		boolean editable = nb instanceof IControl< ? >;

		addControl(label, nb, new NodeBase[]{nb}, mandatory, editable, pmm);
		if(label != null)
			nb.setErrorLocation(label);
		nb.setCalculcatedId(propertyName);
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
	 * FORMAL-INTERFACE.
	 *
	 * @param names
	 */
	public IControl< ? >[] addProps(final String... names) {
		return addListOfProperties(true, names);
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
	 */
	public IControl< ? >[] addDisplayProps(final String... names) {
		return addListOfProperties(false, names);
	}
}

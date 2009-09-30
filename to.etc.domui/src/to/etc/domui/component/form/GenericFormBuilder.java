package to.etc.domui.component.form;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Encapsulates basic actions that can be done with all form builder implementations,
 * and delegates the actual parts that require layout decisions to the actual
 * implementation.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 13, 2009
 */
abstract public class GenericFormBuilder extends FormBuilderBase {
	/**
	 * This is the actual workhorse doing the per-builder actual placement and layouting of a {control, label} pair.
	 *
	 * @param label
	 * @param labelnode
	 * @param list
	 * @param mandatory
	 * @param pmm
	 */
	abstract protected void addControl(final String label, final NodeBase labelnode, final NodeBase[] list, final boolean mandatory, PropertyMetaModel pmm);

	/**
	 * Handle placement of a list of property names, all obeying the current mode in effect.
	 * @param editable
	 * @param names
	 */
	abstract protected IFormControl[] addListOfProperties(boolean editable, final String... names);

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
	public IFormControl addProp(final String name) {
		return addProp(name, (String) null);
	}

	/**
	 * Add an input for the specified property just as <code>addProp(String)</code>,
	 * only this input won't be editable.
	 *
	 * @param name
	 */
	public IFormControl addReadOnlyProp(final String name) {
		return addReadOnlyProp(name, null);
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
	public IFormControl addProp(final String name, String label) {
		PropertyMetaModel pmm = resolveProperty(name);
		if(label == null)
			label = pmm.getDefaultLabel();
		boolean edit = true;
		if(pmm.getReadOnly() == YesNoType.YES)
			edit = false;
		return addPropertyControl(name, label, pmm, edit);
	}

	/**
	 * Add an input for the specified property just as <code>addProp(String, String)</code>,
	 * only this input won't be editable.
	 *
	 * @param name
	 * @param label
	 */
	public IFormControl addReadOnlyProp(final String name, String label) {
		PropertyMetaModel pmm = resolveProperty(name);
		if(label == null)
			label = pmm.getDefaultLabel();
		return addPropertyControl(name, label, pmm, false);
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
	public <T extends NodeBase & IInputNode< ? >> IFormControl addProp(final String propertyname, final T ctl) {
		PropertyMetaModel pmm = resolveProperty(propertyname);
		String label = pmm.getDefaultLabel();
		addControl(label, ctl, new NodeBase[]{ctl}, ctl.isMandatory(), pmm);
		if(label != null)
			ctl.setErrorLocation(label);
		SimpleComponentPropertyBinding b = new SimpleComponentPropertyBinding(getModel(), pmm, ctl);
		getBindings().add(b);
		return b;
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
		addControl(label, control, new NodeBase[]{control}, mandatory, null);
	}

	/**
	 * Add an input for the specified property. The property is based at the current input
	 * class. The input model is default (using metadata) and the property is labeled using
	 * the metadata-provided label.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param name
	 * @param readOnly In case of readOnly set to true behaves same as addReadOnlyProp.
	 */
	public IFormControl addProp(final String name, final boolean readOnly) {
		if(readOnly) {
			return addReadOnlyProp(name);
		} else {
			return addProp(name);
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
	 * @param readOnly In case of readOnly set to true behaves same as addReadOnlyProp.
	 * @param mandatory Specify if field is mandatory. This <b>always</b> overrides the mandatoryness of the metadata which is questionable.
	 */
	public IFormControl addProp(final String name, final boolean readOnly, final boolean mandatory) {
		PropertyMetaModel pmm = resolveProperty(name);
		String label = pmm.getDefaultLabel();

		//-- Check control permissions: does it have view permissions?
		if(!rights().calculate(pmm))
			return null;
		final ControlFactory.Result r = createControlFor(getModel(), pmm, !readOnly && rights().isEditable()); // Add the proper input control for that type
		addControl(label, r.getLabelNode(), r.getNodeList(), mandatory, pmm);

		//-- jal 20090924 Bug 624 Assign the control label to all it's node so it can specify it in error messages
		if(label != null) {
			for(NodeBase b : r.getNodeList())
				b.setErrorLocation(label);
		}

		if(r.getBinding() != null)
			getBindings().add(r.getBinding());
		else
			throw new IllegalStateException("No binding for a " + r);
		return r.getFormControl();
	}

	/**
	 *
	 * @param name
	 * @param label
	 * @param pmm
	 * @param editPossible, when false, the rendered control will be display-only and cannot be changed back to EDITABLE.
	 * @return	If the property was created and is controllable this will return an IFormControl instance. This will explicitly <i>not</i> be
	 * 			created if the control is readonly, not allowed by permissions or simply uncontrollable (the last one is uncommon).
	 */
	protected IFormControl addPropertyControl(final String name, final String label, final PropertyMetaModel pmm, final boolean editPossible) {
		//-- Check control permissions: does it have view permissions?
		if(!rights().calculate(pmm))
			return null;
		final ControlFactory.Result r = createControlFor(getModel(), pmm, editPossible && rights().isEditable()); // Add the proper input control for that type
		addControl(label, r.getLabelNode(), r.getNodeList(), pmm.isRequired(), pmm);

		//-- jal 20090924 Bug 624 Assign the control label to all it's node so it can specify it in error messages
		if(label != null) {
			for(NodeBase b : r.getNodeList())
				b.setErrorLocation(label);
		}

		if(r.getBinding() != null)
			getBindings().add(r.getBinding());
		else
			throw new IllegalStateException("No binding for a " + r);
		return r.getFormControl();
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
		PropertyMetaModel pmm = resolveProperty(propertyName);
		String label = pmm.getDefaultLabel();
		addControl(label, nb, new NodeBase[]{nb}, mandatory, pmm);
		if(label != null)
			nb.setErrorLocation(label);
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
	public IFormControl[] addProps(final String... names) {
		return addListOfProperties(true, names);
		//		return this;
	}

	/**
	 * Add the specified properties to the form as READONLY properties, in the current
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
	public IFormControl[] addReadOnlyProps(final String... names) {
		return addListOfProperties(false, names);
		//		return this;
	}
}

package to.etc.domui.component.form;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * A factory which creates the correct EDITING control to edit a property, specified by the property's
 * PropertyMetaModel. The DomApplication will contain a list of ControlFactories. When an edit control
 * is needed this list is obtained and each ControlFactory in it has it's accepts() method called. This
 * returns a "score" for each control factory. The first factory with the highest score (which must be
 * > 0) will be used to create the control. If no factory returns a &gt; 0 score a control cannot be
 * created which usually results in an exception.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 8, 2008
 */
public interface ControlFactory {
	/**
	 * Represents the result of a call to createControl.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jul 2, 2009
	 */
	static final public class Result {
		/** The list of nodes forming the control */
		private final NodeBase[] m_nodeList;

		/** The binding of the control to it's model and property */
		private final IModelBinding m_binding;

		/** The node to be used as the target for a "label" */
		private final NodeBase m_labelNode;

		/** The FormControl handle for the created control */
		private IFormControl m_handle;

		public Result(final IModelBinding binding, final NodeBase labelNode, final NodeBase[] nodeList) {
			m_binding = binding;
			m_labelNode = labelNode;
			m_nodeList = nodeList;
		}

		public Result(final IModelBinding binding, final NodeBase control) {
			m_binding = binding;
			m_labelNode = control;
			m_nodeList = new NodeBase[]{control};
		}

		public Result(final IModelBinding binding, IFormControl fc, final NodeBase control) {
			m_binding = binding;
			m_labelNode = control;
			m_handle = fc;
			m_nodeList = new NodeBase[]{control};
		}

		public <T extends NodeBase & IInputNode< ? >> Result(final T control, final IReadOnlyModel< ? > model, final PropertyMetaModel pmm) {
			m_labelNode = control;
			m_nodeList = new NodeBase[]{control};
			SimpleComponentPropertyBinding b = new SimpleComponentPropertyBinding(model, pmm, control);
			m_binding = b;
			m_handle = b;
		}

		public NodeBase[] getNodeList() {
			return m_nodeList;
		}

		public IModelBinding getBinding() {
			return m_binding;
		}

		public NodeBase getLabelNode() {
			return m_labelNode;
		}

		public IFormControl getFormControl() {
			if(m_handle != null)
				return m_handle;
			return null;
		}
	}

	/**
	 * This must return a +ve value when this factory accepts the specified property; the returned value
	 * is an eagerness score. The factory returning the highest eagerness wins.
	 * @param pmm
	 * @param editable
	 * @param controlClass When set the control factory *must* be able to return a component which is assignment-compatible with this class type. If it cannot it MUST refuse to create the control.
	 * @return
	 */
	int accepts(PropertyMetaModel pmm, boolean editable, Class< ? > controlClass);

	/**
	 * This MUST create all nodes necessary for a control to edit the specified item. The nodes must be added
	 * to the container; this <i>must</i> return a ModelBinding to bind and unbind a value to the control
	 * created.
	 *
	 * @param container
	 * @param pmm
	 * @param editable
	 * @param controlClass	When set the control factory *must* return a component which is assignment-compatible with this
	 * 						class type. When this method is called it has already (by it's accept method) told us it can, so
	 * 						not creating the proper type is not an option.
	 * @return
	 */
	Result createControl(IReadOnlyModel< ? > model, PropertyMetaModel pmm, boolean editable, Class< ? > controlClass);

	static public final ControlFactory TEXTAREA_CF = new ControlFactoryTextArea();

	/**
	 * This is a fallback factory; it accepts anything and shows a String edit component for it. It
	 * hopes that the Text<?> control can convert the string input value to the actual type using the
	 * registered Converters. This is also the factory for regular Strings.
	 */
	static public final ControlFactory STRING_CF = new ControlFactoryString();

	static public final ControlFactory BOOLEAN_AND_ENUM_CF = new ControlFactoryEnumAndBool();

	static public final ControlFactory DATE_CF = new ControlFactoryDate();

	/**
	 * Factory for UP relations. This creates a combobox input if the property is an
	 * UP relation and has combobox properties set.
	 */
	static public final ControlFactory RELATION_COMBOBOX_CF = new ControlFactoryRelationCombo();

	static public final ControlFactory RELATION_LOOKUP_CF = new ControlFactoryRelationLookup();
}

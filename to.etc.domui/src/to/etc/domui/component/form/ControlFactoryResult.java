package to.etc.domui.component.form;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Represents the result of a call to createControl.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
public class ControlFactoryResult {
	/** The list of nodes forming the control */
	private final NodeBase[] m_nodeList;

	/** The binding of the control to it's model and property */
	private final IModelBinding m_binding;

	/** The node to be used as the target for a "label" */
	private final NodeBase m_labelNode;

	/** The FormControl handle for the created control */
	private IControl< ? > m_handle;

	// jal 20091206 tentative removal of unused/unusable constructors because they do not expose the IFormControl interface
	//		public ControlFactoryResult(final IModelBinding binding, final NodeBase labelNode, final NodeBase[] nodeList) {
	//			m_binding = binding;
	//			m_labelNode = labelNode;
	//			m_nodeList = nodeList;
	//		}

	//		public ControlFactoryResult(final IModelBinding binding, final NodeBase control) {
	//			m_binding = binding;
	//			m_labelNode = control;
	//			m_nodeList = new NodeBase[]{control};
	//		}
	public ControlFactoryResult(final IModelBinding binding, IControl< ? > fc, final NodeBase control) {
		m_binding = binding;
		m_labelNode = control;
		m_handle = fc;
		m_nodeList = new NodeBase[]{control};
	}

	public ControlFactoryResult(IControl< ? > handle, NodeBase[] nodeList, NodeBase labelNode, IModelBinding binding) {
		m_handle = handle;
		m_nodeList = nodeList;
		m_labelNode = labelNode;
		m_binding = binding;
	}

	public <M, C> ControlFactoryResult(final IInputNode<C> control, final IReadOnlyModel<M> model, final PropertyMetaModel pmm) {
		m_labelNode = (NodeBase) control;
		m_nodeList = new NodeBase[]{(NodeBase) control};
		SimpleComponentPropertyBinding<C> b = new SimpleComponentPropertyBinding<C>(model, pmm, control);
		m_binding = b;
		m_handle = b;

		//-- 20091208 jal Experimental: also bind to treemodel ModelBinding
		control.bind().to(model, pmm);
	}

	public <A, B> ControlFactoryResult(final IDisplayControl<A> control, final IReadOnlyModel<B> model, final PropertyMetaModel pmm) {
		m_labelNode = (NodeBase) control;
		m_nodeList = new NodeBase[]{(NodeBase) control};
		DisplayOnlyPropertyBinding<A> b = new DisplayOnlyPropertyBinding<A>(model, pmm, control);
		m_binding = b;
		m_handle = b;

		//-- 20091208 jal Experimental: also bind to treemodel ModelBinding
		((IBindable) control).bind().to(model, pmm);
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

	public IControl< ? > getFormControl() {
		if(m_handle != null) // 20091206 jal WTF??
			return m_handle;
		return null;
	}
}
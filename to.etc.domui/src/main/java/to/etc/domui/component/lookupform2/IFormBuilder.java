package to.etc.domui.component.lookupform2;

import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-12-17.
 */
public interface IFormBuilder {
	/** Defines the target node for the form to be built. */
	void setTarget(NodeContainer target) throws Exception;

	void append(NodeBase label, NodeBase control) throws Exception;
}

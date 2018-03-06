package to.etc.domui.component2.form4;

import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 6-3-18.
 */
public interface IFormLayouter {
	void setHorizontal(boolean horizontal);

	void addControl(NodeBase control, NodeContainer lbl, String controlCss, String labelCss);

	void clear();

}

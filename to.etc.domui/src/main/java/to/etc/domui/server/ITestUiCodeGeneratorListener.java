package to.etc.domui.server;

import to.etc.domui.dom.html.NodeBase;

public interface ITestUiCodeGeneratorListener {

	boolean isShownFor(NodeBase node);

	void showFor(NodeBase node);
}

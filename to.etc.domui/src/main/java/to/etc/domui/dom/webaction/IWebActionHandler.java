package to.etc.domui.dom.webaction;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.server.RequestContextImpl;

public interface IWebActionHandler {
	void handleWebAction(@NonNull NodeBase node, @NonNull RequestContextImpl context, boolean responseExpected) throws Exception;
}

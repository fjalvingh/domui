package to.etc.domui.dom.webaction;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;

public interface IWebActionHandler {
	void handleWebAction(@Nonnull NodeBase node, @Nonnull RequestContextImpl context, boolean responseExpected) throws Exception;
}

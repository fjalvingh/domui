package to.etc.domui.dom.errors;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;

public interface IExceptionListener {
	public boolean	handleException(RequestContext ctx, Page pg, NodeBase source, Throwable t) throws Exception;
}

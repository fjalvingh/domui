package to.etc.domui.dom.html;

import to.etc.domui.server.IRequestContext;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-4-18.
 */
public interface IWebActionListener {
	boolean onAction(String actionName, IRequestContext context) throws Exception;
}

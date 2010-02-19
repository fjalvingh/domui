package to.etc.domui.server;

import to.etc.domui.dom.html.*;

/**
 * Factory used to create the appropriate error displaying fragment
 * used for controls that handle their own errors.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 9, 2009
 */
public interface IControlErrorFragmentFactory {
	/**
	 * Create the control used to display in-control error messages.
	 * @param <T>
	 * @return
	 */
	NodeContainer createErrorFragment();
}

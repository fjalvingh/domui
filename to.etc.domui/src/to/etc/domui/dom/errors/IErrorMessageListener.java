package to.etc.domui.dom.errors;

import to.etc.domui.dom.html.*;

/**
 * A listener for errors that occur in handling the page. An error message
 * listener is responsible for altering the page in such a way that the error
 * becomes visible to the user, and to remove error indications that are no
 * longer present.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 12, 2008
 */
public interface IErrorMessageListener {
	public void		errorMessageAdded(Page pg, UIMessage m);
	public void		errorMessageRemoved(Page pg, UIMessage m);
}

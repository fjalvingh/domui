package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.UrlPage;

/**
 * An action request as part of a GOTO, to be executed on the page gone to.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 22, 2013
 */
public interface IGotoAction {
	void executeAction(@NonNull UrlPage page) throws Exception;
}

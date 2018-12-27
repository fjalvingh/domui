package to.etc.domui.subinjector;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.SubPage;

/**
 * A single thingy doing some injection task in a SubPage.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-12-18.
 */
public interface ISubPageInjector {
	void inject(@NonNull SubPage page) throws Exception;
}

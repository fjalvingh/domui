package to.etc.domui.component.layout;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Page;

/**
 * Used by button container components to have the {@link ButtonFactory} delegate button creation events to.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 11, 2013
 */
public interface IButtonContainer {
	void addButton(@NonNull NodeBase thing, int order);

	@NonNull Page getPage();
}

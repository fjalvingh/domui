package to.etc.domui.component.layout;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * Used by button container components to have the {@link ButtonFactory} delegate button creation events to.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 11, 2013
 */
public interface IButtonContainer {
	void addButton(@Nonnull NodeBase thing, int order);

	@Nonnull Page getPage();
}

package to.etc.domui.component.layout;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.*;

/**
 * A simple div acting as a content panel, with margins all around.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 10, 2013
 */
public class ContentPanel extends Div {
	public ContentPanel() {
		setCssClass("ui-cpnl");
	}

	@NonNull
	@Override
	public ContentPanel css(@NonNull String... classNames) {
		return (ContentPanel) super.css(classNames);
	}
}

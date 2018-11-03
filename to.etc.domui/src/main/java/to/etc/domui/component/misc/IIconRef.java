package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.NodeBase;

/**
 * interface for things accepting different kinds of icons.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-10-18.
 */
public interface IIconRef {
	/**
	 * Create the appropriate icon node for the reference, depending on the reference's type.
	 */
	@NonNull NodeBase createNode();

	/**
	 * Add css to the icon at creation time.
	 */
	@NonNull IIconRef css(@NonNull String... classes);
}

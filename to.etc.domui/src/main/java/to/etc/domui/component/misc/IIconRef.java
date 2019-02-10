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
	@NonNull String getClasses();

	@NonNull NodeBase createNode(@NonNull String cssClasses);

	/**
	 * Create the appropriate icon node for the reference, depending on the reference's type.
	 */
	@NonNull default NodeBase createNode() {
		return createNode(getClasses());
	}

	/**
	 * Create a new icon ref that adds the specified css classes.
	 */
	@NonNull default IIconRef css(@NonNull String... classes) {
		return new WrappedIconRef(this, classes);
	}
}

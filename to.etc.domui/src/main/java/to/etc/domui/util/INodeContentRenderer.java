package to.etc.domui.util;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-3-18.
 */
public interface INodeContentRenderer<T> extends IRenderInto<T> {
	void renderNodeContent(@NonNull NodeBase component, @NonNull NodeContainer node, @Nullable T object, @Nullable Object parameters) throws Exception;

	@Override
	default void render(@NonNull NodeContainer node, @NonNull T object) throws Exception {
		renderNodeContent(node, node, object, null);
	}
}

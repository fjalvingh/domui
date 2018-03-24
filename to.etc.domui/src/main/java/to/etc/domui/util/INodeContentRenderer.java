package to.etc.domui.util;

import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-3-18.
 */
public interface INodeContentRenderer<T> extends IRenderInto<T> {
	void renderNodeContent(@Nonnull NodeBase component, @Nonnull NodeContainer node, @Nullable T object, @Nullable Object parameters) throws Exception;

	@Override
	default void render(@Nonnull NodeContainer node, @Nonnull T object) throws Exception {
		renderNodeContent(node, node, object, null);
	}
}

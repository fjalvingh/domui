package to.etc.domui.util;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeContainer;

/**
 * Renderer that renders the value specified in a DomUI node, when the value is
 * guaranteed to be not null. This replaces, at selected places,
 * the INodeContentRenderer interface.
 *
 * @since 2.0
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-9-17.
 */
public interface IRenderInto<T> {
	void render(@NonNull NodeContainer node, @NonNull T object) throws Exception;

	default void renderOpt(@NonNull NodeContainer node, @Nullable T object) throws Exception {
		if(null != object)
			render(node, object);
	}
}

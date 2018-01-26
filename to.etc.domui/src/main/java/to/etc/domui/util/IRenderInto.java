package to.etc.domui.util;

import to.etc.domui.dom.html.NodeContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
	void render(@Nonnull NodeContainer node, @Nonnull T object) throws Exception;

	default void renderOpt(@Nonnull NodeContainer node, @Nullable T object) throws Exception {
		if(null != object)
			render(node, object);
	}
}

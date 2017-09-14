package to.etc.domui.util;

import to.etc.domui.dom.html.NodeContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Renderer that renders the value specified in a DomUI node, when the value can
 * be null. This replaces, at selected places, the {@link INodeContentRenderer}
 * interface. For a nonnull version (preferred) see {@link IRenderInto}.
 *
 * @since 2.0
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-9-17.
 */
public interface IRenderIntoOpt<T> {
	void render(@Nonnull NodeContainer node, @Nullable T object) throws Exception;
}

package to.etc.domui.component.binding;

import to.etc.domui.dom.html.NodeBase;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 13-3-17.
 */
public interface IBindingHandlerFactory {
	@Nonnull
	IBindingHandler		getBindingHandler(@Nonnull NodeBase node);
}

package to.etc.domui.component.binding;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.NodeBase;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 13-3-17.
 */
public interface IBindingHandlerFactory {
	@NonNull
	IBindingHandler		getBindingHandler(@NonNull NodeBase node);
}

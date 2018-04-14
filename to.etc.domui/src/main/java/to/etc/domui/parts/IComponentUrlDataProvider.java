package to.etc.domui.parts;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.state.PageParameters;

/**
 * When implemented on a NodeBase component, this can be used to accept HTTP requests that target
 * a component. The component can return any data stream it wants. Example usage: say a component
 * is implemented with an iframe inside it. That iframe needs a "src=" URL to get it's content,
 * and the request will enter the server on a separate connection. You can let the component itself
 * provide the content for the iframe by implementing this interface, and crafting the URL to put
 * in the iframe using {@link NodeBase#getComponentDataURL}. You can pass parameters into that
 * URL using a {@link PageParameters} which you can then use inside the component to influence
 * what you generate.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 12, 2013
 */
public interface IComponentUrlDataProvider {
	void provideUrlData(@NonNull RequestContextImpl parameterSource) throws Exception;
}

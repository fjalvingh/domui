package to.etc.domui.util.resources;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.server.DomApplication;

/**
 * A factory which can provide for a resource depending on it's name.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 14, 2011
 */
public interface IResourceFactory {
	/**
	 * Return a &gt; 0 value when this factory can provide (recognises the name format) for this
	 * resource. The return value is a score; the factory returning the highest score will win.
	 * Accepting a resource does not imply that the resource actually <i>exists</i>.
	 * @param name
	 * @return
	 */
	int accept(@NonNull String name);

	/**
	 * Create the ref for the resource.
	 * @param name
	 * @param rdl
	 * @return
	 * @throws Exception
	 */
	@NonNull
	IResourceRef getResource(@NonNull DomApplication da, @NonNull String name, @NonNull IResourceDependencyList rdl) throws Exception;
}

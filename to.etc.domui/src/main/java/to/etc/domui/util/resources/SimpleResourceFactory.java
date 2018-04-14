package to.etc.domui.util.resources;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.server.DomApplication;

/**
 * Handles all normal $xxx type resources. These either come from web files (default) or if not found there
 * from a class resource below /resources/.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 14, 2011
 */
public class SimpleResourceFactory implements IResourceFactory {
	@Override
	public int accept(@NonNull String name) {
		if(name.startsWith("$"))
			return 10;
		return -1;
	}

	@Override
	@NonNull
	public IResourceRef getResource(@NonNull DomApplication da, @NonNull String name, @NonNull IResourceDependencyList rdl) throws Exception {
		IResourceRef r = da.getAppFileOrResource(name.substring(1));
		if(null != rdl)
			rdl.add(r);
		return r;
	}
}

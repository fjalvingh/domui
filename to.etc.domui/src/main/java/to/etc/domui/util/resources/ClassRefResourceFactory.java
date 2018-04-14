package to.etc.domui.util.resources;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.server.DomApplication;
import to.etc.domui.util.Constants;

/**
 * Handles the $REF resource which explicitly refers to a resource in the classpath.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 14, 2011
 */
public class ClassRefResourceFactory implements IResourceFactory {
	@Override
	public int accept(@NonNull String name) {
		if(name.startsWith(Constants.RESOURCE_PREFIX))
			return 10;
		return -1;
	}

	@Override
	@NonNull
	public IResourceRef getResource(@NonNull DomApplication da, @NonNull String name, @NonNull IResourceDependencyList rdl) throws Exception {
		IResourceRef r = da.createClasspathReference(name.substring(Constants.RESOURCE_PREFIX.length() - 1)); // Strip off $RES, rest is absolute resource path starting with /
		if(null != rdl)
			rdl.add(r);
		return r;
	}
}

package to.etc.domui.util.resources;

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * Handles the $REF resource which explicitly refers to a resource in the classpath.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 14, 2011
 */
public class ClassRefResourceFactory implements IResourceFactory {
	@Override
	public int accept(@Nonnull String name) {
		if(name.startsWith(Constants.RESOURCE_PREFIX))
			return 10;
		return -1;
	}

	@Override
	@Nonnull
	public IResourceRef getResource(@Nonnull DomApplication da, @Nonnull String name, @Nonnull IResourceDependencyList rdl) throws Exception {
		IResourceRef r = da.createClasspathReference(name.substring(Constants.RESOURCE_PREFIX.length() - 1)); // Strip off $RES, rest is absolute resource path starting with /
		if(null != rdl)
			rdl.add(r);
		return r;
	}
}

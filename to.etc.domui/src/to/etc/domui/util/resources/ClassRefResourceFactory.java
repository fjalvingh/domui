package to.etc.domui.util.resources;

import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * Handles the $REF resource.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 14, 2011
 */
public class ClassRefResourceFactory implements IResourceFactory {
	@Override
	public int accept(String name) {
		if(name.startsWith(Constants.RESOURCE_PREFIX))
			return 10;
		return -1;
	}

	@Override
	public IResourceRef getResource(DomApplication da, String name, ResourceDependencyList rdl) throws Exception {
		IResourceRef r = da.createClasspathReference(name.substring(Constants.RESOURCE_PREFIX.length() - 1)); // Strip off $RES, rest is absolute resource path starting with /
		if(null != rdl)
			rdl.add(r);
		return r;
	}
}

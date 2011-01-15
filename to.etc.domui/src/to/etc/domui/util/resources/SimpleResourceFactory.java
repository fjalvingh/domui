package to.etc.domui.util.resources;

import to.etc.domui.server.*;

/**
 * Handles all normal $xxx type resources.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 14, 2011
 */
public class SimpleResourceFactory implements IResourceFactory {
	@Override
	public int accept(String name) {
		if(name.startsWith("$"))
			return 10;
		return -1;
	}

	@Override
	public IResourceRef getResource(DomApplication da, String name, ResourceDependencyList rdl) throws Exception {
		return da.getAppFileOrResource(name.substring(1));
	}
}

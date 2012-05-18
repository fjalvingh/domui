package to.etc.domui.util.resources;

import javax.annotation.*;

import to.etc.domui.server.*;

/**
 * Handles all normal $xxx type resources. These either come from web files (default) or if not found there
 * from a class resource below /resources/.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 14, 2011
 */
public class SimpleResourceFactory implements IResourceFactory {
	@Override
	public int accept(@Nonnull String name) {
		if(name.startsWith("$"))
			return 10;
		return -1;
	}

	@Override
	@Nonnull
	public IResourceRef getResource(@Nonnull DomApplication da, @Nonnull String name, @Nonnull IResourceDependencyList rdl) throws Exception {
		IResourceRef r = da.getAppFileOrResource(name.substring(1));
		if(null != rdl)
			rdl.add(r);
		return r;
	}
}

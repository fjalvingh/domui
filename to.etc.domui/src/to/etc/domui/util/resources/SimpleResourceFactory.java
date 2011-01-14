package to.etc.domui.util.resources;

import java.io.*;

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
		name = name.substring(1);

		//-- 1. Is a file-based resource available?
		File f = da.getAppFile(name);
		if(f.exists())
			return new WebappResourceRef(f);
		// 20091019 jal removed: $ resources are literal entries; they are never classnames - that is done using $RES/ only.
		//			//-- In the url, replace all '.' but the last one with /
		//			int pos = name.lastIndexOf('.');
		//			if(pos != -1) {
		//				name = name.substring(0, pos).replace('.', '/') + name.substring(pos);
		//			}

		/*
		 * For class-based resources we are able to select different versions of a resource if it's name
		 * starts with $js/. These will be scanned in resources/js/[scriptversion]/[name] and resources/js/[name].
		 */
		return da.createClasspathReference("/resources/" + name);
	}
}

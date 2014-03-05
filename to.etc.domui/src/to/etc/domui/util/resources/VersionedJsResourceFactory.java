package to.etc.domui.util.resources;

import java.io.*;

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * Handle $js/xxx resources, which are versionable.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 14, 2011
 */
public class VersionedJsResourceFactory implements IResourceFactory {
	@Override
	public int accept(@Nonnull String name) {
		if(name.startsWith("$js/"))
			return 15;
		return -1;
	}

	@Nonnull
	@Override
	public IResourceRef getResource(@Nonnull DomApplication da, @Nonnull String name, @Nonnull IResourceDependencyList rdl) throws Exception {
		//-- 1. Create a 'min version of the name
		name = name.substring(3); 							// Strip $js, leave leading /.
		int pos = name.lastIndexOf('.');
		String min = pos < 0 ? null : name.substring(0, pos) + "-min" + name.substring(pos);

		StringBuilder sb = new StringBuilder(64);
		IResourceRef r;
		if(!da.inDevelopmentMode() && min != null) {
			//-- Try all min versions in production, first
			sb.append("js/").append(da.getScriptVersion()).append(min);
			r = tryVersionedResource(da, sb.toString());
			if(r != null)
				return r;
			sb.setLength(0);
			sb.append("js").append(min);
			r = tryVersionedResource(da, sb.toString());
			if(r != null) {
				if(null != rdl)
					rdl.add(r);
				return r;
			}
		}

		//-- Try normal versions only in development.
		sb.setLength(0);
		sb.append("js/").append(da.getScriptVersion()).append(name);
		r = tryVersionedResource(da, sb.toString());
		if(r == null) {
			r = tryVersionedResource(da, "js" + name);
			if(null == r)
				r = da.getAppFileOrResource("js" + name);
		}
		if(null != rdl)
			rdl.add(r);
		return r;
	}

	/**
	 *
	 * @param da
	 * @param name.
	 * @return
	 */
	private IResourceRef tryVersionedResource(DomApplication da, String iname) throws Exception {
		//-- 1. Try WebFile first
		File f = da.getAppFile(iname);
		if(f.exists() && f.isFile())
			return new WebappResourceRef(f);

		//-- 2. Try /resources/[name] in classpath
		String name = "/resources/" + iname;
		if(DomUtil.classResourceExists(getClass(), name))
			return da.createClasspathReference(name);

		//-- Try constructed from include set.
		name = iname + ".inclspec";						// Perhaps we have an include specification?
		f = da.getAppFile(name);
		String inclset = null;

		if(f.exists() && f.isFile()) {
			inclset = FileTool.readFileAsString(f);
		} else {
			//-- 2. Try /resources/[name] in classpath
			name = "/resources/" + iname + ".inclspec";
			if(DomUtil.classResourceExists(getClass(), name)) {
				inclset = FileTool.readResourceAsString(getClass(), name, "utf-8");
			} else {
				return null;
			}
		}

		String root = "";
		int pos = iname.lastIndexOf("/");
		if(pos > 0) {
			root = iname.substring(0, pos + 1);
		}

		return CompoundResourceRef.loadBySpec(da, root, inclset, iname);
	}
}

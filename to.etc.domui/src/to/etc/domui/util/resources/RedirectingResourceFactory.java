package to.etc.domui.util.resources;

import java.io.*;

import javax.annotation.*;

import to.etc.domui.server.*;

/**
 * This resource factory matches the root of a resource name, and if matched redirects to another file system location to
 * obtain the resource.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 19, 2013
 */
public class RedirectingResourceFactory implements IResourceFactory {
	@Nonnull
	final private String m_match;

	@Nonnull
	final private File m_newroot;

	final private boolean m_isfile;

	public RedirectingResourceFactory(boolean isfile, @Nonnull String match, @Nonnull File newroot) {
		m_isfile = isfile;
		m_match = match;
		m_newroot = newroot;
		if(!newroot.exists())
			throw new IllegalStateException(newroot + ": path does not exist");
	}

	@Override
	public int accept(@Nonnull String name) {
		if(name.startsWith(m_match))
			return 100;
		return -1;
	}

	@Override
	@Nonnull
	public IResourceRef getResource(@Nonnull DomApplication da, @Nonnull String name, @Nonnull IResourceDependencyList rdl) throws Exception {
		if(m_isfile)
			return new WebappResourceRef(m_newroot);

		String sub = name.substring(m_match.length());
		File f = new File(m_newroot, sub);
		return new WebappResourceRef(f);
	}

}

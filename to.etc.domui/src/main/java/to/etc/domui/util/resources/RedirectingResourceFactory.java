package to.etc.domui.util.resources;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.server.DomApplication;

import java.io.File;

/**
 * This resource factory matches the root of a resource name, and if matched redirects to another file system location to
 * obtain the resource.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 19, 2013
 */
public class RedirectingResourceFactory implements IResourceFactory {
	@NonNull
	final private String m_match;

	@NonNull
	final private File m_newroot;

	final private boolean m_isfile;

	public RedirectingResourceFactory(boolean isfile, @NonNull String match, @NonNull File newroot) {
		m_isfile = isfile;
		m_match = match;
		m_newroot = newroot;
		if(!newroot.exists())
			throw new IllegalStateException(newroot + ": path does not exist");
	}

	@Override
	public int accept(@NonNull String name) {
		if(name.startsWith(m_match))
			return 100;
		return -1;
	}

	@Override
	@NonNull
	public IResourceRef getResource(@NonNull DomApplication da, @NonNull String name, @NonNull IResourceDependencyList rdl) throws Exception {
		if(m_isfile)
			return new WebappResourceRef(m_newroot);

		String sub = name.substring(m_match.length());
		File f = new File(m_newroot, sub);
		return new WebappResourceRef(f);
	}

}

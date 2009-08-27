package to.etc.server.cache;

import java.io.*;
import java.util.*;

/**
 * Describes a set of dependencies on VFS objects of a given date/time. This is
 * an immutable object representing a list of VFS sources used to construct another
 * object.
 *
 * <p>Created on Dec 5, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public class CacheDependencies implements Serializable, Iterable<CacheDependency> {
	private static final long	serialVersionUID	= 4434879456374993563L;

	private CacheDependency[]	m_deps;

	public CacheDependencies(Collection<CacheDependency> l) {
		m_deps = l.toArray(new CacheDependency[l.size()]);
	}

	/**
	 * Returns T if any of the dependencies in the list have
	 * changed. The VFS provider determines how and if this
	 * call is implemented. If the provider is solely event
	 * based then it will always return false (not changed)
	 * for all it's resources.
	 *
	 * @return
	 */
	public boolean changed() {
		for(CacheDependency d : m_deps) {
			if(d.changed())
				return true;
		}
		return false;
	}

	protected CacheDependency getDependency(int ix) {
		return m_deps[ix];
	}

	protected int getSize() {
		return m_deps.length;
	}

	public Iterator<CacheDependency> iterator() {
		return new Iterator() {
			private int	m_ix	= 0;

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public Object next() {
				return getDependency(m_ix++);
			}

			public boolean hasNext() {
				return m_ix < getSize();
			}
		};
	}
}

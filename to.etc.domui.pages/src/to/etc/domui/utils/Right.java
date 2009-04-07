package to.etc.domui.utils;

import java.util.*;

import to.etc.webapp.nls.*;

/**
 * A Right class defines a fine-grained permission for accessing something in the system. It is
 * equivalent to the badly named "roles" in a HttpRequest. Rights are declared statically and
 * known system-wide. Each right has an system-wide unique string ID. This id represents the
 * permission in the database and is also used to lookup the textual representation of the right
 * in several languages.
 * <p>Users are not assigned rights directly, that is not possible. Rights are assigned to AppRoles;
 * the full set of Rights for a user are calculated by fully expanding all his roles and then merging
 * all rights for those roles.</p>
 *
 * <p>All rights MUST be known at system-startup time. The default rights for the application are defined here.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 2, 2009
 */
final public class Right {
	private final BundleRef		m_bundle;
	private final String		m_moduleID;
	private final String		m_name;

	static private boolean				m_completed;
	static private Map<String, Right>	m_rightsMap = new HashMap<String, Right>();
	static private BundleRef			m_defaultBundle;

	private Right(final String moduleID, final String id) {
		m_moduleID = moduleID;
		m_name = id;
		m_bundle = null;
		register(this);
	}
	private Right(final BundleRef b, final String moduleID, final String id) {
		m_moduleID = moduleID;
		m_name = id;
		m_bundle = b;
		register(this);
	}

	static synchronized private void	register(final Right r) {
		if(m_completed)
			throw new IllegalStateException("Trying to register a new Right after system initialization: "+r.name());
		if(m_rightsMap.put(r.name(), r) != null)
			throw new IllegalStateException("Duplicate RIGHTS name: "+r.name());
	}

	public String		name() {
		return m_name;
	}

	static public Right	add(final String module, final String name) {
		return new Right(module, name);
	}
	static public Right	add(final BundleRef b, final String module, final String name) {
		return new Right(b, module, name);
	}

	/**
	 * UNSTABLE INTERFACE
	 * Define the default bundle to use when registering rights.
	 * @param ref
	 */
	@Deprecated
	static public synchronized void		setDefaultBundle(final BundleRef ref) {
		if(m_defaultBundle != null)
			throw new IllegalStateException("The default bundle is ALREADY set.");
		m_defaultBundle = ref;
	}
}

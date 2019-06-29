package to.etc.domui.lockhandler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.webapp.query.IIdentifyable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A defined lock on some resource, used with the {@link LockHandler} to handle lockout
 * between dependent tasks. A lock is either shared (R) or exclusive (r/w); multiple
 * users can hold a shared lock but only one user can hold an exclusive lock at a time. A
 * lock's resource is defined by an unique string defining the resource. The lock class
 * is serializable so that it can be stored with persisted job keys.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 24, 2012
 */
@NonNullByDefault
final public class Lock implements Serializable {
	/** The resource ident identifying the resource */
	@NonNull
	final private String m_uniqueId;

	final private boolean m_exclusive;

	/**
	 * Create a shared or exclusive lock.
	 */
	public Lock(@NonNull String resource, boolean exclusive) {
		m_uniqueId = resource;
		m_exclusive = exclusive;
	}

	/**
	 * Create a shared lock on this resource.
	 */
	public Lock(@NonNull String resource) {
		m_uniqueId = resource;
		m_exclusive = false;
	}

	/**
	 * Create a shared or exclusive lock on some identifyable instance.
	 */
	public <T> Lock(@NonNull IIdentifyable<T> instance, boolean exclusive) {
		m_exclusive = exclusive;
		ClassMetaModel cmm = MetaManager.findClassMeta(instance.getClass());		// We ask meta manager for the REAL class name to prevent proxies
		m_uniqueId = cmm.getActualClass().getName() + "#" + String.valueOf(instance.getId());
	}

	/**
	 * Create a shared lock on some identifyable instance.
	 */
	public <T> Lock(@NonNull IIdentifyable<T> instance) {
		this(instance, false);
	}

	public boolean isExclusive() {
		return m_exclusive;
	}

	@NonNull
	public String getUniqueId() {
		return m_uniqueId;
	}

	/**
	 * Creates locks for the specified resources. Allows for nulls and r/w type specs in between.
	 */
	@NonNull
	static public Lock[] locks(Object... lockar) {
		List<Lock> res = new ArrayList<Lock>();
		doLock(res, lockar);
//		System.out.println("Locks are: " + res);
		return res.toArray(new Lock[res.size()]);
	}

	private static void doLock(@NonNull List<Lock> res, @NonNull Object... lockar) {
		boolean wlock = false;
		for(Object o : lockar) {
			if(o instanceof Boolean) {
				wlock = ((Boolean) o).booleanValue();			// R/W lock indicator
				continue;
			}
			Lock l = null;
			if(o == null) {
				; // Ignore
			} else if(o instanceof Lock) {
				l = (Lock) o;
			} else if(o instanceof String) {
				l = new Lock((String) o, wlock);
			} else if(o instanceof IIdentifyable< ? >) {
				l = new Lock((IIdentifyable< ? >) o, wlock);
			} else if(o instanceof Collection<?>) {
				Collection< ? > col = (Collection< ? >) o;
				for(Object ooo : col) {
					doLock(res, ooo);
				}
			} else
				throw new IllegalStateException(o + ": don't know how to lock");
			if(l != null)
				res.add(l);
			wlock = false;
		}
	}

	@Override
	public String toString() {
		return "Lock[" + m_uniqueId + ":" + (m_exclusive ? "x" : "s") + "]";
	}


}

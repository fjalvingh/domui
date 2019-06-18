package to.etc.domui.lockhandler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * An identification and handle for all locks currently held by one entity that locks.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 27, 2009
 */
@NonNullByDefault
final public class Locker {
	@NonNull
	final private LockHandler m_handler;

	@NonNull
	final private String m_who;

	@NonNull
	private List<ResourceLock> m_lockedResources = new ArrayList<ResourceLock>();

	Locker(@NonNull LockHandler handler, @NonNull String who) {
		m_who = who;
		m_handler = handler;
	}

	@NonNull
	@Override
	public String toString() {
		synchronized(m_handler) {
			StringBuilder sb = new StringBuilder();
			if(m_lockedResources.size() == 0) {
				sb.append("[No locks held by ").append(m_who).append("]");
			} else {
				sb.append("[Lock by ").append(m_who).append(" on");
				for(ResourceLock rl : m_lockedResources)
					sb.append(" ").append(rl.toString());
				sb.append(']');
			}
			return sb.toString();
		}
	}

	@NonNull
	public String getWho() {
		return m_who;
	}

	/**
	 * Try to lock additional resources. If this fails it throws {@link ResourceLockedException} and no additional locks will be taken.
	 */
	public void lock(@NonNull Lock... objs) {
		m_handler.lock(this, objs);
	}

	/**
	 * Returns T if this locker locks the specified instance.
	 */
	final public boolean isLocked(@NonNull Lock obj) {
		ResourceLock rl = findLockFor(obj.getUniqueId());
		return rl != null && rl.getLockType() != LockType.UNLOCKED;
	}

	/**
	 * Returns the lock type within this locker for the specified instance.
	 */
	final LockType getLocked(@NonNull Lock obj) {
		ResourceLock rl = findLockFor(obj.getUniqueId());
		if(rl == null)
			return LockType.UNLOCKED;
		return rl.getLockType();
	}

	/**
	 * Atomically release all resources.
	 */
	public void release() {
		synchronized(m_handler) {
			m_handler.unlockResources(m_lockedResources, this);
			m_lockedResources.clear();
		}
	}

	/**
	 * Release all locks passed.
	 */
	public void release(@NonNull Lock... objs) {
		synchronized(m_handler) {
			//-- Collect all locks we actually hold
			List<ResourceLock> dell = new ArrayList<ResourceLock>(objs.length);
			for(Lock lock : objs) {
				ResourceLock rl = findLockFor(lock.getUniqueId());
				if(null != rl) {
					//-- We ignore locks not held.
					dell.add(rl);
				}
			}

			//-- Release these.
			m_handler.unlockResources(dell, this);
			m_lockedResources.removeAll(dell);
			m_handler.notifyAll();
		}
	}

	@Nullable
	private ResourceLock findLockFor(String uniqid) {
		for(ResourceLock l : m_lockedResources) {
			if(l.getUniqueId().equals(uniqid))
				return l;
		}
		return null;
	}

	/**
	 * Called by lockHandler to include new locks claimed by this Locker.
	 */
	void addLockedResources(@NonNull List<ResourceLock> locklist) {
		synchronized(m_handler) {
			for(ResourceLock rl : locklist) {
				if(!m_lockedResources.contains(rl)) // Locks can be respecified or "upgraded" so ignore locks we already own.
					m_lockedResources.add(rl);
			}
		}
	}

}

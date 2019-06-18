package to.etc.domui.lockhandler;


import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains locks (r/w) and handles fast-locking. Locks must be instances of {@link Lock} identifying a resource.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 27, 2009
 */
@NonNullByDefault
final public class LockHandler {
	/** The map of all locked items, identified by their unique ID, and their lock set. */
	final private Map<String, ResourceLock> m_lockMap = new HashMap<String, ResourceLock>();

	private ResourceLock getLock(@NonNull Lock resource) {
		String key = resource.getUniqueId();
		ResourceLock lock = m_lockMap.get(key);
		if(lock == null) {
			lock = new ResourceLock(this, key);
			m_lockMap.put(key, lock);
		}
		return lock;
	}

	/**
	 * Release all resources held by the specified locker instance.
	 */
	synchronized void unlockResources(@NonNull List<ResourceLock> lockList, @NonNull Locker locker) {
		for(ResourceLock rl : lockList) {
			//-- Remove locker from the lock.
			rl.removeLocker(locker);
			if(!rl.hasLockers())
				m_lockMap.remove(rl.getUniqueId());
		}
		notifyAll(); // Any waiters are awakened now
	}


	/**
	 * Fast lock for all objects passed. If locking of any of them fails none of the
	 * resources are locked, and this throws the LockFailedException. Process: for
	 * all locks specified, lock them one by one. If any lock fails release the earlier
	 * ones and throw {@link ResourceLockedException}.
	 */
	@NonNull
	final public synchronized Locker lock(@NonNull String who, @NonNull Lock... instances) {
		Locker locker = getLocker(who); 							// Locks get administered on this later on.
		lock(locker, instances);
		return locker;
	}

	/**
	 * Fast lock for all objects passed. If locking of any of them fails none of the
	 * resources are locked, and this throws the LockFailedException. Process: for
	 * all locks specified, lock them one by one. If any lock fails release the earlier
	 * ones and throw {@link ResourceLockedException}.
	 */
	@NonNull
	final public synchronized Locker lock(@NonNull String who, @NonNull List<Lock> locks) {
		Locker locker = getLocker(who); 							// Locks get administered on this later on.
		lock(locker, locks.toArray(new Lock[locks.size()]));
		return locker;
	}


	/**
	 * Called to extend a lock. This includes upgrading shared locks to exclusive.
	 */
	final synchronized void lock(@NonNull Locker locker, @NonNull Lock[] instances) {
		if(instances.length == 0)
			return;
//			throw new IllegalStateException("Nothing passed to lock..");

		//-- Find all ResourceLock's for instances we need to claim, lock them, and attach to the locker.
		List<ResourceLock> locklist = new ArrayList<ResourceLock>(instances.length);
		List<Runnable> undoList = new ArrayList<Runnable>();
		try {
			//-- 1. Try to see if we can acquire all locks.
			for(Lock ins : instances) {
				if(ins == null)
					continue;
				ResourceLock l = getLock(ins);
				locklist.add(l);
				l.addLocker(locker, ins.isExclusive(), undoList); // ORDERED Try to lock and abort if not possible.
			}

			//-- We locked everything.
			locker.addLockedResources(locklist);
			locklist = null; // Release ownership to Locker.
			undoList = null;
		} finally {
			releaseProvisional(undoList, locklist); // Delete locks that are unused.
		}
	}


	/**
	 * Release partial locks where the lock might have been claimed by locker.
	 */
	private synchronized void releaseProvisional(@Nullable List<Runnable> undoList, @Nullable List<ResourceLock> locklist) {
		if(undoList == null || locklist == null)
			return;

		//-- Undo everything
		for(int i = undoList.size(); --i >= 0;) {
			undoList.get(i).run();
		}

		//-- Delete all unused ResourceLocks
		for(ResourceLock rl: locklist) {
			if(!rl.hasLockers()) {
				m_lockMap.remove(rl.getUniqueId());
			}
		}
	}

	/**
	 * Get an empty locker to keep locks in.
	 * @param name		A name for the process/user that will own this lock, so that he can be displayed.
	 */
	@NonNull
	public Locker getLocker(@NonNull String name) {
		return new Locker(this, name);
	}
}

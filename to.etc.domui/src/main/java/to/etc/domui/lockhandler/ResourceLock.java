package to.etc.domui.lockhandler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This handles fast-fail locking for resource sets. It is a read/write lock
 * on a given system thing.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 27, 2009
 */
@NonNullByDefault
final class ResourceLock {
	/** The lock handle to synchronize on */
	@NonNull
	final private LockHandler m_coreLock;

	/** The lock's unique ID */
	@NonNull
	final private String m_uniqueId;

	@NonNull
	private LockType m_lockType = LockType.UNLOCKED;

	/** The list of all lockers on this resourceLock. */
	final private List<Locker> m_lockerList = new ArrayList<Locker>();

	ResourceLock(@NonNull LockHandler coreLock, @NonNull String uniqueId) {
		m_coreLock = coreLock;
		m_uniqueId = uniqueId;
	}

	@NonNull
	public LockType getLockType() {
		synchronized(m_coreLock) {
			return m_lockType;
		}
	}

	@NonNull
	public String getUniqueId() {
		return m_uniqueId;
	}

	void removeLocker(@NonNull Locker l) {
		synchronized(m_coreLock) {
			if(!m_lockerList.remove(l))
				throw new IllegalStateException(l + ": not part of list!?");
			if(m_lockerList.isEmpty())
				m_lockType = LockType.UNLOCKED;
		}
	}


	void addLocker(@NonNull final Locker l, boolean exclusive, @NonNull List<Runnable> undoList) {
		boolean alreadyPresent = m_lockerList.contains(l); // Is this locker already locking this?
		synchronized(m_coreLock) {
			//-- Is the type of lock compatible with the current lock?
			LockType rt = exclusive ? LockType.EXCLUSIVE : LockType.SHARED;
			switch(getLockType()){
				default:
					throw new IllegalStateException(getLockType() + ": ?");
				case UNLOCKED:
					if(alreadyPresent)
						throw new IllegalStateException("Locker listed but lock is unlocked!?");
					m_lockType = exclusive ? LockType.EXCLUSIVE : LockType.SHARED;
					addLockerList(l);
					addUndoItem(undoList, m_lockType, l);
					return;

				case EXCLUSIVE:
					//-- Exclusively locked; if we're present we're the one already exclusively locking this
					if(alreadyPresent)
						return;

					//-- Already locked by another.
					Locker who = m_lockerList.get(0);
					throw new ResourceLockedException(this, getLockType(), rt, who);

				case SHARED:
					//-- Current lock is shared. If I am the only locker we allow upgrade to exclusive.
					who = m_lockerList.get(0);
					if(alreadyPresent) {
						if(!exclusive) // If I am already locking shared and not trying to upgrade -> do nothing
							return;

						//-- Trying to upgrade. Only allowed if I'm the only one here.
						if(m_lockerList.size() != 1)
							throw new ResourceLockedException(this, getLockType(), rt, who); // Upgrade refused

						//-- I can upgrade.
						m_lockType = LockType.EXCLUSIVE;
						addUndoItem(undoList, LockType.SHARED, null);
						return;
					}
					if(exclusive)
						throw new ResourceLockedException(this, getLockType(), rt, who);

					//-- New shared lock request- grant.
					addLockerList(l);
					addUndoItem(undoList, null, l); // @undo just remove locker again
					return;
			}
//			throw new IllegalStateException("Should not get here.");
		}
	}

	private void addLockerList(@NonNull Locker l) {
		if(m_lockerList.contains(l))
			throw new IllegalStateException("Duplicate locker: " + l);
		m_lockerList.add(l); // Add locker to local list.
	}

	boolean hasLockers() {
		synchronized(m_coreLock) {
			return !m_lockerList.isEmpty();
		}
	}

	private void addUndoItem(@NonNull final List<Runnable> undoList, @Nullable final LockType lt, @Nullable final Locker l) {
		undoList.add(new Runnable() {
			@Override
			public void run() {
				if(null != lt)
					m_lockType = lt;
				if(null != l)
					m_lockerList.remove(l);
			}
		});
	}
}

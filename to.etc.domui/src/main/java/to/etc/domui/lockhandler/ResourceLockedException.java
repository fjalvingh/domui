package to.etc.domui.lockhandler;

import org.eclipse.jdt.annotation.NonNull;

final public class ResourceLockedException extends RuntimeException {
	public ResourceLockedException(@NonNull ResourceLock lock, @NonNull LockType currentType, @NonNull LockType requestedType, @NonNull Locker lockedBy) {
		super(requestedType + " lock of " + lock.getUniqueId() + " failed: it is currently " + currentType + " locked by " + lockedBy.getWho());
	}
}

package to.etc.util;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.function.SupplierEx;

import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * Util that enables simple locking via file system locks.
 * Used in environments when multiple processes are competing over shared resources, like database initialization when running db tests in parallel module executions.
 */
public class FileLockKeeper {

	private static final Map<String, Long> LOCK_TIMEOUT_MS_MAP = new ConcurrentHashMap<>();

	private static final Logger LOG = LoggerFactory.getLogger(FileLockKeeper.class);

	/**
	 *  If lock file is present we do waiting route with provided delays and overall timeout.
	 *  If lock is not present, lock file is created and guarded block is executed.
	 */
	public synchronized static <T> T withLock(String name, Duration delay, @Nullable Duration timeout, SupplierEx<T> retryBlock, SupplierEx<T> guardedBlock) throws Exception {
		File tmpFile = new File(FileTool.getTmpDir(), "FileLockerKeeper_" + name);
		if(tmpFile.exists()) {
			if(null != timeout) {
				Long lockedPeriod = System.currentTimeMillis() - LOCK_TIMEOUT_MS_MAP.get(name);
				if (lockedPeriod > timeout.toMillis()) {
					throw new TimeoutException("File lock " + name + " timeout!");
				}
			}
			LOG.warn("File lock " + name + " present at " + tmpFile.getAbsolutePath() + ", delaying " + StringTool.strNanoTime(delay.getNano()));
			Thread.sleep(delay.toMillis());
			return retryBlock.get();
		}
		try {
			if(!tmpFile.createNewFile()) {
				throw new IllegalStateException("Unable to create lock file??? " + tmpFile.getAbsolutePath());
			}
			LOG.warn("Added file lock " + name + ", as " + tmpFile.getAbsolutePath());
			return guardedBlock.get();
		}finally {
			if(!tmpFile.delete()) {
				throw new IllegalStateException("Unable to delete file lock??? " + tmpFile.getAbsolutePath());
			}
			LOCK_TIMEOUT_MS_MAP.remove(name);
			LOG.warn("Removed file lock " + name + ", as " + tmpFile.getAbsolutePath());
		}
	}
}

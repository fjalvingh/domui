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
 * Simple util that enables simple locking via file system locks.
 * Used in environments when multiple processes are competing over shared resources, like database initialization when running db tests in parallel module executions.
 */
public class FileLockKeeper {

	private static final Map<String, Long> LOCK_TIMEOUT_MS_MAP = new ConcurrentHashMap<>();

	private static final Logger LOG = LoggerFactory.getLogger(FileLockKeeper.class);

	public synchronized static <T> T withLock(String name, Duration delay, @Nullable Duration timeout, SupplierEx<T> retryBlock, SupplierEx<T> guardedBlock) throws Exception {
		File tmpFile = new File(FileTool.getTmpDir(), "FileLockerKeeper_" + name);
		if(tmpFile.exists()) {
			Long lockLength = System.currentTimeMillis() - LOCK_TIMEOUT_MS_MAP.get(name);
			if(lockLength > timeout.toMillis()) {
				throw new TimeoutException("File lock " + name + " has expired!");
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

package to.etc.util;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.function.SupplierEx;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Util that enables simple locking via file system locks.
 * Used in environments when multiple processes are competing over shared resources, like database initialization when running db tests in parallel module executions.
 */
public class FileLockKeeper {

	private static final Logger LOG = LoggerFactory.getLogger(FileLockKeeper.class);

	/**
	 *  Tries to lock by created a new temp file. If file is already present, we loop in delay periods until file disappears or until specified timeout expires.
	 */
	public static <T> T withLock(String name, Duration delay, @Nullable Duration timeout, SupplierEx<T> guardedBlock) throws Exception {
		File tmpFile = new File(FileTool.getTmpDir(), "FileLockerKeeper_" + name);
		Long startedAt = System.currentTimeMillis();
		boolean lockMade = false;
		try {
			while(!lockMade) {
				try {
					lockMade = tmpFile.createNewFile();
				} catch(Exception ex) {
					throw new IllegalStateException("Unable to create lock file??? " + tmpFile.getAbsolutePath(), ex);
				}
				if(!lockMade) {
					if(null != timeout) {
						Long lockedPeriod = System.currentTimeMillis() - startedAt;
						if(lockedPeriod > timeout.toMillis()) {
							throw new TimeoutException("File lock " + name + " timeout!");
						}
					}
					LOG.warn("File lock " + name + " present at " + tmpFile.getAbsolutePath() + ", delaying " + StringTool.strNanoTime(delay.getNano()));
					Thread.sleep(delay.toMillis());
				}
			}
			LOG.warn("Added file lock " + name + ", as " + tmpFile.getAbsolutePath());
			return guardedBlock.get();
		}finally {
			if(lockMade) {
				if(!tmpFile.delete()) {
					throw new IllegalStateException("Unable to delete file lock??? " + tmpFile.getAbsolutePath());
				}
				LOG.warn("Removed file lock " + name + ", as " + tmpFile.getAbsolutePath());
			}
		}
	}
}

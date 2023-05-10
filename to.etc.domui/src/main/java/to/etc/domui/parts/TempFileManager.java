package to.etc.domui.parts;

import to.etc.util.FileTool;
import to.etc.util.TimerUtil;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Keeps track of tempfiles, and has methods to make sure they
 * will be deleted after use.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-05-2023.
 */
final public class TempFileManager {
	private final Map<String, TempRef> m_refMap = new HashMap<>();

	private ScheduledFuture<?> m_scheduledFuture;

	/**
	 * Register one or more files to be deleted after a while. Default expiry is 10 minutes.
	 */
	public void register(String key, List<File> files) {
		register(key, files, Duration.ofMinutes(10));
	}

	/**
	 * Register one or more files to be deleted after a while.
	 */
	public void register(String key, List<File> files, Duration expireAt) {
		synchronized(this) {
			TempRef old = m_refMap.get(key);

			long ets = System.currentTimeMillis() + expireAt.toMillis();
			if(old == null) {
				m_refMap.put(key, new TempRef(new ArrayList<>(files), ets));
			} else {
				List<File> newList = new ArrayList<>(files.size() + old.getFileList().size());
				newList.addAll(old.getFileList());
				newList.addAll(files);
				m_refMap.put(key, new TempRef(newList, ets));
			}
		}
	}

	/**
	 * Post a timer to clean expired files every 5 minutes.
	 */
	public synchronized void initialize() {
		if(m_scheduledFuture != null)
			return;
		m_scheduledFuture = TimerUtil.scheduleAtFixedRate(5, 5, TimeUnit.MINUTES, () -> {
			cleanExpired();
		});
	}

	/**
	 * Terminate the manager and cancel the scheduled updates.
	 */
	public void terminate() {
		synchronized(this) {
			ScheduledFuture<?> sf = m_scheduledFuture;
			if(null != sf) {
				m_scheduledFuture = null;
				sf.cancel(false);
			}
		}
		cleanAll();
	}

	/**
	 * Mark the specified fileset as still being used.
	 */
	public void ping(String key) {
		synchronized(this) {
			TempRef tempRef = m_refMap.get(key);
			if(null != tempRef) {
				tempRef.setExpiresOn(System.currentTimeMillis() + Duration.ofMinutes(10).toMillis());
			}
		}
	}

	/**
	 * Called when the server stops, this deletes ALL tempfiles that are currently
	 * registered.
	 */
	public void cleanAll() {
		List<TempRef> list;
		synchronized(this) {
			list = new ArrayList<>(m_refMap.values());
			m_refMap.clear();
		}

		deleteExpired(list);
	}

	private static void deleteExpired(List<TempRef> list) {
		for(TempRef tempRef : list) {
			for(File file : tempRef.getFileList()) {
				try {
					FileTool.delete(file);
				} catch(Exception x) {
					//-- Willfully ignore; we cannot do anything if deleting fails.
				}
			}
		}
	}

	/**
	 * Should be called regularly to cleanup expired tmpfiles.
	 */
	private void cleanExpired() {
		List<TempRef> expiredList = new ArrayList<>();
		long cts = System.currentTimeMillis();
		synchronized(this) {
			for(Iterator<TempRef> iterator = m_refMap.values().iterator(); iterator.hasNext();) {
				TempRef ref = iterator.next();
				if(ref.getExpiresOn() < cts) {
					expiredList.add(ref);
					iterator.remove();
				}
			}
		}
		deleteExpired(expiredList);
	}

	static private final class TempRef {
		private final List<File> m_fileList;

		private long m_expiresOn;

		public TempRef(List<File> fileList, long expiresOn) {
			m_fileList = fileList;
			m_expiresOn = expiresOn;
		}

		public List<File> getFileList() {
			return m_fileList;
		}

		public long getExpiresOn() {
			return m_expiresOn;
		}

		public void setExpiresOn(long expiresOn) {
			m_expiresOn = expiresOn;
		}
	}
}

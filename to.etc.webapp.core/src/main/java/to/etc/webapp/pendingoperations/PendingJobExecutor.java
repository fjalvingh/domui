package to.etc.webapp.pendingoperations;

import to.etc.util.ILogSink;
import to.etc.util.IProgressListener;
import to.etc.util.Progress;

import javax.annotation.Nonnull;
import java.util.Calendar;
import java.util.Date;


public class PendingJobExecutor implements Runnable {

	private final PendingOperation m_pendingOperation;

	private final ILogSink m_logSink;

	public PendingJobExecutor(final PendingOperation pendingOperation, final ILogSink sink) {
		m_pendingOperation = pendingOperation;
		m_logSink = sink;
	}

	static public void register() {
		PendingOperationTaskProvider.getInstance().registerPendingOperationType("PJEX", new IPendingOperationExecutor() {
			@Override
			public void executePendingOperation(final @Nonnull PendingOperation po, final @Nonnull ILogSink ls) throws Exception {
				new PendingJobExecutor(po, ls).run();
			}
		});
	}

	/**
	 * Registers a delayed pending job. It will be executed as soon as possible.
	 *
	 */

	static public void registerOperation(final String userid, final String submitter, final String desc, final String externalId, final IPendingJob operation) throws Exception {
		PendingOperation po = new PendingOperation();
		po.setCreationTime(new java.util.Date());
		po.setUserID(userid);
		po.setDescription(desc);
		po.setSubmitsource(submitter);
		po.setXid(externalId);

		po.setType("PJEX");
		PendingOperationTaskProvider.getInstance().saveOperation(po, operation);
	}

	public ILogSink getLogSink() {
		return m_logSink;
	}

	public PendingOperation getPendingOperation() {
		return m_pendingOperation;
	}

	@Override
	public void run() {
		try {
			IPendingJob j = (IPendingJob) m_pendingOperation.getSerializedObject();
			Progress p = prepareJobProgress();
			j.execute(m_logSink, m_pendingOperation, p);

		} catch(Exception x) {
			m_logSink.exception(x, "in executing pending job");
			m_pendingOperation.setError(PendingOperationState.FATL, "Error: " + x);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Job progress										*/
	/*--------------------------------------------------------------*/
	/**
	 * represents significant amount of time in ms, after which
	 * update of progress in db is needed
	 */
	private static final long TIME_DELTA = 3000;

	/**
	 * value that represents significant difference in progress,
	 * that needs to be saved
	 *  */
	private static final int VALUE_DELTA = 20;

	/**
	 * previous saved progress value
	 */
	private int m_lastUpdatePercent = 0;

	/**
	 * time of previous progress update
	 */
	private Date m_lastUpdateTime;

	private Progress prepareJobProgress() {

		Progress p = new Progress("Job");
		p.setTotalWork(100);
		p.addListener(new IProgressListener() {

			private long deltaInMilliseconds(Date start, Date end) {
				Calendar startCal = Calendar.getInstance();
				startCal.setTime(start);
				Calendar endCal = Calendar.getInstance();
				endCal.setTime(end);

				return endCal.getTimeInMillis() - startCal.getTimeInMillis();
			}

			private boolean doUpdate(Progress level) {
				return getLastUpdatePercent() == 0 || (deltaInMilliseconds(getLastUpdateTime(), new Date())) > TIME_DELTA || level.getPercentage() - getLastUpdatePercent() > VALUE_DELTA;
			}

			@Override
			public void progressed(@Nonnull Progress level) throws Exception {
				if(doUpdate(level)) {
					getPendingOperation().setProgressPath(level.getActionPath(3));
					getPendingOperation().setProgressPercentage(level.getPercentage());

					PendingOperationTaskProvider.getInstance().updateProgress(getPendingOperation());

					setLastUpdatePercent(level.getPercentage());
					setLastUpdateTime(new Date());
				}
			}
		});

		return p;
	}

	public int getLastUpdatePercent() {
		return m_lastUpdatePercent;
	}

	public void setLastUpdatePercent(int lastUpdatePercent) {
		m_lastUpdatePercent = lastUpdatePercent;
	}

	public Date getLastUpdateTime() {
		return m_lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		m_lastUpdateTime = lastUpdateTime;
	}

	static public synchronized void startOperation(final String userid, final String submitter, final String desc, final String externalId, final IPendingJob operation) throws Exception {
		if(!PendingOperationTaskProvider.getInstance().isBusyWithJob(externalId)) {
			registerOperation(userid, submitter, desc, externalId, operation);
		}
	}
}

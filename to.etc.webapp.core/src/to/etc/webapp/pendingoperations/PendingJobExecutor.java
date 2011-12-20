package to.etc.webapp.pendingoperations;

import to.etc.util.*;


public class PendingJobExecutor implements Runnable {

	private final PendingOperation m_pendingOperation;

	private final ILogSink m_logSink;


	public PendingJobExecutor(final PendingOperation pendingOperation, final ILogSink sink) {
		m_pendingOperation = pendingOperation;
		m_logSink = sink;
	}

	public ILogSink getLogSink() {
		return m_logSink;
	}

	static public void register() {
		PendingOperationTaskProvider.getInstance().registerPendingOperationType("PJEX", new IPendingOperationExecutor() {
			@Override
			public void executePendingOperation(final PendingOperation po, final ILogSink ls) throws Exception {
				new PendingJobExecutor(po, ls).run();
			}
		});
	}

	/**
	 * Registers a delayed pending job. It will be executed as soon as possible.
	 *
	 */

	static public void registerOperation(final String userid, final String submitter, final String desc, final IPendingJob operation) throws Exception {
		PendingOperation po = new PendingOperation();
		po.setCreationTime(new java.util.Date());
		po.setUserID(userid);
		po.setDescription(desc);
		po.setSubmitsource(submitter);

		po.setType("PJEX");
		PendingOperationTaskProvider.getInstance().saveOperation(po, operation);
	}



	public String getRequestID() {
		return "pjex" + m_pendingOperation.getId();
	}

	public PendingOperation getPendingOperation() {
		return m_pendingOperation;
	}

	@Override
	public void run() {
		try {
			IPendingJob j = (IPendingJob) m_pendingOperation.getSerializedObject();
			Progress p = new Progress("Job");
			j.execute(m_logSink, m_pendingOperation, p);

		} catch(Exception x) {
			m_logSink.exception(x, "in executing pending scenario claculation job");
			m_pendingOperation.setError(PendingOperationState.FATL, "Error: " + x);
		}

	}
}

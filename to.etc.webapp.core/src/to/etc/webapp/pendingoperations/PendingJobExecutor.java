package to.etc.webapp.pendingoperations;

import to.etc.util.*;

public class PendingJobExecutor {
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
				IPendingJob j = (IPendingJob) po.getSerializedObject();
				Progress p = new Progress("Job");
				j.execute(ls, po, p);
			}
		});
	}

	public String getRequestID() {
		return "pjix" + m_pendingOperation.getId();
	}

	public PendingOperation getPendingOperation() {
		return m_pendingOperation;
	}
}

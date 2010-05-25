package to.etc.webapp.pendingoperations;

public enum PendingOperationState {
	/** The thingy is currently executing somewhere */
	EXEC,

	/** The thingy needs to be RETRIED because it failed. */
	RTRY,

	/** The thing has failed completely and will not be retried. */
	FATL,

	/** Like FAIL the thing has failed miserably and can only be retried after a system restart (missing classes, missing factories) */
	BOOT,

	/** Operation finished succesfully. */
	DONE,
}

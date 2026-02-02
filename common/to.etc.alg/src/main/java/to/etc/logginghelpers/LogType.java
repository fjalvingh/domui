package to.etc.logginghelpers;

public enum LogType {
	HDR,
	ERR,
	/** More important than log, below header. Used to log checkouts/updates/compiles. */
	IMP,
	LOG,
	DET,

	/** Compile error (regexp) */
	CER,
}

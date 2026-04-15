package to.etc.dbutil.reverse;

public interface IReverseProgressListener {
	void progress(ReverserOption option, ProgressType type, String where);

	default void recordCount(int count) {
		// empty
	}
}

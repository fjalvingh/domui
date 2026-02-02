package to.etc.dbreplay;

public interface IReplayer {
	int decodeArgs(String option, String[] args, int argc);

	void handleRecord(DbReplay r, ReplayRecord rr) throws Exception;
}

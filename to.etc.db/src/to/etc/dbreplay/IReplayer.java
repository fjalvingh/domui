package to.etc.dbreplay;

public interface IReplayer {
	void handleRecord(DbReplay r, ReplayRecord rr) throws Exception;
}

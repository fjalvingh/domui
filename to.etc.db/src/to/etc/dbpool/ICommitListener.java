package to.etc.dbpool;

import java.sql.*;

public interface ICommitListener {
	void onAfterCommit(Connection dbc) throws SQLException;
}

package to.etc.dbcompare.reverse;

import java.sql.*;

public interface ReverserFactory {
	public Reverser createReverser(Connection dbc, String schemaname) throws Exception;
}

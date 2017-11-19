package to.etc.dbutil.reverse;

import java.sql.*;

import javax.sql.*;


public interface ReverserFactory {
	Reverser createReverser(DataSource dbc, DatabaseMetaData dmd) throws Exception;
}

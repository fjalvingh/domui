package to.etc.dbutil.reverse;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;


public interface ReverserFactory {
	Reverser createReverser(DataSource dbc, DatabaseMetaData dmd) throws Exception;
}

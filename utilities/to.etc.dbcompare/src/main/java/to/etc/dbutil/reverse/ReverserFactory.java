package to.etc.dbutil.reverse;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.util.Set;


public interface ReverserFactory {
	Reverser createReverser(DataSource dbc, DatabaseMetaData dmd, Set<ReverserOption> optionSet) throws Exception;
}

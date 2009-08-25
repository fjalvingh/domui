package to.etc.webapp.qsql;

import java.sql.*;

interface IInstanceMaker {
	Object make(ResultSet rs) throws Exception;
}

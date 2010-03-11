package to.etc.webapp.qsql;

import java.sql.*;

import to.etc.webapp.query.*;

interface IInstanceMaker {
	Object make(QDataContext dc, ResultSet rs) throws Exception;
}

package to.etc.webapp.qsql;

import java.sql.*;

public interface IQValueSetter {
	void assign(PreparedStatement ps) throws Exception;
}

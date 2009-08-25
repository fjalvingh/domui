package to.etc.webapp.qsql;

import java.sql.*;

interface ITypeConverter {
	int accept(JdbcPropertyMeta pm);

	Object convertToInstance(ResultSet rs, int index, JdbcPropertyMeta pm) throws Exception;
}

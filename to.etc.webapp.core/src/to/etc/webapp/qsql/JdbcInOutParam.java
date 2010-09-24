package to.etc.webapp.qsql;

import java.sql.*;

import javax.annotation.*;

/**
 * IN OUT parameter definition when calling oracle function/stored procedure that has IN OUT params.<BR/>
 * See {@link JdbcUtil#oracleSpCall(Connection, Class, String, Object...)}.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Sep 24, 2010
 */
public class JdbcInOutParam<T> extends JdbcOutParam<T> {
	public JdbcInOutParam(@Nonnull Class<T> classType, T value) {
		super(classType);
		setValue(value);
	}
}

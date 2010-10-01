package to.etc.webapp.qsql;

import java.sql.*;

import javax.annotation.*;

/**
 * OUT parameter definition when calling oracle function/stored procedure that has OUT params.<BR/>
 * See {@link JdbcUtil#oracleSpCall(Connection, Class, String, Object...)}.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Sep 16, 2010
 */
public class JdbcOutParam<T> {
	@Nonnull
	final private Class<T> m_classType;

	@Nullable
	private T m_value;

	public JdbcOutParam(@Nonnull Class<T> classType) {
		m_classType = classType;
	}

	@Nullable
	public T getValue() {
		return m_value;
	}

	public void setValue(@Nullable T value) {
		m_value = value;
	}

	@Nonnull
	public Class<T> getClassType() {
		return m_classType;
	}
}

package to.etc.webapp.qsql;

/**
 * OUT parameter definition when calling oracle function/stored procedure that has OUT params.<BR/>
 * See {@link JdbcUtil#oracleFunctionCallSP(java.sql.Connection, Class, String, JdbcOutParam[], Object...)}.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Sep 16, 2010
 */
public class JdbcOutParam<T> {
	private Class<T> m_classType;

	private T value;

	public JdbcOutParam(Class<T> classType) {
		m_classType = classType;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public Class<T> getClassType() {
		return m_classType;
	}
}

package to.etc.webapp.qsql;

import java.sql.*;

/**
 * JDBC to java type converter, used to convert column values to Java objects and v.v.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
interface ITypeConverter {
	/**
	 * Must return a +ve nonzero score when this converter accepts this property for conversion.
	 * @param pm
	 * @return
	 */
	int accept(JdbcPropertyMeta pm);

	/**
	 * Must convert the value at the specified location of the result set to the type represented by this type.
	 * @param rs
	 * @param index
	 * @param pm
	 * @return
	 * @throws Exception
	 */
	Object convertToInstance(ResultSet rs, int index, JdbcPropertyMeta pm) throws Exception;

	void assignParameter(PreparedStatement ps, int index, JdbcPropertyMeta pm, Object value) throws Exception;
}

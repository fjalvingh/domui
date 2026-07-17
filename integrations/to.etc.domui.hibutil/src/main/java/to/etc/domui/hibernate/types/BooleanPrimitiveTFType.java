package to.etc.domui.hibernate.types;

import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * A UserType implementation to map a boolean primitive object to a VARCHAR.<br /> A true
 * value maps to "true" and a false value maps to "false". This type does not recognise
 * nullity; it gets interpreted as a false.
 *
 * @author jal
 */
final public class BooleanPrimitiveTFType implements UserType<Boolean> {
	@Override
	public Boolean assemble(Serializable cached, Object owner) throws HibernateException {
		return null;
	}

	@Override
	public Boolean deepCopy(Boolean value) throws HibernateException {
		return value;
	}

	@Override
	public Serializable disassemble(Boolean value) throws HibernateException {
		return null;
	}

	@Override
	public boolean equals(Boolean x, Boolean y) throws HibernateException {
		if(x != null)
			return x.equals(y);
		else
			return x == y;
	}

	@Override
	public int hashCode(Boolean arg0) throws HibernateException {
		return arg0.hashCode();
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Boolean nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
		if(rs == null)
			return null;
		String v = rs.getString(position);
		if(v == null)
			return Boolean.FALSE;
		return parse(v);
	}

	@Override
	public void nullSafeSet(PreparedStatement statement, Boolean value, int position, WrapperOptions options) throws SQLException {
		statement.setString(position, value == null ? "F" : ((Boolean) value).booleanValue() ? "T" : "F");
	}

	@Override
	public Boolean replace(Boolean arg0, Boolean arg1, Object arg2) throws HibernateException {
		return null;
	}

	@Override
	public Class<Boolean> returnedClass() {
		return Boolean.class;
	}

	@Override
	public int getSqlType() {
		return Types.VARCHAR;
	}

	/**
	 * Parsing of a String yields the following results: TRUE: if src equals
	 * y,yes,1 or 'true' (case insensitive) FALSE: in all other cases
	 */
	public static Boolean parse(String src) {
		if("1".equals(src) || "t".equalsIgnoreCase(src) || "true".equalsIgnoreCase(src) || "Y".equalsIgnoreCase(src) || "yes".equalsIgnoreCase(src)) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

}

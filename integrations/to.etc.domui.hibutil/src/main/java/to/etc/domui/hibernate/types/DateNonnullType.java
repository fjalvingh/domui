package to.etc.domui.hibernate.types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import to.etc.util.DateUtil;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public class DateNonnullType implements UserType {
	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return null;
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		if(value == null)
			return value;
		return new Date(((java.util.Date) value).getTime());
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return null;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		if(x != null)
			return x.equals(y);
		else
			return x == y;
	}

	@Override
	public int hashCode(Object arg0) throws HibernateException {
		return arg0.hashCode();
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException, SQLException {
		if(resultSet == null)
			return null;
		Date v = resultSet.getDate(names[0]);
		if(resultSet.wasNull())
			return null;
		if(DateUtil.getYear(v) >= 9999)
			return null;
		return new java.util.Date(v.getTime());
	}

	@Override
	public void nullSafeSet(PreparedStatement statement, Object value, int index) throws HibernateException, SQLException {
		Date dt = (Date) value;
		if(null == dt) {
			dt = DateUtil.dateFor(9999, 0, 1);
		}
		statement.setDate(index, new java.sql.Date(dt.getTime()));
	}

	@Override
	public Object replace(Object arg0, Object arg1, Object arg2) throws HibernateException {
		return null;
	}

	@Override
	public Class< ? > returnedClass() {
		return Date.class;
	}

	@Override
	public int[] sqlTypes() {
		return new int[]{Types.DATE};
	}
}

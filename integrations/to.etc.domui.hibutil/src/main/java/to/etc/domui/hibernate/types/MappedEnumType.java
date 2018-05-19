package to.etc.domui.hibernate.types;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.hibernate.*;
import org.hibernate.usertype.*;

import to.etc.domui.hibernate.config.*;

/**
 * This Hibernate user type allows mapping Java 5 enums onto fields where field values contain
 * names that are invalid for enum labels (like 1, 6, 5, %fout). For this to work an enum must
 * implement {@link IDatabaseCodeEnum} to map between database values and enum labels. This
 * version works on varchar fields only.
 * <p>This code works <b>only</b> when {@link HibernateChecker#enhanceMappings()} is called before use, or when the type of the property is uselessly repeated in Hibernate's config!!</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 18, 2009
 */
public class MappedEnumType implements UserType, ParameterizedType {
	static private final int[] SQLTYPES = {Types.VARCHAR};

	private Class< ? > m_enumClass;

	@Override
	public int[] sqlTypes() {
		return SQLTYPES;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Class< ? > returnedClass() {
		return Enum.class;
	}

	/**
	 * Called with the typedef for this type. It should define the actual class
	 * of the enum.
	 *
	 * @see org.hibernate.usertype.ParameterizedType#setParameterValues(java.util.Properties)
	 */
	@Override
	public void setParameterValues(Properties parameters) {
		if(parameters == null)
			return;
		String name = parameters.getProperty("propertyType");
		if(name == null)
			throw new HibernateException("Missing 'propertyType' parameter on type object: you must call HibernateUtil.enhanceConfig() before using this type");
		try {
			m_enumClass = getClass().getClassLoader().loadClass(name);

			if(!IDatabaseCodeEnum.class.isAssignableFrom(m_enumClass))
				throw new HibernateException("the class " + name + " does not implement IDatabaseCodeEnum");
			if(!m_enumClass.isEnum())
				throw new HibernateException("the class " + name + " is not an enum class");
		} catch(Exception x) {
			throw new HibernateException("The class=" + name + " cannot be loaded: " + x);
		}
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		if(x == y)
			return true;
		else if(x == null || y == null)
			return false;
		if(x.getClass() != y.getClass())
			return false;
		return x == y; // Enum labels.
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached;
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) deepCopy(value);
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		String value = Hibernate.STRING.nullSafeGet(rs, names[0]);
		if(value == null)
			return null;

		//-- Find the enum label having this value.
		Enum< ? >[] ar = (Enum[]) getEnumClass().getEnumConstants();
		for(Enum< ? > label : ar) {
			if(!(label instanceof IDatabaseCodeEnum))
				throw new IllegalStateException("*Now* the label does not implement IDatabaseCodeEnum?!!?");
			String code = ((IDatabaseCodeEnum) label).getCode();
			if(value.equalsIgnoreCase(code))
				return label;
		}
		throw new HibernateException("The database-column value '" + value + "' cannot be mapped onto a label of " + m_enumClass);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		if(value == null)
			st.setNull(index, Types.VARCHAR);
		else {
			try {
				st.setString(index, ((IDatabaseCodeEnum) value).getCode());
			} catch(SQLException | HibernateException ex) {
				//added extra info on error -> since it is hard for trace otherwise
				System.err.println("value: >" + value + "< can not be cased to IDatabaseCodeEnum, at index: " + index);
				System.err.println("raised at " + getClass() + ", " + this);
				throw ex;
			}
		}
	}

	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

	private Class< ? > getEnumClass() {
		if(m_enumClass == null)
			throw new HibernateException("Missing parameters on type object: you must call HibernateUtil.enhanceConfig() before using this type");
		return m_enumClass;
	}
}

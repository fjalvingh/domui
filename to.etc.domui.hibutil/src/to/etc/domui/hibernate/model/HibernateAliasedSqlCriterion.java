package to.etc.domui.hibernate.model;

import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.engine.*;
import org.hibernate.type.*;
import org.hibernate.util.*;

/**
 * This alternative implementation of SQLCriteria allows using SQL criteria's with
 * aliases other than just {alias}. To implement queries on fields of aliased parts of the query
 * use the syntax: {alias.property}. The alias refers to a given subquery or aliased join which
 * will identify the Hibernate class; the property name will then be used to lookup the column
 * name of the property so that it can then be used inside the SQL query.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/18/15.
 */
final public class HibernateAliasedSqlCriterion implements Criterion {
	private static final long serialVersionUID = -4021078009045122349L;

	private final String m_sql;

	private final TypedValue[] m_typedValues;

	public HibernateAliasedSqlCriterion(String sql, Object[] values, Type[] types) {
		m_sql = sql;
		m_typedValues = new TypedValue[values.length];
		for (int i = 0; i < m_typedValues.length; i++) {
			m_typedValues[i] = new TypedValue(types[i], values[i], EntityMode.POJO);
		}
	}

	public HibernateAliasedSqlCriterion(String sql, Object value, Type type) {
		this(sql, new Object[] { value }, new Type[] { type });
	}

	public HibernateAliasedSqlCriterion(String sql) {
		this(sql, ArrayHelper.EMPTY_OBJECT_ARRAY, ArrayHelper.EMPTY_TYPE_ARRAY);
	}

	@Override
	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
		return applyAliases(criteria, criteriaQuery);
	}

	private String applyAliases(Criteria criteria, CriteriaQuery criteriaQuery) {
		StringBuilder res = new StringBuilder();
		int i = 0;
		int length = m_sql.length();
		while(i < length) {
			int l = m_sql.indexOf('{', i);
			if(l == -1) {
				break;
			}

			String before = m_sql.substring(i, l);
			res.append(before);
			l++;											// Past {

			int r = m_sql.indexOf('}', l);
			if(r == -1)
				throw new HibernateException("Missing '}' in sql string: " + m_sql);

			String alias = m_sql.substring(l, r);
			if(alias.isEmpty() || "alias".equals(alias)) {	// root alias?
				res.append(criteriaQuery.getSQLAlias(criteria));
			} else {
				String[] columns = criteriaQuery.getColumnsUsingProjection(criteria, alias);
				if(columns.length != 1)
					throw new HibernateException("SQLAliasedCriterion may only be used with single-column properties: " + alias);
				res.append(columns[0]);
			}
			i = r + 1;
		}
		String after = m_sql.substring(i, length);
		res.append(after);

		return res.toString();
	}

	@Override
	public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
		return m_typedValues;
	}

	@Override
	public String toString() {
		return m_sql;
	}
}

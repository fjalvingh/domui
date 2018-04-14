package to.etc.webapp.query;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A verbatim SQL fragment, with optional JDBC parameters, to use inside a query.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2013
 */
public class QSqlRestriction extends QOperatorNode {
	@NonNull
	private final String m_sql;

	@NonNull
	private final Object[] m_parameters;

	@NonNull
	private final Class< ? >[] m_types;

	public QSqlRestriction(@NonNull String sql, @NonNull Object[] parameters, @NonNull Class< ? >[] types) {
		super(QOperation.SQL);
		m_sql = sql;
		m_parameters = parameters;
		m_types = types;
		if(parameters.length != types.length)
			throw new IllegalStateException("Parameter and type array do not have the same size");
	}

	public QSqlRestriction(@NonNull String sql, @NonNull Object[] parameters) {
		super(QOperation.SQL);
		m_sql = sql;
		m_parameters = parameters;
		m_types = new Class< ? >[parameters.length];
		for(int i = parameters.length; --i >= 0;) {
			Object o = parameters[i];
			if(null == o)
				throw new IllegalStateException("Parameter value cannot be null");
			m_types[i] = o.getClass();
		}
	}

	@Override
	public QSqlRestriction dup() {
		return new QSqlRestriction(getSql(), getParameters(), getTypes());
	}

	@Override
	public void visit(@NonNull QNodeVisitor v) throws Exception {
		v.visitSqlRestriction(this);
	}

	@NonNull
	public String getSql() {
		return m_sql;
	}

	@NonNull
	public Object[] getParameters() {
		return m_parameters;
	}

	@NonNull
	public Class< ? >[] getTypes() {
		return m_types;
	}
}

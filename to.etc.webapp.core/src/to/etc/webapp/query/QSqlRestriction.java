package to.etc.webapp.query;

import javax.annotation.*;

/**
 * A verbatim SQL fragment, with optional JDBC parameters, to use inside a query.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2013
 */
public class QSqlRestriction extends QOperatorNode {
	@Nonnull
	private final String m_sql;

	@Nonnull
	private final Object[] m_parameters;

	@Nonnull
	private final Class< ? >[] m_types;

	public QSqlRestriction(@Nonnull String sql, @Nonnull Object[] parameters, @Nonnull Class< ? >[] types) {
		super(QOperation.SQL);
		m_sql = sql;
		m_parameters = parameters;
		m_types = types;
		if(parameters.length != types.length)
			throw new IllegalStateException("Parameter and type array do not have the same size");
	}

	public QSqlRestriction(@Nonnull String sql, @Nonnull Object[] parameters) {
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
	public void visit(@Nonnull QNodeVisitor v) throws Exception {
		v.visitSqlRestriction(this);
	}

	@Nonnull
	public String getSql() {
		return m_sql;
	}

	@Nonnull
	public Object[] getParameters() {
		return m_parameters;
	}

	@Nonnull
	public Class< ? >[] getTypes() {
		return m_types;
	}
}

package to.etc.webapp.qsql;

import java.util.*;

import to.etc.webapp.query.*;

public class JdbcQueryHandler implements IQueryExecutor<QDataContext>, IQueryExecutorFactory {
	static public final JdbcQueryHandler FACTORY = new JdbcQueryHandler();

	protected JdbcQueryHandler() {}

	/**
	 * FIXME Determine if this is a JDBC query.
	 * @param clz
	 * @return
	 */
	static private boolean isJdbcQuery(Class< ? > clz) {
		return clz.getAnnotation(QJdbcTable.class) != null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IQueryHandlerFactory impl.							*/
	/*--------------------------------------------------------------*/
	@Override
	public IQueryExecutor< ? > findContextHandler(QDataContext root, Class< ? > clz) {
		if(clz != null && isJdbcQuery(clz))
			return this;
		return null;
	}

	@Override
	public IQueryExecutor< ? > findContextHandler(QDataContext root, ICriteriaTableDef< ? > tableMeta) {
		return null;
	}

	@Override
	public IQueryExecutor< ? > findContextHandler(QDataContext root, Object recordInstance) {
		if(recordInstance == null || !isJdbcQuery(recordInstance.getClass()))
			return null;
		return this;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IAbstractQueryHandler impl.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.webapp.query.IQueryExecutor#find(to.etc.webapp.query.QDataContext, java.lang.Class, java.lang.Object)
	 */
	@Override
	public <T> T find(QDataContext root, Class<T> clz, Object pk) throws Exception {
		return JdbcQuery.find(root, clz, pk);
	}

	@Override
	public <T> T getInstance(QDataContext root, Class<T> clz, Object pk) throws Exception {
		return JdbcQuery.getInstance(root, clz, pk);
	}

	@Override
	public <T> List<T> query(QDataContext root, QCriteria<T> q) throws Exception {
		return JdbcQuery.query(root, q);
	}

	@Override
	public List<Object[]> query(QDataContext root, QSelection< ? > sel) throws Exception {
		return JdbcQuery.query(root, sel);
	}

	@Override
	public void refresh(QDataContext root, Object o) throws Exception {
	//-- Noop
	}

	@Override
	public void save(QDataContext root, Object o) throws Exception {
		throw new IllegalStateException("Save operation not implemented for QJdbc classes");
	}

	@Override
	public void delete(QDataContext root, Object o) throws Exception {
		throw new IllegalStateException("delete operation not implemented for QJdbc classes");
	}

	@Override
	public void attach(QDataContext root, Object o) throws Exception {
		throw new IllegalStateException("attach operation not implemented for QJdbc classes");
	}
}

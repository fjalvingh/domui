package to.etc.webapp.query;

public interface IQueryExecutorFactory {
	/**
	 *
	 * @param root
	 * @param clz
	 * @return
	 */
	IQueryExecutor< ? > findContextHandler(QDataContext root, Class< ? > clz);

	IQueryExecutor< ? > findContextHandler(QDataContext root, Object recordInstance);

	IQueryExecutor< ? > findContextHandler(QDataContext root, ICriteriaTableDef< ? > tableMeta);
}

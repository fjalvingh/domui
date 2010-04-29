package to.etc.webapp.query;

public interface IQueryHandlerFactory {
	/**
	 *
	 * @param root
	 * @param clz
	 * @return
	 */
	IAbstractQueryHandler< ? > findContextHandler(QDataContext root, Class< ? > clz);

	IAbstractQueryHandler< ? > findContextHandler(QDataContext root, Object recordInstance);

	/**
	 *
	 * @param root
	 * @param criteria
	 * @return
	 */
	IAbstractQueryHandler< ? > findContextHandler(QDataContext root, QCriteriaQueryBase< ? > criteria);

	IAbstractQueryHandler< ? > findContextHandler(QDataContext root, ICriteriaTableDef< ? > tableMeta);
}

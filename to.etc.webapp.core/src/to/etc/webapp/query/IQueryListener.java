package to.etc.webapp.query;

/**
 * Listener for queries. The methods herein are called before the queries are
 * executed, and can change the query if needed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 15, 2009
 */
public interface IQueryListener {
	/**
	 * Called before a {@link QDataContext.query(QCriteria)} is executed.
	 * @param criteria
	 * @throws Exception
	 */
	void	onBeforeQuery(QCriteria<?> criteria) throws Exception;

	/**
	 * Called before a {@link QDataContext.query(QSelection)} is executed.
	 * @param criteria
	 * @throws Exception
	 */
	void	onBeforeQuery(QSelection<?> criteria) throws Exception;
}

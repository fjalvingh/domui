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
	 * Called before a {@link QDataContext#query(QCriteria)} or {@link QDataContext#query(QSelection)} is
	 * executed; it can alter the query if needed.
	 * @param criteria
	 * @throws Exception
	 */
	void	onBeforeQuery(QDataContext dc, QRestrictionsBase<?> criteria) throws Exception;
}

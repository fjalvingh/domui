package to.etc.dbpool;

import java.sql.*;

import javax.annotation.*;

/**
 * This local interface handles the basic collection of statistic gathering events. Once data for
 * a statement is complete it will be posted to the registered statistics gatherers. Two implementations
 * exist: a sink one that ignores all events (and which is fast) and the real one which will collect
 * statistics to post to listeners.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 27, 2011
 */
interface IInfoHandler {
	/**
	 * Called when a prepare is started. This starts the prepare clock for the statement.
	 * @param sql
	 */
	void prepareStatement(@Nonnull StatementProxy sp);

	/**
	 * Called when the prepare call has finished. Ends the prepare clock, and posts the "statement prepared" event.
	 * @param sql
	 * @param sp
	 */
	void prepareStatementEnd(@Nonnull StatementProxy sp);

	/**
	 * Started a statement query.
	 * @param sp
	 */
	void executeQueryStart(@Nonnull StatementProxy sp, @Nonnull ResultSetProxy rsp);

	void executeQueryError(@Nonnull StatementProxy sp, @Nonnull ResultSetProxy rpx, @Nonnull Exception x);

	void executeQueryEnd(@Nonnull StatementProxy sp, @Nonnull ResultSetProxy rs);

	/**
	 * Generic close result set.
	 * @param sp
	 * @param rsp
	 */
	void resultSetClosed(@Nonnull StatementProxy sp, @Nonnull ResultSetProxy rsp);

	void executeUpdateStart(StatementProxy sp);

	void executeUpdateEnd(StatementProxy sp, SQLException error, int rowcount);

	void executeStart(StatementProxy sp);

	void executeEnd(StatementProxy sp, SQLException error, Boolean result);


}

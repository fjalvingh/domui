package to.etc.dbpool;

import org.eclipse.jdt.annotation.NonNull;

import java.sql.Connection;

/**
 * Gets called on database-connection related events. Calling is a best effort. Calls can be done more than once.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 11, 2012
 */
public interface IDatabaseEventListener {
	/**
	 * Called just before the connection is released back to the pool (or closed). Usually called
	 * within {@link Connection#close()}, but can also be called for other forced close reasons.
	 * @param dbc
	 * @throws Exception
	 */
	void onBeforeRelease(@NonNull Connection dbc) throws Exception;

	/**
	 * Called when a commit has been done <b>and has been succesful</b> (meaning no exception was thrown).
	 * @param dbc
	 * @throws Exception
	 */
	void onAfterCommit(@NonNull Connection dbc) throws Exception;

	/**
	 * Called when a rollback has been done <b>and has been succesful</b> (meaning no exception was thrown).
	 * @param dbc
	 * @throws Exception
	 */
	void onAfterRollback(@NonNull Connection dbc) throws Exception;
}

package to.etc.dbpool;

import java.sql.*;

/**
 * @author jal
 * Created on Jan 22, 2005
 */
public interface DbConnectorSet {
	public String getID();

	public DbConnector getPooledConnector();

	public DbConnector getUnpooledConnector();

	/**
	 * Special use: this returns a NEW connection for a process and NEVER
	 * uses any thread-connections! This can be used in the rare case that
	 * a process needs 2 connections to the same pool at once!
	 * <p>An example of this is the daemon loader: it uses one connection
	 * for the individual loads and another to update load status 
	 * information. Since the load can be cancelled causing a rollback we
	 * do not want to use that connection.
	 * 
	 * @return
	 */
	public Connection getNewPooledConnection() throws SQLException;
}

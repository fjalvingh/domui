package to.etc.dbpool;

import java.sql.*;

/**
 * <p>This is a placeholder for a connection. It keeps a "reference" to a
 * given database connection which is not the physical connection but
 * the parameters needed to create a connection to that database. It
 * is used to keep a holder to the database that was used/is needed
 * to obtain data for an object, without actually keeping a connection
 * to that database. 
 * <p>Connections allocated by this method MUST BE RELEASED WITH CLOSE
 * or they will be lost..
 *  
 * Created on Oct 17, 2003
 * @author jal
 */
public interface DbConnector {
	public String getID();

	public Connection makeConnection() throws SQLException;
}

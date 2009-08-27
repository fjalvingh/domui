package to.etc.binaries.cache;

import java.sql.*;
import java.util.*;

import to.etc.binaries.images.*;

/**
 * Implementation to handle storing stuff in whatever database layout.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 15, 2006
 */
public interface BinariesCacheDAO {
	/**
	 * Checks the database to see if a binary with the appropriate hash and type does exist.
	 *
	 * @param dbc
	 * @param size
	 * @param type
	 * @param hash
	 * @return          The PK of the record containing this binary, or -1 if not found.
	 * @throws SQLException
	 */
	public long checkExisting(Connection dbc, long size, String type, String hash) throws SQLException;

	public long insertBinary(Connection dbc, String type, long original, String hash, ImageDataSource bds) throws Exception;

	public List<BinaryInfo> reloadInfo(Connection dbc, long pk) throws Exception;

	public PreparedStatement findRecord(Connection dbc, long pk) throws Exception;
}

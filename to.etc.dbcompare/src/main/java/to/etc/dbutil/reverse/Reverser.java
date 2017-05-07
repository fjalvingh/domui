package to.etc.dbutil.reverse;

import java.sql.*;
import java.util.*;

import javax.annotation.*;

import to.etc.dbutil.schema.*;
import to.etc.webapp.query.*;

/**
 * Thingy which reads a DB schema.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2006
 */
public interface Reverser {
	@Nonnull
	public String getIdent();

	@Nonnull
	public DbSchema loadSchema(@Nullable String schemaName, boolean lazily) throws Exception;

	public void lazy(@Nonnull IExec what);

	public boolean typeHasPrecision(@Nonnull DbColumn column);

	public boolean typeHasScale(@Nonnull DbColumn column);

	public SQLRowSet getData(@Nonnull QCriteria<SQLRow> query, int start, int end) throws Exception;

	public void addSelectColumnAs(@Nonnull StringBuilder statement, @Nonnull String colname, @Nonnull String alias);

	public String wrapQueryWithRange(@Nonnull List<DbColumn> coll, @Nonnull String sql, int first, int max);


	public void reverseColumns(@Nonnull Connection dbc, @Nonnull DbTable t) throws Exception;

	public void reverseIndexes(@Nonnull Connection dbc, @Nonnull DbTable t) throws Exception;

	public void reversePrimaryKey(@Nonnull Connection dbc, @Nonnull DbTable t) throws Exception;

	public void reverseParentRelation(Connection dbc, @Nonnull DbTable dbTable) throws Exception;

	public void reverseChildRelations(Connection dbc, @Nonnull DbTable dbTable) throws Exception;
}

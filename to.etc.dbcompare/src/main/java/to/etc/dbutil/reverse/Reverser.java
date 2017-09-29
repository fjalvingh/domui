package to.etc.dbutil.reverse;

import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbSchema;
import to.etc.dbutil.schema.DbTable;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

	@Nonnull
	public Set<DbSchema> loadSchemaSet(@Nonnull Collection<String> schemaName, boolean lazily) throws Exception;

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

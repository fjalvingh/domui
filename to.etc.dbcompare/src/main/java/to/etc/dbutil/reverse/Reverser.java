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
	@Nonnull String getIdent();

	@Nonnull DbSchema loadSchema(@Nullable String schemaName, boolean lazily) throws Exception;

	@Nonnull Set<DbSchema> loadSchemaSet(@Nonnull Collection<String> schemaName, boolean lazily) throws Exception;

	void lazy(@Nonnull IExec what);

	boolean typeHasPrecision(@Nonnull DbColumn column);

	boolean typeHasScale(@Nonnull DbColumn column);

	SQLRowSet getData(@Nonnull QCriteria<SQLRow> query, int start, int end) throws Exception;

	void addSelectColumnAs(@Nonnull StringBuilder statement, @Nonnull String colname, @Nonnull String alias);

	String wrapQueryWithRange(@Nonnull List<DbColumn> coll, @Nonnull String sql, int first, int max);


	void reverseColumns(@Nonnull Connection dbc, @Nonnull DbTable t) throws Exception;

	void reverseIndexes(@Nonnull Connection dbc, @Nonnull DbTable t) throws Exception;

	void reversePrimaryKey(@Nonnull Connection dbc, @Nonnull DbTable t) throws Exception;

	void reverseParentRelation(Connection dbc, @Nonnull DbTable dbTable) throws Exception;

	void reverseChildRelations(Connection dbc, @Nonnull DbTable dbTable) throws Exception;
}

package to.etc.dbutil.reverse;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbSchema;
import to.etc.dbutil.schema.DbTable;
import to.etc.webapp.query.QCriteria;

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
	@NonNull String getIdent();

	@NonNull String getDefaultSchemaName() throws Exception;

	@NonNull DbSchema loadSchema(@Nullable String schemaName, boolean lazily) throws Exception;

	@NonNull Set<DbSchema> getSchemas(boolean lazily) throws Exception;

	@NonNull Set<DbSchema> loadSchemaSet(@NonNull Collection<String> schemaName, boolean lazily) throws Exception;

	void lazy(@NonNull IExec what);

	boolean typeHasPrecision(@NonNull DbColumn column);

	boolean typeHasScale(@NonNull DbColumn column);

	SQLRowSet getData(@NonNull QCriteria<SQLRow> query, int start, int end) throws Exception;

	void addSelectColumnAs(@NonNull StringBuilder statement, @NonNull String colname, @NonNull String alias);

	String wrapQueryWithRange(@NonNull List<DbColumn> coll, @NonNull String sql, int first, int max);


	void reverseColumns(@NonNull Connection dbc, @NonNull DbTable t) throws Exception;

	void reverseIndexes(@NonNull Connection dbc, @NonNull DbTable t) throws Exception;

	void reversePrimaryKey(@NonNull Connection dbc, @NonNull DbTable t) throws Exception;

	void reverseParentRelation(Connection dbc, @NonNull DbTable dbTable) throws Exception;

	void reverseChildRelations(Connection dbc, @NonNull DbTable dbTable) throws Exception;
}

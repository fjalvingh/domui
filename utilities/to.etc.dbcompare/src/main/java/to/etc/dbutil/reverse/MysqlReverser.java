package to.etc.dbutil.reverse;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbRelation;
import to.etc.dbutil.schema.DbRelation.RelationUpdateAction;
import to.etc.dbutil.schema.DbSchema;
import to.etc.dbutil.schema.DbTable;
import to.etc.util.FileTool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles the messy way MySQL handles tables (in catalogs, which are actually databases). As
 * catalogs are apparently seen regardless of which "database" you connect to we will treat them
 * as schema's.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 23-06-2022.
 */
public class MysqlReverser extends JDBCReverser {
	public MysqlReverser(DataSource dbc, Set<ReverserOption> optionSet) {
		super(dbc, optionSet);
	}

	public MysqlReverser(Connection conn, Set<ReverserOption> optionSet) {
		super(conn, optionSet);
	}

	@Override
	protected void prepareConnection(Connection dbc) {
		//try(Statement st = dbc.createStatement()) {
		//	st.execute("set innodb_stats_on_metadata=0");
		//	dbc.commit();
		//} catch(Exception x) {
		//	x.printStackTrace();
		//}
	}

	/**
	 * MySQL does not have schema's, but we can access all "databases" as if
	 * they are... So return the databases and treat them as schema's.
	 */
	@Override
	@SuppressWarnings("squid:S2095")		// Connection is optionally closed
	protected Set<DbSchema> getSchemasOnly(boolean lazily) throws Exception {
		Connection dbc = getDataSource().getConnection();
		dbc.setAutoCommit(false);
		prepareConnection(dbc);
		try {
			Set<DbSchema> schemaSet = new HashSet<>();
			DatabaseMetaData dmd = dbc.getMetaData();
			try(ResultSet rs = dmd.getCatalogs()) {
				while(rs.next()) {
					recordLoaded();
					String name = rs.getString("TABLE_CAT");
					name = translateSchemaName(dbc, name);
					if(null != name) {
						DbSchema schema = new DbSchema(this, null, name);		// This is a catalog treated as a schema
						schemaSet.add(schema);
					}
				}
			}
			return schemaSet;
		} finally {
			if(!isKeepConnectionsOpen()) {
				FileTool.closeAll(dbc);
			}
		}
	}

	@Override
	public void reverseRelations(@NonNull Connection dbc, @NonNull Set<DbSchema> schemaSet) throws Exception {
		for(DbSchema dbSchema : schemaSet) {
			reverseRelations(dbc, dbSchema);
		}
	}

	private Set<String> m_usedRoleNames = new HashSet<>();

	private void reverseRelations(Connection dbc, DbSchema schema) throws Exception {
		//-- First: mark all relations initialized
		for(DbTable table : schema.getTables()) {
			table.markRelationsInitialized();
		}

		int roleNameIndex = 0;
		String name = null;
		String sql = "select constraint_name,table_schema,table_name,column_name,referenced_table_schema,referenced_table_name,referenced_column_name,ordinal_position"
			+ " from information_schema.key_column_usage"
			+ " where constraint_schema=? and referenced_table_schema=?";

		try(PreparedStatement ps = dbc.prepareStatement(sql)) {
			ps.setQueryTimeout(240);
			ps.setFetchSize(Integer.MIN_VALUE);
			ps.setString(1, schema.getInternalCatalogName());
			ps.setString(2, schema.getInternalCatalogName());
			try(ResultSet rs = ps.executeQuery()) {
				recordLoaded();

				int lastKeySeq = -1;
				DbRelation rel = null;
				while(rs.next()) {
					int i = 1;
					String fkName = rs.getString(i++);

					String fkSchemaName = rs.getString(i++);
					String childTblName = rs.getString(i++);
					String childColName = rs.getString(i++);

					String pkSchemaName = rs.getString(i++);
					String parentTblName = rs.getString(i++);
					String parentColName = rs.getString(i++);

					System.out.println("rr " + childTblName + " >- " + parentTblName);

					int keySeq = rs.getInt(i++);

					DbSchema fkSchema = findSchema(fkSchemaName);
					DbSchema pkSchema = findSchema(pkSchemaName);

					if(null != parentTblName && null != childTblName && null != parentColName && null != childColName) {
						if(fkName == null)
							fkName = parentTblName + "_" + childTblName;
						String baseName = fkName;
						while(!m_usedRoleNames.add(fkName)) {
							fkName = baseName + "_" + ++roleNameIndex;
						}

						//-- Find tables
						DbTable childTable = schema.findTable(childTblName);
						DbTable parentTable = schema.findTable(parentTblName);
						if(null == childTable) {
							report(ReverserOption.ReverseRelations, ProgressType.Error, "Unknown child table '" + childTblName + "'");
						} else if(parentTable == null) {
							report(ReverserOption.ReverseRelations, ProgressType.Error, "Unknown parent table '" + parentTblName + "'");
						} else {
							DbColumn parentCol = parentTable.findColumn(parentColName);
							DbColumn childCol = childTable.findColumn(childColName);
							if(null == parentCol) {
								report(ReverserOption.ReverseRelations, ProgressType.Error, "Unknown column " + parentColName + " in parent table " + parentTblName + " (relation)");
							} else if(null == childCol) {
								report(ReverserOption.ReverseRelations, ProgressType.Error, "Unknown column " + childColName + " in child table " + childTblName + " (relation)");
							} else {
								if(keySeq == 1) {
									//-- New relation
									rel = new DbRelation(parentTable, childTable, RelationUpdateAction.None, RelationUpdateAction.None);
									parentTable.getParentRelationList().add(rel);
									childTable.getChildRelationList().add(rel);
									rel.setName(fkName);
									rel.addPair(parentCol, childCol);
								} else if(keySeq <= lastKeySeq) {
									report(ReverserOption.ReverseRelations, ProgressType.Error, "error: keyseq is incorrect in relation query");
								} else if(rel == null) {
									report(ReverserOption.ReverseRelations, ProgressType.Error, "error: no relation but keySeq=" + keySeq);
								} else {
									rel.addPair(parentCol, childCol);
								}
							}
						}

						lastKeySeq = keySeq;
					}
				}
			}
		}
	}

	@Override
	@Nullable
	protected String getSchemaFromMetadataSet(ResultSet rs) throws Exception {
		return rs.getString("TABLE_CAT");
	}

}

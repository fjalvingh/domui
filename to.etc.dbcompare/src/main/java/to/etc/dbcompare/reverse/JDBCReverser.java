package to.etc.dbcompare.reverse;

import java.sql.*;
import java.util.*;

import to.etc.dbcompare.db.*;

/**
 * Generic impl of a jdbc-based reverser.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2006
 */
public class JDBCReverser implements Reverser {
	private String				m_schemaname;

	private Connection			m_dbc;

	private Schema				m_schema;

	private DatabaseMetaData	m_dmd;

	public JDBCReverser(Connection dbc, String schemaname) {
		m_schemaname = schemaname;
		m_dbc = dbc;
	}

	public final String getSchemaName() {
		return m_schemaname;
	}

	public final Connection dbc() {
		return m_dbc;
	}

	public final Schema getSchema() {
		return m_schema;
	}

	public Schema loadSchema() throws Exception {
		m_schema = new Schema(m_schemaname);
		m_dmd = m_dbc.getMetaData();
		reverseTables();
		reverseColumns();
		int ncols = 0;
		for(Table t : m_schema.getTableMap().values()) {
			ncols += t.getColumnList().size();
		}
		msg("Loaded " + ncols + " columns");
		reverseIndexes();
		reversePrimaryKeys();
		reverseRelations();
		reverseViews();
		reverseProcedures();
		reversePackages();
		reverseTriggers();
		reverseConstraints();
		return m_schema;
	}

	public void reverseIndexes() throws Exception {
		for(Table t : m_schema.getTableMap().values())
			reverseIndexes(t);
	}

	public void reversePrimaryKeys() throws Exception {
		for(Table t : m_schema.getTableMap().values())
			reversePrimaryKey(t);
	}

	public void reverseRelations() throws Exception {
		for(Table t : m_schema.getTableMap().values())
			reverseRelations(t);
	}

	public void reverseColumns() throws Exception {
		for(Table t : m_schema.getTableMap().values())
			reverseColumns(t);
	}

	public String getIdent() {
		return "Generic JDBC database reverse-engineering plug-in";
	}

	protected void msg(String s) {
		System.out.println(m_schema.getName() + ": " + s);
	}

	protected void warning(String s) {
		System.out.println("WARNING " + m_schema.getName() + ": " + s);
	}

	protected boolean isValidTable(ResultSet rs) throws Exception {
		return true;
	}

	protected void reverseTables() throws Exception {
		ResultSet rs = null;
		try {
			rs = m_dmd.getTables(null, m_schema.getName(), null, new String[]{"TABLE"});
			while(rs.next()) {
				if(isValidTable(rs)) {
					String name = rs.getString("TABLE_NAME");
					Table t = m_schema.createTable(name);
					t.setComments(rs.getString("REMARKS"));
				}
			}
			msg("Loaded " + m_schema.getTableMap().size() + " tables");
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
		}
	}

	protected void reverseColumns(Table t) throws Exception {
		ResultSet rs = null;
		try {
			rs = m_dmd.getColumns(null, m_schema.getName(), t.getName(), null); // All columns in the schema.
			int lastord = -1;
			while(rs.next()) {
				String name = rs.getString("COLUMN_NAME");
				int ord = rs.getInt("ORDINAL_POSITION");
				//                System.out.println(ord+" - " +name);
				if(lastord == -1) {
					lastord = ord;
				} else {
					if(lastord + 1 != ord)
						throw new IllegalStateException("JDBC driver trouble: getColumns() does not return cols ordered by position: " + lastord + ", " + ord);
					lastord = ord;
				}
				if(name.equals("BET_MJB"))
					dumpRow(rs);

				int daty = rs.getInt("DATA_TYPE"); // Types.xxx 
				String typename = rs.getString("TYPE_NAME");
				int prec = rs.getInt("COLUMN_SIZE");
				int scale = rs.getInt("DECIMAL_DIGITS");
				int nulla = rs.getInt("NULLABLE");
				if(nulla == DatabaseMetaData.columnNullableUnknown)
					throw new IllegalStateException("JDBC driver does not know nullability of " + t.getName() + "." + name);
				ColumnType ct = decodeColumnType(daty, typename);
				if(ct == null)
					throw new IllegalStateException("Unknown type: SQLType " + daty + " (" + typename + ") in " + t.getName() + "." + name);

				Column c = t.createColumn(name, ct, prec, scale, nulla == DatabaseMetaData.columnNullable);
				c.setComment(rs.getString("REMARKS"));
				c.setPlatformTypeName(typename);
				c.setSqlType(daty);
			}
			msg("Loaded " + t.getName() + ": " + t.getColumnMap().size() + " columns");
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
		}
	}

	static private void dumpRow(ResultSet rs) throws Exception {
		for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
			System.out.println(i + " (" + rs.getMetaData().getColumnName(i) + ") = " + rs.getString(i));
		}
	}

	protected void reverseIndexes(Table t) throws Exception {
		ResultSet rs = null;
		try {
			rs = m_dmd.getIndexInfo(null, m_schema.getName(), t.getName(), false, true);
			int lastord = -1;
			String lastindex = null;
			Index ix = null;
			while(rs.next()) {
				boolean nonunique = rs.getBoolean("NON_UNIQUE");
				String name = rs.getString("INDEX_NAME");
				int ord = rs.getInt("ORDINAL_POSITION");
				String col = rs.getString("COLUMN_NAME");
				String s = rs.getString("ASC_OR_DESC");
				boolean desc = "D".equalsIgnoreCase(s);
				if(col == null) {
					System.out.println("Null index column in index " + name + " of table " + t.getName());
					continue;
				}
				Column c = t.getColumn(col);

				//-- Is a new index being defined?
				if(lastindex == null || !lastindex.equals(name)) {
					lastindex = name;
					ix = t.createIndex(name, !nonunique);
					m_schema.addIndex(ix);
					lastord = -1;
				} else {
					if(lastord == -1) {
						lastord = ord;
					} else {
						if(lastord + 1 != ord)
							throw new IllegalStateException("JDBC driver trouble: getIndexes() does not return cols ordered by position: " + lastord + ", " + ord);
						lastord = ord;
					}
				}
				ix.addColumn(c, desc);
			}
			msg("Loaded " + t.getName() + ": " + t.getColumnMap().size() + " indexes");
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
		}
	}

	protected void reversePrimaryKey(Table t) throws Exception {
		ResultSet rs = null;
		List<Column> pkl = new ArrayList<Column>(); // Stupid resultset is ordered by NAME instead of ordinal. Dumbfuckers.
		try {
			rs = m_dmd.getPrimaryKeys(null, m_schema.getName(), t.getName());
			PrimaryKey pk = null;
			String name = null;
			while(rs.next()) {
				name = rs.getString("PK_NAME");
				int ord = rs.getInt("KEY_SEQ");
				String col = rs.getString("COLUMN_NAME");
				if(col == null) {
					System.out.println("Null PK column in PK " + name + " of table " + t.getName());
					continue;
				}
				Column c = t.getColumn(col);
				while(pkl.size() <= ord)
					pkl.add(null);
				pkl.set(ord, c);
			}
			if(name != null) {
				pk = new PrimaryKey(t, name);
				t.setPrimaryKey(pk);
				for(Column c : pkl)
					if(c != null)
						pk.addColumn(c);
			}
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Reverse-engineer all PK -> FK relations.
	 * @throws Exception
	 */
	protected void reverseRelations(Table t) throws Exception {
		ResultSet rs = null;
		try {
			String name = null;
			rs = m_dmd.getExportedKeys(null, m_schema.getName(), t.getName());
			int lastord = -1;
			Relation rel = null;
			while(rs.next()) {
				String fktname = rs.getString("FKTABLE_NAME");
				String pktname = rs.getString("PKTABLE_NAME");
				String fkcname = rs.getString("FKCOLUMN_NAME");
				String pkcname = rs.getString("PKCOLUMN_NAME");
				String fkname = rs.getString("FK_NAME");
				if(fkname != null && fkname.length() > 0 && name == null)
					name = fkname;
				int ord = rs.getInt("KEY_SEQ");
				if(!pktname.equals(t.getName()))
					throw new IllegalStateException("JDBC driver trouble: getExportedKeys returned key from table " + pktname + " while asking for table " + t.getName());

				//-- Find FK table and column and PK column referred to
				Table fkt = m_schema.getTable(fktname);
				Column fkc = fkt.getColumn(fkcname);
				Column pkc = t.getColumn(pkcname);

				//-- If this is a new sequence start a new relation else add to current,
				if(lastord == -1 || ord <= lastord) {
					//-- New relation.
					rel = new Relation(t, fkt);
					lastord = ord;
					t.getParentRelationList().add(rel);
					fkt.getChildRelationList().add(rel);
				}

				//-- Add the relation fields.
				if(name != null)
					rel.setName(name);
				rel.addPair(pkc, fkc);
			}
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Very simple and naive impl of mapping the genericized type.
	 * @param sqltype
	 * @param typename
	 * @return
	 */
	public ColumnType decodeColumnType(int sqltype, String typename) {
		for(ColumnType t : ColumnType.getTypes()) {
			if(t.getSqlType() == sqltype)
				return t;
		}
		switch(sqltype){
			case Types.DECIMAL:
				return ColumnType.NUMBER;
			case Types.LONGVARCHAR:
				return ColumnType.CLOB;
			case Types.VARBINARY:
				return ColumnType.BLOB;
			case Types.FLOAT:
				return ColumnType.FLOAT;
			case Types.LONGVARBINARY:
				return ColumnType.BLOB;
		}
		return null;
		//        throw new IllegalStateException("Cannot convert SQLTYPE="+sqltype+": "+typename+" to a generic column type");
	}

	public void reverseViews() throws Exception {
	}

	public void reverseProcedures() throws Exception {
	}

	public void reverseTriggers() throws Exception {
	}

	public void reversePackages() throws Exception {
	}

	public void reverseConstraints() throws Exception {
	}

}

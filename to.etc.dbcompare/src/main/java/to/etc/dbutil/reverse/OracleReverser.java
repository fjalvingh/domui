package to.etc.dbutil.reverse;

import java.sql.*;
import java.util.*;

import javax.annotation.*;
import javax.sql.*;

import to.etc.dbutil.schema.*;
import to.etc.dbutil.schema.Package;
import to.etc.util.*;

/**
 * Oracle kost emmers aan geld, maar de godvergeten stomme eikels zijn te achterlijk
 * om voor dat geld een fatsoenlijke JDBC driver te bouwen. Daarom moeten de meest
 * basale godvergeten calls voor Oracle worden overgeschreven omdat de metadata die
 * uit de fucking driver komt niet klopt. Wat een rotzooi.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 14, 2007
 */
public class OracleReverser extends JDBCReverser {
	public OracleReverser(DataSource dbc, DatabaseMetaData dmd) {
		super(dbc, dmd);
	}

	@Override
	public String getIdent() {
		return "Generic ORACLE Database reverse-engineering plug-in";
	}

	@Override
	protected void afterLoad(@Nonnull Connection dbc, @Nonnull DbSchema schema) throws Exception {
		super.afterLoad(dbc, schema);

		//-- Append extra ORACLE data from meta (column and table comments)
		updateTableComments(dbc, schema);
	}

	@Override
	protected String translateSchemaName(@Nonnull Connection dbc, @Nullable String name) throws Exception {
		if(name != null)
			return name.toUpperCase();

		//-- Get current schema
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc.prepareStatement("select user from dual");
			rs = ps.executeQuery();
			if(!rs.next())
				throw new SQLException("No result");
			return rs.getString(1);
		} finally {
			FileTool.closeAll(rs, ps);
		}
	}

	/**
	 * Override column reverser because crap oracle driver does not properly return column lengths.
	 */
	@Override
	public void reverseColumns(@Nonnull Connection dbc, @Nonnull Set<DbSchema> schema) throws Exception {
		columnScanner(dbc, schema, null);
	}

	private void columnScanner(@Nonnull Connection dbc, @Nonnull Set<DbSchema> schemaSet, @Nullable String tablename) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<DbColumn> columnList = new ArrayList<DbColumn>();
		Map<String, DbColumn> columnMap = new HashMap<String, DbColumn>();

		try {
			String extrawhere = tablename == null ? "" : " and c.table_name=?";
			ps = dbc.prepareStatement(
				"select c.owner, c.table_name,c.column_name,c.data_type,c.data_precision,c.data_scale,c.nullable,c.column_id,c.char_length,c.char_used, r.comments"
					+ " from all_tab_columns c left outer join all_col_comments r"
					+ " on c.owner=r.owner and c.table_name=r.table_name and c.column_name=r.column_name"
					+ " where 1=1 "+extrawhere+" order by c.table_name, c.column_id"
			);
			if(null != tablename)
				ps.setString(2, tablename);
			rs = ps.executeQuery();
			String last = "";
			DbTable t = null;
			while(rs.next()) {
				int i = 1;
				String schn = rs.getString(i++);
				DbSchema schema = findSchema(schemaSet, schn);
				if(null == schema)
					continue;

				String tn = rs.getString(i++);
				String cn = rs.getString(i++);
				String typename = rs.getString(i++);
				int precision = rs.getInt(i++);
				if(rs.wasNull())
					precision = -1;
				int scale = rs.getInt(i++);
				if(rs.wasNull())
					scale = -1;
				String s = rs.getString(i++);
				boolean nullable = s == null || s.equalsIgnoreCase("Y");

				i++;

				int charlen = rs.getInt(i++);
				if(rs.wasNull())
					charlen = -1;
				s = rs.getString(i++);
				String remark = rs.getString(i++);

				if(!last.equals(tn)) {
					//-- New table. Lookkitup
					t = schema.findTable(tn);
					last = tn;

					columnList = new ArrayList<DbColumn>();
					columnMap = new HashMap<String, DbColumn>();
					t.initializeColumns(columnList, columnMap);
				}
				if(t == null)
					continue; // Skip unknown tables (usually BIN$ crapshit from the recycle kludge)

				DbColumn c = t.findColumn(cn);
				if(c != null)
					throw new IllegalStateException("Duplicate column " + cn + " in table " + tn + "!?!?!");

				//-- Create this column thingy..
				int daty = oracleTypeToSQLType(typename);
				ColumnType ct = decodeColumnType(daty, typename);
				if(ct == null)
					throw new IllegalStateException("Unknown type: SQLType " + daty + " (" + typename + ") in " + t.getName() + "." + cn);

				switch(daty){
					case Types.VARCHAR:
					case Types.CHAR:
						precision = charlen;
				}

				c = new DbColumn(t, cn, ct, precision, scale, nullable, Boolean.FALSE);
				if(null != columnMap.put(cn, c))
					throw new IllegalStateException("Duplicate column name '" + cn + "' in table " + t.getName());
				columnList.add(c);

				c.setPlatformTypeName(typename);
				c.setSqlType(daty);
				c.setComment(remark);
			}
			msg("Loaded " + t.getName() + ": " + columnMap.size() + " columns");
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	@Override
	public void reverseColumns(Connection dbc, DbTable t) throws Exception {
		DbSchema schema = t.getSchema();
		Set<DbSchema> set = new HashSet<>();
		set.add(schema);
		columnScanner(dbc, set, t.getName());
	}

	static private Map<String, Integer> m_typeMap = new HashMap<String, Integer>();

	static public void registerType(String t, int c) {
		m_typeMap.put(t.toLowerCase(), Integer.valueOf(c));
	}

	static public int oracleTypeToSQLType(String s) {
		s = s.toLowerCase();
		Integer i = m_typeMap.get(s);
		if(i == null) {
			if(s.startsWith("timestamp"))
				return Types.TIMESTAMP;
			throw new IllegalStateException("Unknown Oracle type '" + s + "'");
		}
		return i.intValue();
	}

	//    protected void  updateColumnComments(Schema s) throws Exception {
	//        PreparedStatement ps = null;
	//        ResultSet rs = null;
	//        try
	//        {
	//            int cc = 0;
	//            ps	= dbc().prepareStatement(
	//                "select table_name,column_name,comments from user_col_comments"
	//            );
	//            rs	= ps.executeQuery();
	//            while(rs.next()) {
	//                String  tn  = rs.getString(1);
	//                String  cn  = rs.getString(2);
	//                String  cmt = rs.getString(3);
	//                if(cmt == null)
	//                    continue;
	//                Table   t   = s.findTable(tn);
	//                if(t != null) {
	//                    Column c = t.findColumn(cn);
	//                    if(c != null) {
	//                        c.setComment(cmt);
	//                        cc++;
	//                    }
	//                }
	//            }
	//            msg("Loaded "+cc+" column comments");
	//        }
	//        finally
	//        {
	//            try { if(rs != null) rs.close(); } catch(Exception x){}
	//            try { if(ps != null) ps.close(); } catch(Exception x){}
	//        }
	//    }
	protected void updateTableComments(@Nonnull Connection dbc, DbSchema s) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			int cc = 0;
			ps = dbc.prepareStatement("select table_name,comments from user_tab_comments where TABLE_TYPE='TABLE'");
			rs = ps.executeQuery();
			while(rs.next()) {
				String tn = rs.getString(1);
				String cmt = rs.getString(2);
				if(cmt == null)
					continue;
				DbTable t = s.findTable(tn);
				if(t != null) {
					t.setComments(cmt);
					cc++;
				}
			}
			msg("Loaded " + cc + " table comments");
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	@Override
	protected boolean isValidTable(ResultSet rs) throws Exception {
		return !rs.getString("TABLE_NAME").startsWith("BIN$");
	}

	/**
	 * Temp constraint data from dictionary.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Nov 13, 2007
	 */
	private static class Cons {
		public String name;

		public String table;

		public String owner;

		public String remoteOwner;

		public String remoteConstraint;

		public Cons(String name, String owner, String table, String remoteOwner, String remoteConstraint) {
			this.name = name;
			this.owner = owner;
			this.table = table;
			this.remoteOwner = remoteOwner;
			this.remoteConstraint = remoteConstraint;
		}
	}

	/**
	 * Get a list of either parent or child constraints.
	 * @param dbc
	 * @param table
	 * @param asparent
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	private List<Cons> getRelationConstraints(@Nonnull Connection dbc, @Nonnull DbTable table, boolean asparent) throws Exception {
		List<Cons> list = new ArrayList<Cons>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			//-- Select list of referential constraints,
			String sql = "select a.constraint_name, a.owner, a.table_name, a.r_owner, a.r_constraint_name, a.delete_rule from all_constraints a";
			if(asparent) {
				sql += ", all_constraints b where a.r_constraint_name = b.constraint_name and a.r_owner = b.owner and a.constraint_type = 'R' and b.table_name=? and b.owner=?";
			} else {
				sql += " where a.constraint_type='R' and a.table_name=? and a.owner=? ";
			}

			ps = dbc.prepareStatement(sql);
			ps.setString(1, table.getName());
			ps.setString(2, table.getSchema().getName());
			rs = ps.executeQuery();
			while(rs.next()) {
				String name = rs.getString(1);
				if(name.startsWith("BIN$"))
					continue;
				Cons c = new Cons(name, rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
				String action = rs.getString(6);
				list.add(c);
			}
			return list;
		} finally {
			FileTool.closeAll(rs, ps);
		}
	}

	@Nonnull
	private List<DbColumn> getRelationColumns(@Nonnull Connection dbc, @Nonnull DbSchema schema, @Nonnull String owner, @Nonnull String constraintName) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<DbColumn> res = new ArrayList<DbColumn>();
		try {
			ps = dbc.prepareStatement("select column_name, table_name, position from all_cons_columns where owner=? and constraint_name=? order by position");
			ps.setString(1, owner);
			ps.setString(2, constraintName);
			rs = ps.executeQuery();
			while(rs.next()) {
				String cn = rs.getString(1);
				String tn = rs.getString(2);
				DbTable tbl = schema.getTable(tn);
				DbColumn col = tbl.getColumn(cn);
				res.add(col);
			}
			return res;
		} finally {
			FileTool.closeAll(rs, ps);
		}
	}

	@Override
	public void reverseParentRelation(Connection dbc, DbTable table) throws Exception {
		reverseRelation(dbc, table, true);
	}

	@Override
	public void reverseChildRelations(Connection dbc, DbTable table) throws Exception {
		reverseRelation(dbc, table, false);
	}

	private void reverseRelation(Connection dbc, DbTable table, boolean parent) throws Exception {
		List<Cons> list = getRelationConstraints(dbc, table, parent);
		if(list.size() == 0)
			return;
		for(Cons c : list) {
			List<DbColumn> childColumns = getRelationColumns(dbc, table.getSchema(), c.owner, c.name);							// Get all columns in the FK part
			List<DbColumn> parentColumns = getRelationColumns(dbc, table.getSchema(), c.remoteOwner, c.remoteConstraint);		// Get all columns in the PK part

			DbRelation rel = createRelation(c.owner, c.name, parentColumns, childColumns);
			if(!rel.getParent().internalGetParentRelationList().contains(rel)) {
				rel.getParent().internalGetParentRelationList().add(rel);
				rel.getChild().internalGetChildRelationList().add(rel);
			}
		}
	}

	@Nonnull
	private DbRelation createRelation(@Nonnull String owner, @Nonnull String name, @Nonnull List<DbColumn> parentColumns, @Nonnull List<DbColumn> childColumns) {
		if(parentColumns.size() != childColumns.size())
			throw new IllegalStateException("Parent and child column lists do not have the same size for constraint=" + owner + "." + name);
		if(parentColumns.size() == 0)
			throw new IllegalStateException("No children in constraint " + owner + "." + name);
		DbTable pt = parentColumns.get(0).getTable();
		DbTable ct = childColumns.get(0).getTable();
		DbRelation rel = new DbRelation(pt, ct);
		rel.setName(name);

		for(int i = 0; i < parentColumns.size(); i++) {
			rel.addPair(parentColumns.get(i), childColumns.get(i));
		}
		return rel;
	}

	/**
	 * Override because the stupid Oracle driver does not report a constraint name.
	 *
	 * @see to.etc.dbutil.reverse.JDBCReverser#reverseRelations(to.etc.dbutil.schema.DbTable)
	 */
	@Override
	protected void reverseRelations(@Nonnull Connection dbc, DbTable t) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;

		List<Cons> list = getRelationConstraints(dbc, t, true);
		if(list.size() == 0)
			return;
		try {

			/*
			 * Handle every relation found by locating the matching columns from each table
			 */
			ps = dbc.prepareStatement("select column_name from all_cons_columns where owner=? and constraint_name=? order by position");
			ps2 = dbc.prepareStatement("select column_name, table_name from all_cons_columns where owner=? and constraint_name=? order by position");
			for(Cons sp : list) {
				DbRelation rel = null;
				//-- Prepare FK part,
				ps.setString(1, t.getSchema().getName());
				ps.setString(2, sp.name);
				rs = ps.executeQuery();

				//-- Prepare PK part,
				ps2.setString(1, sp.remoteOwner);
				ps2.setString(2, sp.remoteConstraint); // Constraint name on the other side
				rs2 = ps2.executeQuery();
				DbTable pt = null;

				while(rs.next()) {
					String cn = rs.getString(1);
					DbColumn fkc = t.findColumn(cn);
					if(fkc == null)
						throw new IllegalStateException("Unknown column " + cn + " in table " + t + " for foreign key constraint " + sp.name);

					//-- We must have a matching column on the other (PK) side,
					if(!rs2.next())
						throw new IllegalStateException("Unmatched column " + cn + " in PK table for foreign key constraint " + sp.name);
					String pkcn = rs2.getString(1);
					if(pt == null) {
						String pktn = rs2.getString(2);
						pt = t.getSchema().findTable(pktn);
						if(pt == null)
							throw new IllegalStateException("Can't find table for PK " + pktn + " in PK table for foreign key constraint " + sp.name);

						//-- Create the relation too.
						rel = new DbRelation(pt, t);
						rel.setName(sp.name);
						pt.getParentRelationList().add(rel);
						t.getChildRelationList().add(rel);
					}
					DbColumn pkc = pt.findColumn(pkcn);
					if(pkc == null)
						throw new IllegalStateException("Unknown column " + pkcn + " in PK table " + pt + " for foreign key constraint " + sp.name);
					rel.addPair(pkc, fkc);
				}
				rs.close();
				rs2.close();
			}
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(rs2 != null)
					rs2.close();
			} catch(Exception x) {}
			try {
				if(ps2 != null)
					ps2.close();
			} catch(Exception x) {}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Oracle Views reverser.								*/
	/*--------------------------------------------------------------*/
	/**
	 * This uses the Oracle datadict plus the DBMS_METADATA package
	 * to obtain the view definitions.
	 */
	@Override
	public void reverseViews(@Nonnull Connection dbc, @Nonnull DbSchema schema) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc.prepareStatement("select view_name,text from all_views where owner=?");
			ps.setString(1, schema.getName().toUpperCase());
			rs = ps.executeQuery();
			while(rs.next()) {
				String name = rs.getString(1);
				String sql = rs.getString(2);
				DbView v = new DbView(name, sql);
				schema.addView(v);
			}
			System.out.println(this + ": loaded " + schema.getViewMap().size() + " views");
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	@Override
	public void reverseProcedures(@Nonnull Connection dbc, @Nonnull DbSchema schema) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		try {
			ps2 = dbc.prepareStatement("select dbms_metadata.get_ddl(?,?) from dual");
			//			ps	= dbc().prepareStatement("select object_name,object_type from all_procedures where owner=? and object_type in ('PROCEDURE', 'FUNCTION')");
			ps = dbc.prepareStatement("select object_name,object_type from all_objects where owner=? and object_type in ('PROCEDURE', 'FUNCTION')");
			ps.setString(1, schema.getName().toUpperCase());
			rs = ps.executeQuery();
			while(rs.next()) {
				String name = rs.getString(1);
				String type = rs.getString(2);

				ps2.setString(1, type);
				ps2.setString(2, name);
				rs2 = ps2.executeQuery();
				if(!rs2.next())
					msg("No DDL for " + type + " " + name + "; skipped.");
				else {
					String ddl = rs2.getString(1);
					Procedure p = new Procedure(name, ddl);
					schema.addProcedure(p);
				}
				rs2.close();
			}
			System.out.println(this + ": loaded " + schema.getProcedureMap().size() + " procedures");
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(rs2 != null)
					rs2.close();
			} catch(Exception x) {}
			try {
				if(ps2 != null)
					ps2.close();
			} catch(Exception x) {}
		}
	}

	@Override
	public void reversePackages(@Nonnull Connection dbc, @Nonnull DbSchema schema) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		try {
			ps2 = dbc.prepareStatement("select dbms_metadata.get_ddl(?,?) from dual");
			ps = dbc.prepareStatement("select object_name,object_type from all_objects where owner=? and OBJECT_TYPE='PACKAGE'");
			ps.setString(1, schema.getName().toUpperCase());
			rs = ps.executeQuery();
			while(rs.next()) {
				String name = rs.getString(1);
				String type = rs.getString(2);

				ps2.setString(1, type);
				ps2.setString(2, name);
				rs2 = ps2.executeQuery();
				if(!rs2.next())
					msg("No DDL for package " + name + "; skipped.");
				else {
					String def = rs2.getString(1);
					rs2.close();

					//-- Get package body
					ps2.setString(1, type);
					ps2.setString(2, name);
					rs2 = ps2.executeQuery();
					if(!rs2.next())
						msg("No DDL for package BODY " + name + "; skipped.");
					else {
						String body = rs2.getString(1);
						Package p = new Package(name, def, body);
						schema.addPackage(p);
					}
				}
				rs2.close();
			}
			System.out.println(this + ": loaded " + schema.getPackageMap().size() + " packages");
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(rs2 != null)
					rs2.close();
			} catch(Exception x) {}
			try {
				if(ps2 != null)
					ps2.close();
			} catch(Exception x) {}
		}
	}

	@Override
	public void reverseTriggers(@Nonnull Connection dbc, @Nonnull DbSchema schema) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		try {
			ps2 = dbc.prepareStatement("select dbms_metadata.get_ddl(?,?) from dual");
			ps = dbc.prepareStatement("select object_name from all_objects where owner=? and object_type='TRIGGER' and object_name not like 'BIN$%'");
			ps.setString(1, schema.getName().toUpperCase());
			rs = ps.executeQuery();
			while(rs.next()) {
				String name = rs.getString(1);

				ps2.setString(1, "TRIGGER");
				ps2.setString(2, name);
				rs2 = ps2.executeQuery();
				if(!rs2.next())
					msg("No DDL for TRIGGER " + name + "; skipped.");
				else {
					String ddl = rs2.getString(1);
					Trigger t = new Trigger(name, ddl);
					schema.addTrigger(t);
				}
				rs2.close();
			}
			System.out.println(this + ": loaded " + schema.getTriggerMap().size() + " triggers");
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(rs2 != null)
					rs2.close();
			} catch(Exception x) {}
			try {
				if(ps2 != null)
					ps2.close();
			} catch(Exception x) {}
		}
	}

	@Override
	public void reverseConstraints(@Nonnull Connection dbc, @Nonnull Set<DbSchema> schemaSet) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc.prepareStatement(
				"select owner,constraint_name,constraint_type,table_name,search_condition,generated,index_name" + " from all_constraints where constraint_type in ('U', 'C')");
			rs = ps.executeQuery();
			while(rs.next()) {
				int i = 1;

				String owner= rs.getString(i++);
				DbSchema schema = findSchema(schemaSet, owner);
				if(null == schema)
					continue;

				String cn = rs.getString(i++);
				String type = rs.getString(i++);
				String tn = rs.getString(i++);
				String srch = rs.getString(i++);
				String s = rs.getString(i++);
				String ixnm = rs.getString(i++);
				boolean generated = s != null && s.equalsIgnoreCase("GENERATED NAME");
				if(cn.startsWith("BIN$") || tn.startsWith("BIN$"))
					continue;

				DbTable ct = schema.findTable(tn);
				if(ct == null) {
					warning("Cannot find table " + tn + " mentioned in constraint " + cn);
					continue;
				}

				if(type.equalsIgnoreCase("C")) {
					//-- Handle duplicate not-null stuff. These all have a generated name and end in "is not null"
					if(generated && srch.toLowerCase().endsWith(" is not null"))
						continue;

					//-- Add a check constraint to the table.
					DbCheckConstraint cc = new DbCheckConstraint(cn, srch);
					ct.addConstraint(cc);
				} else if(type.equalsIgnoreCase("U")) {
					//-- Unique constraint, enforced by an index usually.
					if(ixnm == null) {
						warning("Index for unique constraint is not specified.");
						continue;
					}
					DbIndex bix = ct.findIndex(ixnm);
					if(bix == null) {
						warning("Unknown backing index " + ixnm + " for unique constraint " + cn);
						continue;
					}

					//-- Add unique constraint.
					DbUniqueConstraint uc = new DbUniqueConstraint(cn, bix);
					ct.addConstraint(uc);
				} else
					throw new IllegalStateException("Unknown constraint type " + type);
			}
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * And of course getIndexInfo() in Oracle does not work either. Fine
	 * piece of work.
	 */
	@Override
	public void reverseIndexes(@Nonnull Connection dbc, @Nonnull Set<DbSchema> schemaSet) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps3 = null;
		ResultSet rs3 = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		Map<DbTable, Map<String, DbIndex>> indexMapMap = new HashMap<DbTable, Map<String, DbIndex>>();

		try {
			ps2 = dbc.prepareStatement("select dbms_metadata.get_ddl('INDEX', ?) from dual");
			ps3 = dbc.prepareStatement("select column_name, descend from all_ind_columns where index_owner=? and index_name=? order by column_position");

			ps = dbc.prepareStatement("select owner,index_name,index_type,table_name,uniqueness,tablespace_name from all_indexes");
			rs = ps.executeQuery();
			while(rs.next()) {
				int i = 1;

				String owner = rs.getString(i++);
				DbSchema schema = findSchema(schemaSet, owner);
				if(null == schema)
					continue;

				String name = rs.getString(i++);
				String type = rs.getString(i++);
				String tn = rs.getString(i++);
				String s = rs.getString(i++);
				boolean unique = s != null && s.equalsIgnoreCase("UNIQUE");
				String tsn = rs.getString(i++);
				DbTable it = schema.findTable(tn);

				if("NORMAL".equalsIgnoreCase(type)) {
					//-- Normal column-based index,
					if(it == null) {
						warning("Index " + name + " on unknown table " + tn + " skipped");
						continue;
					}
					DbIndex ix = new DbIndex(it, name, unique);

					Map<String, DbIndex> imap = indexMapMap.get(it);
					if(null == imap) {
						imap = new HashMap<String, DbIndex>();
						indexMapMap.put(it, imap);
						it.setIndexMap(imap);
					}
					imap.put(name, ix);
					schema.addIndex(ix);
					ix.setTablespace(tsn);

					ps3.setString(1, schema.getName().toUpperCase());
					ps3.setString(2, name);
					rs3 = ps3.executeQuery();
					while(rs3.next()) {
						String cn = rs3.getString(1);
						s = rs3.getString(2);
						boolean desc = s != null && s.equalsIgnoreCase("DESC");
						DbColumn c = it.findColumn(cn);
						if(c == null)
							throw new IllegalStateException("Unknown column " + tn + "." + cn + " in index " + name);
						ix.addColumn(c, desc);
					}
					rs3.close();
				} else { // if("FUNC".equalsIgnoreCase(type)) {
					//-- All others: get DDL
					ps2.setString(1, name);
					rs2 = ps2.executeQuery();
					if(!rs2.next()) {
						warning("Cannot obtain index DDL for index=" + name);
						continue;
					}
					String ddl = rs2.getString(1);
					SpecialIndex sx = new SpecialIndex(name, ddl);
					schema.addSpecialIndex(sx);
					rs2.close();

				} //else throw new IllegalStateException("Unexpected index type "+type);
			}
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(rs2 != null)
					rs2.close();
			} catch(Exception x) {}
			try {
				if(ps2 != null)
					ps2.close();
			} catch(Exception x) {}
			try {
				if(rs3 != null)
					rs3.close();
			} catch(Exception x) {}
			try {
				if(ps3 != null)
					ps3.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Must be overridden to properly handle lazy index initialization.
	 *
	 * @see to.etc.dbutil.reverse.JDBCReverser#reverseIndexes(java.sql.Connection, to.etc.dbutil.schema.DbTable)
	 */
	@Override
	public void reverseIndexes(Connection dbc, DbTable t) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps3 = null;
		ResultSet rs3 = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		Map<String, DbIndex> indexMap = new HashMap<String, DbIndex>();

		try {
			ps2 = dbc.prepareStatement("select dbms_metadata.get_ddl('INDEX', ?) from dual");
			ps3 = dbc.prepareStatement("select column_name, descend from all_ind_columns where index_owner=? and index_name=? order by column_position");
			ps = dbc.prepareStatement("select index_name,index_type,table_name,uniqueness,tablespace_name from all_indexes where owner=? and table_name=?");
			ps.setString(1, t.getSchema().getName().toUpperCase());
			ps.setString(2, t.getName().toUpperCase());
			rs = ps.executeQuery();
			while(rs.next()) {
				String name = rs.getString(1);
				String type = rs.getString(2);
				String tn = rs.getString(3);
				String s = rs.getString(4);
				boolean unique = s != null && s.equalsIgnoreCase("UNIQUE");
				String tsn = rs.getString(5);

				if("NORMAL".equalsIgnoreCase(type)) {
					//-- Normal column-based index,
					DbIndex ix = new DbIndex(t, name, unique);
					indexMap.put(name, ix);
					ix.setTablespace(tsn);

					ps3.setString(1, t.getSchema().getName().toUpperCase());
					ps3.setString(2, name);
					rs3 = ps3.executeQuery();
					while(rs3.next()) {
						String cn = rs3.getString(1);
						s = rs3.getString(2);
						boolean desc = s != null && s.equalsIgnoreCase("DESC");
						DbColumn c = t.findColumn(cn);
						if(c == null)
							throw new IllegalStateException("Unknown column " + tn + "." + cn + " in index " + name);
						ix.addColumn(c, desc);
					}
					rs3.close();
				} else { // if("FUNC".equalsIgnoreCase(type)) {
					//-- All others: get DDL
					ps2.setString(1, name);
					rs2 = ps2.executeQuery();
					if(!rs2.next()) {
						warning("Cannot obtain index DDL for index=" + name);
						continue;
					}
					String ddl = rs2.getString(1);
					SpecialIndex sx = new SpecialIndex(name, ddl);
//					schema.addSpecialIndex(sx);
					rs2.close();

				} //else throw new IllegalStateException("Unexpected index type "+type);
			}
			t.setIndexMap(indexMap);
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(rs2 != null)
					rs2.close();
			} catch(Exception x) {}
			try {
				if(ps2 != null)
					ps2.close();
			} catch(Exception x) {}
			try {
				if(rs3 != null)
					rs3.close();
			} catch(Exception x) {}
			try {
				if(ps3 != null)
					ps3.close();
			} catch(Exception x) {}
		}
	}


	static {
		registerType("varchar", Types.VARCHAR);
		registerType("varchar2", Types.VARCHAR);
		registerType("binary_double", Types.DOUBLE);
		registerType("BLOB", Types.BLOB);
		registerType("CHAR", Types.CHAR);
		registerType("CLOB", Types.CLOB);
		registerType("DATE", Types.TIMESTAMP);
		registerType("FLOAT", Types.FLOAT);
		registerType("LONG", Types.LONGVARBINARY);
		registerType("LONG RAW", Types.LONGVARBINARY);
		registerType("NCHAR", Types.CHAR);
		registerType("NCLOB", Types.CLOB);
		registerType("NUMBER", Types.NUMERIC);
		registerType("NVARCHAR2", Types.VARCHAR);
		registerType("RAW", Types.VARBINARY);
		registerType("ROWID", Types.VARCHAR);
	}

	@Override
	public boolean isOracleLimitSyntaxDisaster() {
		return true;
	}
}

package to.etc.dbcompare.reverse;

import java.sql.*;
import java.util.*;

import to.etc.dbcompare.db.*;
import to.etc.dbcompare.db.Package;

/**
 * The oracle driver is a mess. This tries to fix that mess.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 14, 2007
 */
public class OracleReverser extends JDBCReverser {
	public OracleReverser(Connection dbc, String schemaname) {
		super(dbc, schemaname.toUpperCase());
	}

	@Override
	public String getIdent() {
		return "Generic ORACLE Database reverse-engineering plug-in";
	}

	@Override
	public Schema loadSchema() throws Exception {
		Schema s = super.loadSchema();

		//-- Append extra ORACLE data from meta (column and table comments)
		//        updateColumnComments(s);
		updateTableComments(s);
		return s;
	}

	/**
	 * This version uses the datadict to obtain column information for all
	 * tables in the interrogated schema.
	 *
	 * @see to.etc.dbcompare.reverse.JDBCReverser#reverseColumns()
	 */
	@Override
	public void reverseColumns() throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc().prepareStatement(
				"select c.table_name,c.column_name,c.data_type,c.data_precision,c.data_scale,c.nullable,c.column_id,c.char_length,c.char_used, r.comments"
					+ " from all_tab_columns c left outer join all_col_comments r" + " on c.owner=r.owner and c.table_name=r.table_name and c.column_name=r.column_name"
					+ " where c.owner=? order by c.table_name, c.column_id");
			ps.setString(1, getSchemaName());
			rs = ps.executeQuery();
			String last = "";
			Table t = null;
			while(rs.next()) {
				String tn = rs.getString(1);
				String cn = rs.getString(2);
				String typename = rs.getString(3);
				int precision = rs.getInt(4);
				if(rs.wasNull())
					precision = -1;
				int scale = rs.getInt(5);
				if(rs.wasNull())
					scale = -1;
				String s = rs.getString(6);
				boolean nullable = s == null || s.equalsIgnoreCase("Y");
				int charlen = rs.getInt(8);
				if(rs.wasNull())
					charlen = -1;
				s = rs.getString(9);
				String remark = rs.getString(10);

				if(!last.equals(tn)) {
					//-- New table. Lookkitup
					t = getSchema().findTable(tn);
					last = tn;
				}
				if(t == null)
					continue; // Skip unknown tables (usually BIN$ crapshit from the recycle kludge)

				Column c = t.findColumn(cn);
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

				c = t.createColumn(cn, ct, precision, scale, nullable);
				c.setPlatformTypeName(typename);
				c.setSqlType(daty);
				c.setComment(remark);
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

	static private Map<String, Integer>	m_typeMap	= new HashMap<String, Integer>();

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
	protected void updateTableComments(Schema s) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			int cc = 0;
			ps = dbc().prepareStatement("select table_name,comments from user_tab_comments where TABLE_TYPE='TABLE'");
			rs = ps.executeQuery();
			while(rs.next()) {
				String tn = rs.getString(1);
				String cmt = rs.getString(2);
				if(cmt == null)
					continue;
				Table t = s.findTable(tn);
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

		public String	name;

		public String	remoteOwner;

		public String	remoteConstraint;

		public Cons(String name, String remoteOwner, String remoteConstraint) {
			this.name = name;
			this.remoteOwner = remoteOwner;
			this.remoteConstraint = remoteConstraint;
		}
	}

	/**
	 * Override because the stupid Oracle driver does not report a constraint name.
	 *
	 * @see to.etc.dbcompare.reverse.JDBCReverser#reverseRelations(to.etc.dbcompare.db.Table)
	 */
	@Override
	protected void reverseRelations(Table t) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		List<Cons> list = new ArrayList<Cons>();
		try {
			//-- Select list of referential constraints,
			ps = dbc().prepareStatement("select constraint_name, r_owner, r_constraint_name, delete_rule from all_constraints where table_name=? and constraint_type='R'");
			ps.setString(1, t.getName());
			rs = ps.executeQuery();
			while(rs.next()) {
				String name = rs.getString(1);
				if(name.startsWith("BIN$"))
					continue;
				Cons c = new Cons(name, rs.getString(2), rs.getString(3));
				String action = rs.getString(4);
				list.add(c);
			}
			rs.close();
			ps.close();
			if(list.size() == 0)
				return;

			/*
			 * Handle every relation found by locating the matching columns from each table
			 */
			ps = dbc().prepareStatement("select column_name from all_cons_columns where owner=? and constraint_name=? order by position");
			ps2 = dbc().prepareStatement("select column_name, table_name from all_cons_columns where owner=? and constraint_name=? order by position");
			for(Cons sp : list) {
				Relation rel = null;
				//-- Prepare FK part,
				ps.setString(1, getSchemaName());
				ps.setString(2, sp.name);
				rs = ps.executeQuery();

				//-- Prepare PK part,
				ps2.setString(1, sp.remoteOwner);
				ps2.setString(2, sp.remoteConstraint); // Constraint name on the other side
				rs2 = ps2.executeQuery();
				Table pt = null;

				while(rs.next()) {
					String cn = rs.getString(1);
					Column fkc = t.findColumn(cn);
					if(fkc == null)
						throw new IllegalStateException("Unknown column " + cn + " in table " + t + " for foreign key constraint " + sp.name);

					//-- We must have a matching column on the other (PK) side,
					if(!rs2.next())
						throw new IllegalStateException("Unmatched column " + cn + " in PK table for foreign key constraint " + sp.name);
					String pkcn = rs2.getString(1);
					if(pt == null) {
						String pktn = rs2.getString(2);
						pt = getSchema().findTable(pktn);
						if(pt == null)
							throw new IllegalStateException("Can't find table for PK " + pktn + " in PK table for foreign key constraint " + sp.name);

						//-- Create the relation too.
						rel = new Relation(pt, t);
						rel.setName(sp.name);
						pt.getParentRelationList().add(rel);
						t.getChildRelationList().add(rel);
					}
					Column pkc = pt.findColumn(pkcn);
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
	public void reverseViews() throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc().prepareStatement("select view_name,text from all_views where owner=?");
			ps.setString(1, getSchemaName());
			rs = ps.executeQuery();
			while(rs.next()) {
				String name = rs.getString(1);
				String sql = rs.getString(2);
				DbView v = new DbView(name, sql);
				getSchema().addView(v);
			}
			System.out.println(this + ": loaded " + getSchema().getViewMap().size() + " views");
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
	public void reverseProcedures() throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		try {
			ps2 = dbc().prepareStatement("select dbms_metadata.get_ddl(?,?) from dual");
			//			ps	= dbc().prepareStatement("select object_name,object_type from all_procedures where owner=? and object_type in ('PROCEDURE', 'FUNCTION')");
			ps = dbc().prepareStatement("select object_name,object_type from all_objects where owner=? and object_type in ('PROCEDURE', 'FUNCTION')");
			ps.setString(1, getSchemaName());
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
					getSchema().addProcedure(p);
				}
				rs2.close();
			}
			System.out.println(this + ": loaded " + getSchema().getProcedureMap().size() + " procedures");
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
	public void reversePackages() throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		try {
			ps2 = dbc().prepareStatement("select dbms_metadata.get_ddl(?,?) from dual");
			ps = dbc().prepareStatement("select object_name,object_type from all_objects where owner=? and OBJECT_TYPE='PACKAGE'");
			ps.setString(1, getSchemaName());
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
						getSchema().addPackage(p);
					}
				}
				rs2.close();
			}
			System.out.println(this + ": loaded " + getSchema().getPackageMap().size() + " packages");
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
	public void reverseTriggers() throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		try {
			ps2 = dbc().prepareStatement("select dbms_metadata.get_ddl(?,?) from dual");
			ps = dbc().prepareStatement("select object_name from all_objects where owner=? and object_type='TRIGGER' and object_name not like 'BIN$%'");
			ps.setString(1, getSchemaName());
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
					getSchema().addTrigger(t);
				}
				rs2.close();
			}
			System.out.println(this + ": loaded " + getSchema().getTriggerMap().size() + " triggers");
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
	public void reverseConstraints() throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc().prepareStatement(
				"select constraint_name,constraint_type,table_name,search_condition,generated,index_name" + " from all_constraints where owner=? and constraint_type in ('U', 'C')");
			ps.setString(1, getSchemaName());
			rs = ps.executeQuery();
			while(rs.next()) {
				String cn = rs.getString(1);
				String type = rs.getString(2);
				String tn = rs.getString(3);
				String srch = rs.getString(4);
				String s = rs.getString(5);
				String ixnm = rs.getString(6);
				boolean generated = s != null && s.equalsIgnoreCase("GENERATED NAME");
				if(cn.startsWith("BIN$") || tn.startsWith("BIN$"))
					continue;

				Table ct = getSchema().findTable(tn);
				if(ct == null) {
					warning("Cannot find table " + tn + " mentioned in constraint " + cn);
					continue;
				}

				if(type.equalsIgnoreCase("C")) {
					//-- Handle duplicate not-null stuff. These all have a generated name and end in "is not null"
					if(generated && srch.toLowerCase().endsWith(" is not null"))
						continue;

					//-- Add a check constraint to the table.
					CheckConstraint cc = new CheckConstraint(cn, srch);
					ct.addConstraint(cc);
				} else if(type.equalsIgnoreCase("U")) {
					//-- Unique constraint, enforced by an index usually.
					if(ixnm == null) {
						warning("Index for unique constraint is not specified.");
						continue;
					}
					Index bix = ct.findIndex(ixnm);
					if(bix == null) {
						warning("Unknown backing index " + ixnm + " for unique constraint " + cn);
						continue;
					}

					//-- Add unique constraint.
					UniqueConstraint uc = new UniqueConstraint(cn, bix);
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
	 *
	 * @see to.etc.dbcompare.reverse.JDBCReverser#reverseIndexes()
	 */
	@Override
	public void reverseIndexes() throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps3 = null;
		ResultSet rs3 = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		try {
			ps2 = dbc().prepareStatement("select dbms_metadata.get_ddl('INDEX', ?) from dual");
			ps3 = dbc().prepareStatement("select column_name, descend from all_ind_columns where index_owner=? and index_name=? order by column_position");
			ps = dbc().prepareStatement("select index_name,index_type,table_name,uniqueness,tablespace_name from all_indexes where owner=?");
			ps.setString(1, getSchemaName());
			rs = ps.executeQuery();
			while(rs.next()) {
				String name = rs.getString(1);
				String type = rs.getString(2);
				String tn = rs.getString(3);
				String s = rs.getString(4);
				boolean unique = s != null && s.equalsIgnoreCase("UNIQUE");
				String tsn = rs.getString(5);
				Table it = getSchema().findTable(tn);

				if("NORMAL".equalsIgnoreCase(type)) {
					//-- Normal column-based index,
					if(it == null) {
						warning("Index " + name + " on unknown table " + tn + " skipped");
						continue;
					}
					Index ix = new Index(it, name, unique);
					it.addIndex(ix);
					getSchema().addIndex(ix);
					ix.setTablespace(tsn);

					ps3.setString(1, getSchemaName());
					ps3.setString(2, name);
					rs3 = ps3.executeQuery();
					while(rs3.next()) {
						String cn = rs3.getString(1);
						s = rs3.getString(2);
						boolean desc = s != null && s.equalsIgnoreCase("DESC");
						Column c = it.findColumn(cn);
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
					getSchema().addSpecialIndex(sx);
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

}

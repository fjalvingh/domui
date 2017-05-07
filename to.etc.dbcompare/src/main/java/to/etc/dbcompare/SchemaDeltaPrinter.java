package to.etc.dbcompare;

import to.etc.dbutil.schema.*;

public class SchemaDeltaPrinter extends SchemaComparator {
	public SchemaDeltaPrinter(DbSchema src, DbSchema dest) {
		super(src, dest);
	}

	@Override
	public void columnAdded(DbTable dt, DbColumn sc) throws Exception {
		System.out.println("alter " + dt.getName() + " add " + sc.getName());
	}

	@Override
	public void columnChanged(DbTable dt, DbColumn sc, DbColumn dc, int flag) throws Exception {
		System.out.println("column changed: " + dt.getName() + "." + sc.getName() + ": flags=" + Integer.toHexString(flag));
	}

	@Override
	public void columnDeleted(DbTable dt, DbColumn dc) throws Exception {
		System.out.println("alter table " + dt.getName() + " drop column " + dc.getName() + " cascade constraints;");
	}

	@Override
	public void primaryKeyAdded(DbTable dt, DbPrimaryKey pk) throws Exception {}

	@Override
	public void primaryKeyDeleted(DbTable dt, DbPrimaryKey oldpk) throws Exception {}

	@Override
	public void primaryKeyFieldAdded(DbTable dt, int ix, DbColumn sc) throws Exception {}

	@Override
	public void primaryKeyFieldChanged(DbTable dt, int ix, DbColumn oldc, DbColumn newc) throws Exception {}

	@Override
	public void primaryKeyChanged(DbTable oldt, DbTable newt, DbPrimaryKey oldpk, DbPrimaryKey newpk) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void primaryKeyFieldDeleted(DbTable dt, int ix, DbColumn dc) throws Exception {}

	@Override
	public void tableAdded(DbTable st) throws Exception {
		System.out.println("create table " + st.getName());
	}

	@Override
	public void tableDeleted(DbTable dt) throws Exception {
		System.out.println("drop table " + dt.getName() + " cascade constraints;");
	}

	@Override
	public void relationAdded(DbTable st, DbTable dt, DbRelation newrel) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void relationColumnsChanged(DbTable st, DbTable dt, DbRelation sr, DbRelation dr) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void relationDeleted(DbTable st, DbTable dt, DbRelation rel) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void relationNameChanged(DbTable st, DbTable dt, DbRelation sr, DbRelation dr, String newname) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void relationTablesChanged(DbTable st, DbTable dt, DbRelation sr, DbRelation dr) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void viewAdded(DbView v) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void viewChanged(DbView oldview, DbView newview) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void viewDeleted(DbView v) throws Exception {
		// TODO Auto-generated method stub

	}


}

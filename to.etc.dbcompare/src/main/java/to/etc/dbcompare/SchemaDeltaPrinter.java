package to.etc.dbcompare;

import to.etc.dbcompare.db.*;

public class SchemaDeltaPrinter extends SchemaComparator {
	public SchemaDeltaPrinter(Schema src, Schema dest) {
		super(src, dest);
	}

	@Override
	public void columnAdded(Table dt, Column sc) throws Exception {
		System.out.println("alter " + dt.getName() + " add " + sc.getName());
	}

	@Override
	public void columnChanged(Table dt, Column sc, Column dc, int flag) throws Exception {
		System.out.println("column changed: " + dt.getName() + "." + sc.getName() + ": flags=" + Integer.toHexString(flag));
	}

	@Override
	public void columnDeleted(Table dt, Column dc) throws Exception {
		System.out.println("alter table " + dt.getName() + " drop column " + dc.getName() + " cascade constraints;");
	}

	@Override
	public void primaryKeyAdded(Table dt, PrimaryKey pk) throws Exception {
	}

	@Override
	public void primaryKeyDeleted(Table dt, PrimaryKey oldpk) throws Exception {
	}

	@Override
	public void primaryKeyFieldAdded(Table dt, int ix, Column sc) throws Exception {
	}

	@Override
	public void primaryKeyFieldChanged(Table dt, int ix, Column oldc, Column newc) throws Exception {
	}

	@Override
	public void primaryKeyChanged(Table oldt, Table newt, PrimaryKey oldpk, PrimaryKey newpk) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void primaryKeyFieldDeleted(Table dt, int ix, Column dc) throws Exception {
	}

	@Override
	public void tableAdded(Table st) throws Exception {
		System.out.println("create table " + st.getName());
	}

	@Override
	public void tableDeleted(Table dt) throws Exception {
		System.out.println("drop table " + dt.getName() + " cascade constraints;");
	}

	@Override
	public void relationAdded(Table st, Table dt, Relation newrel) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void relationColumnsChanged(Table st, Table dt, Relation sr, Relation dr) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void relationDeleted(Table st, Table dt, Relation rel) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void relationNameChanged(Table st, Table dt, Relation sr, Relation dr, String newname) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void relationTablesChanged(Table st, Table dt, Relation sr, Relation dr) throws Exception {
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

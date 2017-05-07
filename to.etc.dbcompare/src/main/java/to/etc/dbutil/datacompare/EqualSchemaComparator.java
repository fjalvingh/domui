package to.etc.dbutil.datacompare;

import to.etc.dbcompare.*;
import to.etc.dbutil.schema.*;

public class EqualSchemaComparator extends SchemaComparator {
	private StringBuilder m_sb = new StringBuilder();

	public EqualSchemaComparator(DbSchema src, DbSchema dest) {
		super(src, dest);
	}

	public String getChanges() {
		return m_sb.toString();
	}

	private EqualSchemaComparator append(String s) {
		m_sb.append(s);
		return this;
	}

	private EqualSchemaComparator nl() {
		m_sb.append("\n");
		return this;
	}

	private EqualSchemaComparator nl(String s) {
		m_sb.append(s);
		m_sb.append("\n");
		return this;
	}

	@Override
	public void columnAdded(DbTable dt, DbColumn sc) throws Exception {
		append("extra column ").append(sc.getName()).append(" in table ").append(dt.getName());
	}

	@Override
	public void columnChanged(DbTable dt, DbColumn sc, DbColumn dc, int flag) throws Exception {
		append("column changed: " + dt.getName() + "." + sc.getName() + ": flags=" + Integer.toHexString(flag));
	}

	@Override
	public void columnDeleted(DbTable dt, DbColumn dc) throws Exception {
		append("alter table " + dt.getName() + " drop column " + dc.getName() + " cascade constraints;");
	}

	@Override
	public void primaryKeyAdded(DbTable dt, DbPrimaryKey pk) throws Exception {
		append(dt.getName()).append(" primary key added").nl();
	}

	@Override
	public void primaryKeyDeleted(DbTable dt, DbPrimaryKey oldpk) throws Exception {
		append(dt.getName()).append(" primary key deleted").nl();
	}

	@Override
	public void primaryKeyFieldAdded(DbTable dt, int ix, DbColumn sc) throws Exception {
		append(dt.getName()).append(" primary key field added").nl();

	}

	@Override
	public void primaryKeyFieldChanged(DbTable dt, int ix, DbColumn oldc, DbColumn newc) throws Exception {
		append(dt.getName()).append(" primary key field changed").nl();
	}

	@Override
	public void primaryKeyChanged(DbTable oldt, DbTable newt, DbPrimaryKey oldpk, DbPrimaryKey newpk) throws Exception {
		append(newt.getName()).append(" primary key changed").nl();
	}

	@Override
	public void primaryKeyFieldDeleted(DbTable dt, int ix, DbColumn dc) throws Exception {
		append(dt.getName()).append(" primary key field deleted").nl();
	}

	@Override
	public void tableAdded(DbTable st) throws Exception {
		append(st.getName()).nl(": table added");
	}

	@Override
	public void tableDeleted(DbTable dt) throws Exception {
		append(dt.getName()).nl(": table deleted");
	}

	@Override
	public void relationAdded(DbTable st, DbTable dt, DbRelation newrel) throws Exception {
		//		append("Relation added: ").append(newrel.toString()).nl();
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

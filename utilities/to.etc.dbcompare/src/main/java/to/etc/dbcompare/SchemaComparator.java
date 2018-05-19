package to.etc.dbcompare;

import java.util.*;

import to.etc.dbutil.schema.*;
import to.etc.dbutil.schema.Package;

public class SchemaComparator extends AbstractSchemaComparator {
	public SchemaComparator(DbSchema src, DbSchema dest) {
		super(src, dest);
	}

	@Override
	public void columnAdded(DbTable dt, DbColumn sc) throws Exception {}

	@Override
	public void columnChanged(DbTable dt, DbColumn sc, DbColumn dc, int flag) throws Exception {}

	@Override
	public void columnDeleted(DbTable dt, DbColumn dc) throws Exception {}

	@Override
	public void primaryKeyAdded(DbTable dt, DbPrimaryKey pk) throws Exception {}

	@Override
	public void primaryKeyDeleted(DbTable dt, DbPrimaryKey oldpk) throws Exception {}

	@Override
	public void primaryKeyFieldAdded(DbTable dt, int ix, DbColumn sc) throws Exception {}

	@Override
	public void primaryKeyFieldChanged(DbTable dt, int ix, DbColumn oldc, DbColumn newc) throws Exception {}

	@Override
	public void primaryKeyFieldDeleted(DbTable dt, int ix, DbColumn dc) throws Exception {}

	@Override
	public void primaryKeyChanged(DbTable oldt, DbTable newt, DbPrimaryKey oldpk, DbPrimaryKey newpk) throws Exception {}

	@Override
	public void tableAdded(DbTable st) throws Exception {}

	@Override
	public void tableDeleted(DbTable dt) throws Exception {}

	@Override
	public void relationAdded(DbTable st, DbTable dt, DbRelation newrel) throws Exception {}

	@Override
	public void relationColumnsChanged(DbTable st, DbTable dt, DbRelation sr, DbRelation dr) throws Exception {}

	@Override
	public void relationDeleted(DbTable st, DbTable dt, DbRelation rel) throws Exception {}

	@Override
	public void relationNameChanged(DbTable st, DbTable dt, DbRelation sr, DbRelation dr, String newname) throws Exception {}

	@Override
	public void relationTablesChanged(DbTable st, DbTable dt, DbRelation sr, DbRelation dr) throws Exception {}

	@Override
	public void viewAdded(DbView v) throws Exception {}

	@Override
	public void viewChanged(DbView oldview, DbView newview) throws Exception {}

	@Override
	public void viewDeleted(DbView v) throws Exception {}

	@Override
	public void packageAdded(Package p) throws Exception {}

	@Override
	public void packageBodyChanged(Package oldp, Package newp) throws Exception {}

	@Override
	public void packageDefinitionChanged(Package oldp, Package newp) throws Exception {}

	@Override
	public void packageDeleted(Package p) throws Exception {}

	@Override
	public void triggerAdded(Trigger newt) throws Exception {}

	@Override
	public void triggerChanged(Trigger oldt, Trigger newt) throws Exception {}

	@Override
	public void triggerDeleted(Trigger oldt) throws Exception {}

	@Override
	public void indexAdded(DbTable dt, DbIndex newix) throws Exception {}

	@Override
	public void indexChanged(DbIndex six, DbIndex dix, boolean uniquechanged, List<ColumnChange> colchanges) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void indexDeleted(DbIndex oldix) throws Exception {}

	@Override
	public void indexTableChanged(DbIndex oldix, DbIndex newix) throws Exception {}
}

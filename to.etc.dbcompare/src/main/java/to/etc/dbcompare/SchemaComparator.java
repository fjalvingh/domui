package to.etc.dbcompare;

import java.util.*;

import to.etc.dbcompare.db.*;
import to.etc.dbcompare.db.Package;

public class SchemaComparator extends AbstractSchemaComparator {
	public SchemaComparator(Schema src, Schema dest) {
		super(src, dest);
	}

	@Override
	public void columnAdded(Table dt, Column sc) throws Exception {
	}

	@Override
	public void columnChanged(Table dt, Column sc, Column dc, int flag) throws Exception {
	}

	@Override
	public void columnDeleted(Table dt, Column dc) throws Exception {
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
	public void primaryKeyFieldDeleted(Table dt, int ix, Column dc) throws Exception {
	}

	@Override
	public void primaryKeyChanged(Table oldt, Table newt, PrimaryKey oldpk, PrimaryKey newpk) throws Exception {
	}

	@Override
	public void tableAdded(Table st) throws Exception {
	}

	@Override
	public void tableDeleted(Table dt) throws Exception {
	}

	@Override
	public void relationAdded(Table st, Table dt, Relation newrel) throws Exception {
	}

	@Override
	public void relationColumnsChanged(Table st, Table dt, Relation sr, Relation dr) throws Exception {
	}

	@Override
	public void relationDeleted(Table st, Table dt, Relation rel) throws Exception {
	}

	@Override
	public void relationNameChanged(Table st, Table dt, Relation sr, Relation dr, String newname) throws Exception {
	}

	@Override
	public void relationTablesChanged(Table st, Table dt, Relation sr, Relation dr) throws Exception {
	}

	@Override
	public void viewAdded(DbView v) throws Exception {
	}

	@Override
	public void viewChanged(DbView oldview, DbView newview) throws Exception {
	}

	@Override
	public void viewDeleted(DbView v) throws Exception {
	}

	@Override
	public void packageAdded(Package p) throws Exception {
	}

	@Override
	public void packageBodyChanged(Package oldp, Package newp) throws Exception {
	}

	@Override
	public void packageDefinitionChanged(Package oldp, Package newp) throws Exception {
	}

	@Override
	public void packageDeleted(Package p) throws Exception {
	}

	@Override
	public void triggerAdded(Trigger newt) throws Exception {
	}

	@Override
	public void triggerChanged(Trigger oldt, Trigger newt) throws Exception {
	}

	@Override
	public void triggerDeleted(Trigger oldt) throws Exception {
	}

	@Override
	public void indexAdded(Table dt, Index newix) throws Exception {
	}

	@Override
	public void indexChanged(Index six, Index dix, boolean uniquechanged, List<ColumnChange> colchanges) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void indexDeleted(Index oldix) throws Exception {
	}

	@Override
	public void indexTableChanged(Index oldix, Index newix) throws Exception {
	}
}

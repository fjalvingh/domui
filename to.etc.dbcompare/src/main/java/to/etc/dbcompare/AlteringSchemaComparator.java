package to.etc.dbcompare;

import java.io.*;
import java.util.*;

import to.etc.dbcompare.generator.*;
import to.etc.dbutil.schema.*;
import to.etc.dbutil.schema.Package;

public class AlteringSchemaComparator extends AbstractSchemaComparator {
	private AbstractGenerator m_g;

	private Set<String> m_deletedTableSet = new HashSet<String>();

	private List<String> m_tableChanges = new ArrayList<String>();

	private List<String> m_relationDrops = new ArrayList<String>();

	private List<String> m_relationAdds = new ArrayList<String>();

	private List<String> m_columnAdds = new ArrayList<String>();

	private List<String> m_columnDels = new ArrayList<String>();

	private List<String> m_columnMods = new ArrayList<String>();

	private List<String> m_pkMods = new ArrayList<String>();

	private List<String> m_viewMods = new ArrayList<String>();

	private List<String> m_packageMods = new ArrayList<String>();

	private List<String> m_triggerMods = new ArrayList<String>();

	private List<String> m_indexAdds = new ArrayList<String>();

	private List<String> m_indexDels = new ArrayList<String>();

	public AlteringSchemaComparator(DbSchema src, DbSchema dest, AbstractGenerator g) {
		super(src, dest);
		m_g = g;
	}

	private void d(String s) {
		System.out.println("delta: " + s);
	}

	public void renderTableName(Appendable a, String name) throws Exception {
		m_g.renderTableName(a, name);
	}

	public void renderFieldName(Appendable a, String name) throws Exception {
		m_g.renderFieldName(a, name);
	}

	@Override
	public void columnAdded(DbTable dt, DbColumn sc) throws Exception {
		StringBuilder sb = new StringBuilder();
		m_g.renderAddColumn(sb, dt, sc);
		m_columnAdds.add(sb.toString());

		//-- Comments?
		m_g.renderColumnComment(m_columnAdds, sc);
	}

	@Override
	public void columnChanged(DbTable dt, DbColumn newc, DbColumn oldc, int flag) throws Exception {
		m_g.columnChanged(m_columnMods, dt, newc, oldc, flag);
	}

	@Override
	public void columnDeleted(DbTable dt, DbColumn dc) throws Exception {
		if(m_deletedTableSet.contains(dt.getName()))
			return;
		m_g.renderColumnDrop(m_columnDels, dt, dc);
	}

	@Override
	public void tableAdded(DbTable st) throws Exception {
		m_g.addTable(m_tableChanges, st);
	}

	@Override
	public void tableDeleted(DbTable dt) throws Exception {
		m_g.renderDropTable(m_tableChanges, dt);
		m_deletedTableSet.add(dt.getName());
	}

	public void render(PrintWriter pw) {
		pw.println("--------------------------------------------------------");
		pw.println("-- Dropping indexes.");
		pw.println("--------------------------------------------------------");
		for(String s : m_indexDels) {
			pw.println(s);
		}
		pw.println("--------------------------------------------------------");
		pw.println("-- Table creation and deletion.");
		pw.println("--------------------------------------------------------");
		for(String s : m_tableChanges) {
			pw.println(s);
		}
		pw.println("--------------------------------------------------------");
		pw.println("-- Drop relations that are changed.");
		pw.println("--------------------------------------------------------");
		for(String s : m_relationDrops) {
			pw.println(s);
		}

		pw.println("--------------------------------------------------------");
		pw.println("-- Column additions.");
		pw.println("--------------------------------------------------------");
		for(String s : m_columnAdds) {
			pw.println(s);
		}
		pw.println("--------------------------------------------------------");
		pw.println("-- Column changes.");
		pw.println("--------------------------------------------------------");
		for(String s : m_columnMods) {
			pw.println(s);
		}

		pw.println("--------------------------------------------------------");
		pw.println("-- Dropped columns.");
		pw.println("--------------------------------------------------------");
		for(String s : m_columnDels) {
			pw.println(s);
		}
		pw.println("--------------------------------------------------------");
		pw.println("-- Primary key changes.");
		pw.println("--------------------------------------------------------");
		for(String s : m_pkMods) {
			pw.println(s);
		}
		pw.println("--------------------------------------------------------");
		pw.println("-- (re)create relations.");
		pw.println("--------------------------------------------------------");
		for(String s : m_relationAdds) {
			pw.println(s);
		}
		pw.println("--------------------------------------------------------");
		pw.println("-- Changed views.");
		pw.println("--------------------------------------------------------");
		for(String s : m_viewMods) {
			pw.println(s);
		}
		pw.println("--------------------------------------------------------");
		pw.println("-- Changed packages.");
		pw.println("--------------------------------------------------------");
		for(String s : m_packageMods) {
			pw.println(s);
		}
		pw.println("--------------------------------------------------------");
		pw.println("-- Changed triggers.");
		pw.println("--------------------------------------------------------");
		for(String s : m_triggerMods) {
			pw.println(s);
		}
		pw.println("--------------------------------------------------------");
		pw.println("-- Adding indexes.");
		pw.println("--------------------------------------------------------");
		for(String s : m_indexAdds) {
			pw.println(s);
		}
	}

	/*--------------------------------------------------------------*/
	/*  CODING: Primary keys.                                       */
	/*--------------------------------------------------------------*/
	@Override
	public void primaryKeyFieldAdded(DbTable dt, int ix, DbColumn sc) throws Exception {
		//-- does not need an implementation;
	}

	@Override
	public void primaryKeyFieldChanged(DbTable dt, int ix, DbColumn oldc, DbColumn newc) throws Exception {
		//-- does not need an implementation;
	}

	@Override
	public void primaryKeyFieldDeleted(DbTable dt, int ix, DbColumn dc) throws Exception {
		//-- does not need an implementation;
	}

	@Override
	public void primaryKeyChanged(DbTable oldt, DbTable newt, DbPrimaryKey oldpk, DbPrimaryKey newpk) throws Exception {
		//-- Create a primary key constraint.
		m_g.renderDropPK(m_pkMods, oldpk);
		m_g.renderCreatePK(m_pkMods, newpk);
	}

	@Override
	public void primaryKeyAdded(DbTable dt, DbPrimaryKey pk) throws Exception {
		m_g.renderCreatePK(m_pkMods, pk);
	}


	@Override
	public void primaryKeyDeleted(DbTable dt, DbPrimaryKey oldpk) throws Exception {
		m_g.renderDropPK(m_pkMods, oldpk);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Relations.                                       	*/
	/*--------------------------------------------------------------*/
	@Override
	public void relationAdded(DbTable st, DbTable dt, DbRelation newrel) throws Exception {
		m_g.renderAddRelation(m_relationAdds, dt, newrel);
	}

	@Override
	public void relationColumnsChanged(DbTable st, DbTable dt, DbRelation sr, DbRelation dr) throws Exception {
		m_g.renderDropRelation(m_relationDrops, dt, dr);
		m_g.renderAddRelation(m_relationAdds, dt, dr);
	}

	@Override
	public void relationDeleted(DbTable st, DbTable dt, DbRelation rel) throws Exception {
		m_g.renderDropRelation(m_relationDrops, dt, rel);
	}

	@Override
	public void relationNameChanged(DbTable st, DbTable dt, DbRelation sr, DbRelation dr, String newname) throws Exception {
		m_g.renderDropRelation(m_relationDrops, dt, dr);
		m_g.renderAddRelation(m_relationAdds, dt, dr);
	}

	@Override
	public void relationTablesChanged(DbTable st, DbTable dt, DbRelation sr, DbRelation dr) throws Exception {
		m_g.renderDropRelation(m_relationDrops, dt, dr);
		m_g.renderAddRelation(m_relationAdds, dt, dr);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Views												*/
	/*--------------------------------------------------------------*/


	@Override
	public void viewAdded(DbView v) throws Exception {
		m_g.renderCreateView(m_viewMods, v);
	}

	@Override
	public void viewChanged(DbView oldview, DbView newview) throws Exception {
		m_g.renderCreateView(m_viewMods, newview);
	}

	@Override
	public void viewDeleted(DbView v) throws Exception {
		m_g.renderDropView(m_viewMods, v);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Packages											*/
	/*--------------------------------------------------------------*/

	@Override
	public void packageAdded(Package p) throws Exception {
		m_g.renderCreatePackageDefinition(m_packageMods, p);
		m_g.renderCreatePackageBody(m_packageMods, p);
	}

	@Override
	public void packageBodyChanged(Package oldp, Package newp) throws Exception {
		m_g.renderCreatePackageBody(m_packageMods, newp);
	}

	@Override
	public void packageDefinitionChanged(Package oldp, Package newp) throws Exception {
		m_g.renderCreatePackageDefinition(m_packageMods, newp);
	}

	@Override
	public void packageDeleted(Package p) throws Exception {
		m_g.renderDropPackage(m_packageMods, p);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Triggers.											*/
	/*--------------------------------------------------------------*/

	@Override
	public void triggerAdded(Trigger newt) throws Exception {
		m_g.renderAddTrigger(m_triggerMods, newt);
	}

	@Override
	public void triggerChanged(Trigger oldt, Trigger newt) throws Exception {
		m_g.renderDropTrigger(m_triggerMods, oldt);
		m_g.renderAddTrigger(m_triggerMods, newt);
	}

	@Override
	public void triggerDeleted(Trigger oldt) throws Exception {
		m_g.renderDropTrigger(m_triggerMods, oldt);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Indexes.											*/
	/*--------------------------------------------------------------*/
	@Override
	public void indexAdded(DbTable dt, DbIndex newix) throws Exception {
		m_g.renderCreateIndex(m_indexAdds, newix);
	}

	@Override
	public void indexChanged(DbIndex six, DbIndex dix, boolean uniquechanged, List<ColumnChange> colchanges) throws Exception {
		m_g.renderDropIndex(m_indexDels, dix);
		m_g.renderCreateIndex(m_indexAdds, dix);
	}

	@Override
	public void indexDeleted(DbIndex oldix) throws Exception {
		m_g.renderDropIndex(m_indexDels, oldix);
	}

	@Override
	public void indexTableChanged(DbIndex oldix, DbIndex newix) throws Exception {
		m_g.renderDropIndex(m_indexDels, oldix);
		m_g.renderCreateIndex(m_indexAdds, newix);
	}
}

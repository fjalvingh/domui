package to.etc.dbcompare;

import java.io.*;
import java.util.*;

import to.etc.dbcompare.db.*;
import to.etc.dbcompare.db.Package;
import to.etc.dbcompare.generator.*;

public class AlteringSchemaComparator extends AbstractSchemaComparator {
	private AbstractGenerator	m_g;

	private Set<String>			m_deletedTableSet	= new HashSet<String>();

	private List<String>		m_tableChanges		= new ArrayList<String>();

	private List<String>		m_relationDrops		= new ArrayList<String>();

	private List<String>		m_relationAdds		= new ArrayList<String>();

	private List<String>		m_columnAdds		= new ArrayList<String>();

	private List<String>		m_columnDels		= new ArrayList<String>();

	private List<String>		m_columnMods		= new ArrayList<String>();

	private List<String>		m_pkMods			= new ArrayList<String>();

	private List<String>		m_viewMods			= new ArrayList<String>();

	private List<String>		m_packageMods		= new ArrayList<String>();

	private List<String>		m_triggerMods		= new ArrayList<String>();

	private List<String>		m_indexAdds			= new ArrayList<String>();

	private List<String>		m_indexDels			= new ArrayList<String>();

	public AlteringSchemaComparator(Schema src, Schema dest, AbstractGenerator g) {
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
	public void columnAdded(Table dt, Column sc) throws Exception {
		StringBuilder sb = new StringBuilder();
		m_g.renderAddColumn(sb, dt, sc);
		m_columnAdds.add(sb.toString());

		//-- Comments?
		m_g.renderColumnComment(m_columnAdds, sc);
	}

	@Override
	public void columnChanged(Table dt, Column newc, Column oldc, int flag) throws Exception {
		m_g.columnChanged(m_columnMods, dt, newc, oldc, flag);
	}

	@Override
	public void columnDeleted(Table dt, Column dc) throws Exception {
		if(m_deletedTableSet.contains(dt.getName()))
			return;
		m_g.renderColumnDrop(m_columnDels, dt, dc);
	}

	@Override
	public void tableAdded(Table st) throws Exception {
		m_g.addTable(m_tableChanges, st);
	}

	@Override
	public void tableDeleted(Table dt) throws Exception {
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
	public void primaryKeyFieldAdded(Table dt, int ix, Column sc) throws Exception {
		//-- does not need an implementation;
	}

	@Override
	public void primaryKeyFieldChanged(Table dt, int ix, Column oldc, Column newc) throws Exception {
		//-- does not need an implementation;
	}

	@Override
	public void primaryKeyFieldDeleted(Table dt, int ix, Column dc) throws Exception {
		//-- does not need an implementation;
	}

	@Override
	public void primaryKeyChanged(Table oldt, Table newt, PrimaryKey oldpk, PrimaryKey newpk) throws Exception {
		//-- Create a primary key constraint.
		m_g.renderDropPK(m_pkMods, oldpk);
		m_g.renderCreatePK(m_pkMods, newpk);
	}

	@Override
	public void primaryKeyAdded(Table dt, PrimaryKey pk) throws Exception {
		m_g.renderCreatePK(m_pkMods, pk);
	}


	@Override
	public void primaryKeyDeleted(Table dt, PrimaryKey oldpk) throws Exception {
		m_g.renderDropPK(m_pkMods, oldpk);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Relations.                                       	*/
	/*--------------------------------------------------------------*/
	@Override
	public void relationAdded(Table st, Table dt, Relation newrel) throws Exception {
		m_g.renderAddRelation(m_relationAdds, dt, newrel);
	}

	@Override
	public void relationColumnsChanged(Table st, Table dt, Relation sr, Relation dr) throws Exception {
		m_g.renderDropRelation(m_relationDrops, dt, dr);
		m_g.renderAddRelation(m_relationAdds, dt, dr);
	}

	@Override
	public void relationDeleted(Table st, Table dt, Relation rel) throws Exception {
		m_g.renderDropRelation(m_relationDrops, dt, rel);
	}

	@Override
	public void relationNameChanged(Table st, Table dt, Relation sr, Relation dr, String newname) throws Exception {
		m_g.renderDropRelation(m_relationDrops, dt, dr);
		m_g.renderAddRelation(m_relationAdds, dt, dr);
	}

	@Override
	public void relationTablesChanged(Table st, Table dt, Relation sr, Relation dr) throws Exception {
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
	public void indexAdded(Table dt, Index newix) throws Exception {
		m_g.renderCreateIndex(m_indexAdds, newix);
	}

	@Override
	public void indexChanged(Index six, Index dix, boolean uniquechanged, List<ColumnChange> colchanges) throws Exception {
		m_g.renderDropIndex(m_indexDels, dix);
		m_g.renderCreateIndex(m_indexAdds, dix);
	}

	@Override
	public void indexDeleted(Index oldix) throws Exception {
		m_g.renderDropIndex(m_indexDels, oldix);
	}

	@Override
	public void indexTableChanged(Index oldix, Index newix) throws Exception {
		m_g.renderDropIndex(m_indexDels, oldix);
		m_g.renderCreateIndex(m_indexAdds, newix);
	}
}

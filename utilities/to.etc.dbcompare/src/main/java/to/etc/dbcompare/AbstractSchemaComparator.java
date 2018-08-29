package to.etc.dbcompare;

import java.util.*;

import to.etc.dbutil.schema.*;
import to.etc.dbutil.schema.Package;
import to.etc.util.*;

/**
 * Compare two schema's by walking both synchronously.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2006
 */
abstract public class AbstractSchemaComparator {
	private DbSchema m_src;

	private DbSchema m_dest;

	public AbstractSchemaComparator(DbSchema src, DbSchema dest) {
		m_src = src;
		m_dest = dest;
	}

	public void run() throws Exception {
		//-- 1. Compare all tables.
		Set<DbTable> desttables = new HashSet<DbTable>(m_dest.getTables()); // All dest tables that must be found.
		for(DbTable st : m_src.getTables()) {
			//-- Find matching target table,
			DbTable dt = m_dest.findTable(st.getName());
			if(dt != null)
				desttables.remove(dt); // Table used,
			doCompareTables(st, dt); // Handle table compare logic
		}

		//-- 2. Call doCompareTables for all DELETED tables
		for(DbTable dt : desttables)
			doCompareTables(null, dt);
		doCompareIndexes();

		//-- 3. Compare all views
		doCompareViews();
		doComparePackages();

	}

	private boolean doCompareTables(DbTable st, DbTable dt) throws Exception {
		boolean changed = false;

		//-- Compare base table characteristics
		if(st == null) {
			tableDeleted(dt);
			return true;
		} else if(dt == null) {
			tableAdded(st);
			return true;
		}

		//-- Walk the table's fields,
		Set<DbColumn> colset = new HashSet<DbColumn>(dt.getColumnMap().values());
		for(DbColumn sc : st.getColumnMap().values()) {
			DbColumn dc = dt.findColumn(sc.getName());
			if(dc != null)
				colset.remove(dc);
			if(doCompareColumns(st, dt, sc, dc))
				changed = true;
		}
		if(colset.size() != 0)
			changed = true;
		for(DbColumn dc : colset) {
			if(doCompareColumns(st, dt, null, dc))
				changed = true;
		}

		//-- Handle PK comparison
		if(doComparePK(st, dt))
			changed = true;
		if(doCompareRelations(st, dt))
			changed = true;

		//-- The PK's
		return changed;
	}

	private boolean doComparePK(DbTable st, DbTable dt) throws Exception {
		if(st.getPrimaryKey() == null && dt.getPrimaryKey() == null)
			return false;

		if(st.getPrimaryKey() == null) {
			primaryKeyDeleted(dt, dt.getPrimaryKey());
			return true;
		} else if(dt.getPrimaryKey() == null) {
			primaryKeyAdded(dt, st.getPrimaryKey());
			return true;
		}

		//-- Both have a PK; compare PK columns and order.
		boolean changed = false;
		int ix = 0;
		DbPrimaryKey spk = st.getPrimaryKey();
		DbPrimaryKey dpk = dt.getPrimaryKey();
		while(ix < spk.getColumnList().size()) {
			DbColumn sc = spk.getColumnList().get(ix);
			DbColumn dc = ix >= dpk.getColumnList().size() ? null : dpk.getColumnList().get(ix);

			//-- Compare column by name.
			if(dc == null) {
				//-- A new column was added at index=ix
				primaryKeyFieldAdded(dt, ix, sc);
				changed = true;
			} else {
				if(!dc.getName().equals(sc.getName())) {
					//-- PK field changed at indx=
					primaryKeyFieldChanged(dt, ix, dc, sc);
					changed = true;
				}
			}
			ix++;
		}
		while(ix < dpk.getColumnList().size()) {
			DbColumn dc = dpk.getColumnList().get(ix);
			changed = true;
			primaryKeyFieldDeleted(dt, ix, dc);
		}
		if(changed)
			primaryKeyChanged(dt, st, dpk, spk);
		return changed;
	}


	/** Set when column type has changed */
	static public final int csTYPE = 0x0001;

	static public final int csCOMMENT = 0x0002;

	static public final int csPLATFORMTYPE = 0x0004;

	static public final int csPRECISION = 0x0008;

	static public final int csSCALE = 0x0010;

	static public final int csSQLTYPE = 0x0020;

	static public final int csNULLABLE = 0x0040;


	private boolean doCompareColumns(DbTable st, DbTable dt, DbColumn sc, DbColumn dc) throws Exception {
		boolean changed = false;
		int flag = 0;
		if(sc != null && dc != null) {
			//-- Detailed compare of content.
			if(!StringTool.isEqual(sc.getComment(), dc.getComment()))
				flag |= csCOMMENT;
			if(!StringTool.isEqual(sc.getPlatformTypeName(), dc.getPlatformTypeName()))
				flag |= csPLATFORMTYPE;
			if(!StringTool.isEqual(sc.getType(), dc.getType()))
				flag |= csTYPE;
			if(sc.getPrecision() != dc.getPrecision())
				flag |= csPRECISION;
			if(sc.getScale() != dc.getScale())
				flag |= csSCALE;
			if(sc.getSqlType() != dc.getSqlType())
				flag |= csSQLTYPE;
			if(sc.isNullable() != dc.isNullable())
				flag |= csNULLABLE;
			if(flag != 0) {
				columnChanged(dt, sc, dc, flag);
				changed = true;
			}
		} else if(sc == null) {
			//-- Source is deleted
			columnDeleted(dt, dc);
			changed = true;
		} else if(dc == null) {
			//-- New column
			columnAdded(dt, sc);
			changed = true;
		}
		return changed;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handle changed relations.							*/
	/*--------------------------------------------------------------*/
	protected boolean doCompareRelations(DbTable st, DbTable dt) throws Exception {
		List<DbRelation> dallrel = new ArrayList<DbRelation>(dt.getParentRelationList());
		dallrel.addAll(dt.getChildRelationList());
		List<DbRelation> sallrel = new ArrayList<DbRelation>(st.getParentRelationList());
		sallrel.addAll(st.getChildRelationList());

		/*
		 * Pair all relations that belong together in source and dest. We do
		 * this in two runs. First we compare the relation's constraint names
		 * in both tables and pair all relations with the same name. If any
		 * unnamed relations are left we try to pair them by full relation
		 * equality (find relations that have equal matched columns). If this
		 * cannot be found the rest is a full delta.
		 */
		List<DbRelation> slist = new ArrayList<DbRelation>();
		List<DbRelation> dlist = new ArrayList<DbRelation>();

		//-- Pair by name, and remove all matched from the sets.
		for(Iterator<DbRelation> it = sallrel.iterator(); it.hasNext();) {
			DbRelation sr = it.next();
			if(sr.getName() == null)
				continue; // Source is unnamed-> skip.
			DbRelation dr = findRelationByName(dallrel, sr.getName());
			if(dr == null)
				continue;

			//-- We have a match!!
			it.remove(); // Drop from source list,
			dallrel.remove(dr); // Drop from target list,
			slist.add(sr);
			dlist.add(dr);
		}

		/*
		 * Part 2. What's left is relations unmatched by name. Try to find with full column equality.
		 */
		for(Iterator<DbRelation> it = sallrel.iterator(); it.hasNext();) {
			DbRelation sr = it.next();
			DbRelation dr = findRelationByColumns(dallrel, sr);
			if(dr != null) {
				//-- We have a matching src and dest relation. Add them and remove from todo
				slist.add(sr);
				dlist.add(dr);
				dallrel.remove(dr);
			} else {
				//-- Source does not exist in target -> add as (src, null)
				slist.add(sr);
				dlist.add(null);
			}
			it.remove();
		}

		//-- Whatever's left in "dest" is not present in src...
		for(DbRelation r : dallrel) {
			slist.add(null);
			dlist.add(r);
		}

		//-- First tentative map ready. Handle entire missing thingies.
		if(slist.size() != dlist.size())
			throw new IllegalStateException("!!!!! Delta lists differ in size!?");
		boolean changed = true;
		for(int i = 0; i < slist.size(); i++) {
			DbRelation sr = slist.get(i);
			DbRelation dr = dlist.get(i);
			if(sr == null)
				relationDeleted(st, dt, dr);
			else if(dr == null)
				relationAdded(st, dt, sr);
			else {
				if(doCompareRelation(st, dt, sr, dr)) // Compare relation-content.
					changed = true;
			}
		}
		return changed;
	}

	/**
	 * Locates a relation in a collection having the specified name, or returns null if
	 * not found.
	 * @param list
	 * @param name
	 * @return
	 */
	private DbRelation findRelationByName(Collection<DbRelation> list, String name) {
		for(DbRelation r : list) {
			if(name.equals(r.getName()))
				return r;
		}
		return null;
	}

	/**
	 * Tries to locate the corresponding relation by doing a full match of the relation's
	 * table and column mapping. This ignores the name of the relation.
	 * @param list
	 * @param sr
	 * @return
	 */
	protected DbRelation findRelationByColumns(List<DbRelation> list, DbRelation sr) {
		for(DbRelation dr : list) {
			//-- Relations must be to the same tables as child and parent.
			if(!dr.getParent().getName().equals(sr.getParent().getName()))
				continue;
			if(!dr.getChild().getName().equals(sr.getChild().getName()))
				continue;

			//-- Parent and child are equal- match key pairs.
			if(sr.getPairList().size() != dr.getPairList().size())
				continue;
			boolean matched = true;
			for(int i = sr.getPairList().size(); --i >= 0;) {
				FieldPair sp = sr.getPairList().get(i);
				FieldPair dp = dr.getPairList().get(i);
				if(!sp.getChildColumn().getName().equals(dp.getChildColumn().getName())) {
					matched = false;
					break;
				}
				if(!sp.getParentColumn().getName().equals(dp.getParentColumn().getName())) {
					matched = false;
					break;
				}
			}
			if(matched)
				return dr;
		}
		return null;
	}

	/**
	 * Compare two relations. It has already been determined that both relations are paired, either
	 * by name or by columns. This creates a report for the relation's delta and calls a single
	 * "modified" call instead of calling a method for each change.
	 *
	 * @param st
	 * @param dt
	 * @param sr
	 * @param dr
	 * @return
	 * @throws Exception
	 */
	private boolean doCompareRelation(DbTable st, DbTable dt, DbRelation sr, DbRelation dr) throws Exception {
		boolean changed = false;

		/*
		 * If the name has changed this is the _only_ change, since the only way we could pair
		 * this relation was by columns so they do not differ;
		 */
		if(!Util.equalObjects(sr.getName(), dr.getName())) {
			relationNameChanged(st, dt, sr, dr, sr.getName());
			return true;
		}

		//-- Name is the same but tables have changed?
		if(!sr.getParent().getName().equals(dr.getParent().getName()) || !sr.getChild().getName().equals(dr.getChild().getName())) {
			relationTablesChanged(st, dt, sr, dr);
			return true;
		}

		//-- Name and tables have not changed; check content and create a delta: a set of columns added/deleted.
		if(sr.getPairList().size() != dr.getPairList().size())
			changed = true;
		else {
			for(int i = 0; i < sr.getPairList().size(); i++) {
				FieldPair sp = sr.getPairList().get(i);
				FieldPair dp = dr.getPairList().get(i);
				if(!sp.getParentColumn().getName().equals(dp.getParentColumn().getName()) || !sp.getChildColumn().getName().equals(dp.getChildColumn().getName())) {
					changed = true;
					break;
				}
			}
		}
		if(changed)
			relationColumnsChanged(st, dt, sr, dr);
		return changed;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Compare view definitions.							*/
	/*--------------------------------------------------------------*/

	public boolean compareDDL(String a, String b) {
		return a.trim().equalsIgnoreCase(b.trim());
	}

	public void doCompareViews() throws Exception {
		Set<DbView> doneset = new HashSet<DbView>(m_dest.getViewMap().values());
		for(DbView sv : m_src.getViewMap().values()) {
			DbView dv = m_dest.findView(sv.getName());
			if(dv == null)
				viewAdded(sv);
			else {
				//-- Got view with same name
				doneset.remove(dv); // This one was seen;

				//-- Compare view contents
				if(!compareDDL(sv.getSql(), dv.getSql()))
					viewChanged(dv, sv);
			}
		}

		for(DbView dv : doneset)
			viewDeleted(dv);
	}

	public void doComparePackages() throws Exception {
		int bdif = 0, ddif = 0, adds = 0, dels = 0;
		Set<Package> doneset = new HashSet<Package>(m_dest.getPackageMap().values());
		for(Package sp : m_src.getPackageMap().values()) {
			Package dp = m_dest.findPackage(sp.getName());
			if(dp == null) {
				packageAdded(sp);
				adds++;
			} else {
				doneset.remove(dp);

				//-- Compare package contents
				if(!compareDDL(sp.getDefinition(), dp.getDefinition())) {
					packageDefinitionChanged(sp, dp);
					ddif++;
				}
				if(!compareDDL(sp.getBody(), dp.getBody())) {
					packageBodyChanged(sp, dp);
					bdif++;
				}
			}
		}
		for(Package p : doneset) {
			packageDeleted(p);
			dels++;
		}
		System.out.println("Packages: " + m_dest.getPackageMap().size() + " total, " + adds + " added, " + dels + " deleted, " + ddif + " changes in def, " + bdif + " changes in body");
	}

	public void doCompareTriggers() throws Exception {
		int diff = 0, adds = 0, dels = 0;
		Set<Trigger> doneset = new HashSet<Trigger>(m_dest.getTriggerMap().values());
		for(Trigger st : m_src.getTriggerMap().values()) {
			Trigger dt = m_dest.findTrigger(st.getName());
			if(dt == null) {
				triggerAdded(st);
				adds++;
			} else {
				doneset.remove(dt);
				if(!compareDDL(st.getCode(), dt.getCode())) {
					diff++;
					triggerChanged(st, dt);
				}
			}
		}
		for(Trigger dt : doneset) {
			triggerDeleted(dt);
			dels++;
		}
		System.out.println("Triggers: " + m_dest.getTriggerMap().size() + " total, " + adds + " added, " + dels + " deleted, " + diff + " changed");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Indexes.											*/
	/*--------------------------------------------------------------*/

	//    private void	doCompareSpecialIndexes() throws Exception {
	//    	Set<SpecialIndex>	doneset = new HashSet<SpecialIndex>(m_dest.getSpecialIndexMap().values());
	//    	for(SpecialIndex sx : m_src.getSpecialIndexMap().values()) {
	//    		SpecialIndex dx = m_dest.findSpecialIndex(sx.getName());
	//
	//    		if(dx == null)
	//    			specialIndexAdded(sx);
	//    		else {
	//    			doneset.remove(dx);
	//    			if(! compareDDL(sx.getDdl(), dx.getDdl()))
	//    				specialIndexChanged();
	//    		}
	//    	}
	//    	for(SpecialIndex dx : doneset)
	//    		specialIndexDeleted(dx);
	//    }

	/**
	 * Compare full index definitions.
	 */
	private boolean doCompareIndexes() throws Exception {
		Set<DbIndex> doneset = new HashSet<DbIndex>(m_dest.getIndexMap().values());
		boolean changed = false;
		for(DbIndex six : m_src.getIndexMap().values()) {
			DbIndex dix = m_dest.findIndex(six.getName());
			if(dix == null)
				indexAdded(six.getTable(), six);
			else {
				doneset.remove(dix);

				//-- Is it on the same table?
				if(!six.getTable().getName().equals(dix.getTable().getName())) {
					indexTableChanged(six, dix);
					continue;
				}

				//-- Check unicity
				boolean uniqchanged = six.isUnique() != dix.isUnique();

				//-- Check columns and column order to determine order changes.
				List<ColumnChange> cc = findColumnChanges(six.getColumnList(), dix.getColumnList());
				if(uniqchanged || cc.size() != 0) {
					indexChanged(six, dix, uniqchanged, cc);
					changed = true;
				}
			}
		}
		for(DbIndex ix : doneset)
			indexDeleted(ix);
		return changed;
	}

	/**
	 * Creates a list of changes to execute on DEST to get the list in SRC. We first handle all column
	 * deletes, then all adds/inserts; the remaining changes are moves.
	 * @param sl
	 * @param dl
	 * @return
	 */
	private List<ColumnChange> findColumnChanges(List<IndexColumn> slin, List<IndexColumn> dlin) {
		List<ColumnChange> chl = new ArrayList<ColumnChange>();

		List<IndexColumn> sl = new ArrayList<IndexColumn>(slin);
		List<IndexColumn> dl = new ArrayList<IndexColumn>(dlin);

		//-- Generate DELETE changes for all columns no longer present in src
		int ix = 0;
		for(Iterator<IndexColumn> it = dl.iterator(); it.hasNext();) {
			IndexColumn dc = it.next();
			IndexColumn sc = findColumn(sl, dc.getColumn().getName()); // Still exists in source?
			if(sc == null) {
				//-- Column is deleted in source
				chl.add(new ColumnChange(ChangeType.DELETED, dc, ix, -1));
				it.remove(); // Remove from source list.
			}
			ix++;
		}

		//-- Generate ADD or INSERT changes: add all src cols not in dest, and move columns.
		ix = 0;
		while(ix < sl.size()) {
			IndexColumn sc = sl.get(ix);
			int dix = findColumnIndex(dl, sc.getColumn().getName()); // Does same column exist there?
			if(dix == -1) {
				//-- New column at this position in dest. Add an INSERT @ here,
				chl.add(new ColumnChange(ChangeType.ADDED, sc, ix, -1));
				dl.add(ix, sc); // Fake add column @ correct position
			} else {
				if(dix != ix) {
					chl.add(new ColumnChange(ChangeType.MOVED, dl.get(dix), dix, ix));
				}
				IndexColumn dc = dl.get(dix);
				if(sc.isDescending() != dc.isDescending())
					chl.add(new ColumnChange(ChangeType.ASCDESC, dc, dix, -1));
			}
			ix++;
		}

		//-- Column lists should be the same size now
		if(dl.size() != sl.size())
			throw new IllegalStateException("!! Logic error");
		return chl;
	}

	static public IndexColumn findColumn(List<IndexColumn> list, String name) {
		for(IndexColumn c : list) {
			if(name.equals(c.getColumn().getName()))
				return c;
		}
		return null;
	}

	static public int findColumnIndex(List<IndexColumn> list, String name) {
		int ix = 0;
		for(IndexColumn c : list) {
			if(name.equals(c.getColumn().getName()))
				return ix;
			ix++;
		}
		return -1;
	}

	abstract public void tableAdded(DbTable st) throws Exception;

	abstract public void tableDeleted(DbTable dt) throws Exception;

	abstract public void primaryKeyAdded(DbTable dt, DbPrimaryKey pk) throws Exception;

	abstract public void primaryKeyDeleted(DbTable dt, DbPrimaryKey oldpk) throws Exception;

	abstract public void primaryKeyFieldAdded(DbTable dt, int ix, DbColumn sc) throws Exception;

	abstract public void primaryKeyFieldDeleted(DbTable dt, int ix, DbColumn dc) throws Exception;

	abstract public void primaryKeyFieldChanged(DbTable dt, int ix, DbColumn oldc, DbColumn newc) throws Exception;

	abstract public void primaryKeyChanged(DbTable oldt, DbTable newt, DbPrimaryKey oldpk, DbPrimaryKey newpk) throws Exception;

	abstract public void columnChanged(DbTable dt, DbColumn newc, DbColumn oldc, int flag) throws Exception;

	abstract public void columnDeleted(DbTable dt, DbColumn dc) throws Exception;

	abstract public void columnAdded(DbTable dt, DbColumn sc) throws Exception;

	abstract public void relationDeleted(DbTable st, DbTable dt, DbRelation rel) throws Exception;

	abstract public void relationAdded(DbTable st, DbTable dt, DbRelation newrel) throws Exception;

	abstract public void relationNameChanged(DbTable st, DbTable dt, DbRelation sr, DbRelation dr, String newname) throws Exception;

	abstract public void relationTablesChanged(DbTable st, DbTable dt, DbRelation sr, DbRelation dr) throws Exception;

	abstract public void relationColumnsChanged(DbTable st, DbTable dt, DbRelation sr, DbRelation dr) throws Exception;

	abstract public void viewAdded(DbView v) throws Exception;

	abstract public void viewDeleted(DbView v) throws Exception;

	abstract public void viewChanged(DbView oldview, DbView newview) throws Exception;

	abstract public void packageAdded(Package p) throws Exception;

	abstract public void packageDeleted(Package p) throws Exception;

	abstract public void packageDefinitionChanged(Package oldp, Package newp) throws Exception;

	abstract public void packageBodyChanged(Package oldp, Package newp) throws Exception;

	abstract public void triggerAdded(Trigger newt) throws Exception;

	abstract public void triggerDeleted(Trigger oldt) throws Exception;

	abstract public void triggerChanged(Trigger oldt, Trigger newt) throws Exception;

	abstract public void indexAdded(DbTable dt, DbIndex newix) throws Exception;

	abstract public void indexDeleted(DbIndex oldix) throws Exception;

	abstract public void indexTableChanged(DbIndex oldix, DbIndex newix) throws Exception;

	abstract public void indexChanged(DbIndex six, DbIndex dix, boolean uniquechanged, List<ColumnChange> colchanges) throws Exception;
}

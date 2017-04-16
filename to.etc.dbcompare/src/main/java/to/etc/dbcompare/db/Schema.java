package to.etc.dbcompare.db;

import java.io.*;
import java.util.*;

/**
 * A database (schema) definition.
 * 
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2006
 */
public class Schema implements Serializable {
	private String						m_name;

	private Map<String, Table>			m_tableMap			= new HashMap<String, Table>();

	private Map<String, Index>			m_indexMap			= new HashMap<String, Index>();

	private Map<String, DbView>			m_viewMap			= new HashMap<String, DbView>();

	private Map<String, Procedure>		m_procedureMap		= new HashMap<String, Procedure>();

	private Map<String, Package>		m_packageMap		= new HashMap<String, Package>();

	private Map<String, Trigger>		m_triggerMap		= new HashMap<String, Trigger>();

	private Map<String, SpecialIndex>	m_specialIndexMap	= new HashMap<String, SpecialIndex>();

	public Schema(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	public Table createTable(String name) {
		Table t = new Table(this, name);
		m_tableMap.put(name, t);
		return t;
	}

	public Map<String, Table> getTableMap() {
		return m_tableMap;
	}

	public Table findTable(String name) {
		return m_tableMap.get(name);
	}

	public Table getTable(String name) {
		Table t = findTable(name);
		if(t == null)
			throw new IllegalStateException("No table '" + name + "'");
		return t;
	}

	public DbView findView(String name) {
		return m_viewMap.get(name);
	}

	public void addView(DbView v) {
		m_viewMap.put(v.getName(), v);
	}

	public Map<String, DbView> getViewMap() {
		return m_viewMap;
	}

	public void addProcedure(Procedure p) {
		m_procedureMap.put(p.getName(), p);
	}

	public Procedure findProcedure(String name) {
		return m_procedureMap.get(name);
	}

	public Map<String, Procedure> getProcedureMap() {
		return m_procedureMap;
	}

	public void addPackage(Package p) {
		m_packageMap.put(p.getName(), p);
	}

	public Package findPackage(String s) {
		return m_packageMap.get(s);
	}

	public Map<String, Package> getPackageMap() {
		return m_packageMap;
	}

	public void addTrigger(Trigger t) {
		m_triggerMap.put(t.getName(), t);
	}

	public Trigger findTrigger(String name) {
		return m_triggerMap.get(name);
	}

	public Map<String, Trigger> getTriggerMap() {
		return m_triggerMap;
	}

	public Map<String, Index> getIndexMap() {
		return m_indexMap;
	}

	public void addIndex(Index ix) {
		m_indexMap.put(ix.getName(), ix);
	}

	public Index findIndex(String name) {
		return m_indexMap.get(name);
	}

	public Map<String, SpecialIndex> getSpecialIndexMap() {
		return m_specialIndexMap;
	}

	public void addSpecialIndex(SpecialIndex ix) {
		m_specialIndexMap.put(ix.getName(), ix);
	}

	public SpecialIndex findSpecialIndex(String name) {
		return m_specialIndexMap.get(name);
	}

}

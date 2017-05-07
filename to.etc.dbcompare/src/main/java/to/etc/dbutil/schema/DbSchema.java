package to.etc.dbutil.schema;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.dbutil.reverse.*;

/**
 * A database (schema) definition.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2006
 */
public class DbSchema implements Serializable {
	transient private Reverser m_reverser;

	private String m_name;

	private Map<String, DbTable> m_tableMap = new HashMap<String, DbTable>();

	private Map<String, DbIndex> m_indexMap = new HashMap<String, DbIndex>();

	private Map<String, DbView> m_viewMap = new HashMap<String, DbView>();

	private Map<String, Procedure> m_procedureMap = new HashMap<String, Procedure>();

	private Map<String, Package> m_packageMap = new HashMap<String, Package>();

	private Map<String, Trigger> m_triggerMap = new HashMap<String, Trigger>();

	private Map<String, SpecialIndex> m_specialIndexMap = new HashMap<String, SpecialIndex>();

	public DbSchema(Reverser r, String name) {
		m_name = name;
		m_reverser = r;
	}

	public void setReverser(Reverser r) {
		m_reverser = r;
	}

	@Nonnull
	public Reverser getReverser() {
		if(null != m_reverser)
			return m_reverser;
		throw new IllegalStateException("Missing reverser.");
	}

	public String getName() {
		return m_name;
	}

	public DbTable createTable(String name) {
		DbTable t = new DbTable(this, name);
		m_tableMap.put(name, t);
		return t;
	}

	public List<DbTable> getTables() {
		return new ArrayList<DbTable>(m_tableMap.values());
	}

	public DbTable findTable(String name) {
		return m_tableMap.get(name);
	}

	public DbTable getTable(String name) {
		DbTable t = findTable(name);
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

	public Map<String, DbIndex> getIndexMap() {
		return m_indexMap;
	}

	public void addIndex(DbIndex ix) {
		m_indexMap.put(ix.getName(), ix);
	}

	public DbIndex findIndex(String name) {
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

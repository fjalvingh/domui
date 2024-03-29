package to.etc.dbutil.schema;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.dbutil.reverse.Reverser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A database (schema) definition.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2006
 */
@NonNullByDefault
public class DbSchema implements Serializable {
	transient private Reverser m_reverser;

	/** The assigned schema name */
	private String m_name;

	/** If this schema is in reality a catalog: thisis the catalog name (or null) */
	@Nullable
	private String m_catalogName;

	/** If this really is a schema this contains the schema name. */
	@Nullable
	private String m_internalSchemaName;

	private boolean m_forceQuote;

	private Map<String, DbTable> m_tableMap = new HashMap<String, DbTable>();

	private Map<String, DbIndex> m_indexMap = new HashMap<String, DbIndex>();

	private Map<String, DbView> m_viewMap = new HashMap<String, DbView>();

	private Map<String, Procedure> m_procedureMap = new HashMap<String, Procedure>();

	private Map<String, Package> m_packageMap = new HashMap<String, Package>();

	private Map<String, Trigger> m_triggerMap = new HashMap<String, Trigger>();

	private Map<String, SpecialIndex> m_specialIndexMap = new HashMap<String, SpecialIndex>();

	private Map<String, DbSequence> m_sequenceMap = new HashMap<>();

	public DbSchema(Reverser r, @Nullable String internalSchemaName, @Nullable String internalCatalogName) {
		m_catalogName = internalCatalogName;
		m_internalSchemaName = internalSchemaName;
		m_name = internalSchemaName == null ? Objects.requireNonNull(internalCatalogName) : internalSchemaName;
		m_reverser = r;
	}

	//public void setReverser(Reverser r) {
	//	m_reverser = r;
	//}

	@NonNull
	public Reverser getReverser() {
		Reverser reverser = m_reverser;
		if(null != reverser)
			return reverser;
		throw new IllegalStateException("Missing reverser.");
	}

	public String getName() {
		return m_name;
	}

	//public void setName(String name) {
	//	m_name = name;
	//}

	public DbTable createTable(String name) {
		DbTable t = new DbTable(this, name);
		m_tableMap.put(name, t);
		return t;
	}

	public List<DbTable> getTables() {
		return new ArrayList<DbTable>(m_tableMap.values());
	}

	@Nullable
	public DbTable findTable(String name) {
		return m_tableMap.get(name);
	}

	@NonNull
	public DbTable getTable(String name) {
		DbTable t = findTable(name);
		if(t == null)
			throw new IllegalStateException("No table '" + name + "'");
		return t;
	}

	@Nullable
	public DbView findView(String name) {
		return m_viewMap.get(name);
	}

	public void addView(DbView v) {
		m_viewMap.put(v.getName(), v);
	}

	@NonNull
	public Map<String, DbView> getViewMap() {
		return m_viewMap;
	}

	public void addProcedure(Procedure p) {
		m_procedureMap.put(p.getName(), p);
	}

	@Nullable
	public Procedure findProcedure(String name) {
		return m_procedureMap.get(name);
	}

	@NonNull
	public Map<String, Procedure> getProcedureMap() {
		return m_procedureMap;
	}

	public void addPackage(Package p) {
		m_packageMap.put(p.getName(), p);
	}

	@Nullable
	public Package findPackage(String s) {
		return m_packageMap.get(s);
	}

	public Map<String, Package> getPackageMap() {
		return m_packageMap;
	}

	public void addTrigger(Trigger t) {
		m_triggerMap.put(t.getName(), t);
	}

	@Nullable
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

	@Nullable
	public DbIndex findIndex(String name) {
		return m_indexMap.get(name);
	}

	public Map<String, SpecialIndex> getSpecialIndexMap() {
		return m_specialIndexMap;
	}

	public void addSpecialIndex(SpecialIndex ix) {
		m_specialIndexMap.put(ix.getName(), ix);
	}

	@Nullable
	public SpecialIndex findSpecialIndex(String name) {
		return m_specialIndexMap.get(name);
	}

	public void addSequence(DbSequence seq) {
		m_sequenceMap.put(seq.getName(), seq);
	}

	@NonNull
	public Map<String, DbSequence> getSequenceMap() {
		return m_sequenceMap;
	}

	@Nullable
	public DbSequence findSequence(String name) {
		return m_sequenceMap.get(name);
	}

	public List<DbSequence> getSequences() {
		ArrayList<DbSequence> list = new ArrayList<>(m_sequenceMap.values());
		list.sort(Comparator.comparing(DbSequence::getName));
		return list;
	}

	public boolean isForceQuote() {
		return m_forceQuote;
	}

	public void setForceQuote(boolean forceQuote) {
		m_forceQuote = forceQuote;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String qualifyName(String in) {
		if(m_name.isEmpty())
			return in;
		return m_name + "." + in;
	}

	public String getSchemaNameAndDot() {
		if(m_name.isEmpty())
			return "";
		return m_name + ".";
	}

	/**
	 * If this is really a catalog: this returns the nonnull catalog name.
	 */
	@Nullable
	public String getInternalCatalogName() {
		return m_catalogName;
	}

	/**
	 * If this is really a schema this returns the schema na,e
	 */
	@Nullable
	public String getInternalSchemaName() {
		return m_internalSchemaName;
	}
}

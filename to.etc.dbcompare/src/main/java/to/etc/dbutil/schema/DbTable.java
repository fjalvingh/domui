package to.etc.dbutil.schema;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

import javax.annotation.*;

import to.etc.dbutil.reverse.*;
import to.etc.util.*;

/**
 * A database table.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2006
 */
public class DbTable implements Serializable {
	@Nonnull
	final private DbSchema m_schema;

	@Nullable
	private DbPrimaryKey m_primaryKey;

	private boolean m_primaryKeyInitialized;

	private String m_name;

	private String m_comments;

	private List<DbColumn> m_columnList;

	private Map<String, DbColumn> m_columnMap;

	private Map<String, DbIndex> m_indexMap;

	private List<DbRelation> m_parentRelationList = new ArrayList<DbRelation>();

	private List<DbRelation> m_childRelationList = new ArrayList<DbRelation>();

	private boolean m_parentRelationsInitialized;

	private boolean m_childRelationsInitialized;

	private List<DbCheckConstraint> m_checkConstraintList = new ArrayList<DbCheckConstraint>();

	private List<DbUniqueConstraint> m_uniqueConstraintList = new ArrayList<DbUniqueConstraint>();

	transient private boolean m_gotRecordCount;

	/** The #of records in this table, or -1 if not yet acquired. */
	transient private long m_recordCount = -1;

	private List<DbColumn> m_sortedColumns;

	public DbTable(DbSchema schema, String name) {
		m_schema = schema;
		m_name = name;
	}

	@Nonnull
	public Reverser r() {
		return m_schema.getReverser();
	}

	private void initColumns() {
		if(m_columnList == null) {
			r().lazy(new IExec() {
				@Override
				public void exec(Connection dbc) throws Exception {
					r().reverseColumns(dbc, DbTable.this);
				}
			});
		}
	}

	private void initIndexes() {
		if(m_indexMap == null) {
			r().lazy(new IExec() {
				@Override
				public void exec(Connection dbc) throws Exception {
					r().reverseIndexes(dbc, DbTable.this);
				}
			});
		}
	}

	private void initPrimaryKey() {
		if(!m_primaryKeyInitialized) {
			r().lazy(new IExec() {
				@Override
				public void exec(Connection dbc) throws Exception {
					r().reversePrimaryKey(dbc, DbTable.this);
				}
			});
		}
	}

	private void initParentRelationList() {
		if(m_parentRelationsInitialized)
			return;
		m_parentRelationsInitialized = true;
		r().lazy(new IExec() {
			@Override
			public void exec(Connection dbc) throws Exception {
				r().reverseParentRelation(dbc, DbTable.this);
			}
		});
	}

	private void initChildRelationList() {
		if(m_childRelationsInitialized)
			return;
		m_childRelationsInitialized = true;
		r().lazy(new IExec() {
			@Override
			public void exec(Connection dbc) throws Exception {
				r().reverseChildRelations(dbc, DbTable.this);
			}
		});
	}


	public void markRelationsInitialized() {
		m_parentRelationsInitialized = true;
		m_childRelationsInitialized = true;
	}

	/**
	 * Return the relations that I am a <i>parent</i> in.
	 * @return
	 */
	@Nonnull
	public List<DbRelation> getParentRelationList() {
		initParentRelationList();
		return m_parentRelationList;
	}

	@Nonnull
	public List<DbRelation> internalGetParentRelationList() {
		return m_parentRelationList;
	}
	/**
	 * Return the relations that I am a <i>child</i> in.
	 * @return
	 */
	@Nonnull
	public List<DbRelation> getChildRelationList() {
		initChildRelationList();
		return m_childRelationList;
	}

	/**
	 * Return the relations that I am a <i>child</i> in.
	 * @return
	 */
	@Nonnull
	public List<DbRelation> internalGetChildRelationList() {
		return m_childRelationList;
	}

	@Nonnull
	public Map<String, DbColumn> getColumnMap() {
		initColumns();
		return m_columnMap;
	}

	public void setColumnMap(Map<String, DbColumn> columnMap) {
		m_columnMap = columnMap;
	}

	public String getComments() {
		return m_comments;
	}

	public void setComments(String comments) {
		m_comments = comments;
	}

	public String getName() {
		return m_name;
	}

	public DbSchema getSchema() {
		return m_schema;
	}

	public long getRecordCount(Database db) throws Exception {
		if(!m_gotRecordCount) {
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				ps = db.dbc().prepareStatement("select count(*) from " + m_name);
				rs = ps.executeQuery();
				if(!rs.next())
					throw new SQLException("Cannot get table row count");
				m_recordCount = rs.getLong(1);
				m_gotRecordCount = true;
			} finally {
				try {
					if(rs != null)
						rs.close();
				} catch(Exception x) {}
				try {
					if(ps != null)
						ps.close();
				} catch(Exception x) {}
			}
		}
		return m_recordCount;
	}

	public long getRecordCount() throws Exception {
		synchronized(r()) {
			if(!m_gotRecordCount) {
				r().lazy(new IExec() {
					@Override
					public void exec(@Nonnull Connection dbc) throws Exception {
						PreparedStatement ps = null;
						ResultSet rs = null;
						try {
							ps = dbc.prepareStatement("select count(*) from " + m_name);
							rs = ps.executeQuery();
							if(!rs.next())
								throw new SQLException("Cannot get table row count");
							m_recordCount = rs.getLong(1);
							m_gotRecordCount = true;
						} finally {
							FileTool.closeAll(rs, ps);
						}
					}
				});
			}
			return m_recordCount;
		}
	}

	public DbColumn findColumn(String name) {
		return getColumnMap().get(name);
	}

	public DbColumn getColumn(String name) {
		DbColumn c = findColumn(name);
		if(c == null)
			throw new IllegalStateException("No column '" + name + "' in table " + getName());
		return c;
	}

	@Nonnull
	public List<DbColumn> getColumnList() {
		initColumns();
		if(null != m_columnList)
			return m_columnList;
		throw new IllegalStateException("Columns not initialized");
	}

	@Nonnull
	public synchronized List<DbColumn> getColumnListSorted() {
		List<DbColumn> sc = m_sortedColumns;
		if(sc == null) {
			List<DbColumn> all = new ArrayList<DbColumn>(getColumnList());
			Collections.sort(all, new Comparator<DbColumn>() {
				@Override
				public int compare(DbColumn a, DbColumn b) {
					return a.getName().compareTo(b.getName());
				}
			});
			sc = m_sortedColumns = Collections.unmodifiableList(all);
		}
		return sc;
	}


	public Map<String, DbIndex> getIndexMap() {
		initIndexes();
		return m_indexMap;
	}

	public void setIndexMap(Map<String, DbIndex> indexMap) {
		m_indexMap = indexMap;
	}

	public DbIndex findIndex(String name) {
		return getIndexMap().get(name);
	}

	public DbPrimaryKey getPrimaryKey() {
		initPrimaryKey();
		return m_primaryKey;
	}

	public void setPrimaryKey(DbPrimaryKey primaryKey) {
		m_primaryKeyInitialized = true;
		m_primaryKey = primaryKey;
	}

	public void addConstraint(DbCheckConstraint c) {
		m_checkConstraintList.add(c);
	}

	public DbCheckConstraint findCheckConstraint(String s) {
		for(DbCheckConstraint c : m_checkConstraintList) {
			if(s.equals(c.getName()))
				return c;
		}
		return null;
	}

	public List<DbCheckConstraint> getCheckConstraintList() {
		return m_checkConstraintList;
	}

	public DbUniqueConstraint findUniqueConstraint(String name) {
		for(DbUniqueConstraint c : m_uniqueConstraintList) {
			if(name.equals(c.getName()))
				return c;
		}
		return null;
	}

	public void addConstraint(DbUniqueConstraint c) {
		m_uniqueConstraintList.add(c);
	}

	public List<DbUniqueConstraint> getUniqueConstraintList() {
		return m_uniqueConstraintList;
	}

	/**
	 * This tries to find out if most columns have common prefixes in this table. It
	 * does that by finding the most-often used prefix in all column names. Columns
	 * "native" to the table are scored higher in determination than columns that
	 * are foreign.
	 * @return
	 */
	public String getColumnPrefix() {
		Set<String> fkcolset = new HashSet<String>();
		for(DbRelation rel : getChildRelationList()) {			// Relations that I am a child of
			if(rel.getChild() != this)
				throw new IllegalStateException("Bad rel list");
			for(FieldPair col : rel.getPairList()) {
				fkcolset.add(col.getChildColumn().getName());
			}
		}
		Set<DbColumn> pkcolset = new HashSet<DbColumn>();
		if(getPrimaryKey() != null)
			pkcolset.addAll(getPrimaryKey().getColumnList());

		//-- Prefix occurrence score per prefix
		Map<String, Integer> occmap = new HashMap<String, Integer>();
		for(DbColumn c : getColumnList()) {
			String name = c.getName();
			int pos = name.indexOf('_');
			if(pos == -1)
				continue;

			//-- Is this column part of a foreign key?
			int score = 100;
			if(fkcolset.contains(name))
				score -= 20;
			else if(pkcolset.contains(name)) {
				//-- If (a) pk starts with this prefix too (and it's not foreign) increase score
				score += 40;
			}

			String pre = name.substring(0, pos).toLowerCase();
			Integer v = occmap.get(pre);
			if(v != null)
				score += v.intValue();
			occmap.put(pre, Integer.valueOf(score));
		}
		if(occmap.size() == 0)
			return "";
		List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(occmap.entrySet());
		if(list.size() == 1) {
			return list.get(0).getKey();
		}

		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {			// Sort highest first
			@Override
				public int compare(Entry<String, Integer> a, Entry<String, Integer> b) {
					return -(a.getValue().intValue() - b.getValue().intValue());
			}
		});
		Map.Entry<String, Integer> top = list.get(0);
		Map.Entry<String, Integer> next = list.get(1);
		int dt = top.getValue().intValue() - next.getValue().intValue();
		if(dt < 3)
			return "";
		return top.getKey();
	}


	@Override
	public String toString() {
		return m_schema.getName() + "." + getName();
	}

	public void initializeColumns(@Nonnull List<DbColumn> columnList, @Nonnull Map<String, DbColumn> columnMap) {
		m_columnList = columnList;
		m_columnMap = columnMap;
	}
}

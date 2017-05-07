package to.etc.dbcompare.db;

import java.io.*;
import java.util.*;

/**
 * A database table.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2006
 */
public class Table implements Serializable {
	private Schema					m_schema;

	private PrimaryKey				m_primaryKey;

	private String					m_name;

	private String					m_comments;

	private List<Column>			m_columnList			= new ArrayList<Column>();

	private Map<String, Column>		m_columnMap				= new HashMap<String, Column>();

	private Map<String, Index>		m_indexMap				= new HashMap<String, Index>();

	private List<Relation>			m_parentRelationList	= new ArrayList<Relation>();

	private List<Relation>			m_childRelationList		= new ArrayList<Relation>();

	private List<CheckConstraint>	m_checkConstraintList	= new ArrayList<CheckConstraint>();

	private List<UniqueConstraint>	m_uniqueConstraintList	= new ArrayList<UniqueConstraint>();

	/** The #of records in this table, or -1 if not yet aquired. */
	private long					m_recordCount;

	public Table(Schema schema, String name) {
		m_schema = schema;
		m_name = name;
	}

	public List<Relation> getChildRelationList() {
		return m_childRelationList;
	}

	public void setChildRelationList(List<Relation> childRelationList) {
		m_childRelationList = childRelationList;
	}

	public Map<String, Column> getColumnMap() {
		return m_columnMap;
	}

	public void setColumnMap(Map<String, Column> columnMap) {
		m_columnMap = columnMap;
	}

	public String getComments() {
		return m_comments;
	}

	public void setComments(String comments) {
		m_comments = comments;
	}

	public List<Relation> getParentRelationList() {
		return m_parentRelationList;
	}

	public void setParentRelationList(List<Relation> parentRelationList) {
		m_parentRelationList = parentRelationList;
	}

	public String getName() {
		return m_name;
	}

	public Schema getSchema() {
		return m_schema;
	}

	public long getRecordCount() {
		return m_recordCount;
	}

	public Column createColumn(String name, ColumnType t, int prec, int scale, boolean nullable) {
		Column c = new Column(this, name, t, prec, scale, nullable);
		if(null != m_columnMap.put(name, c))
			throw new IllegalStateException("Duplicate column name '" + name + "' in table " + getName());
		m_columnList.add(c);
		return c;
	}

	public Column findColumn(String name) {
		return m_columnMap.get(name);
	}

	public Column getColumn(String name) {
		Column c = findColumn(name);
		if(c == null)
			throw new IllegalStateException("No column '" + name + "' in table " + getName());
		return c;
	}

	public List<Column> getColumnList() {
		return m_columnList;
	}


	public Map<String, Index> getIndexMap() {
		return m_indexMap;
	}

	public void setIndexMap(Map<String, Index> indexMap) {
		m_indexMap = indexMap;
	}

	public Index createIndex(String name, boolean uniq) {
		Index ix = new Index(this, name, uniq);
		m_indexMap.put(name, ix);
		return ix;
	}

	public void addIndex(Index ix) {
		m_indexMap.put(ix.getName(), ix);
	}

	public Index findIndex(String name) {
		return m_indexMap.get(name);
	}

	public PrimaryKey getPrimaryKey() {
		return m_primaryKey;
	}

	public void setPrimaryKey(PrimaryKey primaryKey) {
		m_primaryKey = primaryKey;
	}

	public void addConstraint(CheckConstraint c) {
		m_checkConstraintList.add(c);
	}

	public CheckConstraint findCheckConstraint(String s) {
		for(CheckConstraint c : m_checkConstraintList) {
			if(s.equals(c.getName()))
				return c;
		}
		return null;
	}

	public List<CheckConstraint> getCheckConstraintList() {
		return m_checkConstraintList;
	}

	public UniqueConstraint findUniqueConstraint(String name) {
		for(UniqueConstraint c : m_uniqueConstraintList) {
			if(name.equals(c.getName()))
				return c;
		}
		return null;
	}

	public void addConstraint(UniqueConstraint c) {
		m_uniqueConstraintList.add(c);
	}

	public List<UniqueConstraint> getUniqueConstraintList() {
		return m_uniqueConstraintList;
	}
}

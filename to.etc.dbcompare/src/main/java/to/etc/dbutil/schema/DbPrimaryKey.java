package to.etc.dbutil.schema;

import java.io.*;
import java.util.*;

public class DbPrimaryKey implements Serializable {
	private DbTable m_table;

	private List<DbColumn> m_columnList = new ArrayList<DbColumn>();

	private String m_name;

	public DbPrimaryKey(DbTable table, String name) {
		m_table = table;
		m_name = name;
	}

	public List<DbColumn> getColumnList() {
		return m_columnList;
	}

	public String getConstraintName() {
		return m_name;
	}

	public DbTable getTable() {
		return m_table;
	}

	public void addColumn(DbColumn c) {
		m_columnList.add(c);
	}

	public String getName() {
		return m_name;
	}

}

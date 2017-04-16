package to.etc.dbcompare.db;

import java.io.*;
import java.util.*;

public class PrimaryKey implements Serializable {
	private Table			m_table;

	private List<Column>	m_columnList	= new ArrayList<Column>();

	private String			m_name;

	public PrimaryKey(Table table, String name) {
		m_table = table;
		m_name = name;
	}

	public List<Column> getColumnList() {
		return m_columnList;
	}

	public String getConstraintName() {
		return m_name;
	}

	public Table getTable() {
		return m_table;
	}

	public void addColumn(Column c) {
		m_columnList.add(c);
	}

	public String getName() {
		return m_name;
	}

}

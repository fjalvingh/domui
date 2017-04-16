package to.etc.dbcompare.db;

import java.io.*;

final public class FieldPair implements Serializable {
	private Column	m_parentColumn;

	private Column	m_childColumn;

	public FieldPair(Column parentColumn, Column childColumn) {
		m_parentColumn = parentColumn;
		m_childColumn = childColumn;
	}

	public Column getChildColumn() {
		return m_childColumn;
	}

	public Column getParentColumn() {
		return m_parentColumn;
	}
}

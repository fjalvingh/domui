package to.etc.dbutil.schema;

import java.io.*;

import javax.annotation.*;

final public class FieldPair implements Serializable {
	@Nonnull
	final private DbColumn m_parentColumn;

	@Nonnull
	final private DbColumn m_childColumn;

	public FieldPair(@Nonnull DbColumn parentColumn, @Nonnull DbColumn childColumn) {
		m_parentColumn = parentColumn;
		m_childColumn = childColumn;
	}

	@Nonnull
	public DbColumn getChildColumn() {
		return m_childColumn;
	}

	@Nonnull
	public DbColumn getParentColumn() {
		return m_parentColumn;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof FieldPair))
			return false;
		FieldPair fp = (FieldPair) obj;
		return m_parentColumn == fp.m_parentColumn && m_childColumn == fp.m_childColumn;
	}
}

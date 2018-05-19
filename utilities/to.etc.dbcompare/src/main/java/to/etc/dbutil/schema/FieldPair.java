package to.etc.dbutil.schema;

import org.eclipse.jdt.annotation.NonNull;

import java.io.Serializable;

final public class FieldPair implements Serializable {
	@NonNull
	final private DbColumn m_parentColumn;

	@NonNull
	final private DbColumn m_childColumn;

	public FieldPair(@NonNull DbColumn parentColumn, @NonNull DbColumn childColumn) {
		m_parentColumn = parentColumn;
		m_childColumn = childColumn;
	}

	@NonNull
	public DbColumn getChildColumn() {
		return m_childColumn;
	}

	@NonNull
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

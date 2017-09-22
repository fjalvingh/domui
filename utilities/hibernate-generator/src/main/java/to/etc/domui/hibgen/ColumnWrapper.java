package to.etc.domui.hibgen;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.Type;
import to.etc.dbutil.schema.DbColumn;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-9-17.
 */
public class ColumnWrapper {
	private DbColumn m_column;

	private String m_columnName;

	private FieldDeclaration m_fieldDeclaration;

	private MethodDeclaration	m_setter;

	private MethodDeclaration	m_getter;

	private VariableDeclarator m_variableDeclaration;

	private Type m_propertyType;

	private String m_propertyName;

	public ColumnWrapper() {
	}

	public ColumnWrapper(DbColumn column) {
		m_column = column;
		m_columnName = column.getName();
	}

	public ColumnWrapper setFieldName(String a) {
		//m_fieldName = a;
		return this;
	}

	public void setFieldDeclarator(FieldDeclaration d, VariableDeclarator vd) {
		m_fieldDeclaration = d;
		m_variableDeclaration = vd;

	}

	public String getColumnName() {
		return m_columnName;
	}

	public void setColumnName(String columnName) {
		m_columnName = columnName;
	}

	public FieldDeclaration getFieldDeclaration() {
		return m_fieldDeclaration;
	}

	public void setFieldDeclaration(FieldDeclaration fieldDeclaration) {
		m_fieldDeclaration = fieldDeclaration;
	}

	public MethodDeclaration getSetter() {
		return m_setter;
	}

	public void setSetter(MethodDeclaration setter) {
		m_setter = setter;
	}

	public MethodDeclaration getGetter() {
		return m_getter;
	}

	public void setGetter(MethodDeclaration getter) {
		m_getter = getter;
	}

	public VariableDeclarator getVariableDeclaration() {
		return m_variableDeclaration;
	}

	public void setVariableDeclaration(VariableDeclarator variableDeclaration) {
		m_variableDeclaration = variableDeclaration;
	}

	public void setPropertyType(Type propertyType) {
		m_propertyType = propertyType;
	}

	public Type getPropertyType() {
		return m_propertyType;
	}

	public void setPropertyName(String propertyName) {
		m_propertyName = propertyName;
	}

	public String getPropertyName() {
		return m_propertyName;
	}

	public DbColumn getColumn() {
		return m_column;
	}

	public void setColumn(DbColumn column) {
		m_column = column;
	}
}

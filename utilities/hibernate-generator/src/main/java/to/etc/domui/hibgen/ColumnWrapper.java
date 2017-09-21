package to.etc.domui.hibgen;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-9-17.
 */
public class ColumnWrapper {
	private String m_columnName;

	private FieldDeclaration m_fieldDeclaration;

	private MethodDeclaration	m_setter;

	private MethodDeclaration	m_getter;

	private VariableDeclarator m_variableDeclaration;

	public ColumnWrapper setFieldName(String a) {
		//m_fieldName = a;
		return this;
	}

	public void setFieldDeclarator(FieldDeclaration d, VariableDeclarator vd) {
		m_fieldDeclaration = d;
		m_variableDeclaration = vd;

	}
}

package to.etc.domui.hibgen;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.ast.type.Type;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbPrimaryKey;
import to.etc.dbutil.schema.DbRelation;
import to.etc.dbutil.schema.DbSchema;
import to.etc.dbutil.schema.DbTable;
import to.etc.dbutil.schema.FieldPair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-9-17.
 */
public class ColumnWrapper {
	final private ClassWrapper m_classWrapper;

	private DbColumn m_column;

	private String m_columnName;

	private FieldDeclaration m_fieldDeclaration;

	private MethodDeclaration	m_setter;

	private MethodDeclaration	m_getter;

	private VariableDeclarator m_variableDeclaration;

	private Type m_propertyType;

	private String m_propertyName;

	private List<String> m_valueSet;

	public ColumnWrapper(ClassWrapper cw) {
		m_classWrapper = cw;
	}

	public ColumnWrapper(ClassWrapper cw, DbColumn column) {
		m_classWrapper = cw;
		m_column = column;
		m_columnName = column.getName();
	}

	private AbstractGenerator g() {
		return m_classWrapper.g();
	}

	/**
	 * T if calculations for type etc need to be issued.
	 * @return
	 */
	public boolean isNew() {
		return (m_fieldDeclaration == null || m_getter == null || m_setter == null) && m_column != null;
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
		if(propertyType == null)
			throw new IllegalStateException();
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


	public void calculateColumnType(Connection dbc) throws Exception {
		DbColumn column = m_column;
		if(null == column)
			return;
		DbTable table = column.getTable();
		DbPrimaryKey primaryKey = table.getPrimaryKey();
		boolean ispk = primaryKey != null && primaryKey.getColumnList().contains(column);
		boolean nullable = column.isNullable() || ispk;

		//-- Is this some kind of FK?
		DbRelation parentRelation = isFkOf();
		if(null != parentRelation) {
			generateRelationType(parentRelation);
			return;
		}

		int sqltype = column.getSqlType();
		String typeString = column.getTypeString();

		switch(sqltype) {
			default:
				break;

			case Types.CHAR:
			case Types.VARCHAR:
			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.NUMERIC:
				if(column.getPrecision() < 20) {
					if(calculateDistinctValues(dbc, false))
						return;
				}
				break;

			case Types.BIGINT:
			case Types.INTEGER:
			case Types.DECIMAL:
				if(column.getPrecision() < 20) {
					if(calculateDistinctValues(dbc, true))
						return;
				}
				break;
		}

		if(calculateBasicType())
			return;
		g().error(column + ": cannot calculate the type for a " + typeString + " (sqltype " + sqltype + ")");
	}

	private boolean calculateBasicType() throws Exception {
		DbColumn column = m_column;
		if(null == column)
			return false;
		DbTable table = column.getTable();
		DbPrimaryKey primaryKey = table.getPrimaryKey();
		boolean ispk = primaryKey != null && primaryKey.getColumnList().contains(column);
		boolean nullable = column.isNullable() || ispk;

		int sqltype = column.getSqlType();
		switch(sqltype) {
			default:
				return false;

			case Types.REAL:
			case Types.DOUBLE:
			case Types.FLOAT:
				if(nullable) {
					setPropertyType(new ClassOrInterfaceType("Double"));
				} else {
					setPropertyType(new PrimitiveType(Primitive.DOUBLE));
				}
				return true;

			case Types.NUMERIC:
			case Types.DECIMAL:
			case Types.INTEGER:
			case Types.BIGINT:
			case Types.SMALLINT:
			case Types.TINYINT:
				if(column.getScale() > 0 || column.getPrecision() < 0) {
					setPropertyType(new ClassOrInterfaceType("BigDecimal"));
				} else {
					if(column.getPrecision() < 10) {
						if(nullable) {
							setPropertyType(new ClassOrInterfaceType("Integer"));
						} else {
							setPropertyType(new PrimitiveType(Primitive.INT));
						}
					} else if(column.getPrecision() <= 18 || (column.getPrecision() == 19 && ispk)) {
						if(nullable) {
							setPropertyType(new ClassOrInterfaceType("Long"));
						} else {
							setPropertyType(new PrimitiveType(Primitive.INT));
						}
					} else {
						setPropertyType(new ClassOrInterfaceType("BigInteger"));
					}
				}
				return true;

			case Types.VARCHAR:
			case Types.LONGVARCHAR:
			case Types.CHAR:
			case Types.NVARCHAR:
			case Types.NCHAR:
				setPropertyType(new ClassOrInterfaceType("String"));
				return true;

			case Types.DATE:
			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE:
				setPropertyType(new ClassOrInterfaceType("java.util.Date"));
				return true;
		}
	}


	/**
	 * Try to get the type for the related object.
	 * @param parentRelation
	 */
	private void generateRelationType(DbRelation parentRelation) throws Exception {
		DbTable parent = parentRelation.getParent();
		ClassWrapper parentClass = g().getWrapper(parent);
		if(null == parentClass) {
			g().error("Cannot locate class source file for " + parent + ", mapping as non-relation");
			calculateBasicType();
			return;
		}

		ClassOrInterfaceType referent = new ClassOrInterfaceType(parentClass.getClassName());
		setPropertyType(referent);
	}

	private DbRelation isFkOf() {
		DbColumn column = m_column;
		if(null == column)
			return null;
		List<DbRelation> res = new ArrayList<>();
		for(DbRelation dbRelation : column.getTable().getChildRelationList()) {
			for(FieldPair fieldPair : dbRelation.getPairList()) {
				if(fieldPair.getChildColumn() == column) {
					if(dbRelation.getPairList().size() == 1) {
						res.add(dbRelation);
					} else {
						g().warning(column + ": used in a multi-key relation " + dbRelation + ", skipped");
					}
				}
			}
		}

		if(res.size() == 0)
			return null;
		else if(res.size() == 1)
			return res.get(0);
		else {
			g().warning(column + ": foreign key in more than one relation, skipped");
			return null;
		}
	}

	private boolean calculateDistinctValues(Connection dbc, boolean asnumber) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct ").append(m_column.getName()).append(" from ");
		DbTable table = m_column.getTable();
		DbSchema schema = table.getSchema();
		sb.append(schema.getName())
			.append(".")
			.append(table.getName());

		try(PreparedStatement ps = dbc.prepareStatement(sb.toString());
			ResultSet rs = ps.executeQuery()) {
			List<String> res = new ArrayList<String>();
			boolean codeenum = false;
			while(rs.next()) {
				if(res.size() > 50) { // Too many enum fields?
					g().info(m_column  + ": not an enum because it has too many distinct values");
					return false;
				}
				String name = rs.getString(1);
				if(name != null && name.length() > 0) {
					res.add(name);
					if(! asnumber && !isValidJavaIdent(name)) {
						if(!codeenum)
							g().info(m_column + ": not an enum but a codeenum because label " + name + " is not a valid java identifier");
						codeenum = true;
					}
				}
			}
			if(res.size() == 0)
				return false;

			//-- Got a reasonably-sized list of results... Create an enum?
			if(codeenum) {
				return false;
			}

			//-- Check for boolean type values
			if(res.size() == 2) {
				if(VALID_BOOLEAN_CHAR_SET.contains(res.get(0).toLowerCase()) && VALID_BOOLEAN_CHAR_SET.contains(res.get(1).toLowerCase())) {
					if(m_column.isNullable()) {
						setPropertyType(new ClassOrInterfaceType("Boolean"));
					} else {
						setPropertyType(new PrimitiveType(Primitive.BOOLEAN));
					}
				}
				setValueSet(res);
				return true;
			}

			return false;
		}
	}

	static private final Set<String> VALID_BOOLEAN_CHAR_SET = new HashSet<>(Arrays.asList("y", "n", "t", "f", "true", "false", "0", "1"));

	static private boolean isValidJavaIdent(String name) {
		if(name == null || name.length() == 0)
			return false;
		if(!Character.isJavaIdentifierStart(name.charAt(0)))
			return false;
		for(int i = 1; i < name.length(); i++) {
			char c = name.charAt(i);
			if(!Character.isJavaIdentifierPart(c) || c < 32 || c > 127)
				return false;
		}
		return true;
	}

	public void setValueSet(List<String> valueSet) {
		m_valueSet = valueSet;
	}

	public List<String> getValueSet() {
		return m_valueSet;
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		DbColumn column = m_column;
		if(null != column)
			sb.append("column ").append(column.getName());
		String propertyName = m_propertyName;
		if(null != propertyName) {
			if(sb.length() > 0) {
				sb.append(" (").append(propertyName).append(")");
			} else {
				sb.append("property ").append(propertyName);
			}
		}
		return m_classWrapper + " " + sb.toString();
	}
}

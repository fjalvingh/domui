package to.etc.domui.hibgen;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.AssignExpr.Operator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.w3c.dom.Node;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbPrimaryKey;
import to.etc.dbutil.schema.DbRelation;
import to.etc.dbutil.schema.DbSchema;
import to.etc.dbutil.schema.DbTable;
import to.etc.dbutil.schema.FieldPair;
import to.etc.xml.DomTools;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-9-17.
 */
public class ColumnWrapper {
	enum RelationType {
		oneToMany
		, manyToOne
		, none
	}

	enum ExtraType {
		YesNo,
		TrueFalse,
		OneZero,
		YesNoPrimitive,
		TrueFalsePrimitive,
		OneZeroPrimitive,
		TemporalDate,
		TemporalTimestamp,
		EnumeratedString
	}

	enum ColumnType {
		column
		, compoundKey
	}

	private ColumnType m_type = ColumnType.column;

	private ColumnWrapper m_childsParentProperty;

	/** The parent class wrapper for a recognized parent relation (manyToOne property) */
	private ClassWrapper m_parentClass;

	private String m_setMappedByPropertyName;


	final private ClassWrapper m_classWrapper;

	private RelationType m_relationType = RelationType.none;

	private ExtraType m_extraType;

	private DbColumn m_column;

	private String m_javaColumnName;

	private FieldDeclaration m_fieldDeclaration;

	private MethodDeclaration	m_setter;

	private MethodDeclaration	m_getter;

	private VariableDeclarator m_variableDeclaration;

	private Type m_propertyType;

	private String m_propertyName;

	private List<String> m_valueSet;

	private boolean m_transient;

	private boolean m_setPrecisionField;

	private boolean m_setScaleField;

	private boolean m_setLengthField;

	private boolean m_new;

	private ColumnWrapper m_parentListProperty;

	@Nullable
	private ClassWrapper m_compoundPkWrapper;

	public ColumnWrapper(ClassWrapper cw) {
		m_classWrapper = cw;
	}

	public ColumnWrapper(ClassWrapper cw, DbColumn column) {
		m_classWrapper = cw;
		m_column = column;
		m_javaColumnName = column.getName();
	}

	private AbstractGenerator g() {
		return m_classWrapper.g();
	}

	/**
	 * T if this has source code.
	 * @return
	 */
	public boolean hasSource() {
		return m_fieldDeclaration != null || m_getter != null || m_setter != null;
	}

	public void setFieldDeclarator(FieldDeclaration d, VariableDeclarator vd) {
		m_fieldDeclaration = d;
		m_variableDeclaration = vd;

	}

	public String getJavaColumnName() {
		return m_javaColumnName;
	}

	public void setJavaColumnName(String columnName) {
		m_javaColumnName = columnName;
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
			throw new IllegalStateException(this + ": cannot set null type");
		m_propertyType = propertyType;
	}

	public Type getPropertyType() {
		return m_propertyType;
	}

	public void setPropertyName(String calculatedPropertyName) {
		m_propertyName = calculatedPropertyName;
	}

	public String getPropertyName() {
		return m_propertyName;
	}

	public DbColumn getColumn() {
		return m_column;
	}

	public void setColumn(DbColumn column) {
		m_column = column;
		m_javaColumnName = column.getName();
	}

	public ColumnType getType() {
		return m_type;
	}

	public void setType(ColumnType type) {
		m_type = type;
	}

	/**
	 * Returns T if this is a singular PK field for the object.
	 * @return
	 */
	public boolean isPrimaryKey() {
		if(null == m_column)
			return false;
		DbPrimaryKey primaryKey = m_column.getTable().getPrimaryKey();
		if(null == primaryKey)
			return false;
		return primaryKey.getColumnList().contains(m_column) && primaryKey.getColumnList().size() == 1;
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
		String fk = getConfigProperty("fk");
		if(null != fk) {
			generateFakeRelation(fk);
			return;
		}

		if(column.getName().endsWith("id") && column.getName().length() > 2 && !ispk) {
			boolean warned = false;
			String name = column.getName();
			name = name.substring(0, name.length() - 2);
			while(name.endsWith("_"))
				name = name.substring(0, name.length() - 1);

			//-- Can we find a table with the remaining field name?
			if(name.length() > 0) {
				DbTable possibleTbl = g().findTableByNames(null, name);
				if(null != possibleTbl) {
					setConfigProperty("fk", possibleTbl.getName());
					g().warning(this +": column ending in 'id' but not a foreign key - probably points to table " + possibleTbl);
					warned = true;
				}
			}
			if(! warned)
				g().warning(this +": column ending in 'id' but not a foreign key?");
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
				if(column.getPrecision() <= g().getEnumMaxFieldSize() && ! ispk) {
					if(calculateDistinctValues(dbc, false))
						return;
				}
				break;

			case Types.BIGINT:
			case Types.INTEGER:
			case Types.DECIMAL:
				if(column.getPrecision() < g().getEnumMaxFieldSize() && ! ispk) {
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
		boolean nullable = column.isNullable(); // || ispk;		pk is fixed later.

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
					setPropertyType(new ClassOrInterfaceType("java.math.BigDecimal"));
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
						setPropertyType(new ClassOrInterfaceType("java.math.BigInteger"));
					}
				}
				m_setPrecisionField = true;
				m_setScaleField = true;
				return true;

			case Types.VARCHAR:
			case Types.LONGVARCHAR:
			case Types.CHAR:
			case Types.NVARCHAR:
			case Types.NCHAR:
				m_setLengthField = true;
				setPropertyType(new ClassOrInterfaceType("String"));
				return true;

			case Types.DATE:
			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE:
				setPropertyType(new ClassOrInterfaceType("java.util.Date"));

				m_extraType = sqltype == Types.DATE ? ExtraType.TemporalDate : ExtraType.TemporalTimestamp;
				return true;

			case Types.BOOLEAN:
			case Types.BIT:
				assignBooleanType(null);
				return true;
		}
	}

	private void generateFakeRelation(String fk) throws Exception {
		DbTable parent = g().findTableByNames(null, fk);
		if(null == parent) {
			g().error(this + ": user specified FK specifies unknown table '" + fk + "'");
			return;
		}

		ClassWrapper parentClass = g().findClassByTable(parent);
		if(null == parentClass) {
			g().error("Cannot locate class source file for " + parent + ", mapping as non-relation");
			calculateBasicType();
			return;
		}

		ClassOrInterfaceType referent = new ClassOrInterfaceType(parentClass.getClassName());
		setPropertyType(referent);
		setManyToOne(parentClass);
	}

	/**
	 * Try to get the type for the related object.
	 * @param parentRelation
	 */
	private void generateRelationType(DbRelation parentRelation) throws Exception {
		DbTable parent = parentRelation.getParent();
		ClassWrapper parentClass = g().findClassByTable(parent);
		if(null == parentClass) {
			g().error("Cannot locate class source file for " + parent + ", mapping as non-relation");
			calculateBasicType();
			return;
		}

		ClassOrInterfaceType referent = new ClassOrInterfaceType(parentClass.getClassName());
		setPropertyType(referent);
		setManyToOne(parentClass);
	}

	public void setManyToOne(ClassWrapper parentClass) {
		setRelationType(RelationType.manyToOne);
		m_parentClass = parentClass;
	}
	public void setManyToOne() {
		setRelationType(RelationType.manyToOne);
	}


	public void setOneToMany(String mappedBy) {
		setRelationType(RelationType.oneToMany);
		m_setMappedByPropertyName = mappedBy;
	}

	public void setOneToMany(ColumnWrapper childProperty) {
		if(null == childProperty)
			throw new IllegalStateException();
		setRelationType(RelationType.oneToMany);
		m_setMappedByPropertyName = childProperty.getPropertyName();
		m_childsParentProperty = childProperty;
	}


	public void recalculatePropertyNameFromParentRelation() {
		String name = m_column.getName();
		String parentClassName = m_parentClass.getSimpleName();

		//-- 1. Splittable name?
		List<String> segs = AbstractGenerator.splitName(m_column.getName());
		if(segs.size() > 1) {
			for(int i = segs.size(); --i >= 0;) {
				if(segs.get(i).equalsIgnoreCase("id") || segs.get(i).equalsIgnoreCase(parentClassName)) {
					segs.remove(i);
				}
			}

			segs.addAll(AbstractGenerator.splitName(parentClassName));
			setPropertyName(AbstractGenerator.camelCase(segs));
			return;
		}

		//-- Not splittable. Remove any "id" before or after
		if(name.toLowerCase().startsWith("id"))
			name = name.substring(2);
		if(name.toLowerCase().endsWith("id"))
			name = name.substring(0, name.length() - 2);

		int pana = name.toLowerCase().indexOf(parentClassName.toLowerCase());
		if(pana >= 0) {
			name = name.substring(0, pana) + name.substring(pana + parentClassName.length());
		}
		setPropertyName(AbstractGenerator.camelCase(name));
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
			//-- Got a reasonably-sized list of results... Create an enum?
			if(codeenum) {
				return false;
			}

			if(res.size() < 2) {
				if(g().isMapOneCharVarcharToBoolean() && m_column.getPrecision() == 1) {
					assignBooleanType(ExtraType.YesNo);
					return true;
				}
				return false;
			}

			//-- Check for boolean type values
			if(res.size() == 2) {
				if(VALID_BOOLEAN_CHAR_SET.contains(res.get(0).toLowerCase()) && VALID_BOOLEAN_CHAR_SET.contains(res.get(1).toLowerCase())) {
					ExtraType xt = null;
					if(YESNOSET.contains(res.get(0).toLowerCase()) && YESNOSET.contains(res.get(1).toLowerCase())) {
						xt = ExtraType.YesNo;
					} else if(TRUEFALSESET.contains(res.get(0).toLowerCase()) && TRUEFALSESET.contains(res.get(1).toLowerCase())) {
						xt = ExtraType.TrueFalse;
					} else if(ONEZEROSET.contains(res.get(0).toLowerCase()) && ONEZEROSET.contains(res.get(1).toLowerCase())) {
						xt = ExtraType.OneZero;
					}
					assignBooleanType(xt);
					setValueSet(res);
					return true;
				}
			}

			//-- asnumber is only used to calculate booleans, so bail out
			if(asnumber) {
				return false;
			}

			if(! codeenum) {
				ClassWrapper enumWrapper = generateNormalEnum(res);
				ClassOrInterfaceType referent = new ClassOrInterfaceType(enumWrapper.getClassName());
				setPropertyType(referent);
				m_extraType = ExtraType.EnumeratedString;
				return true;
			}

			g().error(this + " should generate an enum with " + res.size() + " values");

			return false;
		}
	}

	/**
	 * Generate the enum unless it already exists.
	 * @param values
	 */
	private ClassWrapper generateNormalEnum(List<String> values) {
		String name = m_classWrapper.getSimpleName() + AbstractGenerator.capitalizeFirst(getPropertyName());
		ClassWrapper enumWrapper = g().findClassWrapper(m_classWrapper.getPackageName(), name);
		if(null != enumWrapper) {
			return enumWrapper;
		}

		//--
		enumWrapper = g().createEnumWrapper(m_classWrapper.getPackageName(), name);
		EnumDeclaration et = enumWrapper.getEnumType();

		for(String value : values) {
			EnumConstantDeclaration ec = et.addEnumConstant(value);
		}
		return enumWrapper;
	}

	/**
	 * Generate the enum unless it already exists.
	 * @param values
	 */
	private ClassWrapper generateCodeEnum(List<String> values) {
		String name = m_classWrapper.getSimpleName() + AbstractGenerator.capitalizeFirst(getPropertyName());
		ClassWrapper enumWrapper = g().findClassWrapper(m_classWrapper.getPackageName(), name);
		if(null != enumWrapper) {
			return enumWrapper;
		}

		//--
		enumWrapper = g().createEnumWrapper(m_classWrapper.getPackageName(), name);
		EnumDeclaration et = enumWrapper.getEnumType();

		for(String value : values) {
			EnumConstantDeclaration ec = et.addEnumConstant(AbstractGenerator.camelCase(value));
			ec.setJavadocComment("Enum value for '" + value + "'");
		}
		return enumWrapper;
	}


	private static boolean isYes(String s) {
		if(s == null)
			return false;
		if(s.equals("true"))
			return true;
		return s.toLowerCase().startsWith("y");
	}

	private void assignBooleanType(ExtraType extra) {
		//-- Make boolean.
		if(m_column.isNullable()) {
			String fnn = getConfigProperty("forceNotNull");
			if(isYes(fnn)) {
				switch(extra) {
					default:
						break;

					case YesNo:
						extra = ExtraType.YesNoPrimitive;
						break;

					case OneZero:
						extra = ExtraType.OneZeroPrimitive;
						break;

					case TrueFalse:
						extra = ExtraType.TrueFalsePrimitive;
						break;
				}

				setPropertyType(new PrimitiveType(Primitive.BOOLEAN));
			} else {
				setPropertyType(new ClassOrInterfaceType("Boolean"));
			}
		} else {
			setPropertyType(new PrimitiveType(Primitive.BOOLEAN));
		}

		//-- Apply the default boolean type annotation
		m_extraType = extra;

		//-- If the property name starts with "is" then remove that,
		String name = getPropertyName();
		if(name.startsWith("is") && name.length() > 2) {
			name = name.substring(2);
			if(Character.isUpperCase(name.charAt(0)) && name.length() > 1 && ! Character.isUpperCase(name.charAt(1))) {
				name = name.substring(0, 1).toLowerCase() + name.substring(1);
			}
			setPropertyName(name);
		}
	}

	/**
	 * For this ManyToOne property, try to locate the inverse OneToMany property.
	 */
	public void locateOrGenerateListProperty() {
		if(getRelationType() != RelationType.manyToOne)
			throw new IllegalStateException();
		ClassWrapper parentClass = m_parentClass;
		if(null == parentClass)
			throw new IllegalStateException(this + ": Missing parent class for ManyToOne property " + this);

		if(m_classWrapper.getSimpleName().equalsIgnoreCase("Definition")) {
			System.out.println("GOTCHA");
		}

		ColumnWrapper cw = parentClass.findPropertyByChildProperty(this);
		if(cw == null) {
			cw = parentClass.createListProperty(this);
		}
		//System.out.println("1->N: child " + this + " parent list = " + cw);
		m_parentListProperty = cw;
	}



	static private final Set<String> VALID_BOOLEAN_CHAR_SET = new HashSet<>(Arrays.asList("y", "n", "t", "f", "true", "false", "0", "1"));
	static private final Set<String> TRUEFALSESET = new HashSet<>(Arrays.asList("t", "f", "true", "false"));
	static private final Set<String> ONEZEROSET = new HashSet<>(Arrays.asList("0", "1"));

	static private final Set<String> YESNOSET = new HashSet<>(Arrays.asList("y", "n"));


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

	public void setTransient(boolean aTransient) {
		m_transient = aTransient;
	}

	public boolean isTransient() {
		return m_transient;
	}

	public void renderConstant() {
		String fieldName = "p" + getPropertyName().toUpperCase();
		FieldDeclaration fd = m_classWrapper.getRootType().addField(new ClassOrInterfaceType("String"), fieldName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
		VariableDeclarator vd = fd.getVariables().get(0);
		vd.setInitializer(new StringLiteralExpr(getPropertyName()));
	}

	public void renderField() {
		FieldDeclaration fd = getFieldDeclaration();
		String fieldPrefix = g().getFieldPrefix();

		String propertyName = getPropertyName();
		if(null == propertyName) {
			System.out.println("??");
		} else if(propertyName.equalsIgnoreCase("DefinitionProductpartlist")) {
			System.out.println("GOTCHA");
		}

		if(fd == null) {
			Type type = getPropertyType();
			type = importIf(type);
			//g().info(this+ ": new field " + type);

			String fieldName = fieldPrefix == null ? getPropertyName() : fieldPrefix + getPropertyName();

			fd = m_classWrapper.getRootType().addField(type, fieldName, Modifier.PRIVATE);
			setFieldDeclaration(fd);
			VariableDeclarator vd = fd.getVariables().get(0);
			setVariableDeclaration(vd);

			if(m_relationType == RelationType.oneToMany) {
				vd.setInitializer("new ArrayList<>()");
				importIf("java.util.ArrayList");
			}
		} else {
			//if(g().isForceRenameFields() && getRelationType() == RelationType.none) {
			//	String baseFieldName = ClassWrapper.calculatePropertyNameFromColumnName(getJavaColumnName());
			//	if(null != fieldPrefix) {
			//		baseFieldName = fieldPrefix + baseFieldName;
			//	}
			//
			//	String s = getVariableDeclaration().getName().asString();
			//	if(! s.equals(baseFieldName)) {
			//		getVariableDeclaration().setName(baseFieldName);
			//	}
			//}
		}
	}

	private Type importIf(Type type) {
		return m_classWrapper.importIf(type);
	}

	private void importIf(String name) {
		m_classWrapper.importIf(name);
	}

	public void renderSetter() {
		String prefix = "set";
		Type propertyType = getPropertyType();
		String methodName = prefix + AbstractGenerator.capitalizeFirst(getPropertyName());

		MethodDeclaration setter = getSetter();
		if(null == setter) {
			EnumSet<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
			propertyType = importIf(propertyType);
			setter = new MethodDeclaration(modifiers, new VoidType(), methodName);
			setter.setModifiers(modifiers);

			Parameter param = new Parameter(propertyType, "value");
			setter.addParameter(param);

			ClassOrInterfaceDeclaration rootType = m_classWrapper.getRootType();
			rootType.addMember(setter);

			BlockStmt block = new BlockStmt();
			setter.setBody(block);

			AssignExpr ax = new AssignExpr(new NameExpr(m_variableDeclaration.getName().asString()), new NameExpr("value"), Operator.ASSIGN);
			block.addStatement(ax);
			setSetter(setter);

			//// add a statement do the method body
			//NameExpr clazz = new NameExpr("System");
			//FieldAccessExpr field = new FieldAccessExpr(clazz, "out");
			//MethodCallExpr call = new MethodCallExpr(field, "println");
			//call.addArgument(new StringLiteralExpr("Hello World!"));
			//block.addStatement(call);
		} else if(g().isForceRenameMethods()) {
			setter.setName(new SimpleName(methodName));
		}
	}


	public void renderGetter() throws Exception {
		if(getPropertyName().equals("type")) {
			System.out.println("GOTCHA");
		}

		MethodDeclaration getter = renderGetterMethod();

		if(getType() == ColumnType.compoundKey) {
			//-- Compound keys have no column, and have an EmbeddableId annotation
			createOrFindMarkerAnnotation(getter, "javax.persistence.EmbeddedId");
		} else if(getRelationType() == RelationType.manyToOne) {
			renderManyToOneAnnotations(getter);
		} else if(getRelationType() == RelationType.oneToMany) {
			NormalAnnotationExpr ca = createOrFindAnnotation(getter, "javax.persistence.OneToMany");
			importIf("javax.persistence.FetchType");
			addPairIfMissing(ca, "fetch", "FetchType.LAZY");
			ColumnWrapper cpp = m_childsParentProperty;
			if(null == cpp)
				throw new IllegalStateException(this + ": child's property (mappedBy) not set");
			setPair(ca, "mappedBy", cpp.getPropertyName(), true);
			//setPair(ca, "fetch", "FetchType.LAZY", false);

		} else {
			if(m_column == null)
				throw new IllegalStateException(this + ": no column?");

			if(isPrimaryKey()) {
				renderIdAnnotations(getter);
			}

			/*
			 * Normal column annotation
			 */
			NormalAnnotationExpr ca = createOrFindAnnotation(getter, "javax.persistence.Column");
			setPair(ca, "name", m_column.getName(), true);
			if(m_setLengthField && m_column.getPrecision() > 0) {
				setPair(ca, "length", Integer.toString(m_column.getPrecision()), false);
			}
			if(m_setPrecisionField && m_column.getPrecision() > 0) {
				setPair(ca, "precision", Integer.toString(m_column.getPrecision()), false);
			}
			if(m_setScaleField && m_column.getScale() > 0) {
				setPair(ca, "scale", Integer.toString(m_column.getScale()), false);
			}
			setPair(ca, "nullable", Boolean.toString(m_column.isNullable()), false);
		}

		renderExtraTypeAnnotations(getter);
	}

	/**
	 * Render the id annotation and anything related to it.
	 * @param getter
	 */
	private void renderIdAnnotations(MethodDeclaration getter) throws Exception {
		if(g().isAddIdentifyable()) {
			createOrFindMarkerAnnotation(getter, "java.lang.Override");
		}

		Boolean aic = m_column.isAutoIncrement();
		if(aic != null && aic.booleanValue()) {
			if(g().isReplaceSerialWithSequence()) {
				String idSequence = g().getIdColumnSequence(m_column);
				if(null != idSequence) {
					//-- Render a sequence: @SequenceGenerator(name = "sq", sequenceName = "definition.definitioncomment_id_seq")
					NormalAnnotationExpr ca = createOrFindAnnotation(getter, "javax.persistence.SequenceGenerator");
					addPairIfMissing(ca, "name", "\"sq\"");
					setPair(ca, "sequenceName", idSequence, true);

					//-- And its reference: @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq")
					ca = createOrFindAnnotation(getter, "javax.persistence.GeneratedValue");
					importIf("javax.persistence.GenerationType");
					addPairIfMissing(ca, "strategy", "GenerationType.SEQUENCE");
					addPairIfMissing(ca, "generator", "\"sq\"");
					renderIdAnnotation(getter);
					return;
				}
			}

			NormalAnnotationExpr ca = createOrFindAnnotation(getter, "javax.persistence.GeneratedValue");
			MemberValuePair p = findAnnotationPair(ca, "strategy");
			if(null == p) {
				importIf("javax.persistence.GenerationType");
				ca.addPair("strategy", "GenerationType.IDENTITY");
			}
			renderIdAnnotation(getter);
			return;
		}

		g().warning(this + ": no identifier generator known");
		renderIdAnnotation(getter);
	}

	private void setPair(NormalAnnotationExpr ca, String name, String value, boolean quoted) {
		MemberValuePair p = findAnnotationPair(ca, name);
		if(null == p) {
			ca.addPair(name, quoted ? "\"" + value + "\"" : value);
		} else {
			p.setValue(quoted ? new StringLiteralExpr(value) : new NameExpr(value));
		}
	}

	private void addPairIfMissing(NormalAnnotationExpr ca, String name, String value) {
		MemberValuePair pair = findAnnotationPair(ca, name);
		if(null != pair)
			return;
		ca.addPair(name, value);
	}

	private void renderIdAnnotation(MethodDeclaration getter) {
		createOrFindMarkerAnnotation(getter, "javax.persistence.Id");
	}

	private void renderExtraTypeAnnotations(MethodDeclaration getter) {
		ExtraType extraType = getExtraType();
		if(null == extraType) {
			return;
		}

		String hibernateType = getTypeNameFor(extraType);
		if(null != hibernateType) {
			NormalAnnotationExpr type = createOrFindAnnotation(getter, "org.hibernate.annotations.Type");
			MemberValuePair typeValue = findAnnotationPair(type, "type");
			if(null == typeValue) {
				type.addPair("type", "\"" + hibernateType + "\"");
			}
		} else {
			switch(extraType) {
				default:
					break;

				case EnumeratedString:
					NormalAnnotationExpr na = createOrFindAnnotation(getter, "javax.persistence.Enumerated");
					if(findAnnotationPair(na, "value") == null) {
						importIf("javax.persistence.EnumType");
						na.addPair("value", "EnumType.STRING");
					}
					break;

				case TemporalDate:
					na = createOrFindAnnotation(getter, "javax.persistence.Temporal");
					if(findAnnotationPair(na, "value") == null) {
						importIf("javax.persistence.TemporalType");
						na.addPair("value", "TemporalType.DATE");
					}
					break;
				case TemporalTimestamp:
					na = createOrFindAnnotation(getter, "javax.persistence.Temporal");
					if(findAnnotationPair(na, "value") == null) {
						importIf("javax.persistence.TemporalType");
						na.addPair("value", "TemporalType.TIMESTAMP");
					}
					break;
			}
		}
	}

	private void renderManyToOneAnnotations(MethodDeclaration getter) {
		/*
		 * We need JoinColumn and ManyToOne
		 */
		NormalAnnotationExpr m2o = createOrFindAnnotation(getter, "javax.persistence.ManyToOne");
		MemberValuePair fetch = findAnnotationPair(m2o, "fetch");
		if(null == fetch) {
			importIf("javax.persistence.FetchType");
			m2o.addPair("fetch", "FetchType.LAZY");
		}

		MemberValuePair p = findAnnotationPair(m2o, "optional");
		if(null == p) {
			m2o.addPair("optional", Boolean.toString(m_column.isNullable()));
		} else {
			p.setValue(new BooleanLiteralExpr(m_column.isNullable()));
		}

		NormalAnnotationExpr na = createOrFindAnnotation(getter, "javax.persistence.JoinColumn");
		p = findAnnotationPair(na, "name");
		if(null == p) {
			na.addPair("name", "\"" + m_column.getName() + "\"");
		} else {
			p.setValue(new StringLiteralExpr(m_column.getName()));
		}
	}

	/**
	 * Render the get method only.
	 * @return
	 */
	private MethodDeclaration renderGetterMethod() {
		String prefix = "get";
		Type propertyType = getPropertyType();
		if(propertyType.asString().equalsIgnoreCase("boolean")) {
			prefix = "is";
		}
		String getterName = prefix + AbstractGenerator.capitalizeFirst(getPropertyName());
		MethodDeclaration getter = getGetter();
		if(null == getter) {
			EnumSet<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
			propertyType = importIf(propertyType);
			getter = new MethodDeclaration(modifiers, propertyType, getterName);
			getter.setModifiers(modifiers);
			ClassOrInterfaceDeclaration rootType = m_classWrapper.getRootType();
			rootType.addMember(getter);

			BlockStmt block = new BlockStmt();
			getter.setBody(block);

			//FieldAccessExpr field = new FieldAccessExpr(new ThisExpr(), getVariableDeclaration().getName().asString());
			//ReturnStmt rs = new ReturnStmt(field);
			ReturnStmt rs = new ReturnStmt(new NameExpr(getVariableDeclaration().getName().asString()));

			block.addStatement(rs);
			setGetter(getter);

			//// add a statement do the method body
			//NameExpr clazz = new NameExpr("System");
			//FieldAccessExpr field = new FieldAccessExpr(clazz, "out");
			//MethodCallExpr call = new MethodCallExpr(field, "println");
			//call.addArgument(new StringLiteralExpr("Hello World!"));
			//block.addStatement(call);
		} else if(g().isForceRenameMethods()) {
			getter.setName(new SimpleName(getterName));
		}
		return getter;
	}

	static private String getTypeNameFor(ExtraType type) {
		switch(type) {
			default:
				return null;

			case YesNoPrimitive:
				return "to.etc.domui.hibernate.types.BooleanPrimitiveYNType";

			case TrueFalsePrimitive:
				return "to.etc.domui.hibernate.types.BooleanPrimitiveTrueFalseType";

			case OneZeroPrimitive:
				return "to.etc.domui.hibernate.types.BooleanPrimitiveOneZeroType";

			case OneZero:
				return "org.hibernate.type.NumericBooleanType";

			case TrueFalse:
				return "true_false";

			case YesNo:
				return "yes_no";
		}
	}

	static protected MemberValuePair findAnnotationPair(NormalAnnotationExpr nx, String name) {
		return ClassWrapper.findAnnotationPair(nx, name);
	}

	protected NormalAnnotationExpr createOrFindAnnotation(BodyDeclaration<?> getter, String fullAnnotationName) {
		return m_classWrapper.createOrFindAnnotation(getter, fullAnnotationName);
	}

	protected MarkerAnnotationExpr createOrFindMarkerAnnotation(BodyDeclaration<?> getter, String fullAnnotationName) {
		return m_classWrapper.createOrFindMarkerAnnotation(getter, fullAnnotationName);
	}


	public void setNew(boolean aNew) {
		m_new = aNew;
	}

	public boolean isNew() {
		return m_new;
	}

	public RelationType getRelationType() {
		return m_relationType;
	}

	private void setRelationType(RelationType relationType) {
		m_relationType = relationType;
	}

	/** The parent class wrapper for a recognized parent relation (manyToOne property) */
	public ClassWrapper getParentClass() {
		return m_parentClass;
	}

	public void setParentClass(ClassWrapper parentClass) {
		m_parentClass = parentClass;
	}

	public ExtraType getExtraType() {
		return m_extraType;
	}

	public void setSetMappedByPropertyName(String setMappedByPropertyName) {
		m_setMappedByPropertyName = setMappedByPropertyName;
	}

	public String getSetMappedByPropertyName() {
		return m_setMappedByPropertyName;
	}

	public ClassWrapper getClassWrapper() {
		return m_classWrapper;
	}

	public void setChildsParentProperty(ColumnWrapper childsParentProperty) {
		m_childsParentProperty = childsParentProperty;
	}

	public ColumnWrapper getChildsParentProperty() {
		return m_childsParentProperty;
	}

	void resolveMappedBy() {
		String mappedBy = getSetMappedByPropertyName();
		if(null == mappedBy)
			return;

		if("numberlist".equalsIgnoreCase(mappedBy)) {
			System.out.println("GOTCHA");
		}
		//System.out.println("MAPPEDBY: " + cw + " to " + mappedBy);

		//-- Find the class referred to
		Type propertyType = getPropertyType();
		if(propertyType instanceof ClassOrInterfaceType) {
			ClassOrInterfaceType ct = (ClassOrInterfaceType) propertyType;

			String s = ct.getName().asString();
			if("List".equals(s)) {
				Type containerType = ct.getTypeArguments().get().get(0);
				String childName = containerType.asString();
				childName = m_classWrapper.tryResolveFullName(childName);

				ClassWrapper childClass = g().findClassWrapper(m_classWrapper.getPackageName(), childName);
				if(null == childClass) {
					m_classWrapper.error(this + ": cannot locate child class " + childName);
					return;
				}

				ColumnWrapper childColumn = childClass.findColumnByPropertyName(mappedBy);
				if(null != childColumn) {
					setChildsParentProperty(childColumn);
				} else {
					childColumn = childClass.findDeletedProperty(mappedBy);
					if(null == childColumn) {
						m_classWrapper.error(this + ": cannot find mappedBy property '" + mappedBy + "' in child class " + childClass);
					} else {
						m_classWrapper.info(this  + ": child property '" + mappedBy + "' deleted from " + childClass + ", deleting OneToMany");

						m_classWrapper.deleteColumn(this);
					}
				}
				return;
			}
		}
		m_classWrapper.error(this + ": @OneToMany reference but property type is not correct (List<T>, but it is " + propertyType + ")");
	}

	public void resolveManyToOne() {
		if(getRelationType() != RelationType.manyToOne)
			return;

		ClassOrInterfaceType ct = (ClassOrInterfaceType) getPropertyType();
		String parentClassName = ct.getName().asString();
		parentClassName = m_classWrapper.tryResolveFullName(parentClassName);

		ClassWrapper parentClass = g().findClassWrapper(m_classWrapper.getPackageName(), parentClassName);
		if(null == parentClass) {
			m_classWrapper.error(this + ": cannot locate class " + parentClassName + " inside parsed entities");
			return;
		} else {
			m_parentClass = parentClass;
		}
	}

	/**
	 * Called when this property name is a duplicate because the default name is [childClassName]List, and there
	 * are multiple relations to the same child. This recalculates the name by trying to include the child
	 * property name in the name - which will have been made unique by {@link ClassWrapper#calculateRelationNames()}.
	 *
	 */
	public void recalculateOneToManyName() {
		ColumnWrapper childProperty = getChildsParentProperty();
		if(null == childProperty) {
			throw new IllegalStateException(toString() + ": missing oneToMany child prop");
		}
		String childPropertyName = childProperty.getPropertyName();
		String myClass = m_classWrapper.getSimpleName();

		//-- Is my name contained in the child - if so remove that
		int index = childPropertyName.toLowerCase().indexOf(myClass.toLowerCase());
		if(index >= 0) {
			childPropertyName = childPropertyName.substring(0, index) + childPropertyName.substring(index + myClass.length());
		}

		String childClass = childProperty.getClassWrapper().getSimpleName();
		setPropertyName(childPropertyName + childClass + "List");
	}

	public void changePropertyType(ClassOrInterfaceType newType) {
		VariableDeclarator vd = getVariableDeclaration();
		if(null != vd) {
			vd.setType(newType);
		}

		MethodDeclaration getter = getGetter();
		if(null != getter) {
			getter.setType(newType);
		}

		MethodDeclaration setter = getSetter();
		if(null != setter) {
			setter.getParameter(0).setType(newType);
		}
		m_propertyType = newType;
	}

	public void renameFieldName() {
		String fieldPrefix = g().getFieldPrefix();

		String propertyName = getPropertyName();
		String fieldName = propertyName;
		if(null != fieldPrefix) {
			fieldName = fieldPrefix + propertyName;
		}

		VariableDeclarator vd = getVariableDeclaration();
		if(null == vd)
			return;

		//if(m_classWrapper.getTable().getName().equals("definition")) {
		//	System.out.println("GOTCHA");
		//}

		String oldName = vd.getName().asString();
		if(! oldName.equals(fieldName)) {
			vd.setName(fieldName);

			//-- We need to rename inside the getter and setter too
			MethodDeclaration getter = getGetter();
			GetterFieldRenamingVisitor fieldVisitor = new GetterFieldRenamingVisitor(oldName, fieldName);
			if(null != getter) {
				getter.accept(fieldVisitor, null);
			}

			MethodDeclaration setter = getSetter();
			if(null != setter) {
				setter.accept(fieldVisitor, null);
			}

			m_classWrapper.getRootType().accept(new VoidVisitorAdapter<Void>() {
				@Override public void visit(ConstructorDeclaration n, Void arg) {
					n.accept(fieldVisitor, null);
				}
			}, null);
		}
	}

	/**
	 * Finds return and assignment expressions to the old field name, and replaces
	 * them by the new field name.
	 */
	private class GetterFieldRenamingVisitor extends VoidVisitorAdapter<Void> {
		private final String m_oldName;

		private final String m_fieldName;

		public GetterFieldRenamingVisitor(String oldName, String fieldName) {
			m_oldName = oldName;
			m_fieldName = fieldName;
		}

		private boolean isOldFieldName(String name) {
			return name.equals(m_oldName) || name.equals("this." + m_oldName);
		}

		@Override public void visit(AssignExpr n, Void arg) {
			if(n.getOperator().equals(Operator.ASSIGN)) {
				Expression target = n.getTarget();
				if(isOldFieldName(target.toString())) {
					n.setTarget(new NameExpr(m_fieldName));
				}

			}
			super.visit(n, arg);
		}

		@Override public void visit(ReturnStmt n, Void arg) {
			Optional<Expression> expression = n.getExpression();
			if(expression.isPresent()) {
				Expression xp = expression.get();
				if(isOldFieldName(xp.toString())) {
					n.setExpression(new NameExpr(m_fieldName));
				}
			}
		}
	}


	public Node getConfigNode() {
		return m_classWrapper.getColumnConfig(this);
	}

	public String getConfigProperty(String property) {
		return m_classWrapper.getColumnConfigProperty(this, property);
	}

	public void setConfigProperty(String property, String value) {
		Node tc = getConfigNode();
		if(null == tc)
			return;
		String v = DomTools.strAttr(tc, property, null);
		if(v == null || v.length() == 0 || v.startsWith("*")) {
			DomTools.setAttr(tc, property, "*" + value);
		}
	}

	public void generateConfig() {
		String propertyName = getPropertyName();
		setConfigProperty("property", propertyName);
	}

	public boolean isNumeric() {
		Type propertyType = getPropertyType();
		String s = propertyType.asString();
		s = s.substring(0, s.lastIndexOf('.') + 1);
		return s.equals("int")
			|| s.equals("short")
			|| s.equals("byte")
			|| s.equals("long")
			|| s.equals("float")
			|| s.equals("double")
			|| s.equals("BigDecimal")
			|| s.equals("BigInteger")
			|| s.equals("Integer")
			|| s.equals("Short")
			|| s.equals("Byte")
			|| s.equals("Long")
			|| s.equals("Float")
			|| s.equals("Double")
			;
	}

}

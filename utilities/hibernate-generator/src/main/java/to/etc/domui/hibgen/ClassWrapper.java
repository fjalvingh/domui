package to.etc.domui.hibgen;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.w3c.dom.Node;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbPrimaryKey;
import to.etc.dbutil.schema.DbSchema;
import to.etc.dbutil.schema.DbTable;
import to.etc.domui.hibgen.ColumnWrapper.ColumnType;
import to.etc.domui.hibgen.ColumnWrapper.RelationType;
import to.etc.util.FileTool;
import to.etc.util.LineIterator;
import to.etc.webapp.query.IIdentifyable;
import to.etc.xml.DomTools;

import javax.annotation.Nullable;
import java.beans.Introspector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-9-17.
 */
class ClassWrapper {
	private final AbstractGenerator m_generator;

	private final boolean m_isNew;

	private final File m_file;

	private final String m_fullClassName;

	private final String m_simpleName;

	//private final ClassOrInterfaceDeclaration m_rootType;
	private final TypeDeclaration<?> m_rootType;

	private CompilationUnit m_unit;

	private int m_errors;

	private ClassWrapperType m_type = ClassWrapperType.unknown;

	private DbTable m_table;

	///** Mapped by lowercase property name */
	//private Map<String, ColumnWrapper> m_byPropNameMap = new HashMap<>();
	//
	//private Map<String, ColumnWrapper> m_byColNameMap = new HashMap<>();

	private List<ColumnWrapper> m_allColumnWrappers = new ArrayList<>();

	private List<ColumnWrapper> m_deletedColumns = new ArrayList<>();

	private Map<String, SortedProperties> m_propertyByKeyMap = new HashMap<>();

	private boolean m_primaryKeyRecalculated;

	@Nullable
	private ColumnWrapper m_primaryKey;

	@Nullable
	private ClassWrapper m_primaryKeyEmbeddable;

	//private boolean m_baseClass;

	/** When set this class should use the specified base class as it's base class. */
	@Nullable
	private ClassWrapper m_useBaseClass;

	/** Mostly for embedded classes, this marks when it is really used. */
	private boolean m_used;

	public ClassWrapper(AbstractGenerator generator, File file, CompilationUnit unit) {
		m_generator = generator;
		m_file = file;
		m_unit = unit;
		String name = m_file.getName();
		m_simpleName = name.substring(0, name.indexOf("."));                // Strip any suffix
		m_fullClassName = calculateClassName();

		ClassOrInterfaceDeclaration rootType = null;
		Optional<ClassOrInterfaceDeclaration> o = m_unit.getClassByName(getSimpleName());
		if(! o.isPresent()) {
			error("Cannot locate class type");
		} else {
			rootType = o.get();
		}
		m_rootType = rootType;
		m_isNew = false;
	}

	//public ClassWrapper(AbstractGenerator generator, String packageName, String className, CompilationUnit cu, DbTable tbl) {
	//	m_generator = generator;
	//	m_simpleName = className;
	//	m_fullClassName = packageName + "." + className;
	//	m_unit = cu;
	//	m_file = null;
	//	m_table = tbl;
	//	m_isNew = true;
	//
	//	ClassOrInterfaceDeclaration rootType = null;
	//	Optional<ClassOrInterfaceDeclaration> o = m_unit.getClassByName(getSimpleName());
	//	if(! o.isPresent()) {
	//		error("Cannot locate class type");
	//	} else {
	//		rootType = o.get();
	//	}
	//	m_rootType = rootType;
	//	m_type = ClassWrapperType.tableClass;
	//}

	private ClassWrapper(AbstractGenerator generator, String packageName, String className, CompilationUnit cu, TypeDeclaration<?> rootType) {
		m_generator = generator;
		m_simpleName = className;
		m_fullClassName = packageName + "." + className;
		m_unit = cu;
		m_file = null;
		m_isNew = true;
		m_type = ClassWrapperType.unknown;
		m_rootType = rootType;
	}

	static ClassWrapper create(AbstractGenerator g, String packageName, String className) {
		CompilationUnit cu = new CompilationUnit();
		cu.setPackageDeclaration(new PackageDeclaration(Name.parse(packageName)));

		// create the type declaration
		ClassOrInterfaceDeclaration type = cu.addClass(className);
		return new ClassWrapper(g, packageName, className, cu, type);
	}

	static ClassWrapper createEnum(AbstractGenerator g, String packageName, String className) {
		CompilationUnit cu = new CompilationUnit();
		cu.setPackageDeclaration(new PackageDeclaration(Name.parse(packageName)));

		// create the type declaration
		EnumDeclaration type = cu.addEnum(className);
		ClassWrapper cw = new ClassWrapper(g, packageName, className, cu, type);
		cw.setType(ClassWrapperType.enumClass);
		return cw;
	}

	public ClassOrInterfaceDeclaration getRootType() {
		if(m_rootType instanceof ClassOrInterfaceDeclaration)
			return (ClassOrInterfaceDeclaration) m_rootType;
		throw new IllegalStateException("Not a class");
	}

	public EnumDeclaration getEnumType() {
		if(m_rootType instanceof EnumDeclaration)
			return (EnumDeclaration) m_rootType;
		throw new IllegalStateException("Not an enum");
	}

	protected void setTable(DbTable table) {
		m_table = table;
		m_type = ClassWrapperType.tableClass;
	}

	protected void setType(ClassWrapperType type) {
		if(m_type != ClassWrapperType.unknown && type != ClassWrapperType.deleted)
			throw new IllegalStateException(this + ": trying to change class type from " + m_type + " to " + type);
		m_type = type;
	}

	void error(String msg) {
		m_errors++;
		m_generator.error(getOutputFile(), msg);
	}

	void info(String msg) {
		m_generator.info(getOutputFile(), msg);
	}

	@Nullable
	public ColumnWrapper getPrimaryKey() {
		if(m_table == null)
			throw new IllegalStateException(this + ": no table");
		DbPrimaryKey primaryKey = m_table.getPrimaryKey();
		if(primaryKey == null)
			return null;

		if(primaryKey.getColumnList().size() != 1)
			return null;
		return findColumnByColumnName(primaryKey.getColumnList().get(0).getName());
	}

	private File getOutputFile() {
		File base = m_generator.getOutputDirectory();
		if(null == base)
			base = m_generator.getSourceDirectory();
		String name = m_fullClassName.replace('.', File.separatorChar) + ".java";
		return new File(base, name);
	}

	public String calculateClassName() {
		String pkg = m_unit.getPackageDeclaration().get().getName().asString();
		String name = m_file.getName();
		name = name.substring(0, name.indexOf("."));                // Strip .java
		return pkg + "." + name;
	}

	public String getClassName() {
		return m_fullClassName;
	}

	public String getSimpleName() {
		return m_simpleName;
	}

	public String getPackageName() {
		int pos = m_fullClassName.lastIndexOf('.');
		if(pos < 0)
			return "";
		return m_fullClassName.substring(0, pos);
	}

	public boolean isUsed() {
		return m_used;
	}

	public void markUsed() {
		m_used = true;
	}

	public List<ColumnWrapper> getColumnWrappers() {
		return m_allColumnWrappers;
	}

	/**
	 * If this is a wrapper for a Table - this returns that table.
	 * @return
	 */
	@Nullable
	public DbTable getTable() {
		return m_table;
	}

	public void removePropertyNameConstants() {
		for(BodyDeclaration<?> d : new ArrayList<>(m_rootType.getMembers())) {
			if(d instanceof FieldDeclaration) {
				removePropertyNameConstant((FieldDeclaration) d);
			}
		}
	}

	private void removePropertyNameConstant(FieldDeclaration d) {
		List<VariableDeclarator> list = new ArrayList<>();
		for(VariableDeclarator vd : d.getVariables()) {
			String name = vd.getName().asString();
			if(name.startsWith("p") && name.substring(1).toUpperCase().equals(name.substring(1))) {
				//-- Got one
				if(d.getModifiers().contains(Modifier.FINAL) && d.getModifiers().contains(Modifier.STATIC) && d.getModifiers().contains(Modifier.PUBLIC)) {
					list.add(vd);
				}
			}
		}

		for(VariableDeclarator vd : list) {
			d.getVariables().remove(vd);
		}
		if(d.getVariables().size() == 0) {
			d.remove();
		}
	}

	/**
	 * Scan the class and try to find its mapped table or other related data.
	 */
	public void scanAndRegister() {
		TypeDeclaration<?> rootType = m_rootType;
		if(null == rootType)
			return;

		//-- We need to find the annotations that map the table.
		for(AnnotationExpr annotationExpr : rootType.getAnnotations()) {
			String name = annotationExpr.getName().asString();
			if("Table".equals(name)) {
				//m_type = ClassWrapperType.tableClass;
				handleTableAnnotation(annotationExpr);
			} else if("MappedSuperclass".equals(name)) {
				m_type = ClassWrapperType.baseClass;
			} else if("Embeddable".equals(name)) {
				m_type = ClassWrapperType.embeddableClass;
			}
		}

		//-- Find everything that seems to belong to some column in the database
		for(BodyDeclaration<?> d : m_rootType.getMembers()) {
			if(d instanceof FieldDeclaration) {
				handleFieldDeclaration((FieldDeclaration) d);
			} else if(d instanceof MethodDeclaration) {
				MethodDeclaration md = (MethodDeclaration) d;
				handleMethodDeclaration(md);
			}
		}
	}

	private void handleMethodDeclaration(MethodDeclaration md) {
		String name = md.getName().asString();

		//-- We're only interested in getter/setters of a property.
		Type type;
		String propertyName;
		boolean isSetter = false;
		if(name.startsWith("get") || name.startsWith("is")) {
			if(md.getParameters().size() != 0)
				return;
			type = md.getType();
			if(type.equals(new VoidType()))
				return;
			int len = name.startsWith("is") ? 2 : 3;
			propertyName = name.substring(len);
		} else if(name.startsWith("set")) {
			if(md.getParameters().size() != 1)
				return;
			type = md.getParameter(0).getType();
			isSetter = true;
			propertyName = name.substring(3);
		} else
			return;

		if("opentopublic".equalsIgnoreCase(propertyName)) {
			System.out.println("GOTCHA");
		}

		//-- Decode a property name
		ColumnWrapper cw = findColumnByPropertyName(propertyName);
		if(null == cw) {
			cw = new ColumnWrapper(this);
			m_allColumnWrappers.add(cw);
			cw.setPropertyName(Introspector.decapitalize(propertyName));
		}
		cw.setPropertyType(type);
		if(isSetter)
			cw.setSetter(md);
		else
			cw.setGetter(md);

		for(AnnotationExpr annotationExpr : md.getAnnotations()) {
			handleDatabaseAnnotation(cw, annotationExpr);
		}
	}

	@Nullable
	protected ColumnWrapper findColumnByPropertyName(String name) {
		for(ColumnWrapper cw : m_allColumnWrappers) {
			String propertyName = cw.getPropertyName();
			if(null != propertyName) {
				if(propertyName.equalsIgnoreCase(name)) {
					return cw;
				}
			}
		}
		return null;
	}

	@Nullable
	protected ColumnWrapper findDeletedProperty(String name) {
		for(ColumnWrapper cw : m_deletedColumns) {
			String propertyName = cw.getPropertyName();
			if(null != propertyName) {
				if(propertyName.equalsIgnoreCase(name)) {
					return cw;
				}
			}
		}
		return null;
	}

	private void handleFieldDeclaration(FieldDeclaration d) {
		List<ColumnWrapper> list = new ArrayList<>();
		for(VariableDeclarator vd : d.getVariables()) {
			String fieldName = vd.getName().asString();
			String fieldPrefix = m_generator.getFieldPrefix();
			if(null != fieldPrefix) {
				if(fieldName.startsWith(fieldPrefix)) {
					fieldName = fieldName.substring(fieldPrefix.length());
				}
			}

			if(d.getModifiers().contains(Modifier.STATIC) || d.getModifiers().contains(Modifier.FINAL)) {
				//- Skip
			} else {
				if("opentopublic".equals(fieldName)) {
					System.out.println("GOTCHA");
				}
				ColumnWrapper cw = findColumnByPropertyName(fieldName);
				if(null == cw) {
					cw = new ColumnWrapper(this);
					m_allColumnWrappers.add(cw);
					cw.setPropertyName(fieldName);
				}
				cw.setFieldDeclarator(d, vd);
				list.add(cw);
				cw.setPropertyType(vd.getType());
			}
		}

		for(AnnotationExpr annotationExpr : d.getAnnotations()) {
			for(ColumnWrapper columnWrapper : list) {
				handleDatabaseAnnotation(columnWrapper, annotationExpr);
			}
		}
	}

	private void handleDatabaseAnnotation(ColumnWrapper columnWrapper, AnnotationExpr ax) {
		if("type".equalsIgnoreCase(columnWrapper.getPropertyName())) {
			System.out.println("GOTCHA");
		}
		if("DefinitionSubscriptionindicator".equalsIgnoreCase(getSimpleName())) {
			System.out.println("GOTCHA");
		}

		String name = ax.getName().asString();

		if(name.equals("Transient")) {
			columnWrapper.setTransient(true);
			return;
		} else if("Id".equals(name)) {
			if(m_primaryKey != null)
				error("Duplicate @Id annotation");
			m_primaryKey = columnWrapper;
			return;
		} else if("EmbeddedId".equals(name)) {
			if(m_primaryKey != null)
				error("Duplicate @Id annotation");
			m_primaryKey = columnWrapper;
			columnWrapper.setType(ColumnType.compoundKey);
			return;
		}

		if(ax instanceof NormalAnnotationExpr) {
			NormalAnnotationExpr annotationExpr = (NormalAnnotationExpr) ax;

			if(name.equals("Column")) {
				String columnName = null;
				int length = -1;
				for(MemberValuePair pair : annotationExpr.getPairs()) {
					String prop = pair.getName().asString();
					if(prop.equals("name")) {
						columnName = resolveConstant(pair.getValue());
					} else if(prop.equals("length")) {
						length = resolveInt(pair.getValue());
					}
				}

				if(columnName != null && columnName.length() > 0) {
					//m_byColNameMap.put(columnName.toLowerCase(), columnWrapper);
					columnWrapper.setJavaColumnName(columnName);
				}
			} else if(name.equals("JoinColumn")) {
				String columnName = null;
				for(MemberValuePair pair : annotationExpr.getPairs()) {
					String prop = pair.getName().asString();
					if(prop.equals("name")) {
						columnName = resolveConstant(pair.getValue());
					}
				}

				if(columnName != null && columnName.length() > 0) {
					//m_byColNameMap.put(columnName.toLowerCase(), columnWrapper);
					columnWrapper.setJavaColumnName(columnName);
				}
			} else if(name.equals("OneToMany")) {
				String mappedBy = null;
				int length = -1;
				for(MemberValuePair pair : annotationExpr.getPairs()) {
					String prop = pair.getName().asString();
					if(prop.equals("mappedBy")) {
						mappedBy = resolveConstant(pair.getValue());
					}
				}
				if(null != mappedBy) {
					columnWrapper.setOneToMany(mappedBy);
				}
			} else if(name.equals("ManyToOne")) {
				//-- Find the parent class
				Type propertyType = columnWrapper.getPropertyType();

				if(!(propertyType instanceof ClassOrInterfaceType)) {
					error("ManyToOne on primitive");
				} else {
					columnWrapper.setManyToOne();
				}
			}
		}
	}

	private void handleTableAnnotation(AnnotationExpr tableAnn) {
		if(! (tableAnn instanceof NormalAnnotationExpr)) {
			return;
		}

		NormalAnnotationExpr nax = (NormalAnnotationExpr) tableAnn;
		String tableName = null;
		String schemaName = null;
		for(MemberValuePair pair : nax.getPairs()) {
			if(pair.getName().asString().equals("name")) {
				tableName = resolveConstant(pair.getValue());
			} else if(pair.getName().asString().equals("schema")) {
				schemaName = resolveConstant(pair.getValue());
			}
		}
		if(null == tableName)
			return;

		//-- Try to resolve table
		DbTable table = m_generator.findTableByNames(schemaName, tableName);
		if(null == table) {
			error("Database table " + tableName + " in schema " + schemaName + " not found");
		} else {
			m_table = table;
			m_type = ClassWrapperType.tableClass;
		}
	}

	private String resolveConstant(Expression value) {
		if(value == null)
			return null;
		if(value instanceof StringLiteralExpr) {
			return ((StringLiteralExpr) value).asString();
		}
		error("Cannot get constant value for " + value);
		return null;
	}

	private int resolveInt(Expression value) {
		if(value == null)
			return -1;
		if(value instanceof IntegerLiteralExpr) {
			return ((IntegerLiteralExpr) value).asInt();
		}
		error("Cannot get integer constant value for " + value);
		return -1;
	}

	/**
	 * Make sure that all DbColumns have a ColumnWrapper.
	 */
	public void matchColumns() {
		DbTable table = m_table;
		if(table == null)
			return;

		//-- Check all properties not added as columns: without @Column annotation
		for(ColumnWrapper cw : m_allColumnWrappers) { // byPropName
			if(cw.isTransient())
				continue;
			if("numberlistid".equals(cw.getJavaColumnName())) {
				System.out.println("GOTCHA");
			}

			if(cw.getColumn() == null) {
				//-- First try by @Column/@JoinColumn name annotation
				String javaName = cw.getJavaColumnName();
				if(javaName != null) {
					DbColumn column = m_table.findColumn(javaName.toLowerCase());
					if(null != column) {
						cw.setColumn(column);
					}
				} else {
					DbColumn column = m_table.findColumn(cw.getPropertyName().toLowerCase());
					if(null != column) {
						cw.setColumn(column);
						//m_byColNameMap.put(column.getName().toLowerCase(), cw);
					}
				}
			}
		}

		//-- 2. Create NEW wrappers for all columns that do not have one, yet
		for(DbColumn dbColumn : m_table.getColumnList()) {
			String overrideName = getColumnConfigProperty(dbColumn, "property");		// Create config property

			ColumnWrapper cw = findColumnByColumnName(dbColumn.getName());
			if(cw == null) {
				cw = new ColumnWrapper(this, dbColumn);
				m_allColumnWrappers.add(cw);
				cw.setNew(true);
				String propertyName = overrideName == null ? calculatePropertyNameFromColumnName(dbColumn.getName()) : overrideName;

				//m_byPropNameMap.put(propertyName.toLowerCase(), cw);
				cw.setPropertyName(propertyName);
			}

			//List<String> strings = AbstractGenerator.splitName(dbColumn.getName());
			//StringBuilder sb = new StringBuilder();
			//sb.append(strings.remove(0).toLowerCase());
			//strings.forEach(seg -> sb.append(AbstractGenerator.capitalize(seg)));

			cw.setColumn(dbColumn);
		}
	}

	@Nullable
	private ColumnWrapper findColumnByColumnName(String name) {
		for(ColumnWrapper cw : m_allColumnWrappers) {
			DbColumn column = cw.getColumn();
			if(column != null) {
				if(name.equalsIgnoreCase(column.getName())) {
					return cw;
				}
			}
			if(name.equals(cw.getJavaColumnName())) {
				return cw;
			}
		}
		return null;
	}


	/**
	 * Remove all non-transient properties that have no column associated with it (meaning they have been deleted).
	 */
	public void removeUnusedProperties() {
		DbTable table = m_table;
		if(table == null)
			return;

		//-- 1. Find all properties referring to table columns that no longer exist.
		Set<String> columnNameSet = table.getColumnList().stream().map(c -> c.getName().toLowerCase()).collect(Collectors.toSet());

		List<ColumnWrapper> deleteList = new ArrayList<>();

		for(ColumnWrapper cw : m_allColumnWrappers) {
			if(cw.getPropertyName().equals("fullName")) {
				System.out.println("GOTCHA");
			}

			if(! cw.isTransient()) {
				if(cw.getColumn() == null && cw.getRelationType() != RelationType.oneToMany) {
					if(cw != m_primaryKey) {					// Embeddable pk's should remain, please.
						deleteList.add(cw);
					}
				}
			}
		}

		for(ColumnWrapper cw : deleteList) {
			g().info(getClassName() + ": column " + cw.getJavaColumnName() + " deleted, deleting property " + cw.getPropertyName());

			deleteColumn(cw);
		}
	}

	/**
	 * Render all basic table properties.
	 */
	public void renderProperties() throws Exception {
		if(getType() == ClassWrapperType.embeddableClass) {
			if(! isUsed()) {
				//-- Unused embeddable: do not generate anything, just add a javadoc remark to the class.
				markClassJavadoc("This class is no longer used");
				createOrFindMarkerAnnotation(getRootType(), "java.lang.Deprecated");
				return;
			}
		}
		renderClassAnnotations();

		if("PdimetaConfDatavaultSatelliteSsmId".equalsIgnoreCase(getSimpleName())) {
			System.out.println("GOTCHA");
		}

		for(ColumnWrapper cw : m_allColumnWrappers) {
			if("DefinitionProductpartlist".equalsIgnoreCase(cw.getPropertyName())) {
				System.out.println("GOTCHA");
			}

			renderColumnProperty(cw);
			renderPropertyNls(cw.getPropertyName());
		}
	}

	private void markClassJavadoc(String msg) {
		StringBuilder sb = new StringBuilder();
		if(m_rootType.getJavadoc().isPresent()) {
			String line = m_rootType.getJavadoc().get().toText();
			int count = 0;
			for(String s : new LineIterator(line)) {
				if((s.trim().length() > 0 && ! s.trim().startsWith("*") && count > 0) || msg == null) {
					sb.append(" * <b>WARNING</b> ").append(msg).append("\n");
					msg = null;
				}
				sb.append(s).append("\n");
				count++;
			}
			if(msg != null) {
				sb.append(" * <b>WARNING</b> ").append(msg).append("\n");
			}


		} else {
			sb.append("\n * ").append("<h1>WARNING</h1>\n")
				.append(" * This class seems to be no longer used by Entity classes.\n")
				.append(" *\n")
			;
		}
		m_rootType.setJavadocComment(sb.toString());
	}

	private void renderClassAnnotations() {
		ClassOrInterfaceDeclaration rootType = getRootType();
		createOrFindMarkerAnnotation(rootType, "javax.persistence.Entity");
		DbTable table = m_table;
		if(null != table) {
			NormalAnnotationExpr a = createOrFindAnnotation(rootType, "javax.persistence.Table");
			setPair(a, "name", table.getName(), true);
			DbSchema schema = table.getSchema();
			if(g().isAppendSchemaNameInAnnotations()) {
				setPair(a, "schema", schema.getName(), true);
			}
		}
	}

	void deleteColumn(ColumnWrapper cw) {
		FieldDeclaration fieldDeclaration = cw.getFieldDeclaration();
		if(fieldDeclaration != null) {
			if(fieldDeclaration.getVariables().size() == 1) {
				fieldDeclaration.remove();
			} else {
				cw.getVariableDeclaration().remove();
			}
		}

		MethodDeclaration getter = cw.getGetter();
		if(null != getter) {
			getter.remove();
		}

		MethodDeclaration setter = cw.getSetter();
		if(null != setter) {
			setter.remove();
		}

		if(m_allColumnWrappers.remove(cw)) {
			m_deletedColumns.add(cw);
		}

	}

	private void renderColumnProperty(ColumnWrapper dbColumn) throws Exception {
		if(dbColumn.getPropertyType() == null) {
			error(dbColumn + ": unknown type '" + dbColumn.getColumn().getTypeString() + "' (" + dbColumn.getColumn().getSqlType() + "), not generated");
			return;
		}
		if(dbColumn.isTransient()) {
			return;
		}

		dbColumn.renderConstant();
		dbColumn.renderField();
		dbColumn.renderGetter();
		dbColumn.renderSetter();
	}

	private FieldDeclaration	findFieldDeclaration(String baseName) {
		for(BodyDeclaration<?> d : m_rootType.getMembers()) {
			if(d instanceof FieldDeclaration) {
				FieldDeclaration fd = (FieldDeclaration) d;

				for(VariableDeclarator vd : fd.getVariables()) {
					if(vd.getName().asString().equalsIgnoreCase(baseName)
						|| vd.getName().asString().equalsIgnoreCase("m_" + baseName)
						)
						return fd;
				}
			}
		}
		return null;
	}

	static String calculatePropertyNameFromColumnName(String columnName) {
		String s = AbstractGenerator.camelCase(columnName);
		s = AbstractGenerator.fixCamelCase(s);
		return s;
	}

	private String calculateMethodName(String get, String name) {
		List<String> strings = AbstractGenerator.splitName(name);
		StringBuilder sb = new StringBuilder();
		sb.append(get);
		strings.forEach(a -> sb.append(AbstractGenerator.capitalize(a)));
		return sb.toString();
	}

	/**
	 * Compare method names and orders them as follows:
	 * <ul>
	 *     <li>Try to keep the "id" property on top</li>
	 *     <li>Order getters/setters alphabetically by property name</li>
	 *     <li>Keep getters and setters together, with getter before setter</li>
	 * </ul>
	 */
	static private int compareName(String a, String b) {
		if(isIdName(a)) {
			if(isIdName(b)) {
				return a.compareTo(b);
			} else {
				return -1;
			}
		} else if(isIdName(b)) {
			return 1;
		} else if(isGetOrSet(a)) {
			if(isGetOrSet(b)) {
				String aname = propName(a);
				String bname = propName(b);
				int res = aname.compareToIgnoreCase(bname);
				if(res != 0) {
					return res;
				}
				return a.compareToIgnoreCase(b);
			} else {
				return -1;
			}
		} else if(isGetOrSet(b)) {
			return 1;
		} else {
			return a.compareToIgnoreCase(b);
		}
	}


	static private boolean isGetOrSet(String name) {
		return name.startsWith("get") || name.startsWith("set") || name.startsWith("is") || name.startsWith("has") || name.startsWith("can");
	}

	static private String propName(String name) {
		if(name.startsWith("get") || name.startsWith("set") || name.startsWith("has") || name.startsWith("can")) {
			return name.substring(3);
		} else if(name.startsWith("is")) {
			return name.substring(2);
		} else {
			return name;
		}
	}

	static private boolean isIdName(String name) {
		return name.equalsIgnoreCase("id") || name.equalsIgnoreCase("m_id") || name.equalsIgnoreCase("getid") || name.equalsIgnoreCase("setid");
	}

	/**
	 * Sort fields before methods; fields by ascending name/; methods by "property name" if getter/setter, or just
	 * alphabetically if not. The net effect is that getters/setters for a property are kept together and
	 * are ordered by property name.
	 * The exception is the ID property which is always sorted first.
	 */
	public void order() {
		NodeList<BodyDeclaration<?>> members = m_rootType.getMembers();
		members.sort((a, b) -> {
			if(a instanceof FieldDeclaration) {
				if(b instanceof FieldDeclaration) {
					FieldDeclaration fa = (FieldDeclaration) a;
					FieldDeclaration fb = (FieldDeclaration) b;
					return compareName(fa.getVariables().get(0).getName().asString(), fb.getVariables().get(0).getName().asString());
				} else {
					return -1;			// field < method
				}
			} else if(b instanceof FieldDeclaration) {
				return 1;
			}

			if(a instanceof MethodDeclaration) {
				if(b instanceof MethodDeclaration) {
					MethodDeclaration fa = (MethodDeclaration) a;
					MethodDeclaration fb = (MethodDeclaration) b;
					return compareName(fa.getName().asString(), fb.getName().asString());
				} else
					return 1;
			} else if(b instanceof MethodDeclaration) {
				return -1;
			}
			return 0;
		});
	}

	public void print() throws IOException {
		File outputFile = getOutputFile();
		outputFile.getParentFile().mkdirs();

		backupTarget(outputFile);

		try(Writer w = new FileWriter(outputFile)) {
			w.write(m_unit.toString());
		}
	}

	private void backupTarget(File outputFile) throws IOException {
		String newName = outputFile.getName() + ".old";
		File backupFile = new File(outputFile.getParentFile(), newName);
		if(backupFile.exists())
			return;

		if(! outputFile.exists()) {
			backupFile.createNewFile();
		} else if(!outputFile.renameTo(backupFile)) {
			throw new RuntimeException("cannot rename " + outputFile + " to " + backupFile);
		}
	}

	static protected MemberValuePair findAnnotationPair(NormalAnnotationExpr nx, String name) {
		for(MemberValuePair mvp : nx.getPairs()) {
			if(mvp.getName().asString().equals(name)) {
				return mvp;
			}
		}
		return null;
	}

	protected NormalAnnotationExpr createOrFindAnnotation(BodyDeclaration<?> getter, String fullAnnotationName) {
		String name = AbstractGenerator.finalName(fullAnnotationName);
		//importIf(fullAnnotationName);

		for(AnnotationExpr annotationExpr : getter.getAnnotations()) {
			String annName = annotationExpr.getName().asString();
			if(annName.equals(fullAnnotationName) || name.equals(annName)) {
				return (NormalAnnotationExpr) annotationExpr;
			}
		}

		importIf(fullAnnotationName);
		String pkg = AbstractGenerator.packageName(fullAnnotationName);
		NodeList<MemberValuePair> nodes = NodeList.nodeList();
		//Name nm = new Name(new Name(pkg), name);
		NormalAnnotationExpr ax = new NormalAnnotationExpr(new Name(name), nodes);
		getter.addAnnotation(ax);
		return ax;
	}

	@Nullable
	protected NormalAnnotationExpr findAnnotation(BodyDeclaration<?> method, String fullAnnotationName) {
		String name = AbstractGenerator.finalName(fullAnnotationName);
		//importIf(fullAnnotationName);

		for(AnnotationExpr annotationExpr : method.getAnnotations()) {
			String annName = annotationExpr.getName().asString();
			if(annName.equals(fullAnnotationName) || name.equals(annName)) {
				return (NormalAnnotationExpr) annotationExpr;
			}
		}
		return null;
	}

	protected MarkerAnnotationExpr createOrFindMarkerAnnotation(BodyDeclaration<?> getter, String fullAnnotationName) {
		String name = AbstractGenerator.finalName(fullAnnotationName);
		importIf(fullAnnotationName);
		//getUnit().addImport(fullAnnotationName);

		for(AnnotationExpr annotationExpr : getter.getAnnotations()) {
			String annName = annotationExpr.getName().asString();
			if(annName.equals(fullAnnotationName) || name.equals(annName)) {
				return (MarkerAnnotationExpr) annotationExpr;
			}
		}

		String pkg = AbstractGenerator.packageName(fullAnnotationName);
		NodeList<MemberValuePair> nodes = NodeList.nodeList();
		//Name nm = new Name(new Name(pkg), name);
		MarkerAnnotationExpr ax = new MarkerAnnotationExpr(new Name(name));
		getter.addAnnotation(ax);
		return ax;
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

	protected Type importIf(Type type) {
		String name;
		if(type instanceof ClassOrInterfaceType) {
			ClassOrInterfaceType ct = (ClassOrInterfaceType) type;
			name = ct.getName().asString();
		} else {
			name = type.asString();
		}
		String s = AbstractGenerator.packageName(name);
		if(s == null) {
			return type;
		}
		if("java.lang".equals(s)) {
			return type;
		}

		//System.out.println(name);
		getUnit().addImport(name);

		ClassOrInterfaceType nw = new ClassOrInterfaceType(AbstractGenerator.finalName(name));
		if(type instanceof ClassOrInterfaceType) {
			ClassOrInterfaceType ct = (ClassOrInterfaceType) type;
			if(ct.getTypeArguments().isPresent()) {
				nw.setTypeArguments(ct.getTypeArguments().get());
			}
		}
		return nw;
	}

	protected void importIf(String name) {
		String s = AbstractGenerator.packageName(name);
		if(s == null) {
			return;
		}
		if("java.lang".equals(s)) {
			return;
		}
		getUnit().addImport(name);
	}

	/**
	 * For all columns that are "new", calculate a column type.
	 * @param dbc
	 */
	public void calculateColumnTypes(Connection dbc) throws Exception {
		for(ColumnWrapper cw : m_allColumnWrappers) {
			if(cw.isNew()) {
				cw.calculateColumnType(dbc);
			}
		}
	}

	AbstractGenerator g() {
		return m_generator;
	}

	public CompilationUnit getUnit() {
		return m_unit;
	}

	public boolean isNew() {
		return m_isNew;
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getSimpleName());
		DbTable table = m_table;
		if(null != table) {
			sb.append(":").append(table.getSchema().getName()).append(".").append(table.getName());
		}
		if(isNew()) {
			sb.append("(new)");
		}

		return sb.toString();
	}

	static private String calculateClassBasedParentName(String parentClassName) {
		String name = AbstractGenerator.finalName(parentClassName);
		String newName = AbstractGenerator.camelCase(name);
		newName = AbstractGenerator.fixCamelCase(newName);
		return newName;
	}

	/**
	 * Walk all (new) relations, and try to assign a more reasonable property name than the column name.
	 */
	public void calculateRelationNames() {
		if(getSimpleName().equalsIgnoreCase("Definitionnumberlist")) {
			System.out.println("GOTCHA");
		}

		Map<String, List<ColumnWrapper>> dupList = m_allColumnWrappers
			.stream()
			.filter(cw -> cw.isNew() && cw.getRelationType() == RelationType.manyToOne)
			.collect(Collectors.groupingBy(cw -> calculateClassBasedParentName(cw.getParentClass().getClassName()), Collectors.toList()));

		//-- For all properties without duplicates: use the parent class type
		dupList.forEach((name, list) -> {
			if(list.size() == 1) {
				ColumnWrapper w = list.get(0);

				String override = w.getConfigProperty("property");
				w.setPropertyName(override == null ? name : override);
			} else if(list.size() > 1) {
				list.forEach(cw -> {
					String override = cw.getConfigProperty("property");
					if(null == override) {
						cw.recalculatePropertyNameFromParentRelation();
					} else {
						cw.setPropertyName(override);
					}
				});
			}
		});
	}

	/**
	 * When a class has two relations to the same other class then the oneToMany
	 * relations will have gotten the same property name. Fix that here.
	 */
	public void resolveDuplicateOneToManyProperties() {
		if(getSimpleName().equalsIgnoreCase("Definitionnumberlist")) {
			System.out.println("GOTCHA");
		}

		Map<String, List<ColumnWrapper>> dupList = m_allColumnWrappers
			.stream()
			.filter(cw -> cw.getRelationType() == RelationType.oneToMany)
			.collect(Collectors.groupingBy(cw -> cw.getPropertyName(), Collectors.toList()));

		//-- Fix all duplicates
		dupList.forEach((name, list) -> {
			if(list.size() > 1) {
				list.forEach(cw -> cw.recalculateOneToManyName());
			}
		});
	}


	public void renamePrimaryKeys(String pkName) {
		for(ColumnWrapper cw : m_allColumnWrappers) {
			if(cw.isPrimaryKey()) {
				cw.setPropertyName(pkName);
			}
		}
	}

	/**
	 * Walk this class, and find all parent properties. For each parent property, try to find or create
	 * the inverse list property.
	 */
	public void generateOneToManyProperties() {
		for(ColumnWrapper cw : new ArrayList<>(m_allColumnWrappers)) {
			if(cw.isTransient() || cw.getRelationType() != RelationType.manyToOne)
				continue;

			cw.locateOrGenerateListProperty();
		}
	}

	//@Nullable
	//public ColumnWrapper findColumnByMappedBy(ClassWrapper clz, String propertyName) {
	//	for(ColumnWrapper cw : m_allColumnWrappers) {
	//		if(cw.isTransient() || cw.getRelationType() != RelationType.oneToMany)
	//			continue;
	//		if(propertyName.equals(cw.getSetMappedByPropertyName())) {
	//			if(cw.getParentClass())
	//
	//
	//			return cw;
	//		}
	//	}
	//	return null;
	//}

	/**
	 * Find the OneToMany property that refers back (mappedBy) to the target column.
	 * @param target
	 * @return
	 */
	@Nullable
	public ColumnWrapper findPropertyByChildProperty(ColumnWrapper target) {
		for(ColumnWrapper cw : m_allColumnWrappers) {
			if(cw.isTransient() || cw.getRelationType() != RelationType.oneToMany)
				continue;
			if(cw.getChildsParentProperty() == target) {
				return cw;
			}
		}
		return null;
	}


	public ColumnWrapper createColumnWrapper() {
		ColumnWrapper cw = new ColumnWrapper(this);
		m_allColumnWrappers.add(cw);
		return cw;
	}

	public ColumnWrapper createListProperty(ColumnWrapper childsParentProperty) {
		if(getSimpleName().equalsIgnoreCase("EAttribute")) {
			if(childsParentProperty.getClassWrapper().getSimpleName().equalsIgnoreCase("ConfDatamartFact")) {
				System.out.println("GOTCHA");
			}

			System.out.println("GOTCHA");
		}
		ColumnWrapper cw = createColumnWrapper();

		//-- Type is List<T> where T is this-property's type.
		importIf("java.util.List");

		ClassWrapper childClass = childsParentProperty.getClassWrapper();
		ClassOrInterfaceType ct = new ClassOrInterfaceType(childClass.getClassName());
		Type childType = importIf(ct);

		ClassOrInterfaceType lt = new ClassOrInterfaceType(null, new SimpleName("List"), NodeList.nodeList(childType));
		cw.setPropertyType(lt);

		//-- Calculate a simplistic property name provisionally. This step can create duplicate property names.
		String override = childsParentProperty.getConfigProperty("parentName");
		if(null != override) {
			cw.setPropertyName(override);
		} else {
			String childName = childsParentProperty.getClassWrapper().getSimpleName();
			String name = AbstractGenerator.camelCase(childName) + "List";
			cw.setPropertyName(name);
		}
		cw.setOneToMany(childsParentProperty);

		return cw;
	}

	/**
	 * All properties with a mappedBy are resolved to find the property belonging to that mappedBy.
	 */
	public void resolveMappedBy() {
		for(ColumnWrapper cw : new ArrayList<>(m_allColumnWrappers)) {
			cw.resolveMappedBy();
		}
	}

	/**
	 * If the name is dotted this just returns immediately because it's already qualified. If
	 * not this walks all imports trying to find the import that gets this class. When found
	 * it will return the qualified name; if not the name is returned unaltered.
	 */
	String tryResolveFullName(String className) {
		if(className.contains("."))
			return className;

		for(ImportDeclaration id : getUnit().getImports()) {
			String name = id.getName().asString();
			int pos = name.lastIndexOf('.');
			if(pos > 0) {
				if(className.equals(name.substring(pos + 1))) {
					return name;
				}
			}
		}
		return className;
	}

	/**
	 * Try to resolve the ManyToOne class.
	 */
	public void resolveManyToOne() {
		for(ColumnWrapper cw : m_allColumnWrappers) {
			cw.resolveManyToOne();
		}
	}

	public void loadNlsPropertyFiles() throws Exception {
		if(isNew())
			return;
		File file = m_file;
		if(null == file)
			throw new IllegalStateException();
		String baseName = file.getName();
		baseName = baseName.substring(0, baseName.lastIndexOf('.'));		// Strip extension
		File basePath = calculatePropertiesBasePath(file.getParentFile());

		loadPropertyFile(basePath, baseName, "");
		for(String lang : g().getAltBundles()) {
			loadPropertyFile(basePath, baseName, lang);
		}
	}

	private File calculatePropertiesBasePath(File path) {
		String fullPath = path.getAbsolutePath();
		if(fullPath.contains("/src/main/java/")) {
			//-- Maven structure: resources are separate because it's made by idiots.
			return new File(fullPath.replace("/src/main/java/",  "/src/main/resources/"));
		}
		return path;
	}

	private void loadPropertyFile(File basePath, String baseName, String ext) throws Exception {
		String extra = ext.length() == 0 ? "" : "_" + ext;
		baseName = baseName.substring(0, baseName.lastIndexOf('.'));		// Strip extension
		File propertyFile = new File(basePath, baseName + extra + ".properties");
		if(! propertyFile.exists() || ! propertyFile.isFile())
			return;

		SortedProperties sp = new SortedProperties();
		try(Reader r = new InputStreamReader(new FileInputStream(propertyFile), "utf-8")) {
			sp.load(r);
		}

		m_propertyByKeyMap.put(ext, sp);
	}

	void renderPropertyNls(String propertyName) {
		renderPropertyNls("", propertyName);
		g().getAltBundles().forEach(b -> renderPropertyNls(b, propertyName));
	}

	private void renderPropertyNls(String bundle, String propertyName) {
		SortedProperties properties = m_propertyByKeyMap.computeIfAbsent(bundle, b -> new SortedProperties());

		if(properties.get(propertyName + ".label") == null) {
			properties.setProperty(propertyName + ".label", propertyName);
		}

		if(properties.get(propertyName + ".hint") == null) {
			properties.setProperty(propertyName + ".hint", propertyName);
		}
	}

	public void writeNlsPropertyFiles() throws Exception {
		File file = getOutputFile();
		String baseName = file.getName();
		baseName = baseName.substring(0, baseName.lastIndexOf('.'));		// Strip extension

		File basePath = calculatePropertiesBasePath(file.getParentFile());
		for(Entry<String, SortedProperties> e : m_propertyByKeyMap.entrySet()) {
			String ext = e.getKey().length() == 0 ? "" : "_" + e.getKey();

			StringWriter sw = new StringWriter();
			e.getValue().store(sw, "NLS");
			sw.close();

			//-- Remove all comments because they contain an idiotic date causing useless changes
			String txt = sw.getBuffer().toString();
			StringBuilder sb = new StringBuilder();
			for(String line : new LineIterator(txt)) {
				if(line.startsWith("#"))
					continue;
				sb.append(line).append("\n");
			}
			txt = sb.toString();

			File out = new File(basePath, baseName + ext + ".properties");
			out.getParentFile().mkdirs();
			FileTool.writeFileFromString(out, txt, "utf-8");
		}
	}

	public org.w3c.dom.Node getConfig() {
		return g().getTableConfig(m_table);
	}

	public ClassWrapperType getType() {
		return m_type;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Base class handling											*/
	/*----------------------------------------------------------------------*/


	/**
	 * See if this base class matches the columns and properties of the other class. If all
	 * properties of this base class can be found in the other class then we return
	 * true. The properties are matched on column name and property type, not on
	 * property name as those can differ.
	 *
	 * @param other
	 * @return
	 */
	public boolean baseClassMatchesTable(ClassWrapper other) {
		if(m_type != ClassWrapperType.baseClass)
			throw new IllegalStateException(this + ": should be a base class");

		for(ColumnWrapper cw : m_allColumnWrappers) {
			if(cw.isTransient())
				continue;
			String columnName = cw.getJavaColumnName();
			if(null == columnName) {
				columnName = cw.getPropertyName();			// If there is no column name the name of the column is, by definition, the property name
			}

			ColumnWrapper otherColumn = other.findColumnByColumnName(columnName);
			if(null == otherColumn)
				return false;
			if(! g().isMatchBaseClassesOnColumnNameOnly()) {
				if(! Objects.equals(cw.getPropertyType(), otherColumn.getPropertyType())) {
					//-- Try classname resolution
					String myQN = tryResolveFullName(cw.getPropertyType().asString());
					String otherQN = other.tryResolveFullName(otherColumn.getPropertyType().asString());
					if(! myQN.equals(otherQN)) {
						if(g().isVerbose()) {
							System.out.println("baseClassMatch: " + other.getSimpleName()
								+  " does not match base class " + getSimpleName()
								+ " because column " + columnName
								+ " has different types: " + myQN + " and " + otherQN
							);
						}
						return false;
					}
				}
			}
		}
		return true;
	}

	public void assignBaseClass(ClassWrapper baseClass) {
		m_useBaseClass = baseClass;
	}

	public boolean isBaseClassColumn(ColumnWrapper cw) {
		ClassWrapper useBaseClass = m_useBaseClass;
		if(useBaseClass == null)
			return false;
		String columnName = cw.getJavaColumnName();
		if(null == columnName) {
			columnName = cw.getPropertyName();			// If there is no column name the name of the column is, by definition, the property name
		}
		return useBaseClass.findColumnByColumnName(columnName) != null;
	}

	/**
	 * This adjusts the class's definition. If the class extends a base class then the "extends baseclass" will be added,
	 * and if the identifyable option is set it also adds IIdentifyable&lt;T>.
	 */
	public void handleClassDefinition() {
		ClassWrapper useBaseClass = m_useBaseClass;
		ClassOrInterfaceDeclaration rootType = getRootType();
		if(useBaseClass != null) {
			ClassOrInterfaceType baseClass = new ClassOrInterfaceType(useBaseClass.getClassName());
			baseClass = (ClassOrInterfaceType) importIf(baseClass);

			if(! rootType.getExtendedTypes().contains(baseClass)) {
				rootType.getExtendedTypes().add(baseClass);
			}
		}

		ColumnWrapper primaryKey = getPrimaryKey();
		if(g().isAddIdentifyable() && null != primaryKey) {
			ClassOrInterfaceType iident = new ClassOrInterfaceType(IIdentifyable.class.getCanonicalName());
			iident.setTypeArguments(NodeList.nodeList(primaryKey.getPropertyType()));

			iident = (ClassOrInterfaceType) importIf(iident);
			if(! rootType.getImplementedTypes().contains(iident)) {
				rootType.getImplementedTypes().add(iident);
			}
		}
	}

	public void removeBaseClassColumns() {
		List<ColumnWrapper> list = new ArrayList<>();

		for(ColumnWrapper cw : m_allColumnWrappers) {
			if(isBaseClassColumn(cw)) {
				list.add(cw);
			}
		}

		list.forEach(w -> deleteColumn(w));
	}


	/**
	 * If the PK for this class is a primitive then fix its type to become a wrapper.
	 */
	public void fixPkNullity() {
		if(getSimpleName().equalsIgnoreCase("DefinitionSubscriptionindicator")) {
			System.out.println("GOTCHA");
		}


		ColumnWrapper primaryKey = getPrimaryKey();
		if(null == primaryKey)
			return;

		Type type = primaryKey.getPropertyType();
		if(! (type instanceof PrimitiveType)) {
			return;
		}

		//-- If the PK was coming from existing Java code, report an error.
		if(! primaryKey.isNew())
			error("primary key is primitive type, this is not allowed. Changing it to become a wrapper type.");
		PrimitiveType ptype = (PrimitiveType) type;

		ClassOrInterfaceType newType;
		if(type.asString().equals("int") && g().isForcePkToLong()) {
			newType = new ClassOrInterfaceType("Long");
		} else {
			newType = ptype.toBoxedType();
		}
		primaryKey.changePropertyType(newType);
	}

	public void renameFieldName() {
		m_allColumnWrappers.forEach(w -> w.renameFieldName());
	}

	/**
	 * Delete all constructors.
	 */
	public void destroyConstructors() {
		List<ConstructorDeclaration> list = new ArrayList<>();

		getRootType().accept(new VoidVisitorAdapter<Void>() {
			@Override public void visit(ConstructorDeclaration n, Void arg) {
				list.add(n);
			}
		}, null);
		list.forEach(n -> n.remove());

	}

	/**
	 * Check to see whether this class requires or has a complex primary key.
	 */
	public void checkAssignComplexPK() {
		if(m_primaryKeyRecalculated)
			return;
		if("StgManagementSourceTable".equalsIgnoreCase(getSimpleName())) {
			System.out.println("GOTCHA");
		}

		DbTable table = m_table;
		if(null == table)
			return;

		List<ColumnWrapper> list = null;
		DbPrimaryKey primaryKey = table.getPrimaryKey();
		if(null == primaryKey) {
			if(m_primaryKey == null) {
				error("No primary key defined in the database nor in existing code. The code generated for this table will not work.");
				return;
			} else {
				error("No primary key defined in the database. Using the PK as defined in the code - PROBLEMS ARE ON YOUR HEAD....");
			}
		} else {
			if(primaryKey.getColumnList().size() == 1) {
				String fieldName = primaryKey.getColumnList().get(0).getName();
				ColumnWrapper column = findColumnByColumnName(fieldName);
				if(null == column) {
					error("Cannot locate property for primary key column " + fieldName);
					return;
				}
				m_primaryKey = column;
				return;
			}

			//-- We need a compound key from all of the columns that are part of the PK
			list = new ArrayList<>();
			for(DbColumn dbColumn : primaryKey.getColumnList()) {
				ColumnWrapper property = findColumnByColumnName(dbColumn.getName());
				if(property == null) {
					error("Cannot locate property for primary key column " + dbColumn.getName());
					return;
				}
				list.add(property);
			}
		}

		ColumnWrapper pkProperty = m_primaryKey;
		ClassWrapper pkWrapper;
		if(null == pkProperty) {
			//-- we need to create a new class and a new property.
			String pkClassName = getSimpleName() + "Id";
			pkWrapper = g().findClassWrapper(ClassWrapperType.embeddableClass, getPackageName(), pkClassName);
			if(null == pkWrapper) {
				pkWrapper = g().createWrapper(getPackageName(), pkClassName);
				pkWrapper.setType(ClassWrapperType.embeddableClass);

				pkWrapper.getRootType().addImplementedType(Serializable.class);
			}

			pkProperty = new ColumnWrapper(this);
			pkProperty.setPropertyName("id");
			pkProperty.setJavaColumnName("id");
			pkProperty.setNew(true);
			pkProperty.setPropertyType(new ClassOrInterfaceType(pkWrapper.getClassName()));
			pkProperty.setType(ColumnType.compoundKey);
			m_primaryKey = pkProperty;
			m_allColumnWrappers.add(pkProperty);
		} else {
			pkWrapper = g().findClassWrapper(ClassWrapperType.embeddableClass, getPackageName(), pkProperty.getPropertyType().asString());
			if(null == pkWrapper) {
				throw new IllegalStateException(this + ": cannot locate embeddedClass for compound primary key: " + pkProperty.getPropertyType().asString());
			}

			if(primaryKey == null) {
				//-- Try to assign columns for the wrapper from its own definition (no reverse engineering from the db)
				for(ColumnWrapper cw : new ArrayList<>(pkWrapper.getColumnWrappers())) {
					String name = cw.getJavaColumnName();
					if(null != name) {
						DbColumn column = m_table.findColumn(name);
						if(null != column) {
							cw.setColumn(column);
						}

						ColumnWrapper myColumn = findColumnByColumnName(name);
						if(null != myColumn)
							deleteColumn(myColumn);
					}
				}
			}
		}
		pkWrapper.markUsed();

		if(pkWrapper.getType() != ClassWrapperType.embeddableClass) {
			throw new IllegalStateException(this+ ": compound pk class " + pkWrapper + " is not embeddable");
		}

		//-- Handle column assignments for the complex typething
		if(null != list) {										// Only if an actual PK was found
			pkWrapper.assignPkColumnProperties(list);
			m_allColumnWrappers.removeAll(list);
		}

		m_primaryKeyRecalculated = true;
	}

	/**
	 * For an embedded class, merge the properties passed as the properties for the ID class.
	 * @param list
	 */
	private void assignPkColumnProperties(List<ColumnWrapper> list) {
		Set<ColumnWrapper> deleteSet = new HashSet<>(m_allColumnWrappers);

		boolean changed = false;
		for(ColumnWrapper expCw : list) {
			ColumnWrapper cn = findColumnByColumnName(expCw.getColumn().getName());
			if(null == cn) {
				cn = new ColumnWrapper(this);
				m_allColumnWrappers.add(cn);
				cn.setPropertyName(expCw.getPropertyName());
				cn.setType(expCw.getType());
				cn.setPropertyType(expCw.getPropertyType());
				cn.setJavaColumnName(expCw.getColumn().getName());
				cn.setNew(true);
				cn.setColumn(expCw.getColumn());
				changed = true;
			} else {
				deleteSet.remove(cn);
				if(cn.getColumn() == null) {
					cn.setColumn(expCw.getColumn());
				}
			}
		}

		for(ColumnWrapper cw : deleteSet) {
			deleteColumn(cw);
			changed = true;
		}

		if(changed) {
			regenerateEquals();
			regenerateHashcode();
		}

	}

	private void regenerateEquals() {
		importIf("java.util.Objects");
		EnumSet<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
		MethodDeclaration eq = new MethodDeclaration(modifiers, new PrimitiveType(Primitive.BOOLEAN), "equals");
		eq.setModifiers(modifiers);
		createOrFindMarkerAnnotation(eq, "java.lang.Override");

		Parameter param = new Parameter(new ClassOrInterfaceType("Object"), "o");
		eq.addParameter(param);

		ClassOrInterfaceDeclaration rootType = getRootType();
		rootType.addMember(eq);

		BlockStmt block = new BlockStmt();
		block.addStatement(JavaParser.parseStatement("if(o == this)\nreturn true;"));
		block.addStatement(JavaParser.parseStatement("if(o == null || getClass() != o.getClass())\n\t\t\treturn false;"));
		block.addStatement(JavaParser.parseStatement(getSimpleName() + " that = (" + getSimpleName() + ") o;"));

		StringBuilder sb = new StringBuilder();
		sb.append("return ");
		int count = 0;
		for(ColumnWrapper cw : m_allColumnWrappers) {
			String propName = cw.getPropertyName();
			String fieldPrefix = g().getFieldPrefix();
			if(fieldPrefix != null) {
				propName = fieldPrefix + propName;
			}

			sb.append("\t");
			if(count++ > 0)
				sb.append("&& ");
			sb.append("Objects.equals(").append(propName).append(", that.").append(propName).append(")");
			sb.append("\n");
		}
		sb.append(";");
		System.out.println(sb.toString());
		block.addStatement(JavaParser.parseStatement(sb.toString()));
		eq.setBody(block);
	}

	private void regenerateHashcode() {
		importIf("java.util.Objects");
		EnumSet<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
		MethodDeclaration eq = new MethodDeclaration(modifiers, new PrimitiveType(Primitive.INT), "hashCode");
		eq.setModifiers(modifiers);
		createOrFindMarkerAnnotation(eq, "java.lang.Override");

		ClassOrInterfaceDeclaration rootType = getRootType();
		rootType.addMember(eq);

		BlockStmt block = new BlockStmt();
		eq.setBody(block);

		StringBuilder sb = new StringBuilder();
		sb.append("return Objects.hash(");
		int count = 0;
		for(ColumnWrapper cw : m_allColumnWrappers) {
			String propName = cw.getPropertyName();
			String fieldPrefix = g().getFieldPrefix();
			if(fieldPrefix != null) {
				propName = fieldPrefix + propName;
			}
			if(count++ > 0)
				sb.append(", ");
			sb.append(propName);
		}
		sb.append(");");
		block.addStatement(JavaParser.parseStatement(sb.toString()));
	}

	public void renderEmbeddableAnnotations() {
		ClassOrInterfaceDeclaration rootType = getRootType();
		createOrFindMarkerAnnotation(rootType, "javax.persistence.Embeddable");
	}

	protected Node getTableConfig() {
		DbTable table = m_table;
		if(table == null)
			return null;
		return g().getTableConfig(table);
	}

	public void setTableConfigProperty(String property, String value) {
		DbTable table = m_table;
		if(null != table)
			g().setTableConfigProperty(m_table, property, value);
	}

	protected Node getColumnConfig(String colName) {
		Node tableConfig = getTableConfig();
		org.w3c.dom.NodeList childNodes = tableConfig.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if("col".equals(item.getNodeName())) {
				String name = DomTools.strAttr(item, "columnName");
				if(colName.equals(name)) {
					return item;
				}
			}
		}

		//-- Create it
		Node node = tableConfig.getOwnerDocument().createElement("col");
		tableConfig.appendChild(node);
		Node value = tableConfig.getOwnerDocument().createAttribute("columnName");
		value.setNodeValue(colName);
		node.getAttributes().setNamedItem(value);
		return node;
	}

	@Nullable
	protected Node getColumnConfig(ColumnWrapper cw) {
		String colName = cw.getColumn() == null ? cw.getJavaColumnName() : cw.getColumn().getName();
		if(colName == null)
			return null;
		colName = colName.toLowerCase();
		return getColumnConfig(colName);
	}

	protected String getColumnConfigProperty(DbColumn col, String propertyName) {
		Node cc = getColumnConfig(col.getName());
		return AbstractGenerator.getOrCreateNodeValue(cc, propertyName);
	}

	protected String getColumnConfigProperty(ColumnWrapper cw, String propertyName) {
		Node cc = getColumnConfig(cw);
		return AbstractGenerator.getOrCreateNodeValue(cc, propertyName);
	}

	public void generateConfig() {
		String simpleName = getSimpleName();
		setTableConfigProperty("className", getSimpleName());

		//-- Per column
		m_allColumnWrappers.forEach(cw -> cw.generateConfig());
	}

	public void generateDomUIAnnotations() {
		//-- 1. Do we already have an annotation?
		if(null != findAnnotation(getRootType(), "to.etc.domui.component.meta.MetaObject"))
			return;

		List<ColumnWrapper> displist = new ArrayList<>();
		List<ColumnWrapper> searchlist = new ArrayList<>();

		for(ColumnWrapper cw : m_allColumnWrappers) {
			//-- Ignore all silly names.
			DbColumn column = cw.getColumn();
			if(null != column) {
				if(g().isIgnored(column))
					continue;
				if(cw.isPrimaryKey())
					continue;
				if(cw.getRelationType() == RelationType.none)
					displist.add(cw);

				//-- For search list ignore numeric fields.
				//System.out.println(">>> " + c.propname + ", t=" + c.type + ", fk=" + c.isfk);
				if(cw.getRelationType() == RelationType.manyToOne || ! cw.isNumeric()) {
					searchlist.add(cw);
				}
			}
		}

		//-- Sort the result.
		if(displist.size() == 0)
			return;

		Collections.sort(displist, C_DOMUI_ORDER);
		Collections.sort(searchlist, C_DOMUI_ORDER);

		importIf("to.etc.domui.component.meta.MetaObject");
		importIf("to.etc.domui.component.meta.MetaDisplayProperty");
		importIf("to.etc.domui.component.meta.MetaSearchItem");
		importIf("to.etc.domui.component.meta.SearchPropertyType");

		//-- Generate the annotation. Start with display properties.
		StringBuilder sb = new StringBuilder();
		sb.append("@MetaObject(defaultColumns = { //\n");
		int ct = 0;
		for(ColumnWrapper c : displist) {
			sb.append("\t").append(ct++ != 0 ? ", " : "").append("@MetaDisplayProperty(name = ").append(getSimpleName()).append(".p").append(c.getPropertyName().toUpperCase()).append(") //\n");
		}

		//-- Then do search properties
		if(searchlist.size() > 0) {
			sb.append("\t}, //\n");

			sb.append("\tsearchProperties = { //\n");
			ct = 0;
			for(ColumnWrapper c : searchlist) {
				sb.append("\t").append(ct++ != 0 ? ", " : "").append("@MetaSearchItem(name = ").append(getSimpleName()).append(".p").append(c.getPropertyName().toUpperCase()) //
					.append(", order = ").append(ct) //
					.append(", searchType = SearchPropertyType.BOTH) //\n");
			}
		}

		sb.append("})");
		AnnotationExpr annotationExpr = JavaParser.parseAnnotation(sb.toString());
		getRootType().addAnnotation(annotationExpr);
	}

	static private final Comparator<ColumnWrapper> C_DOMUI_ORDER = new Comparator<ColumnWrapper>() {
		@Override
		public int compare(ColumnWrapper ac, ColumnWrapper bc) {
			String an = ac.getPropertyName();
			String bn = bc.getPropertyName();

			int ao = classifyDomUIName(an);
			int bo = classifyDomUIName(bn);
			if(ao != bo)
				return ao - bo;
			return an.compareToIgnoreCase(bn);
		}
	};

	static private int classifyDomUIName(String n) {
		String t = n.toLowerCase();
		if(t.contains("subtype") || t.contains("subsoort"))
			return 2;
		if(t.contains("type") || t.contains("soort") || t.contains("kind"))
			return 1;
		if(t.contains("name") || t.contains("naam"))
			return 3;
		if(t.contains("omschrijving") || t.contains("description"))
			return 4;
		return 5;
	}

}

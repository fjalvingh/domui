package to.etc.domui.hibgen;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbTable;
import to.etc.domui.hibgen.ColumnWrapper.RelationType;

import javax.annotation.Nullable;
import java.beans.Introspector;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private final ClassOrInterfaceDeclaration m_rootType;

	private CompilationUnit m_unit;

	private int m_errors;

	private DbTable m_table;

	/** Mapped by lowercase property name */
	private Map<String, ColumnWrapper> m_byPropNameMap = new HashMap<>();

	private Map<String, ColumnWrapper> m_byColNameMap = new HashMap<>();

	public ClassWrapper(AbstractGenerator generator, File file, CompilationUnit unit) {
		m_generator = generator;
		m_file = file;
		m_unit = unit;
		String name = m_file.getName();
		m_simpleName = name.substring(0, name.lastIndexOf("."));                // Strip .java
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

	public ClassWrapper(AbstractGenerator generator, String packageName, String className, CompilationUnit cu, DbTable tbl) {
		m_generator = generator;
		m_simpleName = className;
		m_fullClassName = packageName + "." + className;
		m_unit = cu;
		m_file = null;
		m_table = tbl;
		m_isNew = true;

		ClassOrInterfaceDeclaration rootType = null;
		Optional<ClassOrInterfaceDeclaration> o = m_unit.getClassByName(getSimpleName());
		if(! o.isPresent()) {
			error("Cannot locate class type");
		} else {
			rootType = o.get();
		}
		m_rootType = rootType;
	}

	public ClassOrInterfaceDeclaration getRootType() {
		return m_rootType;
	}

	private void error(String msg) {
		m_errors++;
		m_generator.error(getOutputFile(), msg);
	}

	private File getOutputFile() {
		String name = m_fullClassName.replace('.', File.separatorChar) + ".java";
		return new File(m_generator.getOutputDirectory(), name);
	}

	public String calculateClassName() {
		String pkg = m_unit.getPackageDeclaration().get().getName().asString();
		String name = m_file.getName();
		name = name.substring(0, name.lastIndexOf("."));                // Strip .java
		return pkg + "." + name;
	}

	public String getClassName() {
		return m_fullClassName;
	}

	public String getSimpleName() {
		return m_simpleName;
	}

	/**
	 * If this is a wrapper for a Table - this returns that table.
	 * @return
	 */
	@Nullable
	public DbTable getTable() {
		return m_table;
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
				handleTableAnnotation(annotationExpr);
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
		ColumnWrapper cw = m_byPropNameMap.computeIfAbsent(propertyName.toLowerCase(), k -> new ColumnWrapper(this));
		cw.setPropertyType(type);
		cw.setPropertyName(Introspector.decapitalize(propertyName));
		if(isSetter)
			cw.setSetter(md);
		else
			cw.setGetter(md);

		for(AnnotationExpr annotationExpr : md.getAnnotations()) {
			if(annotationExpr instanceof NormalAnnotationExpr) {
				handleDatabaseAnnotation(cw, (NormalAnnotationExpr) annotationExpr);
			}
		}
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

			if("opentopublic".equals(fieldName)) {
				System.out.println("GOTCHA");
			}
			ColumnWrapper cw = m_byPropNameMap.computeIfAbsent(fieldName.toLowerCase(), a -> new ColumnWrapper(this).setFieldName(a));
			cw.setFieldDeclarator(d, vd);
			list.add(cw);

			cw.setPropertyName(fieldName);
			cw.setPropertyType(vd.getType());
		}

		for(AnnotationExpr annotationExpr : d.getAnnotations()) {
			if(annotationExpr instanceof NormalAnnotationExpr) {
				for(ColumnWrapper columnWrapper : list) {
					handleDatabaseAnnotation(columnWrapper, (NormalAnnotationExpr) annotationExpr);
				}
			}
		}
	}

	private void handleDatabaseAnnotation(ColumnWrapper columnWrapper, NormalAnnotationExpr annotationExpr) {
		String name = annotationExpr.getName().asString();

		if(name.equals("Transient")) {
			columnWrapper.setTransient(true);
			return;
		}

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
				m_byColNameMap.put(columnName.toLowerCase(), columnWrapper);
				columnWrapper.setColumnName(columnName);
			}
		} else if(name.equals("JoinColumn")) {
			String columnName = null;
			int length = -1;
			for(MemberValuePair pair : annotationExpr.getPairs()) {
				String prop = pair.getName().asString();
				if(prop.equals("name")) {
					columnName = resolveConstant(pair.getValue());
				}
			}

			if(columnName != null && columnName.length() > 0) {
				m_byColNameMap.put(columnName.toLowerCase(), columnWrapper);
				columnWrapper.setColumnName(columnName);
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
			System.out.println("  - " + table);
		}
		m_table = table;
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
		for(ColumnWrapper cw : m_byPropNameMap.values()) {
			if(cw.isTransient())
				continue;

			if(cw.getColumn() == null) {
				if(cw.getPropertyName().equals("opentopublic")) {
					System.out.println("GOTCHA");
				}

				DbColumn column = m_table.findColumn(cw.getPropertyName().toLowerCase());
				if(null != column) {
					cw.setColumn(column);
					m_byColNameMap.put(column.getName().toLowerCase(), cw);
				}
			}
		}

		//-- 2. Create NEW wrappers for all columns that do not have one, yet
		for(DbColumn dbColumn : m_table.getColumnList()) {
			ColumnWrapper cw = m_byColNameMap.computeIfAbsent(dbColumn.getName().toLowerCase(), a -> {
				ColumnWrapper nw = new ColumnWrapper(this, dbColumn);
				nw.setNew(true);

				//List<String> strings = AbstractGenerator.splitName(dbColumn.getName());
				//StringBuilder sb = new StringBuilder();
				//sb.append(strings.remove(0).toLowerCase());
				//strings.forEach(seg -> sb.append(AbstractGenerator.capitalize(seg)));

				return nw;
			});
			cw.setColumn(dbColumn);
			String propertyName = calculatePropertyNameFromColumnName(dbColumn.getName());
			m_byPropNameMap.put(propertyName.toLowerCase(), cw);
			cw.setPropertyName(propertyName);
		}
	}

	/**
	 * Render all basic table properties.
	 */
	public void renderProperties() {
		DbTable table = m_table;
		if(table == null)
			return;

		//-- 1. Find all properties referring to table columns that no longer exist.
		Set<String> columnNameSet = table.getColumnList().stream().map(c -> c.getName().toLowerCase()).collect(Collectors.toSet());

		for(ColumnWrapper cw : m_byColNameMap.values()) {
			String columnName = cw.getColumnName();
			if(null == columnName)
				throw new IllegalStateException("Missing column name " + cw);
			if(! columnNameSet.contains(columnName.toLowerCase())) {
				//-- Deleted thingy.
				deleteColumn(cw);
			}
		}

		//-- 3. Generate all wrappers.
		for(ColumnWrapper cw : m_byColNameMap.values()) {
			if("opentopublic".equalsIgnoreCase(cw.getPropertyName())) {
				System.out.println("GOTCHA");
			}

			if(cw.getColumn() != null) {
				renderColumnProperty(cw);
			}
		}
	}

	private void deleteColumn(ColumnWrapper cw) {
		g().info(getClassName() + ": column " + cw.getColumnName() + " deleted, deleting property " + cw.getPropertyName());

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
	}

	private void renderColumnProperty(ColumnWrapper dbColumn) {
		if(dbColumn.getPropertyType() == null) {
			error(dbColumn + ": unknown type '" + dbColumn.getColumn().getTypeString() + "' (" + dbColumn.getColumn().getSqlType() + "), not generated");
			return;
		}
		if(dbColumn.isTransient()) {
			return;
		}

		dbColumn.renderField();

		renderGetter(dbColumn);
		//renderSetter(dbColumn);
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
		return AbstractGenerator.camelCase(columnName);
		//List<String> strings = AbstractGenerator.splitName(columnName);
		//StringBuilder sb = new StringBuilder();
		//sb.append(strings.remove(0).toLowerCase());						// First one is lowercase
		//strings.forEach(a -> sb.append(AbstractGenerator.capitalize(a)));		// Rest is camelcased
		//return sb.toString();
	}

	private void renderGetter(ColumnWrapper dbColumn) {
		dbColumn.renderGetter();
	}

	private String calculateMethodName(String get, String name) {
		List<String> strings = AbstractGenerator.splitName(name);
		StringBuilder sb = new StringBuilder();
		sb.append(get);
		strings.forEach(a -> sb.append(AbstractGenerator.capitalize(a)));
		return sb.toString();
	}

	static private int compareName(String a, String b) {
		if(isIdName(a)) {
			if(isIdName(b)) {
				return a.compareTo(b);
			} else {
				return -1;
			}
		} else if(isIdName(b)) {
			return 1;
		} else {
			return a.compareToIgnoreCase(b);
		}
	}

	static private boolean isIdName(String name) {
		return name.equalsIgnoreCase("id") || name.equalsIgnoreCase("m_id") || name.equalsIgnoreCase("getid") || name.equalsIgnoreCase("setid");
	}

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
		try(Writer w = new FileWriter(outputFile)) {
			w.write(m_unit.toString());
		}
		//System.out.println("---------------------");
		//System.out.println(m_unit.toString());
	}


	/**
	 * For all columns that are "new", calculate a column type.
	 * @param dbc
	 */
	public void calculateColumnTypes(Connection dbc) throws Exception {
		for(ColumnWrapper cw : m_byColNameMap.values()) {
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
		return newName;
	}

	/**
	 * Walk all (new) relations, and try to assign a more reasonable property name than the column name.
	 */
	public void calculateRelationNames() {
		Map<String, List<ColumnWrapper>> dupList = m_byPropNameMap.values()
			.stream()
			.filter(cw -> cw.isNew() && cw.getRelationType() == RelationType.manyToOne)
			.collect(Collectors.groupingBy(cw -> calculateClassBasedParentName(cw.getParentClass().getClassName()), Collectors.toList()));

		//-- For all properties without duplicates: use the parent class type
		dupList.forEach((name, list) -> {
			if(list.size() == 1) {
				ColumnWrapper w = list.get(0);
				m_byPropNameMap.remove(w.getPropertyName());
				w.setPropertyName(name);
				m_byPropNameMap.put(name.toLowerCase(), w);
			} else if(list.size() > 1) {
				list.forEach(cw -> {
					m_byPropNameMap.remove(cw.getPropertyName());
					cw.recalculatePropertyNameFromParentRelation();
					m_byPropNameMap.put(cw.getPropertyName().toLowerCase(), cw);
				});
			}
		});
	}
}

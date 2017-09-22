package to.etc.domui.hibgen;

import com.github.javaparser.ast.CompilationUnit;
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

import javax.annotation.Nullable;
import java.beans.Introspector;
import java.io.File;
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
	}

	public ClassWrapper(AbstractGenerator generator, String packageName, String className, CompilationUnit cu, DbTable tbl) {
		m_generator = generator;
		m_simpleName = className;
		m_fullClassName = packageName + "." + className;
		m_unit = cu;
		m_file = null;
		m_table = tbl;

		ClassOrInterfaceDeclaration rootType = null;
		Optional<ClassOrInterfaceDeclaration> o = m_unit.getClassByName(getSimpleName());
		if(! o.isPresent()) {
			error("Cannot locate class type");
		} else {
			rootType = o.get();
		}
		m_rootType = rootType;
	}

	private void error(String msg) {
		m_errors++;
		m_generator.error(getOutputFile(), msg);
	}

	private File getOutputFile() {
		String name = m_fullClassName.replace('.', File.separatorChar) + ".java";
		return new File(m_generator.getSourceDirectory(), name);
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
			propertyName = name.substring(len).toLowerCase();
		} else if(name.startsWith("set")) {
			if(md.getParameters().size() != 1)
				return;
			type = md.getParameter(0).getType();
			isSetter = true;
			propertyName = name.substring(3).toLowerCase();
		} else
			return;

		//-- Decode a property name
		ColumnWrapper cw = m_byPropNameMap.computeIfAbsent(propertyName.toLowerCase(), k -> new ColumnWrapper());
		cw.setPropertyType(type);
		cw.setPropertyName(Introspector.decapitalize(propertyName));
		if(isSetter)
			cw.setSetter(md);
		else
			cw.setGetter(md);

		for(AnnotationExpr annotationExpr : md.getAnnotations()) {
			if(annotationExpr instanceof NormalAnnotationExpr) {
				handleFieldAnnotation(cw, (NormalAnnotationExpr) annotationExpr);
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
			ColumnWrapper cw = m_byPropNameMap.computeIfAbsent(fieldName.toLowerCase(), a -> new ColumnWrapper().setFieldName(a));
			cw.setFieldDeclarator(d, vd);
			list.add(cw);

			cw.setPropertyName(fieldName);
		}

		for(AnnotationExpr annotationExpr : d.getAnnotations()) {
			if(annotationExpr instanceof NormalAnnotationExpr) {
				for(ColumnWrapper columnWrapper : list) {
					handleFieldAnnotation(columnWrapper, (NormalAnnotationExpr) annotationExpr);
				}
			}
		}
	}

	private void handleFieldAnnotation(ColumnWrapper columnWrapper, NormalAnnotationExpr annotationExpr) {
		String name = annotationExpr.getName().asString();
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
				m_byColNameMap.put(columnName, columnWrapper);
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

		for(DbColumn dbColumn : m_table.getColumnList()) {
			renderColumnProperty(dbColumn);
		}
	}

	private void deleteColumn(ColumnWrapper cw) {
		System.out.println(getClassName() + ": column " + cw.getColumnName() + " deleted, deleting property " + cw.getPropertyName());
	}

	private void renderColumnProperty(DbColumn dbColumn) {
		renderField(dbColumn);

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

	private void renderField(DbColumn dbColumn) {
		List<String> strings = AbstractGenerator.splitName(dbColumn.getName());
		StringBuilder sb = new StringBuilder();
		//sb.append("m_");
		sb.append(strings.remove(0).toLowerCase());
		strings.forEach(a -> sb.append(AbstractGenerator.capitalize(a)));

		String baseFieldName = sb.toString();
		FieldDeclaration fd = findFieldDeclaration(baseFieldName);
		if(null != fd) {
			fd.remove();
		}

		//com.github.javaparser.ast.type.Type type = new ClassOrInterfaceType("String");
		//FieldDeclaration fd = new FieldDeclaration(EnumSet.of(Modifier.PRIVATE), type, fieldName);
		m_rootType.addField(String.class, "m_" + baseFieldName, com.github.javaparser.ast.Modifier.PRIVATE);

		//m_unit.addType(fd);
	}

	private void renderGetter(DbColumn dbColumn) {
		String methodName = calculateMethodName("get", dbColumn.getName());
	}

	private String calculateMethodName(String get, String name) {
		List<String> strings = AbstractGenerator.splitName(name);
		StringBuilder sb = new StringBuilder();
		sb.append(get);
		strings.forEach(a -> sb.append(AbstractGenerator.capitalize(a)));
		return sb.toString();
	}


	public void print() {
		System.out.println("---------------------");
		System.out.println(m_unit.toString());
	}
}

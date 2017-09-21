package to.etc.domui.hibgen;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.VoidType;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbSchema;
import to.etc.dbutil.schema.DbTable;
import to.etc.util.FileTool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-9-17.
 */
abstract public class AbstractGenerator {
	private Connection m_dbc;

	private Set<DbSchema> m_schemaSet;

	private String m_packageName;

	private File m_sourceDirectory;

	private boolean m_schemaAsPackage;

	private String m_fieldPrefix = "m_";

	/** Map a fq class name to its wrapper */
	private Map<String, ClassWrapper> m_byClassnameMap = new HashMap<>();

	/** Map a table to its wrapper. */
	private Map<DbTable, ClassWrapper> m_byTableMap = new HashMap<>();

	private boolean m_hideSchemaNameFromTableName = true;

	private boolean m_addSchemaNameToClassName = true;

	abstract protected Connection createConnection() throws Exception;

	protected abstract Set<DbSchema> loadSchemas(List<String> schemaSet) throws Exception;

	public void generate(List<String> schemaSet) throws Exception {
		createConnection();								// Fast test whether db can be opened
		m_schemaSet = loadSchemas(schemaSet);
		loadJavaSources();

		matchTablesAndSources();
		generateProperties();


		renderOutput();
	}

	private void renderOutput() {
		for(DbTable dbTable : getAllTables()) {
			ClassWrapper wrapper = m_byTableMap.get(dbTable);
			if(null != wrapper) {
				if(dbTable.getName().equals("definition"))
					wrapper.print();
			}
		}
	}

	private void generateProperties() {
		for(DbTable dbTable : getAllTables()) {
			ClassWrapper wrapper = m_byTableMap.get(dbTable);
			if(null != wrapper) {
				wrapper.renderProperties();
			}
		}
	}

	/**
	 * Walk all tables, and create java sources for all of them that are missing.
	 * @throws Exception
	 */
	private void matchTablesAndSources() throws Exception {
		for(DbTable dbTable : getAllTables()) {
			ClassWrapper wrapper = m_byTableMap.get(dbTable);
			if(null == wrapper) {
				createNewWrapper(dbTable);
			}
		}
	}

	private void createNewWrapper(DbTable tbl) {
		String tableName = tbl.getName();
		String schemaName = tbl.getSchema().getName();
		List<String> splitSchema = splitName(schemaName);
		StringBuilder sb = new StringBuilder();
		splitSchema.forEach(n -> sb.append(capitalize(n)));
		schemaName = sb.toString();

		StringBuilder sbpackage = new StringBuilder();
		sbpackage.append(m_packageName);
		if(m_schemaAsPackage && isExplicitSchema()) {
			sbpackage.append(".").append(schemaName.toLowerCase());
		}

		List<String> names = splitName(tableName);
		if(names.size() != 1) {
			//-- Multipart name. Is the 1st part name the same as the schema name?
			if(names.get(0).equalsIgnoreCase(schemaName)) {
				if(m_hideSchemaNameFromTableName) {
					names.remove(0);
				}
			}
		}

		if(m_addSchemaNameToClassName) {
			names.add(0, schemaName);
		}
		sb.setLength(0);
		names.forEach(n -> sb.append(capitalize(n)));
		String className = sb.toString();
		String packageName = sbpackage.toString();

		CompilationUnit cu = createCompilationUnit(packageName, className, tbl);

		ClassWrapper wrapper = new ClassWrapper(this, packageName, className, cu);

		m_byTableMap.put(tbl, wrapper);
		String fullName = packageName + "." + className;
		m_byClassnameMap.put(fullName, wrapper);

		System.out.println("Created new class " + fullName);
		System.out.println(cu.toString());
	}

	private CompilationUnit createCompilationUnit(String packageName, String className, DbTable tbl) {
		CompilationUnit cu = new CompilationUnit();
		cu.setPackageDeclaration(new PackageDeclaration(Name.parse(packageName)));

		//// or a shortcut
		//cu.setPackageDeclaration("java.parser.test");

		// create the type declaration
		ClassOrInterfaceDeclaration type = cu.addClass(className);

		// create a method
		EnumSet<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
		MethodDeclaration method = new MethodDeclaration(modifiers, new VoidType(), "main");
		modifiers.add(Modifier.STATIC);
		method.setModifiers(modifiers);
		type.addMember(method);

		// or a shortcut
		MethodDeclaration main2 = type.addMethod("main2", Modifier.PUBLIC, Modifier.STATIC);

		// add a parameter to the method
		Parameter param = new Parameter(new ClassOrInterfaceType("String"), "args");
		param.setVarArgs(true);
		method.addParameter(param);

		// or a shortcut
		main2.addAndGetParameter(String.class, "args").setVarArgs(true);

		// add a body to the method
		BlockStmt block = new BlockStmt();
		method.setBody(block);

		// add a statement do the method body
		NameExpr clazz = new NameExpr("System");
		FieldAccessExpr field = new FieldAccessExpr(clazz, "out");
		MethodCallExpr call = new MethodCallExpr(field, "println");
		call.addArgument(new StringLiteralExpr("Hello World!"));
		block.addStatement(call);
		return cu;
	}

	static String capitalize(String in) {
		if(in.length() == 1)
			return in.toUpperCase();
		return in.substring(0, 1).toUpperCase() + in.substring(1).toLowerCase();
	}

	/**
	 * Split a table name on underscores. If it has no underscores but it uses camelcase then split on each hump.
	 * @return
	 */
	static List<String> splitName(String name) {
		List<String> lset = new ArrayList<>();
		String[] names = name.split("_");
		if(names.length > 1) {
			lset.addAll(Arrays.asList(names));
			return lset;
		}
		int lc = 0;
		int uc = 0;
		for(int i = name.length(); --i >= 0;) {
			char c = name.charAt(i);
			if(Character.isLetter(c)) {
				if(Character.isLowerCase(c))
					lc++;
				else if(Character.isUpperCase(c))
					uc++;
			}
		}
		if(lc == 0 || uc == 0) {
			//-- All the same case-> use as a single word.
			lset.add(name);
			return lset;
		}

		//Split on humps.
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if(! Character.isLetter(c) || Character.isLowerCase(c)) {
				sb.append(c);
			} else {
				if(sb.length() > 0) {
					lset.add(sb.toString());
					sb.setLength(0);
				}
				sb.append(c);
			}
		}
		if(sb.length() > 0) {
			lset.add(sb.toString());
		}
		return lset;
	}


	protected List<DbTable> getAllTables() {
		List<DbTable> res = new ArrayList<>();
		m_schemaSet.forEach(s -> res.addAll(s.getTables()));
		return res;
	}

	private void loadJavaSources() throws Exception {
		File packageRoot = new File(m_sourceDirectory, m_packageName.replace('.', File.separatorChar));
		if(! packageRoot.exists()) {
			if(!packageRoot.mkdirs()) {
				throw new IOException("Cannot create package output directory " + packageRoot);
			}
		}

		recurseSources(packageRoot, getPackageName().replace('.', '/'));
	}

	private void recurseSources(File dir, String s) throws Exception {
		for(File file : dir.listFiles()) {
			String relPath = s.length() == 0 ? file.getName() : s + "/" + file.getName();
			if(file.isDirectory()) {
				recurseSources(file, relPath);
			} else {
				if(FileTool.getFileExtension(file.getName()).equalsIgnoreCase("java")) {
					loadJavaFile(file, relPath);
				}
			}
		}
	}

	private void loadJavaFile(File file, String relPath) {
		info("Loading " + relPath);
		try {
			CompilationUnit parse = JavaParser.parse(file);

			Optional<PackageDeclaration> pd = parse.getPackageDeclaration();
			if(! pd.isPresent()) {
				error(file, "No package declaration");
				return;
			}
			PackageDeclaration packageDeclaration = pd.get();
			String id = packageDeclaration.getName().asString();

			String pathPackage = relPath.substring(0, relPath.lastIndexOf('/')).replace('/', '.');

			if(! id.equals(pathPackage)) {
				error(file, "Package name mismatch: declared is '" + id + "' but it is found as '" + pathPackage + "'.");
				return;
			}

			ClassWrapper wrapper = new ClassWrapper(this, file, parse);
			m_byClassnameMap.put(wrapper.getClassName(), wrapper);
			wrapper.scanAndRegister();

			DbTable table = wrapper.getTable();
			if(null != table)
				m_byTableMap.put(table, wrapper);

			//} catch(ParseException px) {
		//	System.out.println(px.toString());
		} catch(FileNotFoundException e) {
			error(file, "Cannot load file");
		}
	}

	void error(File file, String msg) {
		System.err.println(file.toString() + ": " + msg);
	}


	protected Connection dbc() throws Exception {
		Connection dbc = m_dbc;
		if(null == dbc) {
			m_dbc = dbc = createConnection();
		}
		return dbc;
	}

	protected DataSource getFakeDatasource() {
		return new DataSource() {
			@Override public Connection getConnection() throws SQLException {
				try {
					return createConnection();
				} catch(SQLException|RuntimeException x) {
					throw x;
				} catch(Exception z) {
					throw new RuntimeException(z);				// Really useful, those idiotic checked exceptions.
				}
			}

			@Override public Connection getConnection(String username, String password) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override public <T> T unwrap(Class<T> iface) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override public boolean isWrapperFor(Class<?> iface) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override public PrintWriter getLogWriter() throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override public void setLogWriter(PrintWriter out) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override public void setLoginTimeout(int seconds) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override public int getLoginTimeout() throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {
				throw new IllegalStateException("Not implemented");
			}
		};
	}

	public void close() {
		if(m_dbc != null) {
			try {
				m_dbc.close();
			} catch(Exception x) {
				// who the f cares.
			}
		}
	}


	public void setPackageName(String packageName) {
		m_packageName = packageName;
	}

	protected boolean isIgnored(DbTable table) {
		return false;
	}

	protected boolean isIgnored(DbColumn column) {
		return false;
	}

	public String getPackageName() {
		return m_packageName;
	}

	public void setSourceDirectory(File sourceDirectory) {
		m_sourceDirectory = sourceDirectory;
	}

	public File getSourceDirectory() {
		return m_sourceDirectory;
	}

	static protected void info(String s) {
		System.out.println(s);
	}

	@Nullable
	private DbSchema getDefaultSchema() {
		if(m_schemaSet.size() == 1) {
			return m_schemaSet.iterator().next();
		}
		return null;
	}

	@Nullable
	protected DbSchema findSchema(Set<DbSchema> schemaSet, String name) {
		Optional<DbSchema> first = schemaSet.stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst();
		return first.isPresent() ? first.get() : null;
	}

	@Nullable
	protected DbSchema findSchema(String name) {
		return findSchema(m_schemaSet, name);
	}

	protected boolean isExplicitSchema() {
		return m_schemaSet.size() > 1;
	}



	/**
	 *
	 * @param schemaName
	 * @param tableName
	 * @return
	 */
	public DbTable findTableByNames(@Nullable String schemaName, @Nonnull String tableName) {
		if(schemaName == null) {
			//-- Is there a schema in the table name, perhaps?
			String[] ar = tableName.split("\\.");
			if(ar.length == 2) {
				schemaName = ar[0];
				tableName = ar[1];
			}
		}
		DbSchema schema = schemaName == null ? getDefaultSchema() : findSchema(schemaName);
		if(null == schema)
			return null;

		return schema.getTable(tableName);
	}

	public String getFieldPrefix() {
		return m_fieldPrefix;
	}
}

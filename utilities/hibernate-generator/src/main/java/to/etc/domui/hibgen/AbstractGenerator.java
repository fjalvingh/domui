package to.etc.domui.hibgen;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.VoidType;
import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbSchema;
import to.etc.dbutil.schema.DbTable;
import to.etc.util.FileTool;
import to.etc.xml.DomTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-9-17.
 */
abstract public class AbstractGenerator {

	public static final String HIBERNATE_CONFIGURATION = "HibernateConfiguration";

	private List<ClassWrapper> m_wrapperList = new ArrayList<>();

	private Connection m_dbc;

	private Set<DbSchema> m_schemaSet;

	private String m_packageName;

	private File m_sourceDirectory;

	private boolean m_schemaAsPackage;

	private String m_fieldPrefix = "m_";

	private boolean m_addIdentifyable = true;

	private boolean m_hideSchemaNameFromTableName = true;

	private boolean m_addSchemaNameToClassName = true;

	private boolean m_forceRenameFields = true;

	private boolean m_forceRenameMethods = true;

	private boolean m_mapOneCharVarcharToBoolean = true;

	private boolean m_skipBundles = false;

	private boolean m_skipBaseClasses = false;

	private boolean m_forcePkToLong = true;

	private boolean m_verbose;

	private int m_enumMaxFieldSize = 20;

	/** When T, this does not check for the same type on columns to attach the base class. */
	private boolean m_matchBaseClassesOnColumnNameOnly = false;

	private boolean m_destroyConstructors = true;

	private Set<String> m_altBundles = new HashSet<>();

	private Set<String> m_ignoreTableSet = new HashSet<>();

	private String m_forcePKIdentifier = "id";

	/** Postgres: when T, a Hibernate sequence identifier generator is used instead of the @Generated(GenerationType.IDENTIFIER) for an autoincrement column. */
	private boolean m_replaceSerialWithSequence = true;

	/** When T this always appends a schema name, when F it only adds it if there are more than one schemas scanned. */
	private boolean m_appendSchemaNameInAnnotations;

	private File m_configFile;

	private Document m_configDocument;

	private Node m_configRoot;

	abstract protected Connection createConnection() throws Exception;

	protected abstract Set<DbSchema> loadSchemas(List<String> schemaSet) throws Exception;

	public void generate(List<String> schemaSet) throws Exception {
		loadUserConfig();

		// ordered list of actions
		createConnection();								// Fast test whether db can be opened
		m_schemaSet = loadSchemas(schemaSet);
		loadJavaSources();
		matchTablesAndSources();
		matchColumns();

		removeUnusedProperties();
		findManyToOneClasses();
		removePropertyNameConstants();

		if(m_destroyConstructors) {
			getTableClasses().forEach(cw -> cw.destroyConstructors());
		}

		if(isForceRenameFields()) {
			getTableClasses().forEach(w -> w.renameFieldName());
		}

		renamePrimaryKeys();			// If pk name is forced, rename all pk properties

		calculateColumnTypes();

		assignPrimaryKeys();							// Assign PK, either simple or complex.

		getTableClasses().forEach(w -> w.fixPkNullity());				// Must be after assignPrimaryKeys

		assignBaseClasses();

		calculateRelationNames();

		generateOneToManyProperties();

		resolveOneToManyDuplicates();

		getTableClasses().forEach(w -> w.removeBaseClassColumns());

		loadNlsPropertyFiles();

		getTableClasses().forEach(w -> w.handleClassDefinition());

		getEmbeddableClasses().forEach(w -> w.renderEmbeddableAnnotations());

		generateProperties();

		getTableClasses().forEach(c -> c.generateDomUIAnnotations());

		getTableClasses().forEach(w -> w.generateConfig());

		generateLoaderClass();
		renderOutput();

		saveUserConfig();
	}

	protected void generateLoaderClass() throws Exception {
		CompilationUnit cu = new CompilationUnit();
		cu.setPackageDeclaration(new PackageDeclaration(Name.parse(getPackageName())));

		// create the type declaration
		ClassOrInterfaceDeclaration type = cu.addClass(HIBERNATE_CONFIGURATION);

		cu.addImport("to.etc.domui.hibernate.config.HibernateConfigurator");
		EnumSet<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
		MethodDeclaration configurator = new MethodDeclaration(modifiers, new VoidType(), "configure");
		configurator.setModifiers(modifiers);
		type.addMember(configurator);

		BlockStmt block = new BlockStmt();
		configurator.setBody(block);

		List<ClassWrapper> tableClasses = new ArrayList<>(getTableClasses());
		tableClasses.sort((a, b) -> a.getSimpleName().compareToIgnoreCase(b.getSimpleName()));
		tableClasses.forEach(tc -> {
			if(! getPackageName().equals(tc.getPackageName())) {
				cu.addImport(tc.getClassName());
			}

			MethodCallExpr mcx = new MethodCallExpr(new NameExpr("HibernateConfigurator"), "addClasses", com.github.javaparser.ast.NodeList.nodeList(new NameExpr(tc.getSimpleName() + ".class")));
			block.addStatement(mcx);
		});

		cu.addImport("org.hibernate.cfg.Configuration");
		modifiers = EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
		configurator = new MethodDeclaration(modifiers, new VoidType(), "configure");
		configurator.setModifiers(modifiers);
		Parameter param = new Parameter(new ClassOrInterfaceType("Configuration"), "config");
		configurator.addParameter(param);
		type.addMember(configurator);

		BlockStmt block2 = new BlockStmt();		// If Java lambda's sucked only slightly harder they would be fucking black holes.
		configurator.setBody(block2);

		tableClasses.forEach(tc -> {
			MethodCallExpr mcx = new MethodCallExpr(new NameExpr("config"), "addAnnotatedClass", com.github.javaparser.ast.NodeList.nodeList(new NameExpr(tc.getSimpleName() + ".class")));
			block2.addStatement(mcx);
		});

		File dir = getOutputDirectory();
		if(null == dir) {
			dir = new File(getSourceDirectory(), getPackageName().replace('.', File.separatorChar));
		}
		File out = new File(dir, type.getNameAsString() + ".java");
		try(FileWriter fw = new FileWriter(out)) {
			fw.write(cu.toString());
		}
	}

	protected List<ClassWrapper> getTableClasses() {
		return m_wrapperList.stream().filter(a -> a.getType() == ClassWrapperType.tableClass).collect(Collectors.toList());
	}

	protected List<ClassWrapper> getRenderedClasses() {
		return m_wrapperList.stream().filter(a -> a.getType() == ClassWrapperType.tableClass || a.getType() == ClassWrapperType.embeddableClass).collect(Collectors.toList());
	}


	protected List<ClassWrapper> getBaseClasses() {
		return m_wrapperList.stream().filter(a -> a.getType() == ClassWrapperType.baseClass).collect(Collectors.toList());
	}
	protected List<ClassWrapper> getEmbeddableClasses() {
		return m_wrapperList.stream().filter(a -> a.getType() == ClassWrapperType.embeddableClass).collect(Collectors.toList());
	}

	private void assignPrimaryKeys() {
		getTableClasses().forEach(w -> w.checkAssignComplexPK());
	}

	/**
	 * For every data class, check whether it could be served by having a base class.
	 */
	private void assignBaseClasses() {
		if(getBaseClasses().size() == 0 || isSkipBaseClasses())
			return;

		for(ClassWrapper cw : getTableClasses()) {
			ClassWrapper baseClass = findBaseClassFor(cw);
			if(null != baseClass) {
				cw.assignBaseClass(baseClass);
				info(cw + " should use base class " + baseClass);
			}
		}
	}

	private void saveUserConfig() throws Exception {
		sortNodes(m_configRoot, "name");

		NodeList nl = m_configRoot.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				sortNodes(item, "columnName");
			}
		}

		Source source = new DOMSource(m_configDocument);
		StreamResult result = new StreamResult(new OutputStreamWriter(new FileOutputStream(m_configFile), "utf-8"));
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.setOutputProperty(OutputKeys.INDENT, "yes");
		xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		xformer.transform(source, result);
	}

	private void sortNodes(Node root, String attrName) {
		List<Node> children = new ArrayList<>();
		for(int i = 0; i < root.getChildNodes().getLength(); i++) {
			Node item = root.getChildNodes().item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE)
				children.add(item);
		}
		while(root.getChildNodes().getLength() > 0) {
			root.removeChild(root.getChildNodes().item(root.getChildNodes().getLength() - 1));
		}

		children.sort((a, b) -> {
			String an = DomTools.strAttr(a, attrName);
			String bn = DomTools.strAttr(b, attrName);
			return an.compareToIgnoreCase(bn);
		});

		children.forEach(c -> root.appendChild(c));
	}

	private void loadUserConfig() throws Exception {
		String packagePath = getPackageName().replace('.', File.separatorChar);
		m_configFile = new File(getSourceDirectory(), packagePath + File.separatorChar + "genHib.xml");
		if(m_configFile.exists()) {
			m_configDocument = DomTools.getDocument(m_configFile, false);
			m_configRoot = DomTools.getRootElement(m_configDocument);
		} else {
			Document xmlDoc = m_configDocument = new DocumentImpl();
			m_configRoot = xmlDoc.createElement("config");
			xmlDoc.appendChild(m_configRoot);
		}
	}

	private void loadNlsPropertyFiles() throws Exception {
		if(m_skipBundles)
			return;

		for(ClassWrapper classWrapper : getTableClasses()) {
			classWrapper.loadNlsPropertyFiles();
		}
	}

	private void removePropertyNameConstants() {
		for(ClassWrapper classWrapper : getTableClasses()) {
			classWrapper.removePropertyNameConstants();
		}
	}

	private void resolveOneToManyDuplicates() {
		for(ClassWrapper classWrapper : getTableClasses()) {
			classWrapper.resolveDuplicateOneToManyProperties();
		}
	}

	private void findManyToOneClasses() {
		for(ClassWrapper classWrapper : getTableClasses()) {
			classWrapper.resolveManyToOne();
		}
	}

	private void generateOneToManyProperties() {
		for(ClassWrapper classWrapper : getTableClasses()) {
			classWrapper.resolveMappedBy();
		}

		for(ClassWrapper classWrapper : getTableClasses()) {
			classWrapper.generateOneToManyProperties();
		}
	}

	private void renamePrimaryKeys() {
		String pkName = getForcePKIdentifier();
		if(null == pkName)
			return;
		for(ClassWrapper classWrapper : getTableClasses()) {
			classWrapper.renamePrimaryKeys(pkName);
		}
	}

	private void calculateRelationNames() {
		for(ClassWrapper classWrapper : getTableClasses()) {
			classWrapper.calculateRelationNames();
		}
	}

	private void removeUnusedProperties() {
		for(ClassWrapper classWrapper : getTableClasses()) {
			classWrapper.removeUnusedProperties();
		}
	}


	private void matchColumns() {
		for(ClassWrapper classWrapper : getTableClasses()) {
			classWrapper.matchColumns();
		}
	}

	private void calculateColumnTypes() throws Exception {
		for(ClassWrapper classWrapper : getTableClasses()) {
			classWrapper.calculateColumnTypes(dbc());
		}
	}

	private void renderOutput() throws Exception {
		for(ClassWrapper wrapper : m_wrapperList) {
			switch(wrapper.getType()){
				default:
					break;

				case enumClass:
					wrapper.print();
					break;

				case embeddableClass:
					wrapper.order();
					wrapper.print();
					break;

				case tableClass:
					wrapper.order();
					wrapper.print();
					wrapper.writeNlsPropertyFiles();
					break;
			}
		}
	}

	private void generateProperties() throws Exception {
		for(ClassWrapper wrapper : getRenderedClasses()) {
			wrapper.renderProperties();
		}
	}

	private boolean isIgnoredTable(DbTable tbl) {
		if(m_ignoreTableSet.contains(tbl.getName().toLowerCase()))
			return true;
		return m_ignoreTableSet.contains(tbl.getSchema().getName().toLowerCase() + "." + tbl.getName().toLowerCase());

	}

	/**
	 * Walk all tables, and create java sources for all of them that are missing.
	 * @throws Exception
	 */
	private void matchTablesAndSources() throws Exception {
		Set<ClassWrapper> deleteSet = new HashSet<>(getTableClasses());

		for(DbTable dbTable : getAllTables()) {
			if(isIgnoredTable(dbTable))
				continue;

			if(dbTable.getColumnList().size() == 0) {
				error(dbTable + ": table without columns is idiocy, skipping it");
				continue;
			}

			ClassWrapper wrapper = findClassByTable(dbTable);
			if(null == wrapper) {
				wrapper = createNewWrapper(dbTable);
			} else {
				getTableConfigProperty(dbTable, "className");
				deleteSet.remove(wrapper);
			}
			wrapper.getConfig();
		}

		for(ClassWrapper classWrapper : deleteSet) {
			warning(classWrapper + ": table deleted, this class should be removed");
			classWrapper.setType(ClassWrapperType.deleted);
			//m_wrapperList.remove(classWrapper);
		}

		//-- Do the same for all unassigned
		for(ClassWrapper classWrapper : m_wrapperList) {
			if(classWrapper.getType() == ClassWrapperType.unknown) {
				warning(classWrapper + ": class use unknown, skipping");
				classWrapper.setType(ClassWrapperType.deleted);
			}
		}
	}

	@Nullable
	public ClassWrapper findClassByTable(DbTable table) {
		for(ClassWrapper classWrapper : getTableClasses()) {
			if(classWrapper.getTable() == table)
				return classWrapper;
		}
		return null;
	}

	protected ClassWrapper createNewWrapper(DbTable tbl) {
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
		String className = getTableConfigProperty(tbl, "className");
		if(null == className) {
			sb.setLength(0);
			names.forEach(n -> sb.append(capitalize(n)));
			className = sb.toString();
		}

		String packageName = sbpackage.toString();

		//-- Make sure this class name does not already exist
		String oldClassName = className;
		int index = 0;
		for(;;) {
			if(findClassWrapper(packageName, className) == null) {
				break;
			}

			if(index == 0) {
				error("Duplicate class name generated: " + packageName + "." + className);
			}
			index++;
			className = oldClassName + index;
		}

		ClassWrapper wrapper = ClassWrapper.create(this, packageName, className);
		m_wrapperList.add(wrapper);
		wrapper.setTable(tbl);
		wrapper.getRootType().setJavadocComment(createClassComment(tbl));
		return wrapper;
	}

	protected ClassWrapper createWrapper(String packageName, String className) {
		ClassWrapper wrapper = ClassWrapper.create(this, packageName, className);
		m_wrapperList.add(wrapper);
		return wrapper;
	}

	protected ClassWrapper createEnumWrapper(String packageName, String className) {
		ClassWrapper wrapper = ClassWrapper.createEnum(this, packageName, className);
		m_wrapperList.add(wrapper);
		return wrapper;
	}

	private String createClassComment(DbTable table) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append(" * ").append("Table «").append(table.getName()).append("» in schema «").append(table.getSchema().getName()).append("»");
		String comments = table.getComments();
		if(null != comments) {
			sb.append(": ").append(comments);
		}
		sb.append("\n");
		sb.append(" *\n");

		sb.append(" * Class generated by Domui's Hibernate Generator version 1.0 at " + new Date() + "\n");
		sb.append(" ");
		return sb.toString();
	}

	static String capitalize(String in) {
		if(in.length() == 1)
			return in.toUpperCase();
		return in.substring(0, 1).toUpperCase() + in.substring(1).toLowerCase();
	}

	static String capitalizeFirst(String in) {
		if(in.length() == 1)
			return in.toUpperCase();
		return in.substring(0, 1).toUpperCase() + in.substring(1);
	}

	static String finalName(String name) {
		int i = name.lastIndexOf('.');
		if(i < 0)
			return name;
		return name.substring(i + 1);
	}

	@Nullable
	static String packageName(String name) {
		int i = name.lastIndexOf('.');
		if(i < 0)
			return null;
		return name.substring(0, i);
	}

	static String camelCase(String name) {
		List<String> strings = splitName(name);
		return camelCase(strings);
	}

	static String camelCase(List<String> strings) {
		StringBuilder sb = new StringBuilder();
		sb.append(strings.remove(0).toLowerCase());
		strings.forEach(seg -> sb.append(AbstractGenerator.capitalize(seg)));
		return sb.toString();
	}

	static String fixCamelCase(String s) {
		/*
		 * The getter/setter idiocy has trouble with names like "eAttribute", because the setter
		 * for that would be setEAttribute. But in that case the idiot rules say that the name
		 * of the property is EAttribute. Sigh.
		 */
		if(s.length() > 1 && Character.isLowerCase(s.charAt(0)) && Character.isUpperCase(s.charAt(1))) {
			s = s.substring(0, 1).toUpperCase() + s.substring(1);
		}
		return s;
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
					if(! file.getName().equals(HIBERNATE_CONFIGURATION + ".java"))
						loadJavaFile(file, relPath);
				}
			}
		}
	}

	/**
	 * Load the java source file. But if a file exists with the same name but with the '.java.old' suffix then
	 * this .old file gets loaded, so that multiple runs can be done on the same source data.
	 *
	 * @param file
	 * @param relPath
	 */
	private void loadJavaFile(File file, String relPath) {
		String altName = file.getName() + ".old";
		File oldFile = new File(file.getParentFile(), altName);
		if(oldFile.exists() && oldFile.isFile()) {
			file = oldFile;
		}
		if(file.length() == 0)
			return;

		info("Loading " + oldFile);
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
			wrapper.scanAndRegister();

			//-- We're only interested in classes that have something useful..
			if(wrapper.getType() != ClassWrapperType.unknown) {
				m_wrapperList.add(wrapper);
			} else {
				info(file + ": not entity related, skipping");
			}

			//if(wrapper.getTable() != null) {
			//	m_classWrapperList.add(wrapper);
			//} else if(wrapper.isBaseClass()) {
			//	m_baseClassList.add(wrapper);
			//} else if(wrapper.isEmbeddableClass()) {
			//	m_embeddableClassList.add(wrapper);
			//} else {
			//	info(file + ": not entity related, skipping");
			//}
		} catch(FileNotFoundException e) {
			error(file, "Cannot load file");
		}
	}

	void error(File file, String msg) {
		System.err.println("error " + file.toString() + ": " + msg);
	}
	public void info(File file, String msg) {
		System.err.println("info " + file.toString() + ": " + msg);
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

	@Nullable
	public File getOutputDirectory() {
		return null;
		//File file = new File("/tmp/gen");
		//file.mkdirs();
		//return file;
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
	@Nullable
	public DbTable findTableByNames(@Nullable String schemaName, @Nonnull String tableName) {
		if(schemaName == null) {
			//-- Is there a schema in the table name, perhaps?
			String[] ar = tableName.split("\\.");
			if(ar.length == 2) {
				schemaName = ar[0];
				tableName = ar[1];
			}
		}
		if(schemaName != null) {
			DbSchema schema = findSchema(schemaName);
			if(null == schema) {
				return null;
			}
			return schema.findTable(tableName);
		}

		List<DbTable> list = new ArrayList<>();
		for(DbSchema dbSchema : m_schemaSet) {
			DbTable table = dbSchema.findTable(tableName);
			if(null != table)
				list.add(table);
		}
		if(list.size() != 1)
			return null;
		return list.get(0);
	}

	public String getFieldPrefix() {
		return m_fieldPrefix;
	}

	public boolean isForceRenameFields() {
		return m_forceRenameFields;
	}

	protected void info(String s) {
		System.out.println(s);
	}

	protected void error(String s) {
		System.err.println("error: " + s);
	}

	protected void warning(String s) {
		System.out.println("warning: " + s);
	}

	public boolean isForceRenameMethods() {
		return m_forceRenameMethods;
	}

	public boolean isMapOneCharVarcharToBoolean() {
		return m_mapOneCharVarcharToBoolean;
	}

	public boolean isReplaceSerialWithSequence() {
		return m_replaceSerialWithSequence;
	}

	@Nullable
	public String getForcePKIdentifier() {
		return m_forcePKIdentifier;
	}

	@Nullable
	protected String getIdColumnSequence(DbColumn column) throws Exception {
		return null;
	}

	public boolean isAppendSchemaNameInAnnotations() {
		return m_appendSchemaNameInAnnotations || m_schemaSet.size() > 1;
	}

	public ClassWrapper findClassWrapper(String packageName, String className) {
		return findClassWrapper(ClassWrapperType.tableClass, packageName, className);
	}

	/**
	 * Try to find a class wrapper by resolving the specified className in the scope of the
	 * package provided.
	 *
	 * @return
	 */
	public ClassWrapper findClassWrapper(ClassWrapperType type, String packageName, String className) {
		String matchName = className;
		if(! className.contains("."))
			matchName = packageName + "." + className;

		List<ClassWrapper> partialList = new ArrayList<>();
		for(ClassWrapper cw : m_wrapperList) {
			if(cw.getType() == type) {
				if(cw.getClassName().equals(matchName)) {
					return cw;
				}

				if(!className.contains(".")) {                        // Unqualified?
					if(cw.getClassName().endsWith("." + className)) {
						partialList.add(cw);                        // Partial match
					}
				}
			}
		}

		if(partialList.size() == 0) {
			return null;
		} else if(partialList.size() == 1) {
			return partialList.get(0);
		}
		error("Cannot resolve class name for class '" + className + "' relative to package '" + packageName + "'");
		return null;
	}

	public Set<String> getAltBundles() {
		return m_altBundles;
	}

	/**
	 * Find the node for the specified table.
	 * @param table
	 * @return
	 */
	public Node getTableConfig(DbTable table) {
		String tblname = table.getSchema().getName() + "." + table.getName();

		NodeList childNodes = m_configRoot.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if("table".equals(item.getNodeName())) {
				String name = DomTools.strAttr(item, "name");
				if(tblname.equals(name)) {
					return item;
				}
			}
		}

		//-- Create it
		Node node = m_configDocument.createElement("table");
		m_configRoot.appendChild(node);
		Node value = m_configDocument.createAttribute("name");
		value.setNodeValue(tblname);
		node.getAttributes().setNamedItem(value);
		return node;
	}

	/**
	 * Set a property as "*value" if it is nonexistent or has a * value.
	 * @param table
	 * @param property
	 * @param value
	 */
	public void setTableConfigProperty(DbTable table, String property, String value) {
		Node tc = getTableConfig(table);
		String v = DomTools.strAttr(tc, property, null);
		if(v == null || v.length() == 0 || v.startsWith("*")) {
			DomTools.setAttr(tc, property, "*" + value);
		}
	}

	public String getTableConfigProperty(DbTable table, String property) {
		Node tc = getTableConfig(table);
		return getOrCreateNodeValue(tc, property);
	}

	static String getOrCreateNodeValue(Node tc, String property) {
		String v = DomTools.strAttr(tc, property, null);
		if(null != v) {
			return v.length() == 0 || v.startsWith("*") ? null : v;
		}

		Node value = tc.getOwnerDocument().createAttribute(property);
		value.setNodeValue("");
		tc.getAttributes().setNamedItem(value);
		return null;
	}

	public boolean isSkipBaseClasses() {
		return m_skipBaseClasses;
	}

	public boolean isMatchBaseClassesOnColumnNameOnly() {
		return m_matchBaseClassesOnColumnNameOnly;
	}

	public boolean isAddIdentifyable() {
		return m_addIdentifyable;
	}

	public boolean isForcePkToLong() {
		return m_forcePkToLong;
	}

	/**
	 * If base classes are found, and if the columns they expose match those in the
	 * actual table then return the first matching baseclass.
	 */
	@Nullable
	public ClassWrapper findBaseClassFor(ClassWrapper other) {
		if(isSkipBaseClasses())
			return null;
		for(ClassWrapper classWrapper : getBaseClasses()) {
			if(classWrapper.baseClassMatchesTable(other)) {
				return classWrapper;
			}
		}
		return null;
	}


	public void setSchemaAsPackage(boolean schemaAsPackage) {
		m_schemaAsPackage = schemaAsPackage;
	}

	public void setFieldPrefix(String fieldPrefix) {
		m_fieldPrefix = fieldPrefix;
	}

	public void setAddIdentifyable(boolean addIdentifyable) {
		m_addIdentifyable = addIdentifyable;
	}

	public void setHideSchemaNameFromTableName(boolean hideSchemaNameFromTableName) {
		m_hideSchemaNameFromTableName = hideSchemaNameFromTableName;
	}

	public void setAddSchemaNameToClassName(boolean addSchemaNameToClassName) {
		m_addSchemaNameToClassName = addSchemaNameToClassName;
	}

	public void setForceRenameFields(boolean forceRenameFields) {
		m_forceRenameFields = forceRenameFields;
	}

	public void setForceRenameMethods(boolean forceRenameMethods) {
		m_forceRenameMethods = forceRenameMethods;
	}

	public void setMapOneCharVarcharToBoolean(boolean mapOneCharVarcharToBoolean) {
		m_mapOneCharVarcharToBoolean = mapOneCharVarcharToBoolean;
	}

	public void setSkipBundles(boolean skipBundles) {
		m_skipBundles = skipBundles;
	}

	public void setSkipBaseClasses(boolean skipBaseClasses) {
		m_skipBaseClasses = skipBaseClasses;
	}

	public void setForcePkToLong(boolean forcePkToLong) {
		m_forcePkToLong = forcePkToLong;
	}

	public void setMatchBaseClassesOnColumnNameOnly(boolean matchBaseClassesOnColumnNameOnly) {
		m_matchBaseClassesOnColumnNameOnly = matchBaseClassesOnColumnNameOnly;
	}

	public void setDestroyConstructors(boolean destroyConstructors) {
		m_destroyConstructors = destroyConstructors;
	}

	public void setAltBundles(Set<String> altBundles) {
		m_altBundles = altBundles;
	}

	public void setForcePKIdentifier(String forcePKIdentifier) {
		m_forcePKIdentifier = forcePKIdentifier;
	}

	public void setReplaceSerialWithSequence(boolean replaceSerialWithSequence) {
		m_replaceSerialWithSequence = replaceSerialWithSequence;
	}

	public void setAppendSchemaNameInAnnotations(boolean appendSchemaNameInAnnotations) {
		m_appendSchemaNameInAnnotations = appendSchemaNameInAnnotations;
	}

	public boolean isVerbose() {
		return m_verbose;
	}

	public void setVerbose(boolean verbose) {
		m_verbose = verbose;
	}

	public Set<String> getIgnoreTableSet() {
		return m_ignoreTableSet;
	}

	public void ignoreTable(String name) {
		m_ignoreTableSet.add(name.toLowerCase());
	}

	public int getEnumMaxFieldSize() {
		return m_enumMaxFieldSize;
	}

	public void setEnumMaxFieldSize(int enumMaxFieldSize) {
		m_enumMaxFieldSize = enumMaxFieldSize;
	}
}

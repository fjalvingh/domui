package to.etc.domui.hibgen;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import to.etc.util.DbConnectionInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-9-17.
 */
public class HibernateGenerator {
	enum DbType {
		postgres,
		oracle
	}

	@Option(name = "-db", usage = "The connection string to the database, as accepted by the specific database version", required = true)
	private String m_dbUrl;

	@Option(name = "-dbtype", usage = "The type of database (oracle, postgresql)")
	private DbType m_dbType = DbType.postgres;

	@Option(name = "-source", usage = "The root directory for the generated/modified java sources (without the package name)", required = true)
	private File m_targetDirectory;

	@Option(name = "-pkgroot", usage = "The root Java package where the tables will be generated", required = true)
	private String m_packageName;

	@Option(name = "-s", aliases = {"-schema"}, usage = "One or more schema names to include in the reverse action", required = true)
	private List<String> m_schemaSet = new ArrayList<>();

	@Option(name = "-i", aliases = {"-ignore"}, usage = "Ignore a table")
	private List<String> m_ignoreTableSet = new ArrayList<>();

	@Option(name = "-schema-package", usage = "When set, this adds the schema name as the last level to the default package")
	private boolean m_schemaAsPackage;

	@Option(name = "-field-prefix", usage = "Defines a prefix for generated fields. Defaults to 'm_', to remove use -fieldPrefix none")
	private String m_fieldPrefix = "m_";

	@Option(name = "-noi", aliases = {"-no-identifyable"}, usage = "When set this skips adding IIdentifyable<T> to each class (where T is the proper type for the @Id property)")
	private boolean m_skipIdentifyable;

	@Option(name = "-no-remove-schema", usage = "By default, if a table name starts with the schema name that name is removed from the table name. This option leaves the name intact.")
	private boolean m_skipRemoveSchemaNameFromTableName;

	@Option(name = "-asc", aliases = {"-add-schema-classname"}, usage = "Add the schema name to the generated class name")
	private boolean m_addSchemaNameToClassName;

	@Option(name = "-ffr", aliases = {"-force-field-rename"}, usage = "When set, all fields are renamed even when they occur in pre-existing classes")
	private boolean m_forceRenameFields;

	@Option(name = "-fmr", aliases = {"-force-method-rename"}, usage = "Rename getter and setter methods in existing pojos")
	private boolean m_forceRenameMethods;

	@Option(name = "-no-onechar-boolean", usage = "By default the code maps varchar(1) and char(1) with <= 2 values as boolean. This option prevents that.")
	private boolean m_skipMapOneCharVarcharToBoolean;

	@Option(name = "-nb", aliases = {"-no-bundles"}, usage = "Skips the generation/update of the .properties files for each class")
	private boolean m_skipBundles;

	@Option(name = "-no-baseclass", usage = "Does not scan for base classes that can be used for new entities")
	private boolean m_skipBaseClasses;

	@Option(name = "-keep-pktype", usage = "By default numeric primary keys < 18 precision are always mapped to Long, because using Integer is often not a good plan. But if you really want them set this option.")
	private boolean m_skipForcePkToLong;

	/** When T, this does not check for the same type on columns to attach the base class. */
	@Option(name = "-match-columns-only", usage = "When set, this matches base classes by using column names only, not column types")
	private boolean m_matchBaseClassesOnColumnNameOnly;

	@Option(name = "-destroy-constructors", usage = "When set, this removes all constructors of existing POJO's; used to get rid of the silly constructors made by Hibernate's POJO generator")
	private boolean m_destroyConstructors;

	@Option(name = "-bundles", usage = "Each occurrence will add a new bundle language to be generaed, i.e. -bundle nl_NL adds that as an extra bundle")
	private List<String> m_altBundles = new ArrayList<>();

	@Option(name = "-pkname", usage = "By default the PK name is forced to be 'id' regardless of the name of the database column. Set this to none to remove that")
	private String m_forcePKIdentifier = "id";

	/** Postgres: when T, a Hibernate sequence identifier generator is used instead of the @Generated(GenerationType.IDENTIFIER) for an autoincrement column. */
	@Option(name = "-no-deserial", usage = "By default postgreSQL 'serial' columns are generated as a GenerationType.SEQUENCE, so that PKs can be generated before insert. This option disables that.")
	private boolean m_skipReplaceSerialWithSequence;

	/** When T this always appends a schema name, when F it only adds it if there are more than one schemas scanned. */
	@Option(name = "-append-schema-name", aliases = {"-as"}, usage = "When present this always appends a schema name in @Table annotations, when F it only adds it if there are more than one schemas scanned.")
	private boolean m_appendSchemaNameInAnnotations;

	@Option(name = "-enum-max-field-size", aliases = {"-emfs"}, usage = "Set the max size for fields to be scanned for enum values")
	private int m_enumMaxFieldSize = 20;

	@Option(name = "-verbose", usage = "Show a lot of text about why decisions are made")
	private boolean m_verbose;

	private void run(String[] args) throws Exception {
		CmdLineParser p = new CmdLineParser(this);
		try {
			//-- Decode the tasks's arguments
			p.parseArgument(args);
		} catch (CmdLineException x) {
			System.err.println("Invalid arguments: " + x.getMessage());
			System.err.println("Usage:");
			p.printUsage(System.err);
			System.exit(10);
		}

		String dbUrl = Objects.requireNonNull(m_dbUrl);
		DbConnectionInfo url = DbConnectionInfo.decode(dbUrl);

		AbstractGenerator generator;
		switch(m_dbType) {
			default:
				throw new IllegalStateException(m_dbType + ": unsupported");

			case postgres:
				generator = new PostgresGenerator(url);
				break;

			case oracle:
				generator = new OracleGenerator(url);
				break;
		}

		generator.setPackageName(m_packageName);
		generator.setSourceDirectory(m_targetDirectory);
		generator.setSchemaAsPackage(m_schemaAsPackage);
		if(m_fieldPrefix.equalsIgnoreCase("none")) {
			generator.setFieldPrefix(null);
		} else {
			generator.setFieldPrefix(m_fieldPrefix);
		}
		generator.setAddIdentifyable(! m_skipIdentifyable);
		generator.setHideSchemaNameFromTableName(m_skipRemoveSchemaNameFromTableName);
		generator.setAddSchemaNameToClassName(m_addSchemaNameToClassName);
		generator.setForceRenameFields(m_forceRenameFields);
		generator.setForceRenameMethods(m_forceRenameMethods);
		generator.setMapOneCharVarcharToBoolean(! m_skipMapOneCharVarcharToBoolean);
		generator.setSkipBundles(m_skipBundles);
		generator.setSkipBaseClasses(m_skipBaseClasses);
		generator.setForcePkToLong(! m_skipForcePkToLong);
		generator.setMatchBaseClassesOnColumnNameOnly(m_matchBaseClassesOnColumnNameOnly);
		generator.setDestroyConstructors(m_destroyConstructors);
		generator.setAltBundles(new HashSet<>(m_altBundles));
		generator.setEnumMaxFieldSize(m_enumMaxFieldSize);
		if(m_forcePKIdentifier.equalsIgnoreCase("none")) {
			generator.setForcePKIdentifier(null);
		} else {
			generator.setForcePKIdentifier(m_forcePKIdentifier);
		}
		generator.setReplaceSerialWithSequence(! m_skipReplaceSerialWithSequence);
		generator.setAppendSchemaNameInAnnotations(m_appendSchemaNameInAnnotations);
		generator.setVerbose(m_verbose);
		m_ignoreTableSet.forEach(t -> generator.ignoreTable(t));

		try {
			generator.generate(m_schemaSet);


		} finally {
			generator.close();
		}
	}

	static public void main(String[] args) throws Exception {
		new HibernateGenerator().run(args);
	}
}

package to.etc.domui.hibgen;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import to.etc.dbutil.schema.DbSchema;
import to.etc.util.FileTool;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
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

	private Map<String, ClassWrapper> m_byClassnameMap = new HashMap<>();

	abstract protected Connection createConnection() throws Exception;

	protected abstract Set<DbSchema> loadSchemas(List<String> schemaSet) throws Exception;

	public void generate(List<String> schemaSet) throws Exception {
		createConnection();								// Fast test whether db can be opened
		m_schemaSet = loadSchemas(schemaSet);
		loadJavaSources();
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

			//} catch(ParseException px) {
		//	System.out.println(px.toString());
		} catch(FileNotFoundException e) {
			error(file, "Cannot load file");
		}
	}

	private void error(File file, String msg) {
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
}

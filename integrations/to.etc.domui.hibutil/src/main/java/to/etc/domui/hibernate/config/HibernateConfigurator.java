/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.hibernate.config;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import to.etc.domui.hibernate.generic.BuggyHibernateBaseContext;
import to.etc.webapp.query.IQueryExecutorFactory;
import to.etc.webapp.query.IQueryListener;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QDataContextFactory;

import javax.sql.DataSource;
import java.io.File;
import java.util.List;

/**
 * Helper class to help with configuring Hibernate for DomUI easily. You are not required to
 * use this class at all; "normal" hibernate configuration works as "well" (meh), but this
 * tries to hide lots of boilerplate needed to configure properly while only exposing that
 * which is really needed in most cases.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 30, 2010
 */
@NonNullByDefault
final public class HibernateConfigurator {
	/** The default, statically reachable Hibernate config */
	private final static HibernateConfiguratorInstance m_instance = new HibernateConfiguratorInstance();

	///** This is the DataSource that will provide all Hibernate connections for us. */
	//static private DataSource m_dataSource;
	//
	///** The session factory created after initialization. */
	//static private SessionFactory m_sessionFactory;
	//
	///** The DomUI Context source */
	//static private QDataContextFactory m_contextSource;
	//
	///** All classes registered as part of the config. */
	//static final private List<Class<?>> m_annotatedClassList = new ArrayList<Class<?>>();
	//
	///** When non-null, the user has set the "show sql" option. When unset it defaults to the DeveloperOptions setting. */
	//static private Boolean m_showSQL;
	//
	///** The event listener set defined for DomUI. */
	//static private QEventListenerSet m_listeners = new QEventListenerSet();
	//
	///** The registered query handlers for DomUI */
	//static private QQueryExecutorRegistry m_handlers = new QQueryExecutorRegistry();
	//
	//static private List<IHibernateConfigListener> m_onConfigureList = Collections.emptyList();
	//
	//private static boolean m_allowHibernateHiloSequences;
	//
	//private final static Map<String, String> m_hibernateOptions = new HashMap<>();
	//
	//@Nullable
	//private static InterceptorFactory m_interceptorFactory;

	/**
	 * Defines the database update mode (hibernate.hbm2ddl.auto).
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Dec 30, 2010
	 */
	public enum Mode {
		/**
		 * Assume the database is correct and the same as the Hibernate expected schema.
		 */
		NONE,

		/**
		 * Check table definitions and alter the database to correspond to the Hibernate schema as much as possible.
		 */
		UPDATE,

		/**
		 * DANGEROUS: drop the entire database AND ITS DATA, and recreate all tables.
		 */
		CREATE
	}

	///**
	// * The database creation/update mode.
	// */
	//static private Mode m_mode = Mode.NONE;
	//
	//static private boolean m_observableEnabled;
	//
	//static private boolean m_beforeImagesEnabled;
	//

	/*--------------------------------------------------------------*/
	/*	CODING:	Accessing the completed configuration's data.		*/
	/*--------------------------------------------------------------*/
	public static List<Class<?>> getAnnotatedClassList() {
		return m_instance.getAnnotatedClassList();
	}

	static public void setHibernateOption(String option, String value) {
		m_instance.setHibernateOption(option, value);
	}

	/**
	 * Return the datasource, as configured.
	 */
	@NonNull
	static synchronized public DataSource getDataSource() {
		return m_instance.getDataSource();
	}

	/**
	 * Return the Hibernate SessionFactory created by this code. Should not normally be used by common user code.
	 */
	public synchronized static SessionFactory getSessionFactory() {
		return m_instance.getSessionFactory();
	}

	/**
	 * Unwrap the QDataContext and obtain it's Hibernate {@link Session} record.
	 */
	public static Session internalGetSession(final QDataContext dc) throws Exception {
		return m_instance.internalGetSession(dc);
	}

	/**
	 * Returns the data context factory wrapping the hibernate code.
	 */
	public synchronized static QDataContextFactory getDataContextFactory() {
		return m_instance.getDataContextFactory();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Configuration setters.								*/
	/*--------------------------------------------------------------*/

	///**
	// * Abort if we have not yet initialize()d successfully.
	// */
	//static synchronized private void requireConfigured() {
	//	if(null == m_sessionFactory)
	//		throw new IllegalStateException("This method must be called AFTER one of the 'initialize' methods gets called.");
	//}

	static public synchronized void addConfigListener(IHibernateConfigListener listener) {
		m_instance.addConfigListener(listener);
	}

	/**
	 * Must be called before one of the "initialize" methods gets called, to register
	 * all POJO classes that need to be configured with Hibernate. The classes will
	 * be added to the AnnotationConfiguration for Hibernate when initialize() is
	 * called. You can call this as many times as needed; all classes are <i>added</i> to
	 * a list.
	 */
	static public void addClasses(Class<?>... classes) {
		m_instance.addClasses(classes);
	}

	/**
	 * Set the "show sql" setting for hibernate. When called it overrides any "developer.properties" setting.
	 */
	static public void showSQL(boolean on) {
		m_instance.showSQL(on);
	}

	/**
	 * Set the "schema update" mode for Hibernate (corresponding to hbm2ddl.auto). It defaults to NONE. When
	 * set to UPDATE Hibernate will do it's best to change the database schema in such a way that it corresponds
	 * to the annotated classes' definition.
	 */
	static public void schemaUpdate(@NonNull Mode m) {
		m_instance.schemaUpdate(m);
	}

	/**
	 * Register a DomUI {@link IQueryListener} that will be called when DomUI executes {@link QCriteria} queries.
	 */
	static public void registerQueryListener(IQueryListener ql) {
		m_instance.registerQueryListener(ql);
	}

	/**
	 * Register an alternative {@link IQueryExecutorFactory} which can execute QCriteria queries on non-hibernate
	 * classes. <b>WARNING</b>: if
	 * you use this call <i>all default query executors are not registered</i>. This allows you to override them if needed. It also
	 * means that <i>no Hibernate QCriteria code works at all</i> if you do not add them! The default factories should be registered
	 * as follows:
	 * <pre>
	 * registerQueryListener(JdbcQueryExecutor.FACTORY);
	 * registerQueryListener(HibernateQueryExecutor.FACTORY);
	 * </pre>
	 * By ordering your executors with the default ones you can control the order of acceptance for queries.
	 */
	static public void registerQueryExecutor(IQueryExecutorFactory qexecutor) {
		m_instance.registerQueryExecutor(qexecutor);
	}

	static public void enableBeforeImages(boolean yes) {
		m_instance.enableBeforeImages(yes);
	}

	static public void enableObservableCollections(boolean yes) {
		m_instance.enableObservableCollections(yes);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Main initialization entrypoints.					*/
	/*--------------------------------------------------------------*/

	/**
	 * Main worker to initialize the database layer, using Hibernate, with a user-specified core data source. This
	 * code also enables SQL logging when .developer.properties option hibernate.sql=true.
	 */
	public synchronized static void initialize(final DataSource ds) throws Exception {
		m_instance.initialize(ds);
	}

	/**
	 * Alternate entrypoint: initialize the layer using a poolID in the default poolfile.
	 */
	public static void initialize(final String poolname) throws Exception {
		m_instance.initialize(poolname);
	}

	/**
	 * Initialize the layer using a poolid in the specified poolfile.
	 */
	public static void initialize(final File poolfile, final String poolname) throws Exception {
		m_instance.initialize(poolfile, poolname);
	}

	/**
	 * The configurator forces hibernate to obey sequence rules proper so that
	 * interaction with existing application just works. When you are sure that
	 * no other applications update the database or if all of those also use
	 * the same hilo mechanism setting this to TRUE will greatly increase insert
	 * performance.
	 */
	public static void setAllowHiloSequences(boolean allowHibernateSuckySequences) {
		m_instance.setAllowHiloSequences(allowHibernateSuckySequences);

	}

	public static void setInterceptorFactory(InterceptorFactory interceptor) {
		m_instance.setInterceptorFactory(interceptor);
	}

	public interface InterceptorFactory {
		Interceptor create(BuggyHibernateBaseContext cx);
	}
}

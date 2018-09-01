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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.jpa.event.spi.JpaIntegrator;
import org.hibernate.service.ServiceRegistry;
import to.etc.dbpool.ConnectionPool;
import to.etc.dbpool.PoolManager;
import to.etc.domui.hibernate.beforeimages.BeforeImageInterceptor;
import to.etc.domui.hibernate.beforeimages.CopyCollectionEventListener;
import to.etc.domui.hibernate.beforeimages.CreateBeforeImagePostLoadListener;
import to.etc.domui.hibernate.generic.BuggyHibernateBaseContext;
import to.etc.domui.hibernate.generic.HibernateLongSessionContextFactory;
import to.etc.domui.hibernate.generic.HibernateQueryExecutor;
import to.etc.domui.hibernate.generic.HibernateSessionMaker;
import to.etc.util.DeveloperOptions;
import to.etc.util.StringTool;
import to.etc.webapp.qsql.JdbcQueryExecutor;
import to.etc.webapp.query.IQueryExecutorFactory;
import to.etc.webapp.query.IQueryListener;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QDataContextFactory;
import to.etc.webapp.query.QEventListenerSet;
import to.etc.webapp.query.QQueryExecutorRegistry;

import javax.sql.DataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
	/** This is the DataSource that will provide all Hibernate connections for us. */
	static private DataSource m_dataSource;

	/** The session factory created after initialization. */
	static private SessionFactory m_sessionFactory;

	/** The DomUI Context source */
	static private QDataContextFactory m_contextSource;

	/** All classes registered as part of the config. */
	static private List<Class<?>> m_annotatedClassList = new ArrayList<Class<?>>();

	/** When non-null, the user has set the "show sql" option. When unset it defaults to the DeveloperOptions setting. */
	static private Boolean m_showSQL;

	/** The event listener set defined for DomUI. */
	static private QEventListenerSet m_listeners = new QEventListenerSet();

	/** The registered query handlers for DomUI */
	static private QQueryExecutorRegistry m_handlers = new QQueryExecutorRegistry();

	static private List<IHibernateConfigListener> m_onConfigureList = Collections.emptyList();

	private static boolean m_allowHibernateHiloSequences;

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
		NONE

		/**
		 * Check table definitions and alter the database to correspond to the Hibernate schema as much as possible.
		 */
		, UPDATE

		/**
		 * DANGEROUS: drop the entire database AND ITS DATA, and recreate all tables.
		 */
		, CREATE
	}

	/**
	 * The database creation/update mode.
	 */
	static private Mode m_mode = Mode.NONE;

	static private boolean m_observableEnabled;

	static private boolean m_beforeImagesEnabled;

	/*--------------------------------------------------------------*/
	/*	CODING:	Accessing the completed configuration's data.		*/
	/*--------------------------------------------------------------*/

	/**
	 * Return the datasource, as configured.
	 */
	@NonNull
	static synchronized public DataSource getDataSource() {
		if(null == m_dataSource)
			throw new IllegalStateException("The initialize() method has not yet been called with a valid datasource");
		return m_dataSource;
	}

	/**
	 * Return the Hibernate SessionFactory created by this code. Should not normally be used by common user code.
	 */
	public synchronized static SessionFactory getSessionFactory() {
		unconfigured();
		return m_sessionFactory;
	}

	/**
	 * Unwrap the QDataContext and obtain it's Hibernate {@link Session} record.
	 */
	public static Session internalGetSession(final QDataContext dc) throws Exception {
		return ((BuggyHibernateBaseContext) dc).getSession();
	}

	/**
	 * Returns the data context factory wrapping the hibernate code.
	 */
	public synchronized static QDataContextFactory getDataContextFactory() {
		unconfigured();
		return m_contextSource;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Configuration setters.								*/
	/*--------------------------------------------------------------*/

	/**
	 * Abort if initialize() has already completed.
	 */
	static synchronized private void configured() {
		if(null != m_sessionFactory)
			throw new IllegalStateException("This method must be called BEFORE one of the 'initialize' methods gets called.");
	}

	/**
	 * Abort if we have not yet initialize()d successfully.
	 */
	static synchronized private void unconfigured() {
		if(null == m_sessionFactory)
			throw new IllegalStateException("This method must be called AFTER one of the 'initialize' methods gets called.");
	}

	static public synchronized void addConfigListener(IHibernateConfigListener listener) {
		m_onConfigureList = new ArrayList<>(m_onConfigureList);
		m_onConfigureList.add(listener);
	}

	/**
	 * Must be called before one of the "initialize" methods gets called, to register
	 * all POJO classes that need to be configured with Hibernate. The classes will
	 * be added to the AnnotationConfiguration for Hibernate when initialize() is
	 * called. You can call this as many times as needed; all classes are <i>added</i> to
	 * a list.
	 */
	static public void addClasses(Class<?>... classes) {
		configured();
		for(Class<?> clz : classes)
			m_annotatedClassList.add(clz);
	}

	/**
	 * Set the "show sql" setting for hibernate. When called it overrides any "developer.properties" setting.
	 */
	static public void showSQL(boolean on) {
		configured();
		m_showSQL = Boolean.valueOf(on);
	}

	/**
	 * Set the "schema update" mode for Hibernate (corresponding to hbm2ddl.auto). It defaults to NONE. When
	 * set to UPDATE Hibernate will do it's best to change the database schema in such a way that it corresponds
	 * to the annotated classes' definition.
	 */
	static public void schemaUpdate(@NonNull Mode m) {
		configured();
		m_mode = m;
	}

	/**
	 * Register a DomUI {@link IQueryListener} that will be called when DomUI executes {@link QCriteria} queries.
	 */
	static public void registerQueryListener(IQueryListener ql) {
		configured();
		m_listeners.addQueryListener(ql);
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
		configured();
		m_handlers.register(qexecutor);
	}

	static public void enableBeforeImages(boolean yes) {
		configured();
		m_beforeImagesEnabled = yes;
	}

	static public void enableObservableCollections(boolean yes) {
		configured();
		m_observableEnabled = yes;
	}

	static private void enhanceMappings(@NonNull Metadata metaData) throws Exception {
		HibernateChecker hc = new HibernateChecker(metaData, DeveloperOptions.isDeveloperWorkstation(), m_observableEnabled, m_allowHibernateHiloSequences);
		hc.enhanceMappings();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Main initialization entrypoints.					*/
	/*--------------------------------------------------------------*/

	/**
	 * Main worker to initialize the database layer, using Hibernate, with a user-specified core data source. This
	 * code also enables SQL logging when .developer.properties option hibernate.sql=true.
	 */
	public synchronized static void initialize(final DataSource ds) throws Exception {
		if(m_sessionFactory != null)
			throw new IllegalStateException("HibernateConfigurator has already been initialized!");
		if(m_annotatedClassList.size() == 0)
			throw new IllegalStateException("Please call addClasses(Class<?>...) and register your Hibernate data classes before calling me.");

		long ts = System.nanoTime();
		m_dataSource = ds;

		// see https://www.boraji.com/hibernate-5-event-listener-example

		//-- Create Hibernate's config. See https://docs.jboss.org/hibernate/orm/5.1/userguide/html_single/chapters/bootstrap/Bootstrap.html
		/*
		 * Hibernate apparently cannot initialize without the useless hibernate.cfg.xml file. We cannot
		 * add that file at the root location because that would interfere with applications. To have a
		 * working model we add it as a resource in this class's package. And of course Hibernate makes
		 * it hard to reach- we need to calculate the proper name, sigh.
		 */
		BootstrapServiceRegistry bootstrapRegistry =
			new BootstrapServiceRegistryBuilder()
				.applyIntegrator(new JpaIntegrator())
				.build();

		String resname = "/" + HibernateConfigurator.class.getPackage().getName().replace('.', '/') + "/hibernate.cfg.xml";
		StandardServiceRegistryBuilder serviceBuilder = new StandardServiceRegistryBuilder(bootstrapRegistry)
			.configure(resname)
			;

		/*
		 * Set other properties according to config settings made.
		 */
		serviceBuilder.applySetting("hibernate.connection.datasource", ds);
		boolean logsql;
		if(m_showSQL == null)
			logsql = DeveloperOptions.getBool("hibernate.sql", false); // Take default from .developer.properties
		else
			logsql = m_showSQL.booleanValue();

		if(logsql) {
			serviceBuilder.applySetting("show_sql", "true");
			serviceBuilder.applySetting("hibernate.show_sql", "true");
		}

		/*
		 * Hibernate defaults to completely non-standard behavior for sequences, using the
		 * "hilo" sequence generator by default. This irresponsible behavior means that
		 * by default Hibernate code is incompatible with any code using sequences.
		 * Since that is irresponsible and downright DUMB this reverts the behavior to
		 * using sequences in their normal behavior.
		 * See https://stackoverflow.com/questions/12745751/hibernate-sequencegenerator-and-allocationsize
		 */
		serviceBuilder.applySetting("hibernate.id.new_generator_mappings", "true"); // MUST BE BEFORE config.configure

		if(DeveloperOptions.getBool("hibernate.format_sql", true)) {
			serviceBuilder.applySetting("hibernate.format_sql", "true");
		}

		switch(m_mode){
			default:
				throw new IllegalStateException("Mode: " + m_mode);
			case CREATE:
				serviceBuilder.applySetting("hbm2ddl.auto", "create");
				serviceBuilder.applySetting("hibernate.hbm2ddl.auto", "create");
				break;
			case NONE:
				serviceBuilder.applySetting("hbm2ddl.auto", "none");
				serviceBuilder.applySetting("hibernate.hbm2ddl.auto", "none");
				break;
			case UPDATE:
				serviceBuilder.applySetting("hbm2ddl.auto", "update");
				serviceBuilder.applySetting("hibernate.hbm2ddl.auto", "update");
				break;
		}

		// change settings
		for(IHibernateConfigListener listener : m_onConfigureList) {
			listener.onSettings(serviceBuilder);
		}

		ServiceRegistry reg = serviceBuilder.build();
		MetadataSources sources = new MetadataSources(reg);

		for(Class<?> clz : m_annotatedClassList)
			sources.addAnnotatedClass(clz);

		// add classes
		for(IHibernateConfigListener listener : m_onConfigureList) {
			listener.onAddSources(sources);
		}

		m_annotatedClassList = null; 							// Release memory- list is never used.
		Metadata metaData = sources.getMetadataBuilder()
			.applyImplicitNamingStrategy(ImplicitNamingStrategyJpaCompliantImpl.INSTANCE)
			.build();

		enhanceMappings(metaData);

		//for(Consumer<Configuration> listener : m_onConfigureList) {
		//	listener.accept(config);
		//}

		//-- Create the session factory: this completes the Hibernate config part.
		SessionFactoryBuilder sessionFactoryBuilder = metaData.getSessionFactoryBuilder();

//		sessionFactoryBuilder.applyInterceptor( new CustomSessionFactoryInterceptor() );

		//sessionFactoryBuilder.addSessionFactoryObservers( new CustomSessionFactoryObserver() );

		// Apply a CDI BeanManager ( for JPA event listeners )
		//sessionFactoryBuilder.applyBeanManager( getBeanManager() );

		SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) sessionFactoryBuilder.build();
		m_sessionFactory = sessionFactory;

		EventListenerRegistry listenerRegistry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
		if(m_beforeImagesEnabled) {
			// https://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/chapters/events/Events.html
			listenerRegistry.prependListeners(EventType.POST_LOAD, new CreateBeforeImagePostLoadListener());
			listenerRegistry.prependListeners(EventType.INIT_COLLECTION, new CopyCollectionEventListener());
		}
		for(IHibernateConfigListener listener : m_onConfigureList) {
			listener.onAddListeners(listenerRegistry);
		}


		//-- Start DomUI/WebApp.core initialization: generalized database layer
		HibernateSessionMaker hsm;
		if(m_beforeImagesEnabled) {
			//-- We need the copy interceptor to handle these.
			hsm = dc -> {
				return m_sessionFactory.withOptions()
					.interceptor(new BeforeImageInterceptor(dc.getBeforeCache()))
					.openSession();
				//return m_sessionFactory.openSession(new BeforeImageInterceptor(dc.getBeforeCache()));
			};
		} else {
			hsm = dc -> m_sessionFactory.openSession();
		}

		//-- If no handlers are registered: register the default ones.
		if(m_handlers.size() == 0) {
			m_handlers.register(JdbcQueryExecutor.FACTORY);
			m_handlers.register(HibernateQueryExecutor.FACTORY);
		}

		m_contextSource = new HibernateLongSessionContextFactory(m_listeners, hsm, m_handlers);
		System.out.println("domui: Hibernate initialization took a whopping " + StringTool.strNanoTime(System.nanoTime() - ts));
	}

	/**
	 * Alternate entrypoint: initialize the layer using a poolID in the default poolfile.
	 */
	public static void initialize(final String poolname) throws Exception {
		ConnectionPool p = PoolManager.getInstance().definePool(poolname);
		initialize(p.getPooledDataSource());
	}

	/**
	 * Initialize the layer using a poolid in the specified poolfile.
	 */
	public static void initialize(final File poolfile, final String poolname) throws Exception {
		ConnectionPool p = PoolManager.getInstance().definePool(poolfile, poolname);
		initialize(p.getPooledDataSource());
	}

	/**
	 * The configurator forces hibernate to obey sequence rules proper so that
	 * interaction with existing application just works. When you are sure that
	 * no other applications update the database or if all of those also use
	 * the same hilo mechanism setting this to TRUE will greatly increase insert
	 * performance.
	 */
	public static void setAllowHiloSequences(boolean allowHibernateSuckySequences) {
		m_allowHibernateHiloSequences = allowHibernateSuckySequences;
	}
}

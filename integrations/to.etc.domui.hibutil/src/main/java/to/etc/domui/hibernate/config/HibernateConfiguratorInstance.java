package to.etc.domui.hibernate.config;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.hibernate.Interceptor;
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
import org.hibernate.service.ServiceRegistry;
import to.etc.dbpool.ConnectionPool;
import to.etc.dbpool.PoolManager;
import to.etc.domui.component.misc.ExceptionDialog;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The configuration/factory for a single Hibernate factory.
 */
final public class HibernateConfiguratorInstance {
	/** This is the DataSource that will provide all Hibernate connections for us. */
	private DataSource m_dataSource;

	/** The session factory created after initialization. */
	private SessionFactory m_sessionFactory;

	/** The DomUI Context source */
	private QDataContextFactory m_contextSource;

	/** All classes registered as part of the config. */
	final private List<Class<?>> m_annotatedClassList = new ArrayList<Class<?>>();

	/** When non-null, the user has set the "show sql" option. When unset it defaults to the DeveloperOptions setting. */
	private Boolean m_showSQL;

	/** The event listener set defined for DomUI. */
	private QEventListenerSet m_listeners = new QEventListenerSet();

	/** The registered query handlers for DomUI */
	private QQueryExecutorRegistry m_handlers = new QQueryExecutorRegistry();

	private List<IHibernateConfigListener> m_onConfigureList = Collections.emptyList();

	private boolean m_allowHibernateHiloSequences;

	private final Map<String, String> m_hibernateOptions = new HashMap<>();

	@Nullable
	private HibernateConfigurator.InterceptorFactory m_interceptorFactory;

	/**
	 * The database creation/update mode.
	 */
	static private HibernateConfigurator.Mode m_mode = HibernateConfigurator.Mode.NONE;

	static private boolean m_observableEnabled;

	static private boolean m_beforeImagesEnabled;

	public List<Class<?>> getAnnotatedClassList() {
		return m_annotatedClassList;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Accessing the completed configuration's data.		*/
	/*--------------------------------------------------------------*/

	public void setHibernateOption(String option, String value) {
		requireUnconfigured();
		m_hibernateOptions.put(option, value);
	}

	/**
	 * Return the datasource, as configured.
	 */
	@NonNull
	synchronized public DataSource getDataSource() {
		if(null == m_dataSource)
			throw new IllegalStateException("The initialize() method has not yet been called with a valid datasource");
		return m_dataSource;
	}

	/**
	 * Return the Hibernate SessionFactory created by this code. Should not normally be used by common user code.
	 */
	public synchronized SessionFactory getSessionFactory() {
		requireConfigured();
		return m_sessionFactory;
	}

	/**
	 * Unwrap the QDataContext and obtain it's Hibernate {@link Session} record.
	 */
	public Session internalGetSession(final QDataContext dc) throws Exception {
		return ((BuggyHibernateBaseContext) dc).getSession();
	}

	/**
	 * Returns the data context factory wrapping the hibernate code.
	 */
	public synchronized QDataContextFactory getDataContextFactory() {
		requireConfigured();
		return m_contextSource;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Configuration setters.								*/
	/*--------------------------------------------------------------*/

	/**
	 * Abort if initialize() has already completed.
	 */
	synchronized private void requireUnconfigured() {
		if(null != m_sessionFactory)
			throw new IllegalStateException("This method must be called BEFORE one of the 'initialize' methods gets called.");
	}

	/**
	 * Abort if we have not yet initialize()d successfully.
	 */
	synchronized private void requireConfigured() {
		if(null == m_sessionFactory)
			throw new IllegalStateException("This method must be called AFTER one of the 'initialize' methods gets called.");
	}

	public synchronized void addConfigListener(IHibernateConfigListener listener) {
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
	public void addClasses(Class<?>... classes) {
		requireUnconfigured();
		Collections.addAll(m_annotatedClassList, classes);
	}

	/**
	 * Set the "show sql" setting for hibernate. When called it overrides any "developer.properties" setting.
	 */
	public void showSQL(boolean on) {
		requireUnconfigured();
		m_showSQL = Boolean.valueOf(on);
	}

	/**
	 * Set the "schema update" mode for Hibernate (corresponding to hbm2ddl.auto). It defaults to NONE. When
	 * set to UPDATE Hibernate will do it's best to change the database schema in such a way that it corresponds
	 * to the annotated classes' definition.
	 */
	public void schemaUpdate(@NonNull HibernateConfigurator.Mode m) {
		requireUnconfigured();
		m_mode = m;
	}

	/**
	 * Register a DomUI {@link IQueryListener} that will be called when DomUI executes {@link QCriteria} queries.
	 */
	public void registerQueryListener(IQueryListener ql) {
		requireUnconfigured();
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
	public void registerQueryExecutor(IQueryExecutorFactory qexecutor) {
		requireUnconfigured();
		m_handlers.register(qexecutor);
	}

	public void enableBeforeImages(boolean yes) {
		requireUnconfigured();
		requireEmptyInterceptor();
		m_beforeImagesEnabled = yes;
		m_interceptorFactory = x -> new BeforeImageInterceptor(x.getBeforeCache());
	}

	public void enableObservableCollections(boolean yes) {
		requireUnconfigured();
		m_observableEnabled = yes;
	}

	private void enhanceMappings(@NonNull Metadata metaData) throws Exception {
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
	public synchronized void initialize(final DataSource ds) throws Exception {
		System.setProperty("org.jboss.logging.provider", "slf4j");		// Thanks to https://stackoverflow.com/questions/11639997/how-do-you-configure-logging-in-hibernate-4-to-use-slf4j
		if(m_sessionFactory != null)
			throw new IllegalStateException("HibernateConfigurator has already been initialized!");
		if(m_annotatedClassList.isEmpty())
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
				//.applyIntegrator(new JpaIntegrator())
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

		m_hibernateOptions.forEach((option, value) -> serviceBuilder.applySetting(option, value));

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
		if(m_interceptorFactory != null) {
			//-- We need the copy interceptor to handle these.
			hsm = dc -> {
				Interceptor interceptor = dc.getAttribute(Interceptor.class);
				//var interceptor = m_interceptorFactory.create(dc);
				//dc.setAttribute(Interceptor.class, interceptor);
				return m_sessionFactory.withOptions()
					.interceptor(interceptor)
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

		m_contextSource = new HibernateLongSessionContextFactory(m_listeners, hsm, m_handlers, this::onContextCreated);
		ExceptionDialog.register(HibernateMessageDecoder::translateHibernateException);

		System.out.println("domui: Hibernate initialization took a whopping " + StringTool.strNanoTime(System.nanoTime() - ts));
	}

	/**
	 * Called whenever a QDataContext is created, this adds whatever is needed to the QDataContext.
	 */
	private void onContextCreated(QDataContext dc) {
		HibernateConfigurator.InterceptorFactory factory = m_interceptorFactory;
		if(null != factory) {
			var interceptor = factory.create((BuggyHibernateBaseContext) dc);
			dc.setAttribute(Interceptor.class, interceptor);
		}
	}

	/**
	 * Alternate entrypoint: initialize the layer using a poolID in the default poolfile.
	 */
	public void initialize(final String poolname) throws Exception {
		ConnectionPool p = PoolManager.getInstance().definePool(poolname);
		initialize(p.getPooledDataSource());
	}

	/**
	 * Initialize the layer using a poolid in the specified poolfile.
	 */
	public void initialize(final File poolfile, final String poolname) throws Exception {
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
	public void setAllowHiloSequences(boolean allowHibernateSuckySequences) {
		m_allowHibernateHiloSequences = allowHibernateSuckySequences;
	}

	public void setInterceptorFactory(HibernateConfigurator.InterceptorFactory interceptor) {
		requireUnconfigured();
		requireEmptyInterceptor();
		m_interceptorFactory = interceptor;
	}

	private void requireEmptyInterceptor() {
		if(m_interceptorFactory != null) {
			throw new IllegalStateException("Interceptor factory already exits. Can't have more than one.");
		}
	}

	public interface InterceptorFactory {
		Interceptor create(BuggyHibernateBaseContext cx);
	}

	public QDataContext createDataContext() throws Exception {
		return getDataContextFactory().getDataContext();
	}
}

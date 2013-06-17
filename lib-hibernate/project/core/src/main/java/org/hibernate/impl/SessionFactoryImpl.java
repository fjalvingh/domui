/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 *
 */
package org.hibernate.impl;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.naming.*;
import javax.sql.*;
import javax.transaction.*;

import org.hibernate.*;
import org.hibernate.cache.*;
import org.hibernate.cache.access.*;
import org.hibernate.cache.impl.*;
import org.hibernate.cfg.*;
import org.hibernate.classic.Session;
import org.hibernate.connection.*;
import org.hibernate.context.*;
import org.hibernate.dialect.*;
import org.hibernate.dialect.function.*;
import org.hibernate.engine.*;
import org.hibernate.engine.query.*;
import org.hibernate.engine.query.sql.*;
import org.hibernate.event.*;
import org.hibernate.exception.*;
import org.hibernate.id.*;
import org.hibernate.jdbc.*;
import org.hibernate.mapping.*;
import org.hibernate.mapping.Collection;
import org.hibernate.metadata.*;
import org.hibernate.persister.*;
import org.hibernate.persister.collection.*;
import org.hibernate.persister.entity.*;
import org.hibernate.pretty.*;
import org.hibernate.proxy.*;
import org.hibernate.stat.*;
import org.hibernate.tool.hbm2ddl.*;
import org.hibernate.transaction.*;
import org.hibernate.tuple.entity.*;
import org.hibernate.type.*;
import org.hibernate.util.*;
import org.slf4j.*;


/**
 * Concrete implementation of the <tt>SessionFactory</tt> interface. Has the following
 * responsibilites
 * <ul>
 * <li>caches configuration settings (immutably)
 * <li>caches "compiled" mappings ie. <tt>EntityPersister</tt>s and
 *     <tt>CollectionPersister</tt>s (immutable)
 * <li>caches "compiled" queries (memory sensitive cache)
 * <li>manages <tt>PreparedStatement</tt>s
 * <li> delegates JDBC <tt>Connection</tt> management to the <tt>ConnectionProvider</tt>
 * <li>factory for instances of <tt>SessionImpl</tt>
 * </ul>
 * This class must appear immutable to clients, even if it does all kinds of caching
 * and pooling under the covers. It is crucial that the class is not only thread
 * safe, but also highly concurrent. Synchronization must be used extremely sparingly.
 *
 * @see org.hibernate.connection.ConnectionProvider
 * @see org.hibernate.classic.Session
 * @see org.hibernate.hql.QueryTranslator
 * @see org.hibernate.persister.entity.EntityPersister
 * @see org.hibernate.persister.collection.CollectionPersister
 * @author Gavin King
 */
public final class SessionFactoryImpl implements SessionFactory, SessionFactoryImplementor {

	private static final Logger log = LoggerFactory.getLogger(SessionFactoryImpl.class);
	private static final IdentifierGenerator UUID_GENERATOR = new UUIDHexGenerator();

	private final String name;
	private final String uuid;

	private final transient Map entityPersisters;
	private final transient Map classMetadata;
	private final transient Map collectionPersisters;
	private final transient Map collectionMetadata;
	private final transient Map collectionRolesByEntityParticipant;
	private final transient Map identifierGenerators;
	private final transient Map namedQueries;
	private final transient Map namedSqlQueries;
	private final transient Map sqlResultSetMappings;
	private final transient Map filters;
	private final transient Map imports;
	private final transient Interceptor interceptor;
	private final transient Settings settings;
	private final transient Properties properties;
	private transient SchemaExport schemaExport;
	private final transient TransactionManager transactionManager;
	private final transient QueryCache queryCache;
	private final transient UpdateTimestampsCache updateTimestampsCache;
	private final transient Map queryCaches;
	private final transient Map allCacheRegions = new HashMap();
	private final transient StatisticsImpl statistics = new StatisticsImpl(this);
	private final transient EventListeners eventListeners;
	private final transient CurrentSessionContext currentSessionContext;
	private final transient EntityNotFoundDelegate entityNotFoundDelegate;
	private final transient SQLFunctionRegistry sqlFunctionRegistry;
	private final transient SessionFactoryObserver observer;
	private final transient HashMap entityNameResolvers = new HashMap();

	private final QueryPlanCache queryPlanCache = new QueryPlanCache( this );

	private transient boolean isClosed = false;

	public SessionFactoryImpl(
			Configuration cfg,
	        Mapping mapping,
	        Settings settings,
	        EventListeners listeners,
			SessionFactoryObserver observer) throws HibernateException {

		log.info("building session factory");

		this.properties = new Properties();
		this.properties.putAll( cfg.getProperties() );
		this.interceptor = cfg.getInterceptor();
		this.settings = settings;
		this.sqlFunctionRegistry = new SQLFunctionRegistry(settings.getDialect(), cfg.getSqlFunctions());
        this.eventListeners = listeners;
		this.observer = observer != null ? observer : new SessionFactoryObserver() {
			public void sessionFactoryCreated(SessionFactory factory) {
			}
			public void sessionFactoryClosed(SessionFactory factory) {
			}
		};
		this.filters = new HashMap();
		this.filters.putAll( cfg.getFilterDefinitions() );

		if ( log.isDebugEnabled() ) {
			log.debug("Session factory constructed with filter configurations : " + filters);
		}

		if ( log.isDebugEnabled() ) {
			log.debug(
					"instantiating session factory with properties: " + properties
			);
		}

		// Caches
		settings.getRegionFactory().start( settings, properties );

		//Generators:

		identifierGenerators = new HashMap();
		Iterator classes = cfg.getClassMappings();
		while ( classes.hasNext() ) {
			PersistentClass model = (PersistentClass) classes.next();
			if ( !model.isInherited() ) {
				IdentifierGenerator generator = model.getIdentifier().createIdentifierGenerator(
						settings.getDialect(),
				        settings.getDefaultCatalogName(),
				        settings.getDefaultSchemaName(),
				        (RootClass) model
					);
				identifierGenerators.put( model.getEntityName(), generator );
			}
		}


		///////////////////////////////////////////////////////////////////////
		// Prepare persisters and link them up with their cache
		// region/access-strategy

		final String cacheRegionPrefix = settings.getCacheRegionPrefix() == null ? "" : settings.getCacheRegionPrefix() + ".";

		entityPersisters = new HashMap();
		Map entityAccessStrategies = new HashMap();
		Map classMeta = new HashMap();
		classes = cfg.getClassMappings();
		while ( classes.hasNext() ) {
			final PersistentClass model = (PersistentClass) classes.next();
			model.prepareTemporaryTables( mapping, settings.getDialect() );
			final String cacheRegionName = cacheRegionPrefix + model.getRootClass().getCacheRegionName();
			// cache region is defined by the root-class in the hierarchy...
			EntityRegionAccessStrategy accessStrategy = ( EntityRegionAccessStrategy ) entityAccessStrategies.get( cacheRegionName );
			if ( accessStrategy == null && settings.isSecondLevelCacheEnabled() ) {
				final AccessType accessType = AccessType.parse( model.getCacheConcurrencyStrategy() );
				if ( accessType != null ) {
					log.trace( "Building cache for entity data [" + model.getEntityName() + "]" );
					EntityRegion entityRegion = settings.getRegionFactory().buildEntityRegion( cacheRegionName, properties, CacheDataDescriptionImpl.decode( model ) );
					accessStrategy = entityRegion.buildAccessStrategy( accessType );
					entityAccessStrategies.put( cacheRegionName, accessStrategy );
					allCacheRegions.put( cacheRegionName, entityRegion );
				}
			}
			EntityPersister cp = PersisterFactory.createClassPersister( model, accessStrategy, this, mapping );
			entityPersisters.put( model.getEntityName(), cp );
			classMeta.put( model.getEntityName(), cp.getClassMetadata() );
		}
		classMetadata = Collections.unmodifiableMap(classMeta);

		Map tmpEntityToCollectionRoleMap = new HashMap();
		collectionPersisters = new HashMap();
		Iterator collections = cfg.getCollectionMappings();
		while ( collections.hasNext() ) {
			Collection model = (Collection) collections.next();
			final String cacheRegionName = cacheRegionPrefix + model.getCacheRegionName();
			final AccessType accessType = AccessType.parse( model.getCacheConcurrencyStrategy() );
			CollectionRegionAccessStrategy accessStrategy = null;
			if ( accessType != null && settings.isSecondLevelCacheEnabled() ) {
				log.trace( "Building cache for collection data [" + model.getRole() + "]" );
				CollectionRegion collectionRegion = settings.getRegionFactory().buildCollectionRegion( cacheRegionName, properties, CacheDataDescriptionImpl.decode( model ) );
				accessStrategy = collectionRegion.buildAccessStrategy( accessType );
				entityAccessStrategies.put( cacheRegionName, accessStrategy );
				allCacheRegions.put( cacheRegionName, collectionRegion );
			}
			CollectionPersister persister = PersisterFactory.createCollectionPersister( cfg, model, accessStrategy, this) ;
			collectionPersisters.put( model.getRole(), persister.getCollectionMetadata() );
			Type indexType = persister.getIndexType();
			if ( indexType != null && indexType.isAssociationType() && !indexType.isAnyType() ) {
				String entityName = ( ( AssociationType ) indexType ).getAssociatedEntityName( this );
				Set roles = ( Set ) tmpEntityToCollectionRoleMap.get( entityName );
				if ( roles == null ) {
					roles = new HashSet();
					tmpEntityToCollectionRoleMap.put( entityName, roles );
				}
				roles.add( persister.getRole() );
			}
			Type elementType = persister.getElementType();
			if ( elementType.isAssociationType() && !elementType.isAnyType() ) {
				String entityName = ( ( AssociationType ) elementType ).getAssociatedEntityName( this );
				Set roles = ( Set ) tmpEntityToCollectionRoleMap.get( entityName );
				if ( roles == null ) {
					roles = new HashSet();
					tmpEntityToCollectionRoleMap.put( entityName, roles );
				}
				roles.add( persister.getRole() );
			}
		}
		collectionMetadata = Collections.unmodifiableMap(collectionPersisters);
		Iterator itr = tmpEntityToCollectionRoleMap.entrySet().iterator();
		while ( itr.hasNext() ) {
			final Map.Entry entry = ( Map.Entry ) itr.next();
			entry.setValue( Collections.unmodifiableSet( ( Set ) entry.getValue() ) );
		}
		collectionRolesByEntityParticipant = Collections.unmodifiableMap( tmpEntityToCollectionRoleMap );

		//Named Queries:
		namedQueries = new HashMap( cfg.getNamedQueries() );
		namedSqlQueries = new HashMap( cfg.getNamedSQLQueries() );
		sqlResultSetMappings = new HashMap( cfg.getSqlResultSetMappings() );
		imports = new HashMap( cfg.getImports() );

		// after *all* persisters and named queries are registered
		Iterator iter = entityPersisters.values().iterator();
		while ( iter.hasNext() ) {
			final EntityPersister persister = ( ( EntityPersister ) iter.next() );
			persister.postInstantiate();
			registerEntityNameResolvers( persister );

		}
		iter = collectionPersisters.values().iterator();
		while ( iter.hasNext() ) {
			final CollectionPersister persister = ( ( CollectionPersister ) iter.next() );
			persister.postInstantiate();
		}

		//JNDI + Serialization:

		name = settings.getSessionFactoryName();
		try {
			uuid = (String) UUID_GENERATOR.generate(null, null);
		}
		catch (Exception e) {
			throw new AssertionFailure("Could not generate UUID");
		}
		SessionFactoryObjectFactory.addInstance(uuid, name, this, properties);

		log.debug("instantiated session factory");

		if ( settings.isAutoCreateSchema() ) {
			new SchemaExport( cfg, settings ).create( false, true );
		}
		if ( settings.isAutoUpdateSchema() ) {
			new SchemaUpdate( cfg, settings ).execute( false, true );
		}
		if ( settings.isAutoValidateSchema() ) {
			new SchemaValidator( cfg, settings ).validate();
		}
		if ( settings.isAutoDropSchema() ) {
			schemaExport = new SchemaExport( cfg, settings );
		}

		if ( settings.getTransactionManagerLookup()!=null ) {
			log.debug("obtaining JTA TransactionManager");
			transactionManager = settings.getTransactionManagerLookup().getTransactionManager(properties);
		}
		else {
			if ( settings.getTransactionFactory().isTransactionManagerRequired() ) {
				throw new HibernateException("The chosen transaction strategy requires access to the JTA TransactionManager");
			}
			transactionManager = null;
		}

		currentSessionContext = buildCurrentSessionContext();

		if ( settings.isQueryCacheEnabled() ) {
			updateTimestampsCache = new UpdateTimestampsCache(settings, properties);
			queryCache = settings.getQueryCacheFactory()
			        .getQueryCache(null, updateTimestampsCache, settings, properties);
			queryCaches = new HashMap();
			allCacheRegions.put( updateTimestampsCache.getRegion().getName(), updateTimestampsCache.getRegion() );
			allCacheRegions.put( queryCache.getRegion().getName(), queryCache.getRegion() );
		}
		else {
			updateTimestampsCache = null;
			queryCache = null;
			queryCaches = null;
		}

		//checking for named queries
		if ( settings.isNamedQueryStartupCheckingEnabled() ) {
			Map errors = checkNamedQueries();
			if ( !errors.isEmpty() ) {
				Set keys = errors.keySet();
				StringBuffer failingQueries = new StringBuffer( "Errors in named queries: " );
				for ( Iterator iterator = keys.iterator() ; iterator.hasNext() ; ) {
					String queryName = ( String ) iterator.next();
					HibernateException e = ( HibernateException ) errors.get( queryName );
					failingQueries.append( queryName );
					if ( iterator.hasNext() ) {
						failingQueries.append( ", " );
					}
					log.error( "Error in named query: " + queryName, e );
				}
				throw new HibernateException( failingQueries.toString() );
			}
		}

		//stats
		getStatistics().setStatisticsEnabled( settings.isStatisticsEnabled() );

		// EntityNotFoundDelegate
		EntityNotFoundDelegate entityNotFoundDelegate = cfg.getEntityNotFoundDelegate();
		if ( entityNotFoundDelegate == null ) {
			entityNotFoundDelegate = new EntityNotFoundDelegate() {
				public void handleEntityNotFound(final String entityName, final Serializable id) {
					throw new ObjectNotFoundException( id, entityName );
				}
			};
		}
		this.entityNotFoundDelegate = entityNotFoundDelegate;

		this.observer.sessionFactoryCreated( this );
	}

	private void registerEntityNameResolvers(EntityPersister persister) {
		if ( persister.getEntityMetamodel() == null || persister.getEntityMetamodel().getTuplizerMapping() == null ) {
			return;
		}
		Iterator itr = persister.getEntityMetamodel().getTuplizerMapping().iterateTuplizers();
		while ( itr.hasNext() ) {
			final EntityTuplizer tuplizer = ( EntityTuplizer ) itr.next();
			registerEntityNameResolvers( tuplizer );
		}
	}

	private void registerEntityNameResolvers(EntityTuplizer tuplizer) {
		EntityNameResolver[] resolvers = tuplizer.getEntityNameResolvers();
		if ( resolvers == null ) {
			return;
		}

		for ( int i = 0; i < resolvers.length; i++ ) {
			registerEntityNameResolver( resolvers[i], tuplizer.getEntityMode() );
		}
	}

	public void registerEntityNameResolver(EntityNameResolver resolver, EntityMode entityMode) {
		LinkedHashSet resolversForMode = ( LinkedHashSet ) entityNameResolvers.get( entityMode );
		if ( resolversForMode == null ) {
			resolversForMode = new LinkedHashSet();
			entityNameResolvers.put( entityMode, resolversForMode );
		}
		resolversForMode.add( resolver );
	}

	public Iterator iterateEntityNameResolvers(EntityMode entityMode) {
		Set actualEntityNameResolvers = ( Set ) entityNameResolvers.get( entityMode );
		return actualEntityNameResolvers == null
				? EmptyIterator.INSTANCE
				: actualEntityNameResolvers.iterator();
	}

	public QueryPlanCache getQueryPlanCache() {
		return queryPlanCache;
	}

	private Map checkNamedQueries() throws HibernateException {
		Map errors = new HashMap();

		// Check named HQL queries
		log.debug("Checking " + namedQueries.size() + " named HQL queries");
		Iterator itr = namedQueries.entrySet().iterator();
		while ( itr.hasNext() ) {
			final Map.Entry entry = ( Map.Entry ) itr.next();
			final String queryName = ( String ) entry.getKey();
			final NamedQueryDefinition qd = ( NamedQueryDefinition ) entry.getValue();
			// this will throw an error if there's something wrong.
			try {
				log.debug("Checking named query: " + queryName);
				//TODO: BUG! this currently fails for named queries for non-POJO entities
				queryPlanCache.getHQLQueryPlan( qd.getQueryString(), false, CollectionHelper.EMPTY_MAP );
			}
			catch ( QueryException e ) {
				errors.put( queryName, e );
			}
			catch ( MappingException e ) {
				errors.put( queryName, e );
			}
		}

		log.debug("Checking " + namedSqlQueries.size() + " named SQL queries");
		itr = namedSqlQueries.entrySet().iterator();
		while ( itr.hasNext() ) {
			final Map.Entry entry = ( Map.Entry ) itr.next();
			final String queryName = ( String ) entry.getKey();
			final NamedSQLQueryDefinition qd = ( NamedSQLQueryDefinition ) entry.getValue();
			// this will throw an error if there's something wrong.
			try {
				log.debug("Checking named SQL query: " + queryName);
				// TODO : would be really nice to cache the spec on the query-def so as to not have to re-calc the hash;
				// currently not doable though because of the resultset-ref stuff...
				NativeSQLQuerySpecification spec;
				if ( qd.getResultSetRef() != null ) {
					ResultSetMappingDefinition definition = ( ResultSetMappingDefinition ) sqlResultSetMappings.get( qd.getResultSetRef() );
					if ( definition == null ) {
						throw new MappingException( "Unable to find resultset-ref definition: " + qd.getResultSetRef() );
					}
					spec = new NativeSQLQuerySpecification(
							qd.getQueryString(),
					        definition.getQueryReturns(),
					        qd.getQuerySpaces()
					);
				}
				else {
					spec =  new NativeSQLQuerySpecification(
							qd.getQueryString(),
					        qd.getQueryReturns(),
					        qd.getQuerySpaces()
					);
				}
				queryPlanCache.getNativeSQLQueryPlan( spec );
			}
			catch ( QueryException e ) {
				errors.put( queryName, e );
			}
			catch ( MappingException e ) {
				errors.put( queryName, e );
			}
		}

		return errors;
	}

	public StatelessSession openStatelessSession() {
		return new StatelessSessionImpl( null, this, null );
	}

	public StatelessSession openStatelessSession(final Connection connection) {
		return new StatelessSessionImpl( connection, this, null );
	}

	private SessionImpl openSession(Connection connection, boolean autoClose, long timestamp, Interceptor sessionLocalInterceptor, DataSource perSessionDS // 20090605 Fix for per-session DS
	) {
		return new SessionImpl(
		        connection,
		        this,
		        autoClose,
		        timestamp,
		        sessionLocalInterceptor == null ? interceptor : sessionLocalInterceptor,
		        settings.getDefaultEntityMode(),
		        settings.isFlushBeforeCompletionEnabled(),
		        settings.isAutoCloseSessionEnabled(),
		        settings.getConnectionReleaseMode(),
		        perSessionDS
			);
	}

	public org.hibernate.classic.Session openSession(Connection connection, Interceptor sessionLocalInterceptor) {
		return openSession(connection, false, Long.MIN_VALUE, sessionLocalInterceptor, null);
	}

	public org.hibernate.classic.Session openSession(Interceptor sessionLocalInterceptor)
	throws HibernateException {
		// note that this timestamp is not correct if the connection provider
		// returns an older JDBC connection that was associated with a
		// transaction that was already begun before openSession() was called
		// (don't know any possible solution to this!)
		long timestamp = settings.getRegionFactory().nextTimestamp();
		return openSession(null, true, timestamp, sessionLocalInterceptor, null);
	}

	public org.hibernate.classic.Session openSession(Connection connection) {
		return openSession(connection, interceptor); //prevents this session from adding things to cache
	}

	public org.hibernate.classic.Session openSession() throws HibernateException {
		return openSession(interceptor);
	}

	/*------ Extra methods FIX 20090605 jal Fix for per-session ds. ------------*/
	/**
	 * Open a Session which uses the specified datasource for all it's connection allocations. FIX 20090605 jal Fix for per-session ds.
	 * @see org.hibernate.SessionFactory#openSession()
	 */
	public org.hibernate.classic.Session openSession(DataSource ds) throws HibernateException {
		return openSession(interceptor, ds);
	}

	public Session openSession(Interceptor sessionLocalInterceptor, final DataSource ds) {
		// note that this timestamp is not correct if the connection provider
		// returns an older JDBC connection that was associated with a
		// transaction that was already begun before openSession() was called
		// (don't know any possible solution to this!)
		long timestamp = settings.getRegionFactory().nextTimestamp();
		return openSession( null, true, timestamp, sessionLocalInterceptor, ds );
	}

	/*------ End of extra methods FIX 20090605 jal Fix for per-session ds. ------------*/


	public org.hibernate.classic.Session openTemporarySession() throws HibernateException {
		return new SessionImpl(
				null,
		        this,
		        true,
		        settings.getRegionFactory().nextTimestamp(),
		        interceptor,
		        settings.getDefaultEntityMode(),
		        false,
		        false,
		        ConnectionReleaseMode.AFTER_STATEMENT,
		        null
			);
	}

	public org.hibernate.classic.Session openSession(
			final Connection connection,
	        final boolean flushBeforeCompletionEnabled,
	        final boolean autoCloseSessionEnabled,
	        final ConnectionReleaseMode connectionReleaseMode) throws HibernateException {
		return new SessionImpl(
				connection,
		        this,
		        true,
		        settings.getRegionFactory().nextTimestamp(),
		        interceptor,
		        settings.getDefaultEntityMode(),
		        flushBeforeCompletionEnabled,
		        autoCloseSessionEnabled,
		        connectionReleaseMode,
		        null
			);
	}

	public org.hibernate.classic.Session getCurrentSession() throws HibernateException {
		if ( currentSessionContext == null ) {
			throw new HibernateException( "No CurrentSessionContext configured!" );
		}
		return currentSessionContext.currentSession();
	}

	public EntityPersister getEntityPersister(String entityName) throws MappingException {
		EntityPersister result = (EntityPersister) entityPersisters.get(entityName);
		if (result==null) {
			throw new MappingException( "Unknown entity: " + entityName );
		}
		return result;
	}

	public CollectionPersister getCollectionPersister(String role) throws MappingException {
		CollectionPersister result = (CollectionPersister) collectionPersisters.get(role);
		if (result==null) {
			throw new MappingException( "Unknown collection role: " + role );
		}
		return result;
	}

	public Settings getSettings() {
		return settings;
	}

	public Dialect getDialect() {
		return settings.getDialect();
	}

	public Interceptor getInterceptor()
	{
		return interceptor;
	}

	public TransactionFactory getTransactionFactory() {
		return settings.getTransactionFactory();
	}

	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public SQLExceptionConverter getSQLExceptionConverter() {
		return settings.getSQLExceptionConverter();
	}

	public Set getCollectionRolesByEntityParticipant(String entityName) {
		return ( Set ) collectionRolesByEntityParticipant.get( entityName );
	}

	// from javax.naming.Referenceable
	public Reference getReference() throws NamingException {
		log.debug("Returning a Reference to the SessionFactory");
		return new Reference(
			SessionFactoryImpl.class.getName(),
		    new StringRefAddr("uuid", uuid),
		    SessionFactoryObjectFactory.class.getName(),
		    null
		);
	}

	private Object readResolve() throws ObjectStreamException {
		log.trace("Resolving serialized SessionFactory");
		// look for the instance by uuid
		Object result = SessionFactoryObjectFactory.getInstance(uuid);
		if (result==null) {
			// in case we were deserialized in a different JVM, look for an instance with the same name
			// (alternatively we could do an actual JNDI lookup here....)
			result = SessionFactoryObjectFactory.getNamedInstance(name);
			if (result==null) {
				throw new InvalidObjectException("Could not find a SessionFactory named: " + name);
			}
			else {
				log.debug("resolved SessionFactory by name");
			}
		}
		else {
			log.debug("resolved SessionFactory by uid");
		}
		return result;
	}

	public NamedQueryDefinition getNamedQuery(String queryName) {
		return (NamedQueryDefinition) namedQueries.get(queryName);
	}

	public NamedSQLQueryDefinition getNamedSQLQuery(String queryName) {
		return (NamedSQLQueryDefinition) namedSqlQueries.get(queryName);
	}

	public ResultSetMappingDefinition getResultSetMapping(String resultSetName) {
		return (ResultSetMappingDefinition) sqlResultSetMappings.get(resultSetName);
	}

	public Type getIdentifierType(String className) throws MappingException {
		return getEntityPersister(className).getIdentifierType();
	}

	public String getIdentifierPropertyName(String className) throws MappingException {
		return getEntityPersister(className).getIdentifierPropertyName();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		log.trace("deserializing");
		in.defaultReadObject();
		log.debug("deserialized: " + uuid);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		log.debug("serializing: " + uuid);
		out.defaultWriteObject();
		log.trace("serialized");
	}

	public Type[] getReturnTypes(String queryString) throws HibernateException {
		return queryPlanCache.getHQLQueryPlan( queryString, false, CollectionHelper.EMPTY_MAP ).getReturnMetadata().getReturnTypes();
	}

	public String[] getReturnAliases(String queryString) throws HibernateException {
		return queryPlanCache.getHQLQueryPlan( queryString, false, CollectionHelper.EMPTY_MAP ).getReturnMetadata().getReturnAliases();
	}

	public ClassMetadata getClassMetadata(Class persistentClass) throws HibernateException {
		return getClassMetadata( persistentClass.getName() );
	}

	public CollectionMetadata getCollectionMetadata(String roleName) throws HibernateException {
		return (CollectionMetadata) collectionMetadata.get(roleName);
	}

	public ClassMetadata getClassMetadata(String entityName) throws HibernateException {
		return (ClassMetadata) classMetadata.get(entityName);
	}

	/**
	 * Return the names of all persistent (mapped) classes that extend or implement the
	 * given class or interface, accounting for implicit/explicit polymorphism settings
	 * and excluding mapped subclasses/joined-subclasses of other classes in the result.
	 */
	public String[] getImplementors(String className) throws MappingException {

		final Class clazz;
		try {
			clazz = ReflectHelper.classForName(className);
		}
		catch (ClassNotFoundException cnfe) {
			return new String[] { className }; //for a dynamic-class
		}

		ArrayList results = new ArrayList();
		Iterator iter = entityPersisters.values().iterator();
		while ( iter.hasNext() ) {
			//test this entity to see if we must query it
			EntityPersister testPersister = (EntityPersister) iter.next();
			if ( testPersister instanceof Queryable ) {
				Queryable testQueryable = (Queryable) testPersister;
				String testClassName = testQueryable.getEntityName();
				boolean isMappedClass = className.equals(testClassName);
				if ( testQueryable.isExplicitPolymorphism() ) {
					if ( isMappedClass ) {
						return new String[] {className}; //NOTE EARLY EXIT
					}
				}
				else {
					if (isMappedClass) {
						results.add(testClassName);
					}
					else {
						final Class mappedClass = testQueryable.getMappedClass( EntityMode.POJO );
						if ( mappedClass!=null && clazz.isAssignableFrom( mappedClass ) ) {
							final boolean assignableSuperclass;
							if ( testQueryable.isInherited() ) {
								Class mappedSuperclass = getEntityPersister( testQueryable.getMappedSuperclass() ).getMappedClass( EntityMode.POJO);
								assignableSuperclass = clazz.isAssignableFrom(mappedSuperclass);
							}
							else {
								assignableSuperclass = false;
							}
							if ( !assignableSuperclass ) {
								results.add( testClassName );
							}
						}
					}
				}
			}
		}
		return (String[]) results.toArray( new String[ results.size() ] );
	}

	public String getImportedClassName(String className) {
		String result = (String) imports.get(className);
		if (result==null) {
			try {
				ReflectHelper.classForName(className);
				return className;
			}
			catch (ClassNotFoundException cnfe) {
				return null;
			}
		}
		else {
			return result;
		}
	}

	public Map getAllClassMetadata() throws HibernateException {
		return classMetadata;
	}

	public Map getAllCollectionMetadata() throws HibernateException {
		return collectionMetadata;
	}

	/**
	 * Closes the session factory, releasing all held resources.
	 *
	 * <ol>
	 * <li>cleans up used cache regions and "stops" the cache provider.
	 * <li>close the JDBC connection
	 * <li>remove the JNDI binding
	 * </ol>
	 *
	 * Note: Be aware that the sessionfactory instance still can
	 * be a "heavy" object memory wise after close() has been called.  Thus
	 * it is important to not keep referencing the instance to let the garbage
	 * collector release the memory.
	 */
	public void close() throws HibernateException {

		if ( isClosed ) {
			log.trace( "already closed" );
			return;
		}

		log.info("closing");

		isClosed = true;

		Iterator iter = entityPersisters.values().iterator();
		while ( iter.hasNext() ) {
			EntityPersister p = (EntityPersister) iter.next();
			if ( p.hasCache() ) {
				p.getCacheAccessStrategy().getRegion().destroy();
			}
		}

		iter = collectionPersisters.values().iterator();
		while ( iter.hasNext() ) {
			CollectionPersister p = (CollectionPersister) iter.next();
			if ( p.hasCache() ) {
				p.getCacheAccessStrategy().getRegion().destroy();
			}
		}

		if ( settings.isQueryCacheEnabled() )  {
			queryCache.destroy();

			iter = queryCaches.values().iterator();
			while ( iter.hasNext() ) {
				QueryCache cache = (QueryCache) iter.next();
				cache.destroy();
			}
			updateTimestampsCache.destroy();
		}

		settings.getRegionFactory().stop();

		if ( settings.isAutoDropSchema() ) {
			schemaExport.drop( false, true );
		}

		try {
			settings.getConnectionProvider().close();
		}
		finally {
			SessionFactoryObjectFactory.removeInstance(uuid, name, properties);
		}

		observer.sessionFactoryClosed( this );
		eventListeners.destroyListeners();
	}

	public void evictEntity(String entityName, Serializable id) throws HibernateException {
		EntityPersister p = getEntityPersister( entityName );
		if ( p.hasCache() ) {
			if ( log.isDebugEnabled() ) {
				log.debug( "evicting second-level cache: " + MessageHelper.infoString(p, id, this) );
			}
			CacheKey cacheKey = new CacheKey( id, p.getIdentifierType(), p.getRootEntityName(), EntityMode.POJO, this );
			p.getCacheAccessStrategy().evict( cacheKey );
		}
	}

	public void evictEntity(String entityName) throws HibernateException {
		EntityPersister p = getEntityPersister( entityName );
		if ( p.hasCache() ) {
			if ( log.isDebugEnabled() ) {
				log.debug( "evicting second-level cache: " + p.getEntityName() );
			}
			p.getCacheAccessStrategy().evictAll();
		}
	}

	public void evict(Class persistentClass, Serializable id) throws HibernateException {
		EntityPersister p = getEntityPersister( persistentClass.getName() );
		if ( p.hasCache() ) {
			if ( log.isDebugEnabled() ) {
				log.debug( "evicting second-level cache: " + MessageHelper.infoString(p, id, this) );
			}
			CacheKey cacheKey = new CacheKey( id, p.getIdentifierType(), p.getRootEntityName(), EntityMode.POJO, this );
			p.getCacheAccessStrategy().evict( cacheKey );
		}
	}

	public void evict(Class persistentClass) throws HibernateException {
		EntityPersister p = getEntityPersister( persistentClass.getName() );
		if ( p.hasCache() ) {
			if ( log.isDebugEnabled() ) {
				log.debug( "evicting second-level cache: " + p.getEntityName() );
			}
			p.getCacheAccessStrategy().evictAll();
		}
	}

	public void evictCollection(String roleName, Serializable id) throws HibernateException {
		CollectionPersister p = getCollectionPersister( roleName );
		if ( p.hasCache() ) {
			if ( log.isDebugEnabled() ) {
				log.debug( "evicting second-level cache: " + MessageHelper.collectionInfoString(p, id, this) );
			}
			CacheKey cacheKey = new CacheKey( id, p.getKeyType(), p.getRole(), EntityMode.POJO, this );
			p.getCacheAccessStrategy().evict( cacheKey );
		}
	}

	public void evictCollection(String roleName) throws HibernateException {
		CollectionPersister p = getCollectionPersister( roleName );
		if ( p.hasCache() ) {
			if ( log.isDebugEnabled() ) {
				log.debug( "evicting second-level cache: " + p.getRole() );
			}
			p.getCacheAccessStrategy().evictAll();
		}
	}

	public Type getReferencedPropertyType(String className, String propertyName)
		throws MappingException {
		return getEntityPersister(className).getPropertyType(propertyName);
	}

	public ConnectionProvider getConnectionProvider() {
		return settings.getConnectionProvider();
	}

	public UpdateTimestampsCache getUpdateTimestampsCache() {
		return updateTimestampsCache;
	}

	public QueryCache getQueryCache() {
		return queryCache;
	}

	public QueryCache getQueryCache(String regionName) throws HibernateException {
		if ( regionName == null ) {
			return getQueryCache();
		}

		if ( !settings.isQueryCacheEnabled() ) {
			return null;
		}

		synchronized ( allCacheRegions ) {
			QueryCache currentQueryCache = ( QueryCache ) queryCaches.get( regionName );
			if ( currentQueryCache == null ) {
				currentQueryCache = settings.getQueryCacheFactory().getQueryCache( regionName, updateTimestampsCache, settings, properties );
				queryCaches.put( regionName, currentQueryCache );
				allCacheRegions.put( currentQueryCache.getRegion().getName(), currentQueryCache.getRegion() );
			}
			return currentQueryCache;
		}
	}

	public Region getSecondLevelCacheRegion(String regionName) {
		synchronized ( allCacheRegions ) {
			return ( Region ) allCacheRegions.get( regionName );
		}
	}

	public Map getAllSecondLevelCacheRegions() {
		synchronized ( allCacheRegions ) {
			return new HashMap( allCacheRegions );
		}
	}

	public boolean isClosed() {
		return isClosed;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public StatisticsImplementor getStatisticsImplementor() {
		return statistics;
	}

	public void evictQueries() throws HibernateException {
		if ( settings.isQueryCacheEnabled() ) {
			queryCache.clear();
		}
	}

	public void evictQueries(String cacheRegion) throws HibernateException {
		if (cacheRegion==null) {
			throw new NullPointerException("use the zero-argument form to evict the default query cache");
		}
		else {
			synchronized (allCacheRegions) {
				if ( settings.isQueryCacheEnabled() ) {
					QueryCache currentQueryCache = (QueryCache) queryCaches.get(cacheRegion);
					if ( currentQueryCache != null ) {
						currentQueryCache.clear();
					}
				}
			}
		}
	}

	public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
		FilterDefinition def = ( FilterDefinition ) filters.get( filterName );
		if ( def == null ) {
			throw new HibernateException( "No such filter configured [" + filterName + "]" );
		}
		return def;
	}

	public Set getDefinedFilterNames() {
		return filters.keySet();
	}

	public BatcherFactory getBatcherFactory() {
		return settings.getBatcherFactory();
	}

	public IdentifierGenerator getIdentifierGenerator(String rootEntityName) {
		return (IdentifierGenerator) identifierGenerators.get(rootEntityName);
	}

	private CurrentSessionContext buildCurrentSessionContext() {
		String impl = properties.getProperty( Environment.CURRENT_SESSION_CONTEXT_CLASS );
		// for backward-compatability
		if ( impl == null && transactionManager != null ) {
			impl = "jta";
		}

		if ( impl == null ) {
			return null;
		}
		else if ( "jta".equals( impl ) ) {
			if ( settings.getTransactionFactory().areCallbacksLocalToHibernateTransactions() ) {
				log.warn( "JTASessionContext being used with JDBCTransactionFactory; auto-flush will not operate correctly with getCurrentSession()" );
			}
			return new JTASessionContext( this );
		}
		else if ( "thread".equals( impl ) ) {
			return new ThreadLocalSessionContext( this );
		}
		else if ( "managed".equals( impl ) ) {
			return new ManagedSessionContext( this );
		}
		else {
			try {
				Class implClass = ReflectHelper.classForName( impl );
				return ( CurrentSessionContext ) implClass
						.getConstructor( new Class[] { SessionFactoryImplementor.class } )
						.newInstance( new Object[] { this } );
			}
			catch( Throwable t ) {
				log.error( "Unable to construct current session context [" + impl + "]", t );
				return null;
			}
		}
	}

	public EventListeners getEventListeners()
	{
		return eventListeners;
	}

	public EntityNotFoundDelegate getEntityNotFoundDelegate() {
		return entityNotFoundDelegate;
	}

	/**
	 * Custom serialization hook used during Session serialization.
	 *
	 * @param oos The stream to which to write the factory
	 * @throws IOException Indicates problems writing out the serial data stream
	 */
	void serialize(ObjectOutputStream oos) throws IOException {
		oos.writeUTF( uuid );
		oos.writeBoolean( name != null );
		if ( name != null ) {
			oos.writeUTF( name );
		}
	}

	/**
	 * Custom deserialization hook used during Session deserialization.
	 *
	 * @param ois The stream from which to "read" the factory
	 * @return The deserialized factory
	 * @throws IOException indicates problems reading back serial data stream
	 * @throws ClassNotFoundException indicates problems reading back serial data stream
	 */
	static SessionFactoryImpl deserialize(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		String uuid = ois.readUTF();
		boolean isNamed = ois.readBoolean();
		String name = null;
		if ( isNamed ) {
			name = ois.readUTF();
		}
		Object result = SessionFactoryObjectFactory.getInstance( uuid );
		if ( result == null ) {
			log.trace( "could not locate session factory by uuid [" + uuid + "] during session deserialization; trying name" );
			if ( isNamed ) {
				result = SessionFactoryObjectFactory.getNamedInstance( name );
			}
			if ( result == null ) {
				throw new InvalidObjectException( "could not resolve session factory during session deserialization [uuid=" + uuid + ", name=" + name + "]" );
			}
		}
		return ( SessionFactoryImpl ) result;
	}

	public SQLFunctionRegistry getSqlFunctionRegistry() {
		return sqlFunctionRegistry;
	}
}

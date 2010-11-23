package to.etc.server.cache;

import java.util.*;

import to.etc.server.vfs.*;
import to.etc.util.*;

/**
 * <p>A resource cache which stores opague resources that derive from some set
 * of PhysicalKeys. The resources can be cleared (for instance because a key
 * changed) simply by sending an update event using one of their dependencies.
 * </p>
 * <p>The cache uses the vfs framework for retrieval of cache item sources.</p>
 *
 * <p>The objects contained in the cache does not need to know about the cache; the code
 * which <i>creates</i> the objects for cache use does need to know though. Cache items
 * are created using the cache's content factory.</p>
 *
 * <p>The cache is a LRU cache with multiple LRU chains. If unmodified the cache has three
 * chains:
 * <ul>
 * 	<li>The memory chain contains all items that just use memory. Examples are for instance
 * 		binary blob resources like .gif and .png</li>
 *	<li>The file chain contains all items that are spilled over to the file system after
 *		retrieval. This is typically used for huge items that would eat too much memory
 *		when cached in core, and whose retrieval from the actual VfsProvider costs too much
 *		to retrieve them every time they get used. Typical example is a large .pdf residing
 *		in the database: transferring this source to the client usually takes a long time,
 *		and it is problematic to keep a database connection open all that time.</li>
 *	<li>The code chain contains items that are class instances, for instance compiled
 *		template classes.</li>
 * </ul>
 * Each LRU chain is identified by a LruChainHeader which contains the max. size that the
 * chain may reach before items are to be removed. When the total size of allocated resources
 * of any chain exceeds the max. size then resources get deleted until the allocated size
 * drops below the max size.</p>
 *
 * @author jal
 * Created on Dec 8, 2005
 */
public class ResourceCache {
	/**
	 * The class can be used as a singleton; in that case it *must* be initialized
	 * using initialize() before it can get used.
	 */
	static private ResourceCache			m_instance;

	/** This-cache's statistics block. */
	private CacheStats						m_stats			= new CacheStats();

	/** The header for the memory buffers' cache. */
	private CacheListHeader					m_memory_ch;

	/** The header for the classes cache. */
	private CacheListHeader					m_code_ch;

	/** The header for the file space cache. */
	private CacheListHeader					m_file_ch;

	private CacheListHeader[]				m_chain_ar;

	/** The #of LRU cache releases */
	private long							m_lru_cachereleases;

	/** The primary lookup-item-by-key map. This caches ResourceProxies. */
	private Map<Object, ResourceProxy>		m_proxyMap		= new Hashtable<Object, ResourceProxy>();

	/**
	 * Dependency map: this maps any kind of object used during the construction of an element
	 * to all Proxies that used that object in it's construction. For instance when templates
	 * use an include file the VfsPhysicalKey of that included file is mapped to all templates
	 * that used <i>that</i> file. The CMS uses this to keep for instance the Website and Skin
	 * objects as dependencies for all resources that derive from there, so that when those
	 * change we can discard all cached entries from them.
	 */
	private Map<Object, Set<ResourceRef>>	m_dependencyMap	= new Hashtable<Object, Set<ResourceRef>>();

	//	/**
	//	 * Constructor for singleton use. The actual thingy gets initialized by a call
	//	 * to initialize().
	//	 *
	//	 */
	//	private ResourceCache()
	//	{
	//	}

	/**
	 * Constructor for non-singleton use.
	 * @param memsize
	 * @param codesize
	 * @param filesize
	 */
	public ResourceCache(long memsize, long codesize, long filesize) {
		m_memory_ch = new CacheListHeader(this, "memory", memsize);
		m_code_ch = new CacheListHeader(this, "code", codesize);
		m_file_ch = new CacheListHeader(this, "file", filesize);
		m_chain_ar = new CacheListHeader[]{m_memory_ch, m_code_ch, m_file_ch};
	}

	final public CacheListHeader cacheGetMemoryHeader() {
		return m_memory_ch;
	}

	final public CacheListHeader cacheGetCodeHeader() {
		return m_code_ch;
	}

	final public CacheListHeader cacheGetFileHeader() {
		return m_file_ch;
	}

	final synchronized public long getCacheAllocated() {
		return m_memory_ch.getSzCommited();
	}

	final synchronized public long getNumLruCacheReleases() {
		return m_lru_cachereleases;
	}

	/**
	 * Returns all of the cache LRU chains that resources from this
	 * cache are linked in. Can be overridden if more than the usual
	 * chains need to be supported.
	 * @return
	 */
	protected CacheListHeader[] getCacheChains() {
		return m_chain_ar;
	}

	/**
	 * Returns the statistics block for the cache which collects all actions related to
	 * the cache and all of the objects contained therein.
	 *
	 * @return
	 */
	final public CacheStats stat() {
		return m_stats;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Getting a resource...								*/
	/*--------------------------------------------------------------*/
	/**
	 * <p>Get a resource. The resource is looked up from the cache and if found
	 * gets returned immediately. If the resource is not currently cached
	 * the code opens the VfsSource for the key and uses it to either load
	 * or compile the thing depending on it's mime type. After load or compilation
	 * the resource gets cached and is returned ready for use.</p>
	 * <p>This also maintains the dependency cache for all resources.</p>
	 *
	 * <p>This uses fast-locking semantics: it always allocates a Proxy while the
	 * Manager itself is locked; all initialization is done with only the proxy
	 * locked. After initialization of the proxy the manager is locked again to
	 * quickly update the manager's data structures for the initialized proxy.</p>
	 *
	 * @param vr	The resolver which resolves names (include files) to physical keys.
	 * @param key	The root physical key for the resource to get
	 * @param param	Some opague object reference.
	 */
	final public ResourceRef findResource(VfsPathResolver vr, CacheObjectFactory cof, Object key, Object p1, Object p2, Object p3) throws Exception {
		/*
		 * This code loops to handle the case that a Ref was gotten from the
		 * hashtable OK, but it was deleted immediately afterwards, before getting
		 * the ResourceImpl from it. In this case the code returns a null when getting
		 * the impl. The correct way to handle this is to loop: get/create a new ref
		 * to the object and reinitialize.
		 */
		ResourceProxy rp = null;
		for(;;) {
			synchronized(this) // Allocate/find the Ref
			{
				//-- Is an update pending? If so wait until it is handled.
				if(m_pendingUpdateList.size() > 0) {
					try {
						wait(20000);
					} catch(InterruptedException xz) {}
				} else {
					//-- No updates are pending: we can get or compile the resource
					rp = m_proxyMap.get(key); // Find using pk
					if(rp == null) {
						//-- Create an empty proxy.
						rp = new ResourceProxy(this, key, cof.makeStatistics());
						m_proxyMap.put(key, rp); // Push into map for next interested party.
					}
					m_compileDepth++; // Increment compile depth
				}
			}

			//-- Outside of all locks!!
			if(rp != null) {
				try {
					ResourceRef ref = findResource(rp, vr, cof, p1, p2, p3);
					if(ref != null)
						return ref;
				} finally {
					compilationFinished(); // Make sure the end-of-compile is handled
				}
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Retrieving the correct item from a proxy.			*/
	/*--------------------------------------------------------------*/
	@SuppressWarnings("null")
	private ResourceRef findResource(ResourceProxy rp, VfsPathResolver vr, CacheObjectFactory cof, Object p1, Object p2, Object p3) throws Exception {
		Throwable exc = null;
		ResourceRef ref = null;
		ResourceRef oldref = null;
		synchronized(rp) {
			ref = rp.getCachedRef(); // Get currently-cached ref
			boolean needinit = ref == null; // We need to initialize if it is empty
			if(!needinit) {
				CacheDependencies cd = ref.getDependencies(); // Get current ref's dependencies
				if(cd != null) {
					//-- There are dependencies. Check to see if they changed
					if(cd.changed())
						needinit = true;
				}
			}

			//-- We now know whether we have to reinit or not.
			if(needinit) {
				oldref = ref; // Ref to be discarded
				rp.setCachedRef(null); // Discard of the old item (causes reinit on error)

				/*
				 * Exception handling: if initializing the object causes an exception
				 * here then we keep the entry as if it was an uninitialized entry.
				 * This means that the old ref gets discarded (if present) and no
				 * new ref will be linked. This will cause a reinit the next time
				 * the resource gets accessed.
				 */
				try {
					ref = initializeResource(rp, vr, cof, p1, p2, p3); // Try to initialize
					rp.setCachedRef(ref);
				} catch(Exception x) {
					exc = x;
				} catch(Error e) {
					exc = e;
				}
			}
			//			else
			//				System.out.println("nema: reusing cached resource "+rp.getVfsKey());
		}

		//-- Outside proxy lock: make sure the proxy data is updated in the cache.
		updateLinkage(rp, ref, oldref);

		if(exc != null) {
			if(exc instanceof Exception)
				throw (Exception) exc;
			else if(exc instanceof Error)
				throw (Error) exc;
			else
				throw new WrappedException("!? Unexpected exception type: " + exc, exc);
		}

		return ref;
	}

	/**
	 * Creates a new reference to some thingy. This asks the factory to create a new object. The factory will do
	 * that and add it's dependencies and the like.
	 *
	 * @param vr
	 * @param cof
	 * @param param
	 * @return
	 * @throws Exception
	 */
	private ResourceRef initializeResource(ResourceProxy rp, VfsPathResolver vr, CacheObjectFactory cof, Object p1, Object p2, Object p3) throws Exception {
		// FIXME Need better impl of default check interval
		DependencySet depset = new DependencySet();
		ResourceRef ref = new ResourceRef(rp);
		//		System.out.println("nema: (re)initializing resource "+rp.getVfsKey());
		Object fob = cof.makeObject(ref, vr, rp.getVfsKey(), depset, p1, p2, p3);
		if(fob == null)
			return null;
		ref.setDependencies(depset); // Store all dependencies
		ref.setCachedObject(fob); // Save the object thus created.
		return ref;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Cache LRU and dependency maps maintenance.			*/
	/*--------------------------------------------------------------*/
	/**
	 * Updates the cache's LRU lists and dependency tables when a ref
	 * is added, deleted or changed. If an "old" ref is passed it's
	 * data gets deleted from the tables. This reduces the cache load.
	 * If a ref is present we check it's link state: it is is linked
	 * then it will be relinked at the top of the chains (MRU position).
	 * If it is not linked then it will be linked at the MRU position AND
	 * it's dependencies will be stored in the dependency table.
	 * And finally all LRU chains will be checked for overflow and resources
	 * will be discarded until the overflow condition is cleared.
	 */
	private void updateLinkage(ResourceProxy rp, ResourceRef ref, ResourceRef oldref) {
		List<ResourceProxy> dellist = null; // If items need to be deleted they get stored herein
		synchronized(this) {
			//-- Unlink any old resource
			if(oldref != null)
				unlinkRef(oldref); // Remove from LRU chains

			//-- Handle new ref
			if(ref != null)
				linkRef(ref);

			/*
			 * At this point we are integer and all data has been linked. We may have overflowed
			 * a chain though so handle that...
			 */
			for(CacheListHeader lh : getCacheChains())
				dellist = checkChainOverflow(dellist, lh);
		}
		if(dellist == null)
			return;

		//-- Discard all proxies.
		destroyProxies(dellist);
	}

	/**
	 * Check the specified list for overflow. If the list has overflowed remove
	 * LRU resources until it no longer overflows.
	 * @param dellist
	 * @param lh
	 * @return
	 */
	private synchronized List<ResourceProxy> checkChainOverflow(List<ResourceProxy> dellist, CacheListHeader lh) {
		while(lh.getSzCommited() > lh.getSzMaxCommited()) {
			//-- Find, then remove a resource
			ResourceRef ref = lh.getLruItem(); // Find oldest item
			if(dellist == null)
				dellist = new ArrayList<ResourceProxy>();
			dellist.add(ref.getProxy());
			unlinkRef(ref);
			lh.incCacheReleases();

			m_proxyMap.remove(ref.getProxy().getVfsKey());
		}
		return dellist;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Linking and unlinking from chains					*/
	/*--------------------------------------------------------------*/
	private synchronized void unlinkRef(ResourceRef ref) {
		if(ref.getLinkState() != ResourceLinkState.lsLINKED)
			return;

		//-- Walk all chains and unlink
		for(CacheListEntry ce : ref.getListHeads())
			ce.unlink();
		removeDependencies(ref);
		ref.setLinkStatus(ResourceLinkState.lsUNLINKED);
	}

	/**
	 * Links or relinks a ref. If the ref is unlinked it gets
	 * linked; if it is linked it is relinked to get to the MRU
	 * position in it's chains.
	 * @param ref
	 */
	@SuppressWarnings("fallthrough")
	private synchronized void linkRef(ResourceRef ref) {
		switch(ref.getLinkState()){
			default:
				throw new IllegalStateException("Bad link state to (re)link reference: " + ref.getLinkState());

			case lsNONE:
				ref.setLinkStatus(ResourceLinkState.lsLINKED);
				addDependencies(ref);
				//-- Continue into linked, below to link all chain entries

			case lsLINKED:
				for(CacheListEntry ce : ref.getListHeads())
					ce.link();
				break;
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Primitive dependency table maintenance.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Adds all dependencies for the specified ref to the map.
	 */
	private void addDependencies(ResourceRef ref) {
		for(CacheDependency cd : ref.getDependencies()) {
			Object key = cd.getKey();
			Set<ResourceRef> set = m_dependencyMap.get(key);
			if(set == null) {
				set = new HashSet<ResourceRef>(5);
				m_dependencyMap.put(key, set);
			}
			set.add(ref);
		}
	}

	private void removeDependencies(ResourceRef ref) {
		for(CacheDependency cd : ref.getDependencies()) {
			Object key = cd.getKey();
			Set<ResourceRef> set = m_dependencyMap.get(key);
			if(set == null) {
				throw new IllegalStateException("!? Cannot remove dependency: key not in dependency map!?");
			}
			if(!set.remove(ref)) // Remove proxy from depset
			{
				throw new IllegalStateException("!? Cannot remove dependency: ref not in set!?");
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Posting update events for a key.					*/
	/*--------------------------------------------------------------*/
	/**
	 * The list of pending updates. This gets filled when an update event
	 * is received for a dependee while a template is (possibly) compiling.
	 */
	private List<Object>	m_pendingUpdateList	= new ArrayList<Object>();

	/**
	 * The #of threads that are currently compiling resources. If >0 all update events
	 * will be cached.
	 */
	private int				m_compileDepth;

	/**
	 * <p>When an event-driven update is received it must be posted here
	 * using the Physical key of the updated resource. All cached resources
	 * that depend on that VfsSource will be discarded from the cache,
	 * causing them to be reloaded next time they're used.
	 * </p>
	 * <p>Update events are handled immediately *if* no template is currenly
	 * being located (i.e. no call to findResource->getResource() is in progress).
	 * If such a call is in progress then we cannot immediately execute the
	 * update because any compile can use dependencies that are not yet
	 * registered with the manager. For instance if the compile in progress
	 * has just read "file213" and then another task posts an update to that
	 * file the update manager would look in the dependency map but would not
	 * find any resource using it. When compilation completes (using the old file)
	 * it will register the dependency but the resource will not be recompiled
	 * because the update was lost.
	 * </p>
	 * <p>To prevent this we queue any updates that are received while a
	 * (possible) compile is in progress. As soon as the compiling process is
	 * ready it will register it's dependencies; as part of that process it
	 * will check the event queue and handle all events there.</p>
	 */
	public void resourceChanged(Object key) {
		Collection<ResourceProxy> dumplist = null;// The list of invalidated proxies,
		synchronized(this) {
			if(m_compileDepth > 0) // I am compiling?
			{
				m_pendingUpdateList.add(key); // Add the update.
				return;
			}

			//-- I am not updating anything - we can execute immediately.
			dumplist = removeProxiesDependingOn(key);// Get list of thingies to kill
		}

		/*
		 * Out of lock. If proxies were removed then we must finish them off by forcing them to
		 * discard their impl.
		 */
		if(dumplist != null)
			destroyProxies(dumplist);
	}

	/**
	 * Looks up all refs that depend on the specified key and removes them from
	 * the cache structures. All resources depending on that key are removed from
	 * all chains and the maps.
	 * @param pk
	 * @return
	 */
	private synchronized Collection<ResourceProxy> removeProxiesDependingOn(Object key) {
		Set<ResourceRef> rset = m_dependencyMap.remove(key); // Get and remove the set of dependents on the key
		if(rset == null) // No-one depends on this key?
			return null; // Done: nothing to do

		//-- Remove all of the refs *and* proxies from the cache table.
		List<ResourceProxy> list = new ArrayList<ResourceProxy>(rset.size());
		ResourceRef[] ar = rset.toArray(new ResourceRef[rset.size()]); // Dup the set because it will be changed
		for(ResourceRef ref : ar) {
			ResourceProxy px = ref.getProxy();
			m_proxyMap.remove(px.getVfsKey()); // Remove from primary map
			unlinkRef(ref); // Remove resource completely from chains and depset
			m_proxyMap.remove(px.getVfsKey()); // Remove from map.
			list.add(px);
		}
		return list;
	}

	/**
	 * Called when a resource has been gotten and compiled. This decrements
	 * the compilation busy counter. When it reaches zero and updates are
	 * pending then it issues all updates.
	 */
	private void compilationFinished() {
		Collection<ResourceProxy> dumplist = null;
		synchronized(this) {
			if(m_compileDepth <= 0)
				throw new IllegalStateException("Compilation depth underflow!?");
			m_compileDepth--;
			if(m_compileDepth > 0 || m_pendingUpdateList.size() == 0)
				return;

			//-- We have to handle pending updates...
			dumplist = new ArrayList<ResourceProxy>();
			for(Object key : m_pendingUpdateList) // For all pending updates
			{
				Collection<ResourceProxy> rset = removeProxiesDependingOn(key);
				if(rset != null)
					dumplist.addAll(rset); // Append to total list of todos
			}
			m_pendingUpdateList.clear(); // All pending updates done now
		}

		/*
		 * Out of lock. All updates have been handled; all that's left is
		 * to discard all of the proxies just removed.
		 */
		destroyProxies(dumplist);
	}

	static private void destroyProxies(Collection<ResourceProxy> coll) {
		for(ResourceProxy px : coll) {
			try {
				px.delete();
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Singleton initialization and retrieval.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Retrieves the singleton instance. If the instance has not yet been
	 * initialized this inits the thing.
	 */
	static synchronized public ResourceCache getInstance() {
		while(m_instance == null) {
			System.out.println("ResourceCache: waiting for initialization.");
			try {
				ResourceCache.class.wait(20000);
			} catch(InterruptedException ix) {}
		}
		return m_instance;
	}

	static synchronized public void initialize(long memsize, long codesize, long filesize) {
		if(m_instance != null)
			throw new IllegalStateException("Attempt to re-initialize cache");
		m_instance = new ResourceCache(memsize, codesize, filesize);
		ResourceCache.class.notifyAll();
	}

	static synchronized public void initialize() {
		initialize(10 * 1024 * 1024, 10 * 1024 * 1024, 100 * 1024 * 1024);
	}

	static {
		initialize();
	}
}

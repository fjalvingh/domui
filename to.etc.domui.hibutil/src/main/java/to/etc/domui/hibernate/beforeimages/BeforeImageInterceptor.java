package to.etc.domui.hibernate.beforeimages;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;

import org.hibernate.*;
import org.hibernate.collection.*;
import org.hibernate.event.*;
import org.hibernate.proxy.*;

import to.etc.domui.component.meta.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

/**
 * This session interceptor delegates load events to the before-image load cache.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 13, 2014
 */
public class BeforeImageInterceptor extends EmptyInterceptor {
	@Nonnull
	final private IBeforeImageCache m_cache;

	static private final boolean DEBUG = false;

	/**
	 * Identifies a collection inside a given instance.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jan 20, 2014
	 */
	@Immutable
	static private class CollectionKey {
		@Nonnull
		final private String m_role;

		@Nonnull
		final private Serializable m_key;

		public CollectionKey(@Nonnull String role, @Nonnull Serializable key) {
			m_role = role;
			m_key = key;
		}

		@Nonnull
		public String getRole() {
			return m_role;
		}

		@Nonnull
		public Serializable getKey() {
			return m_key;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((m_key == null) ? 0 : m_key.hashCode());
			result = prime * result + ((m_role == null) ? 0 : m_role.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			CollectionKey other = (CollectionKey) obj;
			if(m_key == null) {
				if(other.m_key != null)
					return false;
			} else if(!m_key.equals(other.m_key))
				return false;
			if(m_role == null) {
				return other.m_role == null;
			} else
				return m_role.equals(other.m_role);
		}
	}

	@Nonnull
	final private Map<CollectionKey, IBeforeImageCollectionProxy< ? >> m_mirrorMap = new HashMap<CollectionKey, IBeforeImageCollectionProxy< ? >>();

	public BeforeImageInterceptor(@Nonnull IBeforeImageCache cache) {
		m_cache = cache;
	}

	@Nonnull
	public IBeforeImageCache getCache() {
		return m_cache;
	}

	/**
	 * Whenever an entity is loaded make sure that a "before" image of it exists.
	 *
	 * EXPERIMENTAL This uses a non-hibernate method which is added to Hibernate's source code.
	 *
	 * @see org.hibernate.EmptyInterceptor#onAfterLoad(org.hibernate.event.PostLoadEvent)
	 */
	public void onAfterLoad(@Nonnull PostLoadEvent loadevent) {
		Object instance = loadevent.getEntity();
		if(null == instance)
			throw new IllegalStateException("entity instance null in interceptor!?");

//		System.out.println("Interceptor: afterload " + MetaManager.identify(instance));
		try {
			Class real = Hibernate.getClass(instance);

			Object copy = m_cache.createImage(real, instance, true);
			copyProperties(copy, instance);				// Copy whatever properties we can
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	/**
	 * Copy all properties from src to dst, taking the different types into consideration. All "simple"
	 * types can just be copied by reference, but we need to take special care of the following:
	 * <ul>
	 *	<li>ManyToOne properties can refer either to an existing instance in the cache OR they can be "unloaded". If they are loaded
	 *		then we must replace the reference to the "before" image for that thing. If the thing is currently unloaded we must make
	 *		sure we create the before image already, but with a "unloaded" indication.</li>
	 * </ul>
	 * @param copy
	 * @param instance
	 */
	private <T> void copyProperties(@Nonnull T dst, @Nonnull T src) throws Exception {
		for(PropertyMetaModel< ? > pmm : MetaManager.findClassMeta(src.getClass()).getProperties()) {
//			System.out.println("   >> copy property " + pmm + " of " + src.getClass());
			copyProperty(dst, src, pmm);
		}
	}

	private <T, V> void copyProperty(@Nonnull T dst, @Nonnull T src, @Nonnull PropertyMetaModel<V> pmm) throws Exception {
		if(pmm.getReadOnly() == YesNoType.YES)					// Cannot set readonlies
			return;
		if(pmm.isTransient())									// We don't wanna play with transients.
			return;

		V value = pmm.getValue(src);							// Get the source instance.

		switch(pmm.getRelationType()){
			case NONE:
				pmm.setValue(dst, value);						// Just copy
				break;

			case DOWN:
				if(null != value) {
					if(!(value instanceof Collection))
						throw new IllegalStateException("Before-image is supported only for OneToMany of type Collection<T>.");
					value = (V) convertChildCollection((Collection) value);
					pmm.setValue(dst, value);
				}
				break;

			case UP:
				if(value != null)
					value = convertParentRelation(value);
				pmm.setValue(dst, value);
				break;

		}
	}

	/**
	 * If the instance is a "loaded" one then just get it's before image: it must be found.
	 *
	 * @param src
	 * @return
	 */
	private <V> V convertParentRelation(@Nonnull V src) throws Exception {
		if(Hibernate.isInitialized(src)) {						// Loaded?
			//-- Replace the instance with the before image of that instance.
			V before = m_cache.findBeforeImage(src);
			if(null != before) {
				return before;
			}
			/*
			 * 20140414 jal Loads for complex sets can have delayed registration of a "loaded" entity. This means we cannot
			 * yet register it.
			 */
			if(DEBUG)
				System.err.println("The 'before' image for " + MetaManager.identify(src) + " cannot be found, even though it is loaded by Hibernate!?");
//				throw new IllegalStateException("The 'before' image for " + MetaManager.identify(src) + " cannot be found, even though it is loaded by Hibernate!?");
		}

		/*
		 * The instance is not loaded 8-/. We need to create a copy that is marked as "uninitialized".
		 */
		Class<V> realclass = getProxyClass(src);
		V copy = m_cache.createImage(realclass, src, false);
		if(m_cache.wasNew()) {
			//-- We need to dup the PK and insert it into the copy. The PK itself can be compound and so also hold data to be copied 8-/
			ClassMetaModel cmm = MetaManager.findClassMeta(src.getClass());
			PropertyMetaModel< ? > pkmm = cmm.getPrimaryKey();
			if(null == pkmm)
				throw new IllegalStateException("Cannot locate the private key property for class " + cmm);
			copyProperty(copy, src, pkmm);
		}
		return copy;
	}



	/**
	 * We cannot just copy collections because they might be lazy-loaded. So for lazy collections we have to do
	 * the following, conceptually:
	 * <ul>
	 *	<li>Create our own "mirror" proxy class as the Collection<> type.</li>
	 *	<li>Register the "mirror" as belonging to the original</li>
	 *	<li>We will have added a {@link InitializeCollectionEventListener} to Hibernate. This listener will be called after the collection is initialized.</li>
	 *	<li>That listener will lookup the "mirror" collection and copy the just-loaded data in there.</li>
	 * </ul>
	 *
	 * @param src
	 * @return
	 * @throws Exception
	 */
	private <E, C extends Collection<E>> C convertChildCollection(@Nonnull C src) throws Exception {
		if(Hibernate.isInitialized(src)) {
			return createMirrorCollection(src);					// Just create an immutable copy.
		}

		//-- We need to create the correct mirror proxy.
		IBeforeImageCollectionProxy<E> proxy = (IBeforeImageCollectionProxy<E>) createMirrorCollectionProxy(src);

		PersistentCollection pc = (PersistentCollection) src;
		CollectionKey kk = new CollectionKey(pc.getRole(), pc.getKey());
		m_mirrorMap.put(kk, proxy);
		return (C) proxy;
	}

	/**
	 * This creates the mirrored collection for an already-loaded collection. It just creates a new base
	 * type of the real collection type expected as an immutable type, then returns it.
	 * @param source
	 * @return
	 */
	@Nonnull
	static private <T, V extends Collection<T>, R extends IBeforeImageCollectionProxy<V>> R createMirrorCollectionProxy(@Nonnull V source) {
		Class<V> clz = (Class<V>) source.getClass();
		if(List.class.isAssignableFrom(clz)) {
			return (R) new BeforeImageListProxy<T>();
		} else if(Set.class.isAssignableFrom(clz)) {
			return (R) new BeforeImageSetProxy<T>();
		} else
			throw new IllegalStateException("Before Images Interceptor: cannot create before images for collection of type " + source.getClass());
	}

	/**
	 * This creates the mirrored collection for an already-loaded collection. It just creates a new base
	 * type of the real collection type expected as an immutable type, then returns it.
	 * @param source
	 * @return
	 */
	@Nonnull
	static private <T, V extends Collection<T>> V createMirrorCollection(@Nonnull V source) {
		Class<V> clz = (Class<V>) source.getClass();
		if(List.class.isAssignableFrom(clz)) {
			return (V) Collections.unmodifiableList(new ArrayList<T>(source));
		} else if(Set.class.isAssignableFrom(clz)) {
			return (V) Collections.unmodifiableSet(new HashSet<T>(source));
		} else
			throw new IllegalStateException("Before Images Interceptor: cannot create before images for collection of type " + source.getClass());
	}


	@Nonnull
	static private <T> Class<T> getProxyClass(@Nonnull T proxy) {
		if(proxy instanceof HibernateProxy) {
			return ((HibernateProxy) proxy).getHibernateLazyInitializer().getPersistentClass();
		} else {
			return (Class<T>) proxy.getClass();
		}
	}

	/**
	 * Called from {@link CopyCollectionEventListener} when a lazy collection is loaded, this
	 * initializes the "before" image of that collection.
	 * @param collection
	 */
	public void collectionLoaded(@Nonnull PersistentCollection collection) {
		CollectionKey kk = new CollectionKey(collection.getRole(), collection.getKey());
		IBeforeImageCollectionProxy mirror = m_mirrorMap.remove(kk);
		if(null == mirror) {
//			System.out.println("CopyInterceptor: no 'mirror' collection for collection " + collection.getClass().getName() + " @" + System.identityHashCode(collection));
			return;
		}
//		System.out.println("CopyInterceptor: load event for " + collection.getClass().getName() + " @" + System.identityHashCode(collection));

		copyCollection(mirror, (Collection) collection);
	}

	private <E, C extends Collection<E>> void copyCollection(IBeforeImageCollectionProxy<C> mirror, C collection) {
		mirror.initializeFromOriginal(collection);
	}

}

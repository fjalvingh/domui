package to.etc.domui.hibernate.config;

import java.util.*;

import javax.annotation.*;

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
public class CreateCopyInterceptor extends EmptyInterceptor {
	@Nonnull
	final private IBeforeImageCache m_cache;

	@Nonnull
	final private Map<Object, Object> m_mirrorMap = new HashMap<Object, Object>();

	public CreateCopyInterceptor(@Nonnull IBeforeImageCache cache) {
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
	@Override
	public void onAfterLoad(@Nonnull PostLoadEvent loadevent) {
		Object instance = loadevent.getEntity();
		if(null == instance)
			throw new IllegalStateException("entity instance null in interceptor!?");

		System.out.println("Interceptor: afterload " + MetaManager.identify(instance));
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
			System.out.println("   >> copy property " + pmm + " of " + src.getClass());
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
				if(null != value)
					value = convertChildCollection(value);
				break;

			case UP:
				if(pmm.getName().equals("btwCode"))
					System.out.println("Gotcha");
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
			if(null == before)
				throw new IllegalStateException("The 'before' image for " + MetaManager.identify(src) + " cannot be found, even though it is loaded by Hibernate!?");
			return before;
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
	private <V, E, C extends Collection<E>> V convertChildCollection(@Nonnull V src) throws Exception {
		if(!(src instanceof Collection))
			throw new IllegalStateException("Before-image is supported only for OneToMany of type Collection<T>.");

		C mirror = createMirrorCollection((C) src);

		if(Hibernate.isInitialized(src)) {
			mirror.addAll((C) src);
		} else {
			//-- We need to create a mirror-proxy.
			m_mirrorMap.put(src, mirror);
		}
		return (V) mirror;
	}

	@Nonnull
	static private <T, V extends Collection<T>> V createMirrorCollection(@Nonnull V source) {
		Class<V> clz = (Class<V>) source.getClass();
		V res;
		if(List.class.isAssignableFrom(clz)) {
			res = (V) new ArrayList<T>(source.size());
		} else if(Set.class.isAssignableFrom(clz)) {
			res = (V) new HashSet<T>(source.size());
		} else
			throw new IllegalStateException("Before Images Interceptor: cannot create before images for collection of type " + source.getClass());
		return res;
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
		Object mirror = m_mirrorMap.remove(collection);
		if(null == mirror) {
			System.out.println("CopyInterceptor: no 'mirror' collection for collection " + collection.getClass().getName() + " @" + System.identityHashCode(collection));
			return;
		}
		System.out.println("CopyInterceptor: load event for " + collection.getClass().getName() + " @" + System.identityHashCode(collection));

		copyCollection((Collection< ? >) mirror, (Collection) collection);
	}

	private <E, C extends Collection<E>> void copyCollection(C mirror, C collection) {
		mirror.addAll(collection);
	}

}

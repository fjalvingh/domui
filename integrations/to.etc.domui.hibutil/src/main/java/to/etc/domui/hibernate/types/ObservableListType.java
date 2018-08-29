package to.etc.domui.hibernate.types;

import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.UserCollectionType;
import to.etc.domui.databinding.observables.ObservableList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A type for List<T> type properties which makes the property a ObservableList.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 11, 2014
 */
public class ObservableListType implements UserCollectionType {
	/**
	 * Return the basic wrapped instance.
	 */
	@Override
	public Object instantiate(int anticipatedSize) {
		return new ObservableList<Object>();
	}

	@Override public PersistentCollection instantiate(SharedSessionContractImplementor session, CollectionPersister collectionPersister) throws HibernateException {
		return new PersistentObservableList(session);
	}

	@Override public PersistentCollection wrap(SharedSessionContractImplementor session, Object collection) {
//		if(!(collection instanceof IObservableList))
//			throw new IllegalStateException("Expecting IObservableList but got a " + collection.getClass());
//
		return new PersistentObservableList(session, (List) collection);
	}

	@Override
	public Iterator getElementsIterator(Object collection) {
		if(null == collection)
			return null;
		PersistentObservableList<Object> ol = (PersistentObservableList<Object>) collection;
		return ol.iterator();
	}

	@Override
	public boolean contains(Object collection, Object entity) {
		if(null == collection)
			return false;
		PersistentObservableList<Object> ol = (PersistentObservableList<Object>) collection;
		return ol.contains(entity);
	}

	@Override
	public Object indexOf(Object collection, Object entity) {
		if(null == collection)
			return null;
		PersistentObservableList<Object> ol = (PersistentObservableList<Object>) collection;
		return Integer.valueOf(ol.indexOf(entity));
	}

	@Override public Object replaceElements(Object original, Object target, CollectionPersister persister, Object owner, Map copyCache, SharedSessionContractImplementor session) {
		PersistentObservableList<Object> src = (PersistentObservableList<Object>) original;
		PersistentObservableList<Object> dst = (PersistentObservableList<Object>) target;

		//-- Update the lists with as little disturbance as necessary.
		int nch = src.size();
		dst.retainAll(src);								// Remove from b all that is not in a
		nch = nch - dst.size();							// #elements removed
		int di = dst.size() - 1;
		int si = src.size() - 1;
		while(si >= 0) {
			Object sv = src.get(si);
			Object dv = dst.get(di);
			if(sv.equals(dv)) {
				di--;
				si--;
			} else {
				//-- Must be an insert.
				dst.add(di + 1, sv);
				si--;
				nch++;
			}
		}
		if(nch > 0)
			System.out.println("DEBUG OBSERVABLE: changed " + nch + " list entries");
		return dst;
	}
}

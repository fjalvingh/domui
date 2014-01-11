package to.etc.domui.hibernate.types;

import java.util.*;

import org.hibernate.*;
import org.hibernate.collection.*;
import org.hibernate.engine.*;
import org.hibernate.persister.collection.*;
import org.hibernate.usertype.*;

import to.etc.domui.databinding.observables.*;

/**
 * A type for List<T> type properties which makes the property a ObservableList.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 11, 2014
 */
public class ObservableListType implements UserCollectionType {
	@Override
	public Object instantiate(int anticipatedSize) {
		return new ObservableList<Object>();
	}

	@Override
	public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister) throws HibernateException {
		return new PersistentList(session);
	}

	@Override
	public PersistentCollection wrap(SessionImplementor session, Object collection) {
		return new PersistentList(session, (ObservableList< ? >) collection);
	}

	@Override
	public Iterator getElementsIterator(Object collection) {
		if(null == collection)
			return null;
		ObservableList<Object> ol = (ObservableList<Object>) collection;
		return ol.iterator();
	}

	@Override
	public boolean contains(Object collection, Object entity) {
		if(null == collection)
			return false;
		ObservableList<Object> ol = (ObservableList<Object>) collection;
		return ol.contains(entity);
	}

	@Override
	public Object indexOf(Object collection, Object entity) {
		if(null == collection)
			return null;
		ObservableList<Object> ol = (ObservableList<Object>) collection;
		return Integer.valueOf(ol.indexOf(entity));
	}

	@Override
	public Object replaceElements(Object original, Object target, CollectionPersister persister, Object owner, Map copyCache, SessionImplementor session) throws HibernateException {
		ObservableList<Object> src = (ObservableList<Object>) original;
		ObservableList<Object> dst = (ObservableList<Object>) target;

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

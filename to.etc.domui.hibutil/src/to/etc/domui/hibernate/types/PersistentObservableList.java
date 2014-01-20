package to.etc.domui.hibernate.types;

import java.util.*;

import javax.annotation.*;

import org.hibernate.collection.*;
import org.hibernate.engine.*;

import to.etc.domui.databinding.*;
import to.etc.domui.databinding.list.*;
import to.etc.domui.databinding.list2.*;
import to.etc.domui.databinding.observables.*;
import to.etc.util.*;

/**
 * An Observable list also containing the logic Hibernate requires to have the list lazy
 * and updateable. This list type is used when a OneToMany field is marked to have the
 * {@link ObservableListType}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 12, 2014
 */
public class PersistentObservableList<T> extends PersistentBag implements IObservableList {
	@Nonnull
	static private final IListChangeListener< ? >[] NONE = new IListChangeListener[0];

	@Nonnull
	private IListChangeListener<T>[] m_listeners = (IListChangeListener<T>[]) NONE;

	public PersistentObservableList() {
		super();
	}

	public PersistentObservableList(SessionImplementor session, List<T> list) {
		super(session, list);
	}

	public PersistentObservableList(SessionImplementor session) {
		super(session);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	List methods.										*/
	/*--------------------------------------------------------------*/
	@Override
	public boolean add(@Nonnull Object e) {
		int indx = size();
		boolean res = super.add(e);

		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		el.add(new ListChangeAdd<T>(indx, (T) e));
		fireEvent(new ListChangeEvent<T>(this, el));
		return res;
	}

	@Override
	public T set(int index, Object element) {
		T res = (T) super.set(index, element);

		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		el.add(new ListChangeModify<T>(index, res, (T) element));
		fireEvent(new ListChangeEvent<T>(this, el));

		return res;
	}

	@Override
	public void add(int index, Object element) {
		super.add(index, element);

		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		el.add(new ListChangeAdd<T>(index, (T) element));
		fireEvent(new ListChangeEvent<T>(this, el));
	}

	@Override
	public T remove(int index) {
		T res = (T) super.remove(index);

		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		el.add(new ListChangeDelete<T>(index, res));
		fireEvent(new ListChangeEvent<T>(this, el));

		return res;
	}

	@Override
	public boolean remove(Object o) {
		int indx = super.indexOf(o);
		if(indx < 0)
			return false;
		super.remove(indx);

		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		el.add(new ListChangeDelete<T>(indx, (T) o));
		fireEvent(new ListChangeEvent<T>(this, el));

		return true;
	}

	@Override
	public boolean addAll(Collection c) {
		boolean res = super.addAll(c);

		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		int indx = size();
		for(Object o : c) {
			T v = (T) o;
			el.add(new ListChangeAdd<T>(indx++, v));
		}
		fireEvent(new ListChangeEvent<T>(this, el));

		return res;
	}

	@Override
	public boolean addAll(int index, Collection c) {
		boolean res = super.addAll(index, c);

		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		int indx = index;
		for(Object o : c) {
			T v = (T) o;
			el.add(new ListChangeAdd<T>(indx++, v));
		}
		fireEvent(new ListChangeEvent<T>(this, el));

		return res;
	}

	@Override
	public boolean removeAll(Collection c) {
		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		boolean done = false;
		for(Object v : c) {
			int indx = indexOf(v);
			if(indx >= 0) {
				super.remove(indx);
				el.add(new ListChangeDelete<T>(indx, (T) v));
				done = true;
			}
		}
		fireEvent(new ListChangeEvent<T>(this, el));
		return done;
	}

	@Override
	public boolean retainAll(Collection c) {
		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		boolean done = false;
		for(int i = size(); --i >= 0;) {
			T cv = (T) super.get(i);
			if(!c.contains(cv)) {
				super.remove(i);
				el.add(new ListChangeDelete<T>(i, cv));
				done = true;
			}
		}
		fireEvent(new ListChangeEvent<T>(this, el));
		return done;
	}

	@Override
	public void clear() {
		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		for(int i = size(); --i >= 0;) {
			T cv = (T) super.get(i);
			el.add(new ListChangeDelete<T>(i, cv));
		}
		fireEvent(new ListChangeEvent<T>(this, el));
		super.clear();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Listener registration.								*/
	/*--------------------------------------------------------------*/

	/**
	 * Add a new listener to the set.
	 * @param listener
	 */
	@Override
	public void addChangeListener(to.etc.domui.databinding.IChangeListener listener) {
		//-- Already exists?
		final int length = m_listeners.length;
		for(int i = length; --i >= 0;) {
			if(m_listeners[i] == listener)
				return;
		}

		//-- We need a change. Reallocate, then add
		IListChangeListener<T>[] ar = new IListChangeListener[length + 1];
		System.arraycopy(m_listeners, 0, ar, 0, length);
		ar[length] = (IListChangeListener<T>) listener;
		m_listeners = ar;
	}

	/**
	 * Remove the listener if it exists. This leaves a null hole in the array.
	 * @param listener
	 */
	@Override
	public void removeChangeListener(IChangeListener listener) {
		//-- Already exists?
		final int length = m_listeners.length;
		for(int i = length; --i >= 0;) {
			if(m_listeners[i] == listener) {
				m_listeners[i] = null;
				return;
			}
		}
	}

	@Nonnull
	private synchronized IListChangeListener<T>[] getListeners() {
		return m_listeners;
	}

	/**
	 * Call all listeners.
	 * @param event
	 */
	public void fireEvent(@Nonnull ListChangeEvent<T> event) {
		try {
			for(IListChangeListener<T> o : getListeners()) {
				if(null != o) {
					o.handleChange(event);
				}
			}
		} catch(Exception x) {
			/*
			 * It's evil but we must wrap here, else all observed objects need throws clauses in their setters. It's a nice
			 * example of how completely and utterly useless checked exceptions are: we will still have an exception, which
			 * no one knows how to handle, but it is now also masked.
			 */
			throw WrappedException.wrap(x);
		}
	}
}

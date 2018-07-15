package to.etc.domui.hibernate.types;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import to.etc.domui.databinding.IChangeListener;
import to.etc.domui.databinding.list2.IListChangeListener;
import to.etc.domui.databinding.observables.IObservableList;
import to.etc.domui.databinding.observables.ObservableList;
import to.etc.util.StringTool;

import java.util.Iterator;
import java.util.List;

/**
 * This is the Hibernate "wrapper" for an Observable list. This delegates all methods to
 * the wrapped type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 12, 2014
 */
public class PersistentObservableList<T> extends PersistentBag implements IObservableList {
	private String m_from;

	@NonNull
	static private final IListChangeListener< ? >[] NONE = new IListChangeListener[0];

	@NonNull
	private IListChangeListener<T>[] m_listeners = (IListChangeListener<T>[]) NONE;

	public PersistentObservableList() {
		m_from = "Parameterless constructor called at " + StringTool.getLocation();
	}

	public PersistentObservableList(SharedSessionContractImplementor session, List<T> coll) {
		super(session);
		m_from = "2 param constructor called at " + StringTool.getLocation();

		if(coll instanceof IObservableList) {
			bag = coll;
		} else {
			bag = new ObservableList();
			Iterator iter = coll.iterator();
			while(iter.hasNext()) {
				bag.add(iter.next());
			}
		}
		setInitialized();
		setDirectlyAccessible(true);
	}

	public PersistentObservableList(SharedSessionContractImplementor session) {
		super(session);
		m_from = "1 param constructor called at " + StringTool.getLocation();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Listener registration.								*/
	/*--------------------------------------------------------------*/

	private IObservableList instance() {
		return (IObservableList) bag;
	}

	/**
	 * Add a new listener to the set.
	 */
	@Override
	public void addChangeListener(to.etc.domui.databinding.IChangeListener listener) {
		instance().addChangeListener(listener);
	}

	/**
	 * Remove the listener if it exists. This leaves a null hole in the array.
	 */
	@Override
	public void removeChangeListener(IChangeListener listener) {
		instance().removeChangeListener(listener);
	}
}

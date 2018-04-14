package to.etc.domui.databinding.observables;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.databinding.IObservable;
import to.etc.domui.databinding.list2.IListChangeListener;
import to.etc.domui.databinding.list2.ListChangeEvent;

import java.util.Iterator;
import java.util.List;

/**
 * This extension to the List interface describes a list that can be Observed, and which sends "updates" for
 * every change made to it's contents. This interface describes the observable list <b>itself</b>, not a property
 * that contains a list.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 1, 2013
 */
public interface IObservableList<T> extends List<T>, IObservable<T, ListChangeEvent<T>, IListChangeListener<T>> {
	@Override
	@NonNull
	Iterator<T> iterator();

	@Override
	boolean add(T item);

	@Override
	@NonNull
	T get(int index);

	@Override
	@Nullable
	T set(int index, T element);

	@Override
	void add(int index, T element);

	@Override
	T remove(int index);

	@NonNull
	@Override
	List<T> subList(int fromIndex, int toIndex);
}

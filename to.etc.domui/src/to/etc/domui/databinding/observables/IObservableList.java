package to.etc.domui.databinding.observables;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.databinding.*;
import to.etc.domui.databinding.list2.*;

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
	@Nonnull
	public Iterator<T> iterator();

	@Override
	public boolean add(@Nonnull T item);

	@Override
	@Nonnull
	public T get(int index);

	@Override
	@Nullable
	public T set(int index, T element);

	@Override
	public void add(int index, T element);

	@Override
	public T remove(int index);

	@Nonnull
	@Override
	public List<T> subList(int fromIndex, int toIndex);
}

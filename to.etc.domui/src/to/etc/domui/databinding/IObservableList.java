package to.etc.domui.databinding;

import java.util.*;

import javax.annotation.*;

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

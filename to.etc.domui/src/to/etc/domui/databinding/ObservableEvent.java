package to.etc.domui.databinding;

import javax.annotation.*;

public class ObservableEvent<V, E extends IChangeEvent<V, E, L>, L extends IChangeListener<V, E, L>> implements IChangeEvent<V, E, L> {
	@Nonnull
	final private IObservable<V, E, L> m_source;

	public ObservableEvent(@Nonnull IObservable<V, E, L> source) {
		if(null == source)
			throw new IllegalArgumentException("Source cannot be null");
		m_source = source;
	}

	@Override
	@Nonnull
	public IObservable<V, E, L> getSource() {
		return m_source;
	}
}

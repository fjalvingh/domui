package to.etc.domui.databinding;

import javax.annotation.*;

public interface IChangeEvent<V, E extends IChangeEvent<V, E, T>, T extends IChangeListener<V, E, T>> {
	/** The thingy that generated this event */
	@Nonnull
	public IObservable<V, E, T> getSource();
}

package to.etc.domui.databinding;

import javax.annotation.*;

/**
 * The basic data binding change event core interface - not intended for direct use; use it's implementations
 * instead.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 23, 2013
 */
public interface IChangeEvent<V, E extends IChangeEvent<V, E, T>, T extends IChangeListener<V, E, T>> {
	/** The thingy that generated this event */
	@Nonnull
	public IObservable<V, E, T> getSource();
}

package to.etc.domui.databinding;

import javax.annotation.*;

/**
 * This is a special observable handler for DomUI components. This observer handles
 * property changes in such a way that errors in the validation and conversion of the
 * properties are handled differently for binding than for normal "getValue" calls.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 8, 2013
 */
public class ComponentObserverSupport<C> extends ObserverSupport<C> {
	public ComponentObserverSupport(@Nonnull C instance) {
		super(instance);
	}
}


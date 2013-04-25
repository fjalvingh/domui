package to.etc.domui.databinding;

import javax.annotation.*;

/**
 * Thrown when an attempt is made to observe a property that does not support it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 24, 2013
 */
final public class PropertyNotObservableException extends RuntimeException {
	public PropertyNotObservableException(@Nonnull Class< ? > clz, @Nonnull String property) {
		super("The property '" + property + "' in class '" + clz + "' is not Observable");
	}
}

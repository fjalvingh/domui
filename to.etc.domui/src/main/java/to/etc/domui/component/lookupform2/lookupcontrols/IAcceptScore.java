package to.etc.domui.component.lookupform2.lookupcontrols;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 13-12-17.
 */
@FunctionalInterface
public interface IAcceptScore<T> {
	int score(@Nonnull T item);
}

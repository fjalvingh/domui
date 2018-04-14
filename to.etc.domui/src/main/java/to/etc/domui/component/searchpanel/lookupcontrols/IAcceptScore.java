package to.etc.domui.component.searchpanel.lookupcontrols;

import org.eclipse.jdt.annotation.NonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 13-12-17.
 */
@FunctionalInterface
public interface IAcceptScore<T> {
	int score(@NonNull T item);
}

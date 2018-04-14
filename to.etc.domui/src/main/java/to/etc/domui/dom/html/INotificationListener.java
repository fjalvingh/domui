package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/4/15.
 */
public interface INotificationListener<T> {
	void notify(@NonNull T event) throws Exception;
}

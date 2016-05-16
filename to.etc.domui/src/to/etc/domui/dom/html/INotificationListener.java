package to.etc.domui.dom.html;

import javax.annotation.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/4/15.
 */
public interface INotificationListener<T> {
	void notify(@Nonnull T event) throws Exception;
}

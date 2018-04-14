package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Interface to have controls created.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 23, 2013
 */
public interface IControlFactory<T> {
	@NonNull IControl<T> createControl() throws Exception;
}

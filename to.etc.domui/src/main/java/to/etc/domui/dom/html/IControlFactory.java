package to.etc.domui.dom.html;

import javax.annotation.*;

/**
 * Interface to have controls created.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 23, 2013
 */
public interface IControlFactory<T> {
	@Nonnull
	public IControl<T> createControl() throws Exception;
}

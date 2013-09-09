package to.etc.domui.component.menu;

import javax.annotation.*;

/**
 * Extension of {@link IUIAction<T>} that support automatic tests.
 *
 * @author <a href="mailto:avisekruna@execom.eu">Andjelko Visekruna</a>
 * Created on Sep 5, 2013
 */
public interface IUITestableAction<T> extends IUIAction<T> {

	/**
	 * Returns TestID needed for automatic tests.
	 *
	 * @param instance
	 * @return
	 * @throws Exception
	 */
	@Nullable
	String getTestId(@Nullable T instance) throws Exception;

}

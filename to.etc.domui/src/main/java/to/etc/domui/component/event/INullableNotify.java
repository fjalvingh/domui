package to.etc.domui.component.event;

import javax.annotation.*;

/**
 * General callback, can be used for definition of general purpose call-backs with null as valid callback input.
 * Difference from {@link INotify} is to emphasize that value is nullable.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Jun 11, 2014
 */
public interface INullableNotify<T> {

	/**
	 * Callback listener.
	 * @param value
	 * @throws Exception
	 */
	void onNotify(@Nullable T value) throws Exception;

}

package to.etc.domui.component.input;

import javax.annotation.*;

/**
 * General notification event, can be used for definition of general purpose call-backs.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 27 Oct 2011
 */
public interface INotifyEvent<T, E> {

	/**
	 * Notify listener with event and optional parameters.
	 * @param sender
	 * @param event
	 * @param params
	 * @throws Exception
	 */
	void onNotify(@Nonnull T sender, @Nullable E event, @Nullable Object... params) throws Exception;

}

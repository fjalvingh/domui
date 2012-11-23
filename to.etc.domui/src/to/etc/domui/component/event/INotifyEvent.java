package to.etc.domui.component.event;

import javax.annotation.*;

/**
 * General notification event, can be used for definition of general purpose call-backs.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 27 Oct 2011
 */
public interface INotifyEvent<T, D> {

	/**
	 * Notify listener with event and optional parameters.
	 * @param sender
	 * @param event
	 * @throws Exception
	 */
	void onNotify(@Nonnull T sender, @Nonnull D data) throws Exception;

}

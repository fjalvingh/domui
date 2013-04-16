package to.etc.domui.component.event;

import javax.annotation.*;

/**
 * General notification event, can be used for definition of general purpose call-backs.
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Nov 23, 2012
 */
public interface INotify<T> {

	/**
	 * Notify listener.
	 * @param sender
	 * @throws Exception
	 */
	void onNotify(@Nonnull T sender) throws Exception;

}

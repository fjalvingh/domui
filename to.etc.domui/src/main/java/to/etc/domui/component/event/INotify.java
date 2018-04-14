package to.etc.domui.component.event;

import org.eclipse.jdt.annotation.NonNull;

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
	void onNotify(@NonNull T sender) throws Exception;

}

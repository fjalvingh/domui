package to.etc.domui.component.event;

import org.eclipse.jdt.annotation.NonNull;

/**
 * General notification event, can be used for definition of general purpose call-backs with sending related event data.
 * In case that no evant data is needed, use {@link INotify} instead.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 27 Oct 2011
 */
public interface INotifyEvent<T, D> {

	/**
	 * Notify listener with expected data.
	 * @param sender
	 * @param data
	 * @throws Exception
	 */
	void onNotify(@NonNull T sender, @NonNull D data) throws Exception;

}

package to.etc.domui.component.event;

import org.eclipse.jdt.annotation.NonNull;

public interface ICheckCallback< T > {

	/**
	 * Check listener.
	 * @param sender
	 * @throws Exception
	 */
	boolean check(@NonNull T sender) throws Exception;
}

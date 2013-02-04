package to.etc.domui.component.event;

import javax.annotation.*;

public interface ICheckCallback< T > {

	/**
	 * Check listener.
	 * @param sender
	 * @throws Exception
	 */
	boolean check(@Nonnull T sender) throws Exception;
}

package to.etc.domui.component.layout;

import javax.annotation.*;

import to.etc.domui.component.event.*;

/**
 * For making an already existing tab the current one.
 *
 * @author <a href="mailto:marc.mol@itris.nl">Marc Mol</a>
 * @since Dec 5, 2014
 */
public interface ITabHandle {
	
	/**
	 * Sets what function is called when this tab is closed
	 */
	void setOnClose(@Nullable INotify<ITabHandle> notify);
}

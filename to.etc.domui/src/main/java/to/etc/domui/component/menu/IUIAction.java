package to.etc.domui.component.menu;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.dom.html.NodeBase;

public interface IUIAction {
	/**
	 * If this menu item is disabled, return a reason why it is. This will be shown as a hint when
	 * the entry is shown as disabled. If the item is not disabled return null.
	 */
	@Nullable
	String getDisableReason() throws Exception;

	@NonNull
	String getName() throws Exception;

	@Nullable
	String getTitle() throws Exception;

	@Nullable
	IIconRef getIcon() throws Exception;

	void execute(@NonNull NodeBase component) throws Exception;
}

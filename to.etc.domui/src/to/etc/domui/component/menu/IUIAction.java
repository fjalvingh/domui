package to.etc.domui.component.menu;

import javax.annotation.*;

public interface IUIAction<T> {
	/**
	 * If this menu item is disabled, return a reason why it is. This will be shown as a hint when
	 * the entry is shown as disabled. If the item is not disabled return null.
	 * @param instance
	 * @return
	 * @throws Exception
	 */
	@Nullable
	String getDisableReason(T instance) throws Exception;

	@Nonnull
	String getName(T instance) throws Exception;

	@Nullable
	String getIcon(T instance) throws Exception;

	boolean accepts(Object instance);
}

package to.etc.domui.component.menu;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

public interface IUIAction<T> {
	/**
	 * If this menu item is disabled, return a reason why it is. This will be shown as a hint when
	 * the entry is shown as disabled. If the item is not disabled return null.
	 * @param instance
	 * @return
	 * @throws Exception
	 */
	@Nullable
	String getDisableReason(@Nullable T instance) throws Exception;

	@Nonnull
	String getName(@Nullable T instance) throws Exception;

	@Nullable
	String getTitle(@Nullable T instance) throws Exception;

	@Nullable
	String getIcon(@Nullable T instance) throws Exception;

	void execute(@Nonnull NodeBase component, @Nullable T instance) throws Exception;
}

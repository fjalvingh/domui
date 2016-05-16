package to.etc.domui.component.menu;

import javax.annotation.*;

abstract public class UIAction<T> implements IUIAction<T> {
	final private String m_name;

	final private String m_title;

	final private String m_icon;

	@Nullable
	private String m_disableReason;

	public UIAction(String name, String title, String icon) {
		m_name = name;
		m_title = title;
		m_icon = icon;
	}

	@Override
	@Nullable
	public String getDisableReason(@Nullable T instance) throws Exception {
		return m_disableReason;
	}

	public void setDisableReason(@Nullable String disableReason) {
		m_disableReason = disableReason;
	}

	@Override
	@Nonnull
	public String getName(@Nullable T instance) throws Exception {
		return m_name;
	}

	@Override
	@Nullable
	public String getTitle(@Nullable T instance) throws Exception {
		return m_title;
	}

	@Override
	@Nullable
	public String getIcon(@Nullable T instance) throws Exception {
		return m_icon;
	}
}

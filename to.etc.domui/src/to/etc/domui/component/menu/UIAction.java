package to.etc.domui.component.menu;

import javax.annotation.*;

abstract public class UIAction<T> implements IUIAction<T> {
	final private String m_name;

	final private String m_title;

	final private String m_icon;

	public UIAction(String name, String title, String icon) {
		m_name = name;
		m_title = title;
		m_icon = icon;
	}

	@Override
	@Nullable
	public String getDisableReason(T instance) throws Exception {
		return null;
	}

	@Override
	@Nonnull
	public String getName(T instance) throws Exception {
		return m_name;
	}

	@Override
	@Nullable
	public String getTitle(T instance) throws Exception {
		return m_title;
	}

	@Override
	@Nullable
	public String getIcon(T instance) throws Exception {
		return m_icon;
	}
}

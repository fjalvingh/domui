package to.etc.domui.component.menu;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.misc.IIconRef;

abstract public class UIAction<T> implements IUIAction<T> {
	final private String m_name;

	final private String m_title;

	final private IIconRef m_icon;

	@Nullable
	private String m_disableReason;

	public UIAction(String name, String title, IIconRef icon) {
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
	@NonNull
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
	public IIconRef getIcon(@Nullable T instance) throws Exception {
		return m_icon;
	}
}

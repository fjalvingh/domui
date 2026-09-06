package to.etc.domui.component.menu;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.dom.html.NodeBase;
import to.etc.function.ConsumerEx;

public class UIAction implements IUIAction {
	final private String m_name;

	final private String m_title;

	final private IIconRef m_icon;

	@Nullable
	private String m_disableReason;

	private final ConsumerEx<NodeBase> m_execute;

	public UIAction(String name, String title, IIconRef icon) {
		m_name = name;
		m_title = title;
		m_icon = icon;
		m_execute = (b) -> {
			throw new IllegalStateException("Missing execute");
		};
	}

	public UIAction(String name, String title, IIconRef icon, @Nullable String disableReason, ConsumerEx<NodeBase> execute) {
		m_name = name;
		m_title = title;
		m_icon = icon;
		m_disableReason = disableReason;
		m_execute = execute;
	}

	@Override
	@Nullable
	public String getDisableReason() throws Exception {
		return m_disableReason;
	}

	public void setDisableReason(@Nullable String disableReason) {
		m_disableReason = disableReason;
	}

	@Override
	@NonNull
	public String getName() throws Exception {
		return m_name;
	}

	@Override
	@Nullable
	public String getTitle() throws Exception {
		return m_title;
	}

	@Override
	@Nullable
	public IIconRef getIcon() throws Exception {
		return m_icon;
	}

	@Override
	public void execute(@NonNull NodeBase component) throws Exception {
		m_execute.accept(component);
	}
}

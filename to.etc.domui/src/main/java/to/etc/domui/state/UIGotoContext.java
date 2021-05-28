package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.UrlPage;

/**
 * Contains context for performing delayed UIGoto navigation.
 */
@NonNullByDefault
public class UIGotoContext {

	@Nullable
	private final Class< ? extends UrlPage> m_targetPageClass;

	@Nullable
	private final IPageParameters m_targetPageParameters;

	@Nullable
	private final Class< ? extends ConversationContext> m_targetConversationClass;

	@Nullable
	private final ConversationContext m_targetConversation;

	@Nullable
	private final MoveMode m_targetMode;

	@Nullable
	private final String m_targetURL;

	private final boolean m_ajax;

	public UIGotoContext(
		@Nullable Class< ? extends UrlPage> targetPageClass,
		@Nullable IPageParameters targetPageParameters,
		@Nullable Class< ? extends ConversationContext> targetConversationClass,
		@Nullable ConversationContext targetConversation,
		@Nullable MoveMode targetMode,
		@Nullable
		String targetURL,
		boolean ajax
	) {
		m_targetPageClass = targetPageClass;
		m_targetPageParameters = targetPageParameters;
		m_targetConversationClass = targetConversationClass;
		m_targetConversation = targetConversation;
		m_targetMode = targetMode;
		m_targetURL = targetURL;
		m_ajax = ajax;
	}

	@Nullable
	public Class< ? extends UrlPage> getTargetPageClass() {
		return m_targetPageClass;
	}

	@Nullable
	public IPageParameters getTargetPageParameters() {
		return m_targetPageParameters;
	}

	@Nullable
	public Class< ? extends ConversationContext> getTargetConversationClass() {
		return m_targetConversationClass;
	}

	@Nullable
	public ConversationContext getTargetConversation() {
		return m_targetConversation;
	}

	@Nullable
	public MoveMode getTargetMode() {
		return m_targetMode;
	}

	@Nullable
	public String getTargetURL() {
		return m_targetURL;
	}

	public boolean isAjax() {
		return m_ajax;
	}
}

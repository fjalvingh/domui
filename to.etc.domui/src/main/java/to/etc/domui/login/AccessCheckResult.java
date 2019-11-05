package to.etc.domui.login;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.annotations.UIRights;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.AbstractPage;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-12-18.
 */
final public class AccessCheckResult {
	@NonNull
	private final PageAccessCheckResult m_result;

	@Nullable
	private final UIRights m_rights;

	private final List<UIMessage> m_messageList;

	final private AbstractPage m_page;

	private AccessCheckResult(PageAccessCheckResult result, UIRights rights, List<UIMessage> messageList, AbstractPage page) {
		m_result = result;
		m_rights = rights;
		m_messageList = Collections.unmodifiableList(messageList);
		m_page = page;
	}
	private AccessCheckResult(PageAccessCheckResult result) {
		m_result = result;
		m_rights = null;
		m_messageList = Collections.emptyList();
		m_page = null;
	}

	public PageAccessCheckResult getResult() {
		return m_result;
	}

	public UIRights getRights() {
		return m_rights;
	}

	public List<UIMessage> getMessageList() {
		return m_messageList;
	}

	public AbstractPage getPage() {
		return m_page;
	}

	public static AccessCheckResult accepted() {
		return new AccessCheckResult(PageAccessCheckResult.Accepted);
	}

	public static AccessCheckResult needLogin() {
		return new AccessCheckResult(PageAccessCheckResult.NeedLogin);
	}

	public static AccessCheckResult refused(AbstractPage page, UIRights rights, List<UIMessage> errors) {
		return new AccessCheckResult(PageAccessCheckResult.Refused, rights, errors, page);
	}
}

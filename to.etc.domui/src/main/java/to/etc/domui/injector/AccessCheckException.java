package to.etc.domui.injector;

import to.etc.domui.login.AccessCheckResult;

import java.util.stream.Collectors;

public class AccessCheckException extends Exception {

	private AccessCheckResult m_accessResult;

	public AccessCheckException(AccessCheckResult accessResult) {
		m_accessResult = accessResult;
	}

	@Override
	public String getMessage() {
		return "AccessCheckException: " + m_accessResult.getMessageList().stream().map(it -> it.getMessage()).collect(Collectors.joining(", "));
	}

	public AccessCheckResult getAccessResult() {
		return m_accessResult;
	}
}

package to.etc.domui.trouble;

public class ValidationException extends UIException {
	public ValidationException(String code, Object... param) {
		super(code, param);
	}
}

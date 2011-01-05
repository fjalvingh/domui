package to.etc.domui.themes;

public class StyleException extends RuntimeException {
	public StyleException(Exception cause, String message) {
		super(message, cause);
	}

	public StyleException(String message) {
		super(message);
	}
}

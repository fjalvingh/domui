package to.etc.nio.server;

public class DisconnectionException extends RuntimeException {
	public DisconnectionException() {}

	public DisconnectionException(String message) {
		super(message);
	}

	public DisconnectionException(Throwable cause) {
		super(cause);
	}

	public DisconnectionException(String message, Throwable cause) {
		super(message, cause);
	}
}

package to.etc.ssh;

public class SshException extends RuntimeException {
	public SshException(Throwable c, String message) {
		super(message, c);
	}

	public SshException(String message) {
		super(message);
	}

	public SshException(Throwable cause) {
		super(cause);
	}
}

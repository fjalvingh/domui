package to.etc.webapp;

final public class ProgrammerErrorException extends RuntimeException {
	public ProgrammerErrorException(String arg0) {
		super(arg0);
	}

	public ProgrammerErrorException(Throwable arg0) {
		super(arg0);
	}

	public ProgrammerErrorException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}

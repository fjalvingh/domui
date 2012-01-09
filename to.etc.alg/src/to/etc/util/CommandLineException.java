package to.etc.util;

public class CommandLineException extends Exception {
	public CommandLineException(String what) {
		super(what);
	}

	public CommandLineException(String arg, String what) {
		super(arg + ": " + what);
	}

}

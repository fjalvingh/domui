package to.etc.domui.trouble;

public class Trouble {
	private Trouble() {}

	static public void wrapException(Throwable t) {
		if(t instanceof RuntimeException)
			throw (RuntimeException) t;
		else if(t instanceof Error)
			throw (Error) t;
		else
			throw new RuntimeException(t);
	}
}

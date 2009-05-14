package to.etc.domui.ajax;

public class AjaxException extends RuntimeException {
	public AjaxException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

	public AjaxException(final String arg0) {
		super(arg0);
	}

	public AjaxException(final Throwable arg0) {
		super(arg0);
	}
}

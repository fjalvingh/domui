package to.etc.server.ajax;

public class AjaxHandlerException extends ServiceException {
	public AjaxHandlerException(String why) {
		super(why);
	}

	public AjaxHandlerException(String why, Throwable cause) {
		super(why, cause);
	}
}

package to.etc.server.ajax;

import to.etc.server.servlet.*;

public class AjaxServiceException extends ServiceException {
	public AjaxServiceException(RequestContext ctx, String message, Throwable cause) {
		super(ctx, message, cause);
	}

	public AjaxServiceException(RequestContext ctx, String message) {
		super(ctx, message);
	}

	public AjaxServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public AjaxServiceException(String message) {
		super(message);
	}

}

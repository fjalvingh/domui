package to.etc.server.misc;

/**
 *	Thrown when a resource was not found. Should generate a 404 error
 *  on the browser.
 */

public class ResourceNotFoundException extends Exception {
	public ResourceNotFoundException(String rurl) {
		super(rurl);
	}
}

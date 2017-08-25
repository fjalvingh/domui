package to.etc.domui.webdriver.core;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-8-17.
 */
public class ElementNotFoundException extends RuntimeException {
	public ElementNotFoundException(String id) {
		super("The element " + id + " is not found");
	}
}

package to.etc.domui.dom;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Defines contract for supporting the Http Content-Security-Policy (CSP) header.
 */
public interface ICSPSupport {

	/**
	 * Defines if given attribute needs to be handled by CSP support code.
	 */
	boolean isAttributeHandled(@NonNull String attributeName);

	/**
	 * Renders given attribute value as javascript expression for given element identified by selector.
	 *
	 * @param selector Normally just jquery selector for element ID, like #_AA.
	 * @param attribute Name of the attribute.
	 * @param value Attribute value that has to be translated to a javascript expression. I.e. onclick = "WebUI.clicked('#_AA');" -> $('#_AA').click(function() { WebUI.clicked('#_AA'); });
	 */
	void renderAsJavaScript(@NonNull String selector, @NonNull String attribute, @NonNull String value);
}

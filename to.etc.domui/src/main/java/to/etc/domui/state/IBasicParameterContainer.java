package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.server.BrowserVersion;

import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-10-19.
 */
@NonNullByDefault
public interface IBasicParameterContainer {
	/**
	 * Return either a String or a String[], depending on the #of values for a parameter.
	 */
	@Nullable
	Object getObject(String name);

	/**
	 * Get the #of parameters in the container.
	 */
	int size();

	/**
	 * Gets all the names of the parameters this object is holding
	 * @return the parameter names in an array
	 */
	Set<String> getParameterNames();

	@Nullable
	String getUrlContextString();

	/**
	 * Return the number of characters that this would take on an url.
	 */
	int getDataLength();

	@Nullable
	String getThemeName();

	/**
	 * The complete input URL without the JSDK context part and the query part. Specifically:
	 * <ul>
	 *	<li>The JSDK webapp context is always removed from the start, i.e. this is always the webapp-relative path</li>
	 *	<li>The context NEVER starts with a slash</li>
	 * </ul>
	 */
	String getInputPath();

	BrowserVersion getBrowserVersion();
}

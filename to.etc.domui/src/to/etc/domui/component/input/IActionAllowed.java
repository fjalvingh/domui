package to.etc.domui.component.input;

/**
 *
 *
 * @author <a href="mailto:dprica@execom.eu">Darko Prica</a>
 * Created on 29 Sep 2009
 */
public interface IActionAllowed {
	/**
	 * Check if action is allowed
	 *
	 * @return
	 * @throws Exception
	 */
	boolean isAllowed() throws Exception;
}

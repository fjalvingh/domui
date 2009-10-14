package to.etc.domui.dom.html;

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

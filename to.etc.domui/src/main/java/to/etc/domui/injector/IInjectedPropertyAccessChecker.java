package to.etc.domui.injector;

/**
 * Checks whether the injected value is allowed on the page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-12-18.
 */
public interface IInjectedPropertyAccessChecker {
	boolean isAccessAllowed();

}

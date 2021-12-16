package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A selector for Selenium that can be generated as something Selenium would understand.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
@NonNullByDefault
public interface IPoSelector {
	String selectorAsCode();
}

package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Generates a selector that asks the row (this) for its
 * selector.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-12-21.
 */
@NonNullByDefault
final public class PoSelectorRow implements IPoSelector {
	@Override
	public String selectorAsCode() {
		return "() -> this.getRowSelector()";
	}
}

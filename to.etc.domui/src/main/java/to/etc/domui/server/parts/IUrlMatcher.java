package to.etc.domui.server.parts;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.server.IParameterInfo;

/**
 * Predicate which checks whether some kind of URL like input
 * is acceptable for something.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-5-17.
 */
public interface IUrlMatcher {
	boolean accepts(@NonNull IParameterInfo parameters);
}

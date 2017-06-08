package to.etc.domui.server.parts;

import to.etc.domui.server.IParameterInfo;

import javax.annotation.Nonnull;

/**
 * Predicate which checks whether some kind of URL like input
 * is acceptable for something.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-5-17.
 */
public interface IUrlMatcher {
	boolean accepts(@Nonnull IParameterInfo parameters);
}

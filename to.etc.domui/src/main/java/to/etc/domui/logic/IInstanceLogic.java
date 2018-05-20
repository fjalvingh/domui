package to.etc.domui.logic;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.webapp.query.IIdentifyable;

/**
 * Marker interface for any logic class wrapping data instance T.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 13, 2014
 */
public interface IInstanceLogic<T extends IIdentifyable< ? >> extends IClassLogic {

	@NonNull T getInstance();
}

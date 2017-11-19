package to.etc.domui.logic;

import javax.annotation.*;

/**
 * Marker interface for any logic class.
 *
 * @author <a href="mailto:imilovanovic@execom.eu">Igor Milovanović</a>
 * Created on May 23, 2014
 */
public interface IClassLogic extends ILogic {

	@Nonnull ILogicContext lc();

}
